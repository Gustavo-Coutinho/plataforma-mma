package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para operações de roles/perfis
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca role por nome
     */
    Optional<Role> findByName(String name);

    /**
     * Verifica se role existe
     */
    boolean existsByName(String name);
}