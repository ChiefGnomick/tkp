package com.hakaton.tkp.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

import redis.clients.jedis.util.SafeEncoder;

@RequiredArgsConstructor
public class SearchhService {

    private static final int EMBEDDING_DIM = 128;

    private final EmbeddingService embeddingService;

    public void search() {
        Jedis jedis = new Jedis("localhost", 6379);
        Random random = new Random();

        // 1. Массив материалов
        String[] materials = {
                "Короб 100x100 мм, L=3000 мм, горячее цинкование",
                "Крышка вертикального внешнего углового лотка 90°, 800мм",
                "Гайка с насечкой препятствующей откручиванию, М6",
                "Лоток перфорированный, оцинкованный, ширина 100 мм",
                "Профиль С-образный сдвоенный, оцинкованный"
        };

        // 2. Добавляем материалы с embedding
        for (int i = 0; i < materials.length; i++) {
            String key = "material:" + i;
            List<Float> embeddingList = embeddingService.generateEmbedding(materials[i]);
            float[] embedding = new float[embeddingList.size()];
            for (int j = 0; j < embeddingList.size(); j++) {
                embedding[j] = embeddingList.get(j);
            }
            byte[] vectorBytes = floatArrayToByteArray(embedding);

            Map<String, String> hash = new HashMap<>();
            hash.put("name", materials[i]);
            hash.put("embedding", new String(vectorBytes)); // временно, для хранения в HASH
            jedis.hset(key, hash);
        }

        System.out.println("Материалы добавлены.");

        // 3. Создаём embedding для запроса
        float[] queryEmbedding = new float[EMBEDDING_DIM];
        
        for (int i = 0; i < EMBEDDING_DIM; i++) queryEmbedding[i] = random.nextFloat();
        byte[] queryBytes = floatArrayToByteArray(queryEmbedding);

        // 4. FT.SEARCH с KNN
        String[] ftArgs = new String[]{
                "materials_idx",
                "*=>[KNN 3 @embedding $vec AS vector_score]",
                "PARAMS", "2",
                "$vec", new String(queryBytes),
                "RETURN", "2", "name", "vector_score"
        };

        try {
            Object result = jedis.sendCommand(new redis.clients.jedis.commands.ProtocolCommand() {
                @Override
                public byte[] getRaw() {
                    return SafeEncoder.encode("FT.SEARCH");
                }
            }, ftArgs);

            System.out.println("Результаты KNN-поиска: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        jedis.close();
    }

    // float[] -> byte[]
    private static byte[] floatArrayToByteArray(float[] array) {
        ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
        for (float f : array) buffer.putFloat(f);
        return buffer.array();
    }

}
