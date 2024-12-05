package com.pincio.telegramwebhook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("pages", new String[]{
                "/questions",         // Lista delle domande
                "/confirm-responses", // Pagina per confermare risposte
                "/unprocessed",       // Domande non processate
                "/replacement-history" // Storico delle sostituzioni
        });
        return "main";
    }
}

