package com.exemplo.eventos.controller;

import com.exemplo.eventos.dto.InscricaoDto;
import com.exemplo.eventos.model.Evento;
import com.exemplo.eventos.service.EventoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Controller
@RequestMapping("/")
public class EventoController {

    private static final String ADMIN_SESSION_KEY =
            "ADMIN_AUTORIZADO";

    private final EventoService eventoService;

    @Value("${app.admin.codigo:}")
    private String codigoAdminConfigurado;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    private boolean adminAutorizado(HttpSession session) {
        return Boolean.TRUE.equals(
                session.getAttribute(ADMIN_SESSION_KEY));
    }

    // PÁGINA INICIAL
    @GetMapping
    public String listar(Model model) {
        model.addAttribute(
                "eventos", eventoService.listarEventosOrdenados());

        model.addAttribute(
                "inscricaoDto", new InscricaoDto(null, "", ""));

        return "index";
    }

    // REALIZAR INSCRIÇÃO
    @PostMapping("/inscrever")
    public String inscrever(
            @Valid @ModelAttribute("inscricaoDto") InscricaoDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Dados de inscrição inválidos.");
            return "redirect:/";
        }

        try {
            String codigo = eventoService.realizarInscricao(dto);

            redirectAttributes.addFlashAttribute(
                    "sucesso", "Inscrição realizada com sucesso!");

            redirectAttributes.addFlashAttribute(
                    "codigoCancelamento", codigo);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro", e.getMessage());
        }

        return "redirect:/";
    }

    // CANCELAR INSCRIÇÃO
    @PostMapping("/cancelar-inscricao")
    public String cancelarInscricao(
            @RequestParam String email,
            @RequestParam String codigoCancelamento,
            RedirectAttributes redirectAttributes) {

        try {
            eventoService.cancelarInscricao(
                    email, codigoCancelamento);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Inscrição cancelada. A vaga voltou a ficar disponível.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro", e.getMessage());
        }

        return "redirect:/";
    }

    // TELA DE LOGIN ADMINISTRATIVO
    @GetMapping("/admin/login")
    public String loginForm(HttpSession session) {
        return adminAutorizado(session)
                ? "redirect:/admin"
                : "admin-login";
    }

    // VALIDAR CÓDIGO ADMINISTRATIVO
    @PostMapping("/admin/login")
    public String login(
            @RequestParam String codigo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (codigoAdminConfigurado == null
                || codigoAdminConfigurado.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "erroLogin",
                    "O código administrativo não foi configurado.");

            return "redirect:/admin/login";
        }

        boolean correto = MessageDigest.isEqual(
                codigo.getBytes(StandardCharsets.UTF_8),
                codigoAdminConfigurado.getBytes(StandardCharsets.UTF_8));

        if (correto) {
            session.setAttribute(ADMIN_SESSION_KEY, true);
            session.setMaxInactiveInterval(30 * 60);

            return "redirect:/admin";
        }

        redirectAttributes.addFlashAttribute(
                "erroLogin", "Código incorreto.");

        return "redirect:/admin/login";
    }

    // SAIR DA ÁREA ADMINISTRATIVA
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // PAINEL ADMINISTRATIVO
    @GetMapping("/admin")
    public String painelAdmin(
            HttpSession session,
            Model model) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "eventos", eventoService.listarEventosOrdenados());

        return "admin";
    }

    // VISUALIZAR INSCRITOS
    @GetMapping("/admin/inscricoes/{id}")
    public String listarInscricoes(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "evento", eventoService.buscarPorId(id));

        model.addAttribute(
                "inscricoes",
                eventoService.listarInscricoesDoEvento(id));

        return "inscricoes";
    }

    // CADASTRAR EVENTO
    @GetMapping("/admin/novo")
    public String novoEventoForm(
            HttpSession session,
            Model model) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("evento", new Evento());
        model.addAttribute(
                "tituloPagina", "Cadastrar novo evento");

        return "evento-form";
    }

    // EDITAR EVENTO
    @GetMapping("/admin/editar/{id}")
    public String editarEventoForm(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "evento", eventoService.buscarPorId(id));

        model.addAttribute(
                "tituloPagina", "Editar evento");

        return "evento-form";
    }

    // SALVAR EVENTO
    @PostMapping("/admin/salvar")
    public String salvarEvento(
            @ModelAttribute Evento evento,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        try {
            eventoService.salvarEvento(evento);

            redirectAttributes.addFlashAttribute(
                    "sucesso", "Evento salvo com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro", e.getMessage());
        }

        return "redirect:/admin";
    }


    // Edita os dados de um participante
    @PostMapping("/admin/inscricoes/{id}/editar")
    public String editarInscricaoAdmin(
            @PathVariable Long id,
            @RequestParam String nomeParticipante,
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        try {
            eventoService.atualizarInscricaoAdmin(
                    id, nomeParticipante, email);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Participante atualizado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro", e.getMessage());
        }

        return "redirect:/admin";
    }

    // Remove um participante
    @PostMapping("/admin/inscricoes/{id}/excluir")
    public String excluirInscricaoAdmin(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        try {
            eventoService.removerInscricaoAdmin(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Inscrição removida. A vaga foi liberada!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro", e.getMessage());
        }

        return "redirect:/admin";
    }
    // EXCLUIR EVENTO
    @GetMapping("/admin/deletar/{id}")
    public String deletarEvento(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!adminAutorizado(session)) {
            return "redirect:/admin/login";
        }

        try {
            eventoService.deletarEvento(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso", "Evento excluído com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Não foi possível excluir o evento.");
        }

        return "redirect:/admin";
    }
}