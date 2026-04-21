package com.clinica.controller;

import com.clinica.dto.BloqueRequest;
import com.clinica.dto.CitaRequest;
import com.clinica.dto.EditarDoctorRequest;
import com.clinica.entity.Doctor;
import com.clinica.entity.Paciente;
import com.clinica.entity.Usuario;
import com.clinica.enums.EstadoCita;
import com.clinica.repository.CitaRepository;
import com.clinica.repository.DoctorRepository;
import com.clinica.repository.PacienteRepository;
import com.clinica.repository.UsuarioRepository;
import com.clinica.service.BloqueHorarioService;
import com.clinica.service.CitaService;
import com.clinica.service.UsuarioSesionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CitaRepository citaRepository;

    private final UsuarioSesionService usuarioSesionService;
    private final BloqueHorarioService bloqueHorarioService;
    private final CitaService citaService;

    public DoctorController(UsuarioSesionService usuarioSesionService,
                            BloqueHorarioService bloqueHorarioService,
                            CitaService citaService) {
        this.usuarioSesionService = usuarioSesionService;
        this.bloqueHorarioService = bloqueHorarioService;
        this.citaService = citaService;
    }

    @ModelAttribute
    public void agregarNombreUsuario(Model model) {
        Usuario u = usuarioSesionService.getUsuarioAutenticado();
        if (u != null) {
            model.addAttribute("nombreUsuario",
                    "Dr. " + u.getNombre() + " " + u.getApellido1());
        }
    }

    @GetMapping
    public String dashboard() {
        return "doctor/dashboard";
    }

    @GetMapping("/datos")
    public String datos(Authentication auth, Model model) {
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();

        Doctor doctor = doctorRepository.findById(usuario.getId())
                .orElseThrow();

        EditarDoctorRequest req = new EditarDoctorRequest();
        req.setCorreo(usuario.getCorreo());
        req.setTelefono(usuario.getTelefono());
        req.setEspecialidad(doctor.getEspecialidad());

        model.addAttribute("datosRequest", req);
        model.addAttribute("username", username);

        return "doctor/datos";
    }

    @PostMapping("/datos")
    public String actualizarDatos(@ModelAttribute("datosRequest") EditarDoctorRequest req,
                                  Authentication auth,
                                  RedirectAttributes ra) {
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();

        Doctor doctor = doctorRepository.findById(usuario.getId())
                .orElseThrow();

        usuario.setCorreo(req.getCorreo());
        usuario.setTelefono(req.getTelefono());
        doctor.setEspecialidad(req.getEspecialidad());

        usuarioRepository.save(usuario);
        doctorRepository.save(doctor);

        ra.addFlashAttribute("success", "Datos actualizados");
        return "redirect:/doctor/datos";
    }

    @GetMapping("/bloques")
    public String bloques(Model model) {
        Long usuarioId = usuarioSesionService.getUsuarioAutenticado().getId();
        model.addAttribute("bloques", bloqueHorarioService.listarPorDoctor(usuarioId));
        model.addAttribute("bloqueRequest", new BloqueRequest());
        return "doctor/bloques";
    }

    @PostMapping("/bloques")
    public String crearBloque(@Valid @ModelAttribute BloqueRequest bloqueRequest,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Datos inválidos para el bloque");
            return "redirect:/doctor/bloques";
        }

        try {
            bloqueHorarioService.reservarBloque(
                    usuarioSesionService.getUsuarioAutenticado(),
                    bloqueRequest
            );
            redirectAttributes.addFlashAttribute("success", "Bloque reservado correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/doctor/bloques";
    }

    @GetMapping("/generar-citas")
    public String generarCitas(Model model) {
        Long usuarioId = usuarioSesionService.getUsuarioAutenticado().getId();
        model.addAttribute("bloques", bloqueHorarioService.listarPorDoctor(usuarioId));
        model.addAttribute("citaRequest", new CitaRequest());
        return "doctor/generar-citas";
    }

    @PostMapping("/generar-citas")
    public String crearCita(@Valid @ModelAttribute CitaRequest citaRequest,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Datos inválidos para la cita");
            return "redirect:/doctor/generar-citas";
        }

        try {
            citaService.crearCita(
                    usuarioSesionService.getUsuarioAutenticado(),
                    citaRequest
            );
            redirectAttributes.addFlashAttribute("success", "Cita creada correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/doctor/generar-citas";
    }

    @GetMapping("/citas")
    public String citas(Model model) {
        Long usuarioId = usuarioSesionService.getUsuarioAutenticado().getId();
        model.addAttribute("citas", citaService.listarDoctor(usuarioId));
        return "doctor/citas";
    }

    @PostMapping("/citas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citaService.cancelarCitaDoctor(id, usuarioSesionService.getUsuarioAutenticado());
            ra.addFlashAttribute("success", "Cita cancelada");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/doctor/citas";
    }

    @PostMapping("/citas/{id}/atendida")
    public String atender(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citaService.marcarAtendida(id, usuarioSesionService.getUsuarioAutenticado());
            ra.addFlashAttribute("success", "Cita marcada como atendida");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/doctor/citas";
    }

    @PostMapping("/citas/{id}/ausente")
    public String ausente(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citaService.marcarAusente(id, usuarioSesionService.getUsuarioAutenticado());
            ra.addFlashAttribute("success", "Cita marcada como paciente ausente");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/doctor/citas";
    }

    @GetMapping("/pacientes")
    public String pacientes(Model model) {
        List<Paciente> pacientes = pacienteRepository.findAll();
        List<PacienteView> filas = new ArrayList<>();

        for (Paciente paciente : pacientes) {
            Usuario usuario = usuarioRepository.findById(paciente.getId())
                    .orElse(null);

            if (usuario != null) {
                filas.add(new PacienteView(
                        paciente.getId(),
                        usuario.getNombre(),
                        usuario.getApellido1(),
                        usuario.getApellido2(),
                        usuario.getCorreo(),
                        usuario.getTelefono(),
                        paciente.getDireccion()
                ));
            }
        }

        model.addAttribute("pacientes", filas);
        return "doctor/pacientes";
    }

    @Transactional
    @PostMapping("/pacientes/{id}/eliminar")
    public String eliminarPaciente(@PathVariable Long id, RedirectAttributes ra) {
        boolean tieneCitaReservada =
                citaRepository.existsByPacienteIdAndEstadoCita(id, EstadoCita.RESERVADA);

        if (tieneCitaReservada) {
            ra.addFlashAttribute("error",
                    "No se puede eliminar el paciente porque tiene citas reservadas activas.");
            return "redirect:/doctor/pacientes";
        }

        citaRepository.deleteByPacienteId(id);
        pacienteRepository.deleteById(id);
        usuarioRepository.deleteById(id);

        ra.addFlashAttribute("success", "Paciente eliminado correctamente.");
        return "redirect:/doctor/pacientes";
    }

    public static class PacienteView {
        private final Long id;
        private final String nombre;
        private final String apellido1;
        private final String apellido2;
        private final String correo;
        private final String telefono;
        private final String direccion;

        public PacienteView(Long id, String nombre, String apellido1, String apellido2,
                            String correo, String telefono, String direccion) {
            this.id = id;
            this.nombre = nombre;
            this.apellido1 = apellido1;
            this.apellido2 = apellido2;
            this.correo = correo;
            this.telefono = telefono;
            this.direccion = direccion;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellido1() { return apellido1; }
        public String getApellido2() { return apellido2; }
        public String getCorreo() { return correo; }
        public String getTelefono() { return telefono; }
        public String getDireccion() { return direccion; }
    }
}