package com.alugafacil.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservaItemRequest(
		@NotNull(message = "O item da reserva é obrigatório")
		Long itemId,

		@NotNull(message = "A quantidade do item é obrigatória")
		@Positive(message = "A quantidade do item deve ser maior que zero")
		Integer quantidade
) {
}
