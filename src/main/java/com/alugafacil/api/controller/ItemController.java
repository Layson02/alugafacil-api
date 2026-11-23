package com.alugafacil.api.controller;

import com.alugafacil.api.dto.ItemRequest;
import com.alugafacil.api.dto.ItemResponse;
import com.alugafacil.api.service.ItemService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/itens")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@PostMapping
	public ResponseEntity<ItemResponse> criar(@Valid @RequestBody ItemRequest request) {
		ItemResponse response = itemService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ItemResponse>> listar() {
		return ResponseEntity.ok(itemService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ItemResponse> buscarPorId(@PathVariable("id") Long id) {
		return ResponseEntity.ok(itemService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ItemResponse> atualizar(
			@PathVariable("id") Long id,
			@Valid @RequestBody ItemRequest request
	) {
		return ResponseEntity.ok(itemService.atualizar(id, request));
	}

	@PatchMapping("/{id}/desativar")
	public ResponseEntity<Void> desativar(@PathVariable("id") Long id) {
		itemService.desativar(id);
		return ResponseEntity.noContent().build();
	}
}
