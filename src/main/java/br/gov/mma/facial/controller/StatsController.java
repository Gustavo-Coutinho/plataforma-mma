package br.gov.mma.facial.controller;

import br.gov.mma.facial.dto.ApiResponse;
import br.gov.mma.facial.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para estatísticas do sistema (apenas ROLE_MINISTRO e ROLE_PERFIL_2)
 */
@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatsController {

    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);

    private final UserRepository userRepository;
    private final FaceTemplateRepository faceTemplateRepository;
    private final PropriedadeRepository propriedadeRepository;
    private final RelatorioRepository relatorioRepository;
    private final FiscalizacaoRepository fiscalizacaoRepository;
    private final AgrotoxicoRepository agrotoxicoRepository;

    public StatsController(UserRepository userRepository, 
                          FaceTemplateRepository faceTemplateRepository,
                          PropriedadeRepository propriedadeRepository,
                          RelatorioRepository relatorioRepository,
                          FiscalizacaoRepository fiscalizacaoRepository,
                          AgrotoxicoRepository agrotoxicoRepository) {
        this.userRepository = userRepository;
        this.faceTemplateRepository = faceTemplateRepository;
        this.propriedadeRepository = propriedadeRepository;
        this.relatorioRepository = relatorioRepository;
        this.fiscalizacaoRepository = fiscalizacaoRepository;
        this.agrotoxicoRepository = agrotoxicoRepository;
    }

    /**
     * Get executive statistics (ROLE_MINISTRO only) - Includes MMA Environmental Data
     */
    @GetMapping("/executive")
    @PreAuthorize("hasRole('ROLE_MINISTRO')")
    public ResponseEntity<?> getExecutiveStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // System user stats
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            
            long totalTemplates = faceTemplateRepository.count();
            stats.put("totalTemplates", totalTemplates);
            
            Long activeTemplates = faceTemplateRepository.countByIsPrimary(true);
            stats.put("activeTemplates", activeTemplates);
            
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            long loggedInToday = userRepository.countByLastLoginAfter(todayStart);
            stats.put("authenticationsToday", loggedInToday);
            
            double successRate = totalTemplates > 0 ? 
                (double) activeTemplates / totalTemplates : 0.0;
            stats.put("successRate", successRate);
            
            long lockedAccounts = userRepository.countByAccountLocked(true);
            stats.put("lockedAccounts", lockedAccounts);
            
            // MMA Environmental Data
            long totalPropriedades = propriedadeRepository.count();
            stats.put("totalPropriedades", totalPropriedades);
            
            long propriedadesIrregulares = propriedadeRepository.countByStatusRegularidadeString("IRREGULAR");
            stats.put("propriedadesIrregulares", propriedadesIrregulares);
            
            long totalFiscalizacoes = fiscalizacaoRepository.count();
            stats.put("totalFiscalizacoes", totalFiscalizacoes);
            
            long fiscalizacoesAbertas = fiscalizacaoRepository.countByStatus("EM_ANDAMENTO");
            stats.put("fiscalizacoesAbertas", fiscalizacoesAbertas);
            
            long totalRelatorios = relatorioRepository.count();
            stats.put("totalRelatorios", totalRelatorios);
            
            long relatoriosUltrasecretos = relatorioRepository.countByConfidencialidadeString("ULTRASSECRETO");
            stats.put("relatoriosUltrasecretos", relatoriosUltrasecretos);
            
            long totalAgrotoxicos = agrotoxicoRepository.count();
            stats.put("totalAgrotoxicos", totalAgrotoxicos);
            
            long agrotoxicosProibidos = agrotoxicoRepository.countByStatusProibicaoString("PROIBIDO");
            long agrotoxicosBanidos = agrotoxicoRepository.countByStatusProibicaoString("BANIDO");
            stats.put("agrotoxicosProibidos", agrotoxicosProibidos + agrotoxicosBanidos);
            
            logger.info("Executive stats retrieved: {} users, {} properties, {} reports", 
                totalUsers, totalPropriedades, totalRelatorios);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting executive stats", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar estatísticas executivas"));
        }
    }

    /**
     * Get administrative statistics (ROLE_PERFIL_2+) - Director-level strategic information
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getAdminStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // User system stats
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            
            long activeUsers = userRepository.countByAccountLocked(false);
            stats.put("activeUsers", activeUsers);
            
            long lockedAccounts = userRepository.countByAccountLocked(true);
            stats.put("lockedAccounts", lockedAccounts);
            
            long totalTemplates = faceTemplateRepository.count();
            stats.put("totalTemplates", totalTemplates);
            
            // Strategic environmental data for directors
            long totalPropriedades = propriedadeRepository.count();
            stats.put("totalPropriedades", totalPropriedades);
            
            long propriedadesIrregulares = propriedadeRepository.countByStatusRegularidadeString("IRREGULAR");
            stats.put("propriedadesIrregulares", propriedadesIrregulares);
            
            long totalFiscalizacoes = fiscalizacaoRepository.count();
            stats.put("totalFiscalizacoes", totalFiscalizacoes);
            
            // Mock data for total fines (would require query in FiscalizacaoRepository)
            stats.put("totalMultas", 1415000.00); // Sum of fines from sample data
            
            long relatoriosRestritos = relatorioRepository.countByConfidencialidadeString("RESTRITO");
            stats.put("relatoriosRestritos", relatoriosRestritos);
            
            long agrotoxicosProibidos = agrotoxicoRepository.countByStatusProibicaoString("PROIBIDO");
            long agrotoxicosBanidos = agrotoxicoRepository.countByStatusProibicaoString("BANIDO");
            stats.put("agrotoxicosProibidos", agrotoxicosProibidos + agrotoxicosBanidos);
            
            // Mock data for contaminated areas
            stats.put("areasContaminadas", propriedadesIrregulares); // Use irregular properties as proxy
            
            logger.info("Admin stats retrieved for ROLE_PERFIL_2 (Directors)");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting admin stats", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar estatísticas administrativas"));
        }
    }

    /**
     * Get system health (all authenticated users)
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_1', 'ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("totalUsers", userRepository.count());
            health.put("databaseConnected", true);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error checking system health", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }
    
    /**
     * Get public MMA statistics (all authenticated users - ROLE_PERFIL_1)
     */
    @GetMapping("/public")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_1', 'ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getPublicStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Public information available on MMA intranet
            long relatoriosPublicos = relatorioRepository.countByConfidencialidadeString("PUBLICO");
            stats.put("relatoriosPublicos", relatoriosPublicos);
            
            long totalAgrotoxicos = agrotoxicoRepository.count();
            stats.put("totalAgrotoxicos", totalAgrotoxicos);
            
            // Mock data for legislation count
            stats.put("legislacoes", 3);
            
            logger.info("Public stats retrieved for general user access");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting public stats", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar estatísticas públicas"));
        }
    }
}
