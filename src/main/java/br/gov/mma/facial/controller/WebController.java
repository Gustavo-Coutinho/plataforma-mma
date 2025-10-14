package br.gov.mma.facial.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller para páginas web
 * Serve interfaces de usuário para autenticação biométrica
 */
@Controller
@RequestMapping("/")
public class WebController {

    /**
     * Página principal - Interface de autenticação biométrica
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Plataforma de Inteligência Ambiental - MMA");
        model.addAttribute("systemName", "Plataforma de Inteligência Ambiental");
        model.addAttribute("organization", "Ministério do Meio Ambiente - Brasil");
        model.addAttribute("version", "v1.0.0");
        return "index";
    }

    /**
     * Página de dashboard (após login)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard - MMA");
        return "dashboard";
    }

    /**
     * Página de registro de usuário
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Cadastro de Usuário - MMA");
        model.addAttribute("systemName", "Plataforma de Inteligência Ambiental");
        model.addAttribute("organization", "Ministério do Meio Ambiente - Brasil");
        return "register";
    }

    /**
     * Página de cadastro biométrico
     */
    @GetMapping("/enroll")
    public String enroll(Model model) {
        model.addAttribute("pageTitle", "Cadastro Biométrico - MMA");
        return "enroll";
    }

    /**
     * Página de configurações
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Configurações - MMA");
        return "settings";
    }

    /**
     * Página de ajuda
     */
    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("pageTitle", "Ajuda - MMA");
        return "help";
    }
}