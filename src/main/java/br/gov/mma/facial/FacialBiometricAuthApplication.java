package br.gov.mma.facial;

import br.gov.mma.facial.config.BiometricProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Plataforma de Inteligência Ambiental
 * Ministério do Meio Ambiente - Brasil
 * 
 * Sistema completo de Plataforma de Inteligência Ambiental usando OpenCV,
 * com controle de acesso por perfis, auditoria e conformidade com LGPD.
 * 
 * Funcionalidades principais:
 * - Plataforma de Inteligência Ambiental com OpenCV e LBPH
 * - Controle de acesso baseado em roles (ROLE_PERFIL_1, ROLE_PERFIL_2, ROLE_MINISTRO)
 * - Sistema de auditoria completo
 * - Anti-spoofing e liveness detection
 * - Conformidade com LGPD
 * - API REST para integração
 * 
 * @author Sistema MMA
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
@EnableConfigurationProperties({BiometricProperties.class})
public class FacialBiometricAuthApplication {

    public static void main(String[] args) {
        // Inicialização do OpenCV com mecanismo de fallback
        if (br.gov.mma.facial.util.OpenCVLoader.loadOpenCV()) {
            System.out.println("✅ OpenCV carregado com sucesso");
        } else {
            System.err.println("❌ Falha ao carregar OpenCV após múltiplas tentativas");
            System.err.println("⚠️  O sistema continuará mas funcionalidades biométricas podem não funcionar");
        }

        // Configuração de propriedades do sistema
        System.setProperty("spring.jpa.open-in-view", "false");
        System.setProperty("spring.profiles.active", 
            System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "development"));

        // Banner personalizado
        System.setProperty("spring.banner.location", "classpath:banner.txt");

        // Inicialização da aplicação
        SpringApplication app = new SpringApplication(FacialBiometricAuthApplication.class);

        // Configurações adicionais de inicialização
        app.setAdditionalProfiles(getActiveProfiles());

        System.out.println("🚀  Iniciando...ataforma de Inteligência Ambiental do MMA");
        System.out.println("📋 Perfil ativo: " + String.join(", ", getActiveProfiles()));

        app.run(args);
    }

    /**
     * Determina os perfis ativos baseado em variáveis de ambiente
     */
    private static String[] getActiveProfiles() {
        String env = System.getenv("ENVIRONMENT");
        if (env == null) {
            env = System.getProperty("spring.profiles.active", "development");
        }

        return switch (env.toLowerCase()) {
            case "prod", "production" -> new String[]{"production"};
            case "test", "testing" -> new String[]{"test"};
            default -> new String[]{"development"};
        };
    }
}