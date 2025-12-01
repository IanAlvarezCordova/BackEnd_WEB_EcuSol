package com.ecusol.web.repository;
import com.ecusol.web.model.UsuarioWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioWebRepository extends JpaRepository<UsuarioWeb, Integer> {
    Optional<UsuarioWeb> findByUsername(String username);
    boolean existsByUsername(String username);
}