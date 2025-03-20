package com.example.tp3.controller;

import com.example.tp3.service.AkkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

//import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class MapReduceController {

    private final AkkaService akkaService;

    @Autowired
    public MapReduceController(AkkaService akkaService) {
        this.akkaService = akkaService;
        // Initialise les acteurs une seule fois au démarrage
        this.akkaService.initializeActors();
    }
    
    
    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide.");
            }
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            akkaService.processFile(content);
            model.addAttribute("message", "Fichier téléversé et traité avec succès !");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du traitement du fichier : " + e.getMessage());
        }
        return "index";
    }

    @PostMapping("/search")
    public String searchWord(@RequestParam("word") String word, Model model) {
        System.out.println("Recherche du mot : " + word);
        try {
            int count = akkaService.getWordCount(word);
            System.out.println("Résultat trouvé : " + count);
            model.addAttribute("message", "Le mot '" + word + "' apparaît " + count + " fois.");
        } catch (Exception e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
            model.addAttribute("error", "Erreur lors de la recherche du mot : " + e.getMessage());
        }
        
        return "index";
    }

    @PostMapping("/initialize")
    public String initializeActors(Model model) {
        akkaService.initializeActors();
        model.addAttribute("message", "Acteurs réinitialisés avec succès !");
        return "index";
    }
}