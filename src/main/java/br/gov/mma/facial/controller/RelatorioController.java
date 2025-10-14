package br.gov.mma.facial.controller;

import br.gov.mma.facial.dto.ApiResponse;
import br.gov.mma.facial.entity.Relatorio;
import br.gov.mma.facial.enums.NivelConfidencialidade;
import br.gov.mma.facial.repository.RelatorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing and accessing environmental reports
 * CORS is configured globally in WebSecurityConfig
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioController.class);

    private final RelatorioRepository relatorioRepository;

    public RelatorioController(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    /**
     * Get reports by confidentiality level
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_1', 'ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) String confidencialidade,
            @RequestParam(required = false) String tag,
            Authentication authentication) {
        try {
            List<Relatorio> reports;
            
            if (confidencialidade != null && !confidencialidade.isEmpty()) {
                // Check if user has access to this confidentiality level
                if (!hasAccess(confidencialidade, authentication)) {
                    return ResponseEntity.status(403).body(
                        ApiResponse.error("Acesso negado a relatórios " + confidencialidade));
                }
                
                if (tag != null && !tag.isEmpty()) {
                    reports = relatorioRepository.findByConfidencialidadeStringAndTagsContaining(
                        confidencialidade, tag);
                } else {
                    reports = relatorioRepository.findByConfidencialidadeString(confidencialidade);
                }
            } else {
                // Return all accessible reports based on user role
                reports = getAccessibleReports(authentication);
            }
            
            logger.info("Retrieved {} reports for confidencialidade: {}, tag: {}", 
                reports.size(), confidencialidade, tag);
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            logger.error("Error getting reports", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar relatórios"));
        }
    }

    /**
     * Get a specific report by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_1', 'ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getReportById(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Relatorio> reportOpt = relatorioRepository.findById(id);
            
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404).body(
                    ApiResponse.error("Relatório não encontrado"));
            }
            
            Relatorio report = reportOpt.get();
            
            // Check access based on confidentiality level  
            String confidencialidade = "PUBLICO";
            if (report.getConfidencialidade() != null) {
                // Get the enum value as string
                NivelConfidencialidade nivel = report.getConfidencialidade();
                switch (nivel) {
                    case PUBLICO:
                        confidencialidade = "PUBLICO";
                        break;
                    case RESTRITO:
                        confidencialidade = "RESTRITO";
                        break;
                    case ULTRASSECRETO:
                        confidencialidade = "ULTRASSECRETO";
                        break;
                    case CONFIDENCIAL:
                        confidencialidade = "CONFIDENCIAL";
                        break;
                    case SIGILOSO:
                        confidencialidade = "SIGILOSO";
                        break;
                    default:
                        confidencialidade = "PUBLICO";
                }
            }
            
            if (!hasAccess(confidencialidade, authentication)) {
                logger.warn("Unauthorized access attempt to report {} by user {}", 
                    id, authentication.getName());
                return ResponseEntity.status(403).body(
                    ApiResponse.error("Acesso negado a este relatório"));
            }
            
            logger.info("Report {} accessed by user {}", id, authentication.getName());
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            logger.error("Error getting report by ID", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar relatório"));
        }
    }

    /**
     * Check if user has access to reports of a given confidentiality level
     */
    private boolean hasAccess(String confidencialidade, Authentication authentication) {
        var authorities = authentication.getAuthorities();
        
        switch (confidencialidade.toUpperCase()) {
            case "PUBLICO":
                // All authenticated users can access public reports
                return true;
                
            case "RESTRITO":
            case "CONFIDENCIAL":
            case "SIGILOSO":
                // ROLE_PERFIL_2 and ROLE_MINISTRO can access restricted reports
                return authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PERFIL__2") || 
                                   a.getAuthority().equals("ROLE_MINISTRO"));
                
            case "ULTRASSECRETO":
                // Only ROLE_MINISTRO can access ultrasecret reports
                return authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MINISTRO"));
                
            default:
                return false;
        }
    }

    /**
     * Get all accessible reports based on user's highest role
     */
    private List<Relatorio> getAccessibleReports(Authentication authentication) {
        var authorities = authentication.getAuthorities();
        
        logger.info("Getting accessible reports for user with authorities: {}", authorities);
        
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MINISTRO"))) {
            // ROLE_MINISTRO can see all published reports (PUBLICO + RESTRITO + ULTRASSECRETO)
            List<Relatorio> allReports = new ArrayList<>();
            allReports.addAll(relatorioRepository.findByConfidencialidadeString("PUBLICO"));
            allReports.addAll(relatorioRepository.findByConfidencialidadeString("RESTRITO"));
            allReports.addAll(relatorioRepository.findByConfidencialidadeString("ULTRASSECRETO"));
            logger.info("ROLE_MINISTRO access: returning {} total reports", allReports.size());
            return allReports;
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PERFIL__2"))) {
            // ROLE_PERFIL_2 can see public and restricted (no ultrasecret)
            List<Relatorio> publicReports = relatorioRepository.findByConfidencialidadeString("PUBLICO");
            List<Relatorio> restrictedReports = relatorioRepository.findByConfidencialidadeString("RESTRITO");
            
            List<Relatorio> combined = new ArrayList<>(publicReports);
            combined.addAll(restrictedReports);
            logger.info("ROLE_PERFIL_2 access: {} public + {} restricted = {} total reports", 
                publicReports.size(), restrictedReports.size(), combined.size());
            return combined;
        } else {
            // ROLE_PERFIL_1 can only see public
            List<Relatorio> publicReports = relatorioRepository.findByConfidencialidadeString("PUBLICO");
            logger.info("ROLE_PERFIL_1 access: returning {} public reports", publicReports.size());
            return publicReports;
        }
    }
}

