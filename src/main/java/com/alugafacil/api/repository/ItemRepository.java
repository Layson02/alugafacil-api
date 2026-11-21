package com.alugafacil.api.repository;

import com.alugafacil.api.entity.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	List<Item> findByAtivoTrue();

	List<Item> findByNomeContainingIgnoreCase(String nome);

	List<Item> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

	List<Item> findByCategoriaIgnoreCase(String categoria);

	List<Item> findByCategoriaIgnoreCaseAndAtivoTrue(String categoria);
}
