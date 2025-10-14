package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.FaceTemplate;
import br.gov.mma.facial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para templates biométricos faciais
 */
@Repository
public interface FaceTemplateRepository extends JpaRepository<FaceTemplate, Long> {

    /**
     * Busca templates por usuário
     */
    List<FaceTemplate> findByUser(User user);

    /**
     * Busca templates por ID do usuário
     */
    List<FaceTemplate> findByUserId(Long userId);

    /**
     * Busca template principal do usuário
     */
    @Query("SELECT ft FROM FaceTemplate ft WHERE ft.user = :user AND ft.isPrimary = true")
    Optional<FaceTemplate> findPrimaryTemplateByUser(@Param("user") User user);

    /**
     * Busca template principal por ID do usuário
     */
    @Query("SELECT ft FROM FaceTemplate ft WHERE ft.user.id = :userId AND ft.isPrimary = true")
    Optional<FaceTemplate> findPrimaryTemplateByUserId(@Param("userId") Long userId);

    /**
     * Busca todos os templates primários (para reconhecimento)
     */
    @Query("SELECT ft FROM FaceTemplate ft WHERE ft.isPrimary = true AND ft.expiresAt IS NULL OR ft.expiresAt > :now")
    List<FaceTemplate> findAllActivePrimaryTemplates(@Param("now") LocalDateTime now);

    /**
     * Busca templates por versão do algoritmo
     */
    List<FaceTemplate> findByAlgorithmVersion(String algorithmVersion);

    /**
     * Busca templates expirados
     */
    @Query("SELECT ft FROM FaceTemplate ft WHERE ft.expiresAt IS NOT NULL AND ft.expiresAt < :now")
    List<FaceTemplate> findExpiredTemplates(@Param("now") LocalDateTime now);

    /**
     * Remove flag primária de outros templates do usuário
     */
    @Modifying
    @Query("UPDATE FaceTemplate ft SET ft.isPrimary = false WHERE ft.user = :user AND ft.id != :excludeId")
    void removePrimaryFlagFromOtherTemplates(@Param("user") User user, @Param("excludeId") Long excludeId);

    /**
     * Remove flag primária de todos os templates do usuário
     */
    @Modifying
    @Query("UPDATE FaceTemplate ft SET ft.isPrimary = false WHERE ft.user = :user")
    void removePrimaryFlagFromAllTemplates(@Param("user") User user);

    /**
     * Remove templates expirados
     */
    @Modifying
    @Query("DELETE FROM FaceTemplate ft WHERE ft.expiresAt IS NOT NULL AND ft.expiresAt < :now")
    void deleteExpiredTemplates(@Param("now") LocalDateTime now);

    /**
     * Conta templates por usuário
     */
    @Query("SELECT COUNT(ft) FROM FaceTemplate ft WHERE ft.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * Busca templates com qualidade mínima
     */
    @Query("SELECT ft FROM FaceTemplate ft WHERE ft.qualityScore >= :minQuality")
    List<FaceTemplate> findByQualityScoreGreaterThanEqual(@Param("minQuality") Double minQuality);

    /**
     * Busca todos os templates primários para identificação
     */
    @Query("SELECT ft FROM FaceTemplate ft WHERE ft.isPrimary = true")
    List<FaceTemplate> findAllPrimaryTemplates();

    /**
     * Conta templates por flag primary
     */
    Long countByIsPrimary(Boolean isPrimary);
}