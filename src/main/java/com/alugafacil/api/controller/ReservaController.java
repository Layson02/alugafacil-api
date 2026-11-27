package com.alugafacil.api.controller;

import com.alugafacil.api.dto.ReservaRequest;
import com.alugafacil.api.dto.ReservaResponse;
import com.alugafacil.api.service.ReservaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

	private final ReservaService reservaService;

	@PostMapping
	public ResponseEntity<ReservaResponse> criar(@Valid @RequestBody ReservaRequest request) {
		ReservaResponse response = reservaService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ReservaResponse>> listar() {
		return ResponseEntity.ok(reservaService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable("id") Long id) {
		return ResponseEntity.ok(reservaService.buscarPorId(id));
	}
}
