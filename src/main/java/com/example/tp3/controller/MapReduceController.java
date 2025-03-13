package com.example.tp3.controller;

import com.example.tp3.service.AkkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class MapReduceController {

    private final AkkaService akkaService;

    @Autowired
    public MapReduceController(AkkaService akkaService) {
        this.akkaService = akkaService;
        // Initialisation manuelle des acteurs
        this.akkaService.initializeActors();
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (!file.isEmpty()) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            akkaService.processFile(content);
            model.addAttribute("message", "Fichier téléversé et traité avec succès !");
        } else {
            model.addAttribute("message", "Le fichier est vide.");
        }
        return "index";
    }

    
    @PostMapping("/search")
    public String searchWord(@RequestParam("word") String word, Model model) {
        int count = akkaService.getWordCount(word);
        model.addAttribute("word", word);
        model.addAttribute("count", count);
        return "index";
    }

    
    @PostMapping("/initialize")
    public String initializeActors(Model model) {
        akkaService.initializeActors();
        model.addAttribute("message", "Acteurs réinitialisés avec succès !");
        return "index";
    }
}