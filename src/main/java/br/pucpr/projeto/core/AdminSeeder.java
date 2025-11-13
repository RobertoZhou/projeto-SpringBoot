package br.pucpr.projeto.core;

import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {
    private static final String ROLE_ADMIN = "ADMIN";
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        final String adminEmail = "admin123@gmail.com";
        var opt = users.findByEmail(adminEmail);
        if (opt.isPresent()) {
            var admin = opt.get();
            if (!admin.getRoles().contains(ROLE_ADMIN)) {
                var roles = admin.getRoles();
                roles.add(ROLE_ADMIN);
                admin.setRoles(roles);
                users.save(admin);
                System.out.println("[Seeder] ROLE_ADMIN atribuída ao usuário existente: " + adminEmail);
            }
            return;
        }
        String initial = System.getenv("ADMIN_INITIAL_PASSWORD");
        if (initial == null || initial.isBlank()) {
            // Gera uma senha forte aleatória (12 chars)
            initial = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        User admin = new User("Admin", adminEmail, encoder.encode(initial));
        // garantir role admin
        var roles = admin.getRoles();
    roles.add(ROLE_ADMIN);
        admin.setRoles(roles);
        users.save(admin);
        System.out.println("[Seeder] Usuário ADMIN criado: " + adminEmail + ". Defina a senha via env ADMIN_INITIAL_PASSWORD para controlar. Senha inicial gerada: " + initial);
    }
}
