package com.learning.user.infrastructure.bootstrap;

import com.learning.user.infrastructure.persistence.RoleDocument;
import com.learning.user.infrastructure.persistence.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class RbacDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RbacDataSeeder.class);
    private final RoleRepository roleRepository;

    public RbacDataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("RBAC roles already exist in MongoDB ({} roles found). Skipping seeder.", roleRepository.count());
            return;
        }

        log.info("Bootstrapping baseline RBAC roles into MongoDB...");
        Instant now = Instant.now();

        List<RoleDocument> defaultRoles = List.of(
                RoleDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .name("ROLE_USER")
                        .description("Standard retail banking customer")
                        .permissions(Set.of(
                                "account:read",
                                "transfer:execute",
                                "card:read",
                                "card:update_status",
                                "ai:chat"
                        ))
                        .isSystemRole(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                RoleDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .name("ROLE_PREMIUM")
                        .description("Premium banking tier with high limits and priority support")
                        .permissions(Set.of(
                                "account:read",
                                "transfer:execute",
                                "card:read",
                                "card:update_status",
                                "ai:chat",
                                "transfer:high_limit",
                                "ai:premium_features",
                                "priority:support"
                        ))
                        .isSystemRole(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                RoleDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .name("ROLE_SUPPORT")
                        .description("Support desk agent")
                        .permissions(Set.of(
                                "account:read",
                                "chat:support_agent",
                                "card:read",
                                "ticket:manage"
                        ))
                        .isSystemRole(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                RoleDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .name("ROLE_ADMIN")
                        .description("Super Administrator with full permissions")
                        .permissions(Set.of(
                                "user:manage_roles",
                                "user:audit",
                                "kpi:read_analytics",
                                "system:config",
                                "account:read",
                                "transfer:execute"
                        ))
                        .isSystemRole(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        roleRepository.saveAll(defaultRoles);
        log.info("Successfully seeded {} baseline RBAC roles into MongoDB.", defaultRoles.size());
    }
}
