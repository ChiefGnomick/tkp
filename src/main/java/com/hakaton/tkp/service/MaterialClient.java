package com.hakaton.tkp.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class MaterialClient {
    
    private static final String BASE_URL = "http://localhost:5000";
    
    /*public static void main(String[] args) {
        // Пример сохранения материалов
        String[] materials = {
            "Короб 100x100 мм, L=3000 мм, горячее цинкование, толщина покрытия не менее 80 мкм DKC арт. 35101 - 61263 руб.",
            "Крышка вертикального внешнего углового лотка 90°, 800мм, горячее цинкование, толщина покрытия не менее 80 мкм - 1860 руб.",
            "Гайка с насечкой препятствующей откручиванию, М6 DKC арт. СМ100600 - 38194 руб.",
            "Короб 200x200 мм, L=2000 мм, горячее цинкование, толщина покрытия не менее 80 мкм UNM322 - 88498 руб.",
            "Гайка М6,оцинкованная - 13688 руб.",
            "Винт с крестообразным шлицем М6х10 DKC арт. СМ010610 или аналог - 12534 руб.",
            "Крышка 200 мм, L=2000 мм, горячее цинкование, толщина покрытия не менее 80 мкм UNM322 - 45131 руб."
        };
        
        // Сохраняем материалы
        JSONObject saveResponse = saveMaterials(materials);
        System.out.println("Save response: " + saveResponse.toString(2));
        
        // Ищем материалы
        JSONObject searchResponse = searchMaterials("оцинкованная гайка М6", 3);
        System.out.println("\nSearch response: " + searchResponse.toString(2));
    }*/
    
    public JSONObject saveMaterials(String[] materials) {
        try {
            JSONObject requestData = new JSONObject();
            JSONArray materialsArray = new JSONArray();
            
            for (String material : materials) {
                materialsArray.put(material);
            }
            
            requestData.put("materials", materialsArray);
            
            return sendPostRequest(BASE_URL + "/save_materials", requestData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("error", e.getMessage());
        }
    }
    
    public JSONObject searchMaterials(String query, int topK) {
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("query", query);
            requestData.put("top_k", topK);
            
            return sendPostRequest(BASE_URL + "/search_materials", requestData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("error", e.getMessage());
        }
    }
    
    private JSONObject sendPostRequest(String urlString, JSONObject data) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = data.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                return new JSONObject(response.toString());
            }
        } else {
            throw new RuntimeException("HTTP error: " + responseCode);
        }
    }
}