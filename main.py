from flask import Flask, request, jsonify
import redis
import requests
import numpy as np
import re

# === Настройки ===
REDIS_HOST = "localhost"
REDIS_PORT = 6379
INDEX_NAME = "materials_idx"
MODEL = "mxbai-embed-large"
OLLAMA_URL = "http://localhost:11434/api/embeddings"

app = Flask(__name__)

# === Инициализация Redis ===
r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=False)


# === Функция получения embedding от Ollama ===
def get_embedding(text: str):
    payload = {"model": MODEL, "prompt": text}
    response = requests.post(OLLAMA_URL, json=payload)
    response.raise_for_status()
    data = response.json()
    return np.array(data["embedding"], dtype=np.float32)


# === Преобразование float32 → bytes для Redis ===
def to_bytes(v: np.ndarray) -> bytes:
    return v.astype(np.float32).tobytes()


# === Создание индекса Redis ===
def create_index():
    try:
        r.ft(INDEX_NAME).info()
        print("✅ Индекс уже существует.")
    except redis.exceptions.ResponseError:
        print("🧱 Создаём новый индекс...")
        from redis.commands.search.field import TextField, VectorField
        r.ft(INDEX_NAME).create_index([
            TextField("name"),
            VectorField(
                "embedding",
                "HNSW",
                {
                    "TYPE": "FLOAT32",
                    "DIM": 1024,
                    "DISTANCE_METRIC": "COSINE",
                    "M": 16,
                    "EF_CONSTRUCTION": 200
                }
            ),
        ])


# === Базовый поиск материалов ===
def search_materials_base(user_query: str, top_k=5):
    """Базовая функция поиска по одному запросу"""
    try:
        # Получаем эмбеддинг для запроса
        query_emb = get_embedding(user_query)

        from redis.commands.search.query import Query
        search_query = f"*=> [KNN {top_k} @embedding $vec AS score]"
        q = Query(search_query).sort_by("score").return_fields("name", "score").dialect(2)

        results = r.ft(INDEX_NAME).search(q, query_params={"vec": query_emb.tobytes()})

        if not results.docs:
            return []

        # Форматируем результаты
        formatted_results = []
        for doc in results.docs:
            name = getattr(doc, "name", None)
            if name is None:
                name = doc.__dict__.get("name", "")

            if isinstance(name, bytes):
                name = name.decode("utf-8")

            formatted_results.append({
                'name': name,
                'score': float(doc.score),
                'query_part': user_query  # Добавляем информацию о части запроса
            })

        return formatted_results

    except Exception as e:
        print(f"❌ Ошибка при поиске '{user_query}': {str(e)}")
        return []


# === Расширенный поиск с разделением ===
def search_materials_advanced(user_query: str, top_k=5):
    """
    Разделяет запрос по запятым, 'или', 'и' выполняет поиск для каждой части
    и объединяет результаты
    """
    # Разделяем по запятым или словам "или", "и" (с игнорированием регистра)
    search_parts = re.split(r',|\s+или\s+|\s+и\s+', user_query, flags=re.IGNORECASE)

    # Очищаем части от лишних пробелов и пустых строк
    search_parts = [part.strip() for part in search_parts if part.strip()]

    # Если после разделения осталась только одна часть, используем обычный поиск
    if len(search_parts) == 1:
        return search_materials_base(user_query, top_k)

    all_results = []

    print(f"🔍 Расширенный поиск по {len(search_parts)} частям запроса:")
    for i, part in enumerate(search_parts, 1):
        print(f"  {i}. '{part}'")

        part_results = search_materials_base(part, top_k)
        all_results.extend(part_results)

    # Удаляем дубликаты по названию материала
    unique_results = []
    seen_names = set()

    for result in all_results:
        if result["name"] not in seen_names:
            seen_names.add(result["name"])
            unique_results.append(result)

    # Сортируем по score (чем меньше - тем лучше)
    unique_results.sort(key=lambda x: x["score"])

    return unique_results


# === API метод для сохранения эмбеддингов ===
@app.route('/save_materials', methods=['POST'])
def save_materials():
    try:
        data = request.get_json()

        if not data or 'materials' not in data:
            return jsonify({'error': 'No materials array provided'}), 400

        materials = data['materials']

        if not isinstance(materials, list):
            return jsonify({'error': 'Materials should be an array'}), 400

        # Создаем индекс если нужно
        create_index()

        print(f"📦 Загружаем {len(materials)} материалов в Redis...")

        saved_count = 0
        for i, text in enumerate(materials):
            try:
                emb = get_embedding(text)
                key = f"material:{i}"
                r.hset(
                    key,
                    mapping={
                        "name": text,
                        "embedding": to_bytes(emb),
                    },
                )
                saved_count += 1
            except Exception as e:
                print(f"❌ Ошибка при сохранении материала {i}: {str(e)}")
                continue

        return jsonify({
            'status': 'success',
            'message': f'Успешно сохранено {saved_count} материалов',
            'saved_count': saved_count,
            'total_requested': len(materials)
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500


# === API метод для поиска материалов ===
@app.route('/search_materials', methods=['POST'])
def search_materials():
    try:
        data = request.get_json()

        if not data or 'query' not in data:
            return jsonify({'error': 'No search query provided'}), 400

        user_query = data['query']
        top_k = data.get('top_k', 5)
        use_advanced_search = data.get('advanced_search', True)  # По умолчанию включен расширенный поиск

        if use_advanced_search:
            # Используем расширенный поиск с разделением
            results = search_materials_advanced(user_query, top_k)
            search_type = "advanced"
            search_parts = re.split(r',|\s+или\s+|\s+и\s+', user_query, flags=re.IGNORECASE)
            search_parts = [part.strip() for part in search_parts if part.strip()]
        else:
            # Используем обычный поиск
            results = search_materials_base(user_query, top_k)
            search_type = "simple"
            search_parts = [user_query]

        if not results:
            return jsonify({
                'status': 'success',
                'query': user_query,
                'search_type': search_type,
                'search_parts': search_parts,
                'results': [],
                'message': 'Ничего не найдено'
            })

        return jsonify({
            'status': 'success',
            'query': user_query,
            'search_type': search_type,
            'search_parts': search_parts,
            'results': results,
            'count': len(results)
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500


# === Health check ===
@app.route('/health', methods=['GET'])
def health_check():
    try:
        # Проверяем подключение к Redis
        r.ping()
        return jsonify({'status': 'healthy', 'redis': 'connected'})
    except Exception as e:
        return jsonify({'status': 'unhealthy', 'redis': str(e)}), 500


if __name__ == '__main__':
    # Создаем индекс при запуске сервера
    create_index()
    app.run(host='0.0.0.0', port=5000, debug=True)