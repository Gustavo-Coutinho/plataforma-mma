package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.Relatorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para relatórios
 */
@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    /**
     * Busca relatórios por tipo
     */
    List<Relatorio> findByTipo(br.gov.mma.facial.enums.TipoRelatorio tipo);

    /**
     * Busca relatórios por nível de confidencialidade
     */
    Page<Relatorio> findByConfidencialidade(br.gov.mma.facial.enums.NivelConfidencialidade confidencialidade, Pageable pageable);

    /**
     * Busca relatórios por status
     */
    List<Relatorio> findByStatus(br.gov.mma.facial.enums.StatusRelatorio status);

    /**
     * Busca relatórios públicos publicados
     */
    @Query("SELECT r FROM Relatorio r WHERE r.confidencialidade = 'PUBLICO' AND r.status = 'PUBLICADO' ORDER BY r.publishedAt DESC")
    Page<Relatorio> findRelatoriosPublicos(Pageable pageable);

    /**
     * Busca relatórios restritos para diretores
     */
    @Query("SELECT r FROM Relatorio r WHERE r.confidencialidade = 'RESTRITO' AND r.status = 'PUBLICADO' ORDER BY r.publishedAt DESC")
    Page<Relatorio> findRelatoriosRestritos(Pageable pageable);

    /**
     * Busca relatórios ultrasecretos para ministro
     */
    @Query("SELECT r FROM Relatorio r WHERE r.confidencialidade = 'ULTRASSECRETO' AND r.status = 'PUBLICADO' ORDER BY r.publishedAt DESC")
    Page<Relatorio> findRelatoriosUltrasecretos(Pageable pageable);

    /**
     * Busca relatórios por autor
     */
    Page<Relatorio> findByAutorContainingIgnoreCase(String autor, Pageable pageable);

    /**
     * Busca relatórios por matrícula do autor
     */
    List<Relatorio> findByMatriculaAutor(String matricula);

    /**
     * Busca relatórios por título
     */
    Page<Relatorio> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    /**
     * Busca relatórios por período de publicação
     */
    @Query("SELECT r FROM Relatorio r WHERE r.publishedAt BETWEEN :dataInicio AND :dataFim")
    List<Relatorio> findByPublishedAtBetween(@Param("dataInicio") LocalDateTime dataInicio, 
                                            @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca relatórios por período de criação
     */
    @Query("SELECT r FROM Relatorio r WHERE r.createdAt BETWEEN :dataInicio AND :dataFim")
    List<Relatorio> findByCreatedAtBetween(@Param("dataInicio") LocalDateTime dataInicio, 
                                          @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca relatórios por tags
     */
    @Query("SELECT r FROM Relatorio r WHERE r.tags LIKE %:tag%")
    List<Relatorio> findByTag(@Param("tag") String tag);

    /**
     * Busca relatórios recentes por confidencialidade
     */
    @Query("SELECT r FROM Relatorio r WHERE r.confidencialidade = :confidencialidade AND r.status = 'PUBLICADO' ORDER BY r.publishedAt DESC")
    List<Relatorio> findRecentByConfidencialidade(@Param("confidencialidade") br.gov.mma.facial.enums.NivelConfidencialidade confidencialidade);

    /**
     * Conta relatórios por tipo
     */
    Long countByTipo(br.gov.mma.facial.enums.TipoRelatorio tipo);

    /**
     * Conta relatórios por confidencialidade
     */
    Long countByConfidencialidade(br.gov.mma.facial.enums.NivelConfidencialidade confidencialidade);
    
    /**
     * Conta relatórios por confidencialidade (String)
     */
    @Query(value = "SELECT COUNT(*) FROM relatorios WHERE CAST(confidencialidade AS TEXT) = :confidencialidade AND CAST(status AS TEXT) = 'PUBLICADO'", nativeQuery = true)
    Long countByConfidencialidadeString(@Param("confidencialidade") String confidencialidade);
    
    /**
     * Busca relatórios por confidencialidade (String)
     */
    @Query(value = "SELECT * FROM relatorios WHERE CAST(confidencialidade AS TEXT) = :confidencialidade AND CAST(status AS TEXT) = 'PUBLICADO' ORDER BY published_at DESC", nativeQuery = true)
    List<Relatorio> findByConfidencialidadeString(@Param("confidencialidade") String confidencialidade);
    
    /**
     * Busca relatórios por confidencialidade e tags
     */
    @Query(value = "SELECT * FROM relatorios WHERE CAST(confidencialidade AS TEXT) = :confidencialidade AND tags LIKE CONCAT('%', :tag, '%') AND CAST(status AS TEXT) = 'PUBLICADO' ORDER BY published_at DESC", nativeQuery = true)
    List<Relatorio> findByConfidencialidadeStringAndTagsContaining(@Param("confidencialidade") String confidencialidade, @Param("tag") String tag);
    
    /**
     * Busca relatórios por lista de confidencialidade
     */
    @Query("SELECT r FROM Relatorio r WHERE r.confidencialidade IN :confidencialidades AND r.status = 'PUBLICADO' ORDER BY r.publishedAt DESC")
    List<Relatorio> findByConfidencialidadeIn(@Param("confidencialidades") List<br.gov.mma.facial.enums.NivelConfidencialidade> confidencialidades);

    /**
     * Busca relatórios de compliance/auditoria
     */
    @Query("SELECT r FROM Relatorio r WHERE r.tipo = 'COMPLIANCE' OR r.tags LIKE '%auditoria%' OR r.tags LIKE '%lgpd%'")
    List<Relatorio> findRelatoriosCompliance();

    /**
     * Relatórios por escopo geográfico
     */
    @Query("SELECT r FROM Relatorio r WHERE r.escopo LIKE %:escopo%")
    List<Relatorio> findByEscopo(@Param("escopo") String escopo);
}