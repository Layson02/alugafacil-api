package com.alugafacil.api.dto;

import com.alugafacil.api.entity.Reserva;
import com.alugafacil.api.entity.StatusReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponse(
		Long id,
		Long clienteId,
		String clienteNome,
		LocalDate dataInicio,
		LocalDate dataFim,
		StatusReserva status,
		LocalDateTime criadoEm,
		List<ReservaItemResponse> itens
) {

	public static ReservaResponse fromEntity(Reserva reserva) {
		return new ReservaResponse(
				reserva.getId(),
				reserva.getCliente().getId(),
				reserva.getCliente().getNome(),
				reserva.getDataInicio(),
				reserva.getDataFim(),
				reserva.getStatus(),
				reserva.getCriadoEm(),
				reserva.getItens().stream()
						.map(ReservaItemResponse::fromEntity)
						.toList()
		);
	}
}
