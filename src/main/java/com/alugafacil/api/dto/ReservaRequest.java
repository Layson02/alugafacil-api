package com.alugafacil.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ReservaRequest(
		@NotNull(message = "O cliente da reserva é obrigatório")
		Long clienteId,

		@NotNull(message = "A data de início da reserva é obrigatória")
		LocalDate dataInicio,

		@NotNull(message = "A data de fim da reserva é obrigatória")
		LocalDate dataFim,

		@NotEmpty(message = "A reserva deve conter pelo menos um item")
		List<@NotNull(message = "O item da reserva é obrigatório") @Valid ReservaItemRequest> itens
) {

	@JsonIgnore
	@AssertTrue(message = "A data final deve ser maior ou igual à data inicial")
	public boolean isPeriodoValido() {
		return dataInicio == null || dataFim == null || !dataFim.isBefore(dataInicio);
	}

	@JsonIgnore
	@AssertTrue(message = "A reserva não pode conter o mesmo item mais de uma vez")
	public boolean isItensSemDuplicidade() {
		if (itens == null) {
			return true;
		}
		Set<Long> itemIds = new HashSet<>();
		return itens.stream()
				.filter(Objects::nonNull)
				.map(ReservaItemRequest::itemId)
				.filter(Objects::nonNull)
				.allMatch(itemIds::add);
	}
}
