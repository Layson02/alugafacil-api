package com.alugafacil.api.dto;

import com.alugafacil.api.entity.Item;

public record ItemResponse(
		Long id,
		String nome,
		String descricao,
		String categoria,
		Integer quantidadeTotal,
		Boolean ativo
) {

	public static ItemResponse fromEntity(Item item) {
		return new ItemResponse(
				item.getId(),
				item.getNome(),
				item.getDescricao(),
				item.getCategoria(),
				item.getQuantidadeTotal(),
				item.getAtivo()
		);
	}
}
