package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.Agrotoxico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para agrotóxicos
 */
@Repository
public interface AgrotoxicoRepository extends JpaRepository<Agrotoxico, Long> {

    /**
     * Busca agrotóxicos por nome
     */
    Page<Agrotoxico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Busca agrotóxicos por princípio ativo
     */
    List<Agrotoxico> findByPrincipioAtivoContainingIgnoreCase(String principioAtivo);

    /**
     * Busca agrotóxicos por status de proibição
     */
    List<Agrotoxico> findByStatusProibicao(br.gov.mma.facial.enums.StatusProibicao status);

    /**
     * Busca agrotóxicos por classificação de risco
     */
    List<Agrotoxico> findByRiscoClassificacao(br.gov.mma.facial.enums.RiscoClassificacao riscoClassificacao);

    /**
     * Busca agrotóxicos proibidos
     */
    @Query("SELECT a FROM Agrotoxico a WHERE a.statusProibicao IN ('PROIBIDO', 'PARCIALMENTE_PROIBIDO')")
    List<Agrotoxico> findAgrotoxicosProibidos();

    /**
     * Busca agrotóxicos de alto risco
     */
    @Query("SELECT a FROM Agrotoxico a WHERE a.riscoClassificacao IN ('ALTO', 'EXTREMO', 'CRITICO')")
    List<Agrotoxico> findAgrotoxicosAltoRisco();

    /**
     * Estatísticas por status
     */
    @Query("SELECT a.statusProibicao, COUNT(a) FROM Agrotoxico a GROUP BY a.statusProibicao")
    List<Object[]> getEstatisticasPorStatus();

    /**
     * Estatísticas por risco
     */
    @Query("SELECT a.riscoClassificacao, COUNT(a) FROM Agrotoxico a GROUP BY a.riscoClassificacao")
    List<Object[]> getEstatisticasPorRisco();

    /**
     * Conta agrotóxicos por status
     */
    Long countByStatusProibicao(br.gov.mma.facial.enums.StatusProibicao status);
    
    /**
     * Conta agrotóxicos por status (String)
     */
    @Query("SELECT COUNT(a) FROM Agrotoxico a WHERE CAST(a.statusProibicao AS string) = :status")
    Long countByStatusProibicaoString(@Param("status") String status);

    /**
     * Conta agrotóxicos por risco
     */
    Long countByRiscoClassificacao(br.gov.mma.facial.enums.RiscoClassificacao risco);
}