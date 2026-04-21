package com.clinica.controller;

import com.clinica.dto.EditarPacienteRequest;
import com.clinica.entity.Paciente;
import com.clinica.entity.Usuario;
import com.clinica.repository.PacienteRepository;
import com.clinica.repository.UsuarioRepository;
import com.clinica.service.CitaService;
import com.clinica.service.UsuarioSesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/paciente")
public class PacienteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private final UsuarioSesionService usuarioSesionService;
    private final CitaService citaService;

    public PacienteController(UsuarioSesionService usuarioSesionService, CitaService citaService) {
        this.usuarioSesionService = usuarioSesionService;
        this.citaService = citaService;
    }

    // 🔥 GLOBAL (evita repetir código)
    @ModelAttribute
    public void agregarNombreUsuario(Model model) {
        Usuario u = usuarioSesionService.getUsuarioAutenticado();
        if (u != null) {
            model.addAttribute("nombreUsuario",
                    u.getNombre() + " " + u.getApellido1());
        }
    }

    @GetMapping
    public String dashboard() {
        return "paciente/dashboard";
    }

    @GetMapping("/datos")
    public String verDatos(Authentication auth, Model model) {
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();

        Paciente paciente = pacienteRepository.findById(usuario.getId())
                .orElseThrow();

        EditarPacienteRequest req = new EditarPacienteRequest();
        req.setCorreo(usuario.getCorreo());
        req.setTelefono(usuario.getTelefono());
        req.setDireccion(paciente.getDireccion());

        model.addAttribute("datosRequest", req);
        model.addAttribute("username", username);

        return "paciente/datos";
    }

    @GetMapping("/citas")
    public String citasDisponibles(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model) {

        LocalDate fechaBase = fecha != null ? fecha : LocalDate.now();
        model.addAttribute("fechaBase", fechaBase);
        model.addAttribute("citas", citaService.listarDisponiblesSemana(fechaBase));

        return "paciente/citas";
    }

    @PostMapping("/citas/{id}/reservar")
    public String reservar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citaService.reservarCita(id, usuarioSesionService.getUsuarioAutenticado());
            ra.addFlashAttribute("success", "Cita reservada correctamente");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/paciente/citas";
    }

    @GetMapping("/historial")
    public String historial(Model model) {
        Long usuarioId = usuarioSesionService.getUsuarioAutenticado().getId();
        model.addAttribute("citas", citaService.listarPaciente(usuarioId));
        return "paciente/historial";
    }

    @PostMapping("/datos")
    public String actualizarDatos(
            @ModelAttribute("datosRequest") EditarPacienteRequest req,
            Authentication auth,
            RedirectAttributes ra) {

        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();

        Paciente paciente = pacienteRepository.findById(usuario.getId())
                .orElseThrow();

        usuario.setCorreo(req.getCorreo());
        usuario.setTelefono(req.getTelefono());
        paciente.setDireccion(req.getDireccion());

        usuarioRepository.save(usuario);
        pacienteRepository.save(paciente);

        ra.addFlashAttribute("success", "Datos actualizados");

        return "redirect:/paciente/datos";
    }

    @PostMapping("/historial/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citaService.cancelarCitaPaciente(id, usuarioSesionService.getUsuarioAutenticado());
            ra.addFlashAttribute("success", "Cita cancelada y liberada");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/paciente/historial";
    }
}