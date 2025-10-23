package com.hakaton.tkp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hakaton.tkp.service.InputDataService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/input")
@AllArgsConstructor
@NoArgsConstructor
public class InputDataController {
    private InputDataService inputDataService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file
    ){
        inputDataService.parseFile(file);
        return ResponseEntity.ok().build();
    }
}
