package com.alugafacil.api.service;

import com.alugafacil.api.dto.ItemRequest;
import com.alugafacil.api.dto.ItemResponse;
import com.alugafacil.api.entity.Item;
import com.alugafacil.api.exception.ResourceNotFoundException;
import com.alugafacil.api.repository.ItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	@Transactional
	public ItemResponse criar(ItemRequest request) {
		Item item = Item.builder()
				.nome(request.nome().trim())
				.descricao(normalizarDescricao(request.descricao()))
				.categoria(request.categoria().trim())
				.quantidadeTotal(request.quantidadeTotal())
				.build();

		Item salvo = itemRepository.saveAndFlush(item);
		return ItemResponse.fromEntity(salvo);
	}

	@Transactional(readOnly = true)
	public List<ItemResponse> listar() {
		List<Item> itens = itemRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

		return itens.stream()
				.map(ItemResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public ItemResponse buscarPorId(Long id) {
		Item item = buscarEntidadePorId(id);
		return ItemResponse.fromEntity(item);
	}

	@Transactional
	public ItemResponse atualizar(Long id, ItemRequest request) {
		Item item = buscarEntidadePorId(id);

		item.setNome(request.nome().trim());
		item.setDescricao(normalizarDescricao(request.descricao()));
		item.setCategoria(request.categoria().trim());
		item.setQuantidadeTotal(request.quantidadeTotal());

		Item atualizado = itemRepository.saveAndFlush(item);
		return ItemResponse.fromEntity(atualizado);
	}

	@Transactional
	public void desativar(Long id) {
		Item item = buscarEntidadePorId(id);
		item.desativar();
		itemRepository.saveAndFlush(item);
	}

	private Item buscarEntidadePorId(Long id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item não encontrado com o id: " + id));
	}

	private String normalizarDescricao(String descricao) {
		if (descricao == null || descricao.isBlank()) {
			return null;
		}
		return descricao.trim();
	}
}
