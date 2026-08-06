package com.tiendaTech.tienda.controller;

import com.tiendaTech.tienda.domain.Usuario;
import com.tiendaTech.tienda.service.UsuarioService;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario_rol")
public class UsuarioRolController {

    private final UsuarioService usuarioService;

    public UsuarioRolController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/mantenimiento")
    public String mantenimiento(
            @RequestParam(
                    name = "username",
                    required = false
            ) String username,
            Model model
    ) {

        model.addAttribute(
                "rolesDisponibles",
                usuarioService.getRoles()
        );

        if (username != null && !username.isBlank()) {

            Optional<Usuario> usuarioOpt
                    = usuarioService
                            .getUsuarioConRolesPorUsername(
                                    username.trim()
                            );

            if (usuarioOpt.isPresent()) {
                model.addAttribute(
                        "usuarioEncontrado",
                        usuarioOpt.get()
                );
            } else {
                model.addAttribute(
                        "error",
                        "No se encontró un usuario con el nombre "
                        + username
                        + "."
                );
            }
        }

        return "/usuario_rol/mantenimiento";
    }

    @PostMapping("/asignar")
    public String asignar(
            @RequestParam String username,
            @RequestParam String rol,
            RedirectAttributes redirectAttributes
    ) {

        try {
            usuarioService.asignarRolPorUsername(
                    username,
                    rol
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "El rol se asignó correctamente."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible asignar el rol."
            );
        }

        redirectAttributes.addAttribute(
                "username",
                username
        );

        return "redirect:/usuario_rol/mantenimiento";
    }

    @PostMapping("/revocar")
    public String revocar(
            @RequestParam String username,
            @RequestParam Integer idRol,
            RedirectAttributes redirectAttributes
    ) {

        try {
            usuarioService.revocarRolPorUsername(
                    username,
                    idRol
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "El rol se revocó correctamente."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible revocar el rol."
            );
        }

        redirectAttributes.addAttribute(
                "username",
                username
        );

        return "redirect:/usuario_rol/mantenimiento";
    }
}