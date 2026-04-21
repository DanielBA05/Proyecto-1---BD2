package com.clinica.controller;

import com.clinica.entity.Usuario;
import com.clinica.enums.Rol;
import com.clinica.service.UsuarioSesionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UsuarioSesionService usuarioSesionService;

    public HomeController(UsuarioSesionService usuarioSesionService) {
        this.usuarioSesionService = usuarioSesionService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        Usuario usuario = usuarioSesionService.getUsuarioAutenticado();
        return usuario.getRol() == Rol.DOCTOR ? "redirect:/doctor" : "redirect:/paciente";
    }
}
