package com.alugafacil.api.dto;

import com.alugafacil.api.entity.Cliente;

public record ClienteResponse(
		Long id,
		String nome,
		String documento,
		String telefone,
		String email
) {

	public static ClienteResponse fromEntity(Cliente cliente) {
		return new ClienteResponse(
				cliente.getId(),
				cliente.getNome(),
				cliente.getDocumento(),
				cliente.getTelefone(),
				cliente.getEmail()
		);
	}
}
