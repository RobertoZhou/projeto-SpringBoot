package br.pucpr.projeto.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/auth")
    public Map<String, Object> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> info = new HashMap<>();
        
        if (auth == null) {
            info.put("authenticated", false);
            info.put("message", "Nenhuma autenticação encontrada");
            return info;
        }
        
        info.put("authenticated", auth.isAuthenticated());
        info.put("principal", auth.getPrincipal());
        info.put("name", auth.getName());
        info.put("authorities", auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList());
        info.put("details", auth.getDetails());
        
        return info;
    }
}
