package com.hakaton.tkp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hakaton.tkp.service.InputDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/input")
@RequiredArgsConstructor
public class InputDataController {

    private final InputDataService inputDataService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file
    ){
        return inputDataService.parseFile(file);
    }
}
