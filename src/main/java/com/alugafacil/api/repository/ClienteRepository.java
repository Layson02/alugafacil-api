package com.alugafacil.api.repository;

import com.alugafacil.api.entity.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	Optional<Cliente> findByDocumento(String documento);

	Optional<Cliente> findByEmail(String email);

	boolean existsByDocumento(String documento);

	boolean existsByDocumentoAndIdNot(String documento, Long id);

	boolean existsByEmail(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
