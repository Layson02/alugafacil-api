package com.alugafacil.api.controller;

import com.alugafacil.api.dto.ClienteRequest;
import com.alugafacil.api.dto.ClienteResponse;
import com.alugafacil.api.service.ClienteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

	private final ClienteService clienteService;

	@PostMapping
	public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
		ClienteResponse response = clienteService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ClienteResponse>> listar(
			@RequestParam(name = "nome", required = false) String nome
	) {
		return ResponseEntity.ok(clienteService.listar(nome));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable("id") Long id) {
		return ResponseEntity.ok(clienteService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ClienteResponse> atualizar(
			@PathVariable("id") Long id,
			@Valid @RequestBody ClienteRequest request
	) {
		return ResponseEntity.ok(clienteService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
		clienteService.remover(id);
		return ResponseEntity.noContent().build();
	}
}
