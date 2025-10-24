package com.hakaton.tkp.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParser {
    
    /**
     * Парсит JSONObject и извлекает все поля 'name' в строку, разделенную пробелами
     * @param jsonObject JSON объект с результатами поиска
     * @return строка с названиями материалов, разделенными пробелами
     */
    public static String parseNamesToString(JSONObject jsonObject) {
        StringBuilder result = new StringBuilder();
        
        try {
            // Проверяем статус ответа
            if (!jsonObject.has("status") || !"success".equals(jsonObject.getString("status"))) {
                return "Ошибка: некорректный ответ от сервера";
            }
            
            // Получаем массив результатов
            JSONArray results = jsonObject.getJSONArray("results");
            
            // Извлекаем все поля 'name'
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                String name = item.getString("name");
                
                // Добавляем к результату
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(name);
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Ошибка при парсинге JSON: " + e.getMessage();
        }
    }
    
    /**
     * Альтернативная версия с ограничением количества результатов
     * @param jsonObject JSON объект с результатами поиска
     * @param maxResults максимальное количество результатов для извлечения
     * @return строка с названиями материалов, разделенными пробелами
     */
    public static String parseNamesToString(JSONObject jsonObject, int maxResults) {
        StringBuilder result = new StringBuilder();
        
        try {
            if (!jsonObject.has("status") || !"success".equals(jsonObject.getString("status"))) {
                return "Ошибка: некорректный ответ от сервера";
            }
            
            JSONArray results = jsonObject.getJSONArray("results");
            int count = Math.min(results.length(), maxResults);
            
            for (int i = 0; i < count; i++) {
                JSONObject item = results.getJSONObject(i);
                String name = item.getString("name");
                
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(name);
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Ошибка при парсинге JSON: " + e.getMessage();
        }
    }
    
    /**
     * Версия с дополнительной информацией о количестве
     * @param jsonObject JSON объект с результатами поиска
     * @return строка с названиями и информацией о количестве
     */
    public static String parseNamesWithCount(JSONObject jsonObject) {
        StringBuilder result = new StringBuilder();
        
        try {
            if (!jsonObject.has("status") || !"success".equals(jsonObject.getString("status"))) {
                return "Ошибка: некорректный ответ от сервера";
            }
            
            int totalCount = jsonObject.getInt("count");
            JSONArray results = jsonObject.getJSONArray("results");
            
            result.append("Найдено материалов: ").append(totalCount).append(". ");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                String name = item.getString("name");
                
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(name);
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Ошибка при парсинге JSON: " + e.getMessage();
        }
    }
}