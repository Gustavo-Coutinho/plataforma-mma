package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.Fiscalizacao;
import br.gov.mma.facial.entity.Propriedade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositório para fiscalizações
 */
@Repository
public interface FiscalizacaoRepository extends JpaRepository<Fiscalizacao, Long> {

    /**
     * Busca fiscalizações por propriedade
     */
    List<Fiscalizacao> findByPropriedade(Propriedade propriedade);

    /**
     * Busca fiscalizações por responsável
     */
    Page<Fiscalizacao> findByResponsavelContainingIgnoreCase(String responsavel, Pageable pageable);

    /**
     * Busca fiscalizações por matrícula do responsável
     */
    List<Fiscalizacao> findByMatriculaResponsavel(String matricula);

    /**
     * Busca fiscalizações por período
     */
    @Query("SELECT f FROM Fiscalizacao f WHERE f.dataFiscalizacao BETWEEN :dataInicio AND :dataFim")
    List<Fiscalizacao> findByDataFiscalizacaoBetween(@Param("dataInicio") LocalDate dataInicio, 
                                                    @Param("dataFim") LocalDate dataFim);

    /**
     * Busca fiscalizações por status
     */
    List<Fiscalizacao> findByStatus(br.gov.mma.facial.enums.StatusFiscalizacao status);
    
    /**
     * Conta fiscalizações por status (String)
     */
    @Query("SELECT COUNT(f) FROM Fiscalizacao f WHERE CAST(f.status AS string) = :status")
    Long countByStatus(@Param("status") String status);

    /**
     * Busca fiscalizações por resultado
     */
    List<Fiscalizacao> findByResultado(br.gov.mma.facial.enums.ResultadoFiscalizacao resultado);

    /**
     * Busca fiscalizações com irregularidades
     */
    @Query("SELECT f FROM Fiscalizacao f WHERE f.irregularidades IS NOT NULL AND f.irregularidades != ''")
    Page<Fiscalizacao> findFiscalizacoesComIrregularidades(Pageable pageable);

    /**
     * Busca fiscalizações com multa
     */
    @Query("SELECT f FROM Fiscalizacao f WHERE f.valorMulta IS NOT NULL AND f.valorMulta > 0")
    List<Fiscalizacao> findFiscalizacoesComMulta();

    /**
     * Busca fiscalizações recentes
     */
    @Query("SELECT f FROM Fiscalizacao f WHERE f.dataFiscalizacao >= :dataLimite ORDER BY f.dataFiscalizacao DESC")
    List<Fiscalizacao> findFiscalizacoesRecentes(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Estatísticas por resultado
     */
    @Query("SELECT f.resultado, COUNT(f) FROM Fiscalizacao f GROUP BY f.resultado")
    List<Object[]> getEstatisticasPorResultado();

    /**
     * Soma total de multas por período
     */
    @Query("SELECT SUM(f.valorMulta) FROM Fiscalizacao f WHERE f.dataFiscalizacao BETWEEN :dataInicio AND :dataFim AND f.valorMulta IS NOT NULL")
    java.math.BigDecimal getSomaTotalMultasPorPeriodo(@Param("dataInicio") LocalDate dataInicio, 
                                                     @Param("dataFim") LocalDate dataFim);

    /**
     * Conta fiscalizações por UF
     */
    @Query("SELECT p.uf, COUNT(f) FROM Fiscalizacao f JOIN f.propriedade p GROUP BY p.uf")
    List<Object[]> countFiscalizacoesPorUf();

    /**
     * Fiscalizações por propriedade e período
     */
    @Query("SELECT f FROM Fiscalizacao f WHERE f.propriedade = :propriedade AND f.dataFiscalizacao BETWEEN :dataInicio AND :dataFim")
    List<Fiscalizacao> findByPropriedadeAndPeriodo(@Param("propriedade") Propriedade propriedade,
                                                  @Param("dataInicio") LocalDate dataInicio,
                                                  @Param("dataFim") LocalDate dataFim);
}