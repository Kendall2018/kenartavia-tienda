package com.tiendaTech.tienda.controller;

import com.tiendaTech.tienda.domain.Constante;
import com.tiendaTech.tienda.service.ConstanteService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/constante")
public class ConstanteController {

    private final ConstanteService constanteService;

    public ConstanteController(ConstanteService constanteService) {
        this.constanteService = constanteService;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var constantes = constanteService.getConstantes();

        model.addAttribute("constantes", constantes);
        model.addAttribute("totalConstantes", constantes.size());
        model.addAttribute("constante", new Constante());

        return "/constante/listado";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid Constante constante,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            String mensaje = bindingResult
                    .getFieldErrors()
                    .stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("Revise la información ingresada.");

            redirectAttributes.addFlashAttribute("error", mensaje);

            if (constante.getIdConstante() != null) {
                return "redirect:/constante/modificar/"
                        + constante.getIdConstante();
            }

            return "redirect:/constante/listado";
        }

        try {
            constanteService.save(constante);

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "La constante se guardó correctamente."
            );

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una constante con ese atributo."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible guardar la constante."
            );
        }

        return "redirect:/constante/listado";
    }

    @GetMapping("/modificar/{idConstante}")
    public String modificar(
            @PathVariable Integer idConstante,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        Optional<Constante> constanteOpt
                = constanteService.getConstante(idConstante);

        if (constanteOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "La constante solicitada no existe."
            );

            return "redirect:/constante/listado";
        }

        model.addAttribute("constante", constanteOpt.get());

        return "/constante/modifica";
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam Integer idConstante,
            RedirectAttributes redirectAttributes
    ) {

        try {
            constanteService.delete(idConstante);

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "La constante se eliminó correctamente."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible eliminar la constante."
            );
        }

        return "redirect:/constante/listado";
    }
}