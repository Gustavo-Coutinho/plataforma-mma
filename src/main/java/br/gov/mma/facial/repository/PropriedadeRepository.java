package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.Propriedade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositório para propriedades rurais
 */
@Repository
public interface PropriedadeRepository extends JpaRepository<Propriedade, Long> {

    /**
     * Busca propriedades por UF
     */
    List<Propriedade> findByUf(String uf);

    /**
     * Busca propriedades por município
     */
    Page<Propriedade> findByMunicipioContainingIgnoreCase(String municipio, Pageable pageable);

    /**
     * Busca propriedades por UF e município
     */
    List<Propriedade> findByUfAndMunicipioContainingIgnoreCase(String uf, String municipio);

    /**
     * Busca propriedades por status de regularidade
     */
    Page<Propriedade> findByStatusRegularidade(br.gov.mma.facial.enums.StatusRegularidade status, Pageable pageable);

    /**
     * Busca propriedades por CPF/CNPJ do proprietário
     */
    List<Propriedade> findByCpfCnpjProprietario(String cpfCnpj);

    /**
     * Busca propriedades com infrações
     */
    @Query("SELECT p FROM Propriedade p WHERE p.numeroInfracoes > 0")
    Page<Propriedade> findPropriedadesComInfracoes(Pageable pageable);

    /**
     * Busca propriedades por número mínimo de infrações
     */
    @Query("SELECT p FROM Propriedade p WHERE p.numeroInfracoes >= :minInfracoes")
    List<Propriedade> findByNumeroInfracoesGreaterThanEqual(@Param("minInfracoes") Integer minInfracoes);

    /**
     * Busca propriedades por área mínima
     */
    @Query("SELECT p FROM Propriedade p WHERE p.areaHa >= :minArea")
    List<Propriedade> findByAreaHaGreaterThanEqual(@Param("minArea") BigDecimal minArea);

    /**
     * Busca propriedades irregulares por UF
     */
    @Query("SELECT p FROM Propriedade p WHERE p.uf = :uf AND p.statusRegularidade = 'IRREGULAR'")
    List<Propriedade> findPropriedadesIrregularesByUf(@Param("uf") String uf);

    /**
     * Estatísticas por UF
     */
    @Query("SELECT p.uf, COUNT(p), SUM(p.areaHa) FROM Propriedade p GROUP BY p.uf")
    List<Object[]> getEstatisticasPorUf();

    /**
     * Propriedades com fiscalizações recentes
     */
    @Query("SELECT DISTINCT p FROM Propriedade p JOIN p.fiscalizacoes f WHERE f.dataFiscalizacao >= :dataInicio")
    List<Propriedade> findPropriedadesComFiscalizacoesRecentes(@Param("dataInicio") java.time.LocalDate dataInicio);

    /**
     * Conta propriedades por status
     */
    @Query("SELECT COUNT(p) FROM Propriedade p WHERE p.statusRegularidade = :status")
    Long countByStatusRegularidade(@Param("status") br.gov.mma.facial.enums.StatusRegularidade status);
    
    /**
     * Conta propriedades por status (String)
     */
    @Query("SELECT COUNT(p) FROM Propriedade p WHERE CAST(p.statusRegularidade AS string) = :status")
    Long countByStatusRegularidadeString(@Param("status") String status);

    /**
     * Busca propriedades para dados públicos (dados mascarados)
     */
    @Query("SELECT NEW br.gov.mma.facial.dto.PropriedadePublicaDTO(p.id, p.municipio, p.uf, p.areaHa, p.statusRegularidade, p.numeroInfracoes) FROM Propriedade p")
    Page<br.gov.mma.facial.dto.PropriedadePublicaDTO> findPropriedadesPublicas(Pageable pageable);
}