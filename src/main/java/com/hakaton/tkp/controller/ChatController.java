package com.hakaton.tkp.controller;

import com.hakaton.tkp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private List<ChatMessage> messages = new ArrayList<>();

    @GetMapping("/")
    public String chat(Model model) {
        model.addAttribute("messages", messages);
        model.addAttribute("newMessage", new ChatMessage());
        return "chat";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        if (message != null && !message.trim().isEmpty()) {
            // Сообщение пользователя
            messages.add(new ChatMessage("user", message));

            // Ответ бота (просто возвращаем ту же строку)
            String botResponse = chatService.sendPromptWithRAG(message);
            messages.add(new ChatMessage("bot", botResponse));
        }
        return "redirect:/";
    }
    @PostMapping("/saveBotMessage")
    @ResponseBody
    public ResponseEntity<String> saveBotMessage(@RequestBody ChatMessage message) {
        try {
            // Папка для сохранения (создаётся автоматически)
            Path dir = Path.of("saved_messages");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // Имя файла
            String filename = "bot-message-" + System.currentTimeMillis() + ".json";
            Path file = dir.resolve(filename);

            // Запись JSON вручную
            String json = chatService.extractJsonFromResponse(message.getMessage().replace("\"", "\\\""));

            try (FileWriter writer = new FileWriter(file.toFile())) {
                writer.write(json);
            }

            return ResponseEntity.ok("Файл сохранён: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка при сохранении JSON");
        }
    }
    // Функция которая принимает строку и возвращает ее как сообщение в чате
    private String processMessage(String message) {
        // Просто возвращаем ту же строку как ответ бота
        return "Бот: " + message;
    }

    @PostMapping("/clear")
    public String clearChat() {
        messages.clear();
        return "redirect:/";
    }

}