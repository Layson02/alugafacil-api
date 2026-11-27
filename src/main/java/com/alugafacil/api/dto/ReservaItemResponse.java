package com.alugafacil.api.dto;

import com.alugafacil.api.entity.ReservaItem;

public record ReservaItemResponse(
		Long itemId,
		String itemNome,
		Integer quantidade
) {

	public static ReservaItemResponse fromEntity(ReservaItem reservaItem) {
		return new ReservaItemResponse(
				reservaItem.getItem().getId(),
				reservaItem.getItem().getNome(),
				reservaItem.getQuantidade()
		);
	}
}
