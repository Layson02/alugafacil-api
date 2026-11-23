package com.alugafacil.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemRequest(
		@NotBlank(message = "O nome do item é obrigatório")
		@Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
		String nome,

		@Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
		String descricao,

		@NotBlank(message = "A categoria do item é obrigatória")
		@Size(max = 100, message = "A categoria deve ter no máximo 100 caracteres")
		String categoria,

		@NotNull(message = "A quantidade total do item é obrigatória")
		@Positive(message = "A quantidade total deve ser maior que zero")
		Integer quantidadeTotal
) {
}
