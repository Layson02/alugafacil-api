package com.alugafacil.api.service;

import com.alugafacil.api.dto.ReservaItemRequest;
import com.alugafacil.api.dto.ReservaRequest;
import com.alugafacil.api.dto.ReservaResponse;
import com.alugafacil.api.entity.Cliente;
import com.alugafacil.api.entity.Item;
import com.alugafacil.api.entity.Reserva;
import com.alugafacil.api.entity.ReservaItem;
import com.alugafacil.api.exception.ConflictException;
import com.alugafacil.api.exception.ResourceNotFoundException;
import com.alugafacil.api.repository.ClienteRepository;
import com.alugafacil.api.repository.ItemRepository;
import com.alugafacil.api.repository.ReservaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

	private final ReservaRepository reservaRepository;
	private final ClienteRepository clienteRepository;
	private final ItemRepository itemRepository;

	@Transactional
	public ReservaResponse criar(ReservaRequest request) {
		Cliente cliente = clienteRepository.findById(request.clienteId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Cliente não encontrado com o id: " + request.clienteId()));

		Reserva reserva = Reserva.builder()
				.cliente(cliente)
				.dataInicio(request.dataInicio())
				.dataFim(request.dataFim())
				.build();

		for (ReservaItemRequest itemRequest : request.itens()) {
			Item item = itemRepository.findById(itemRequest.itemId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Item não encontrado com o id: " + itemRequest.itemId()));

			if (!Boolean.TRUE.equals(item.getAtivo())) {
				throw new ConflictException("O item " + itemRequest.itemId() + " não está ativo para reserva");
			}

			ReservaItem reservaItem = ReservaItem.builder()
					.item(item)
					.quantidade(itemRequest.quantidade())
					.build();

			reserva.adicionarItem(reservaItem);
		}

		Reserva salva = reservaRepository.saveAndFlush(reserva);
		return ReservaResponse.fromEntity(salva);
	}

	@Transactional(readOnly = true)
	public List<ReservaResponse> listar() {
		List<Reserva> reservas = reservaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

		return reservas.stream()
				.map(ReservaResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public ReservaResponse buscarPorId(Long id) {
		Reserva reserva = reservaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com o id: " + id));
		return ReservaResponse.fromEntity(reserva);
	}
}
