package br.com.fiap.aguiax.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloWorldController {

    @GetMapping("/english")
    public String getSayHello() {
        return "<h1>Hello World!</h1>";
    }

    @GetMapping("/deutsch")
    public String getSagHallo() {
        return "<h1>Servus zusammen!</h1>";
    }
}

