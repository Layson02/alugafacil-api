package com.alugafacil.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alugafacil.api.dto.ItemRequest;
import com.alugafacil.api.entity.Item;
import com.alugafacil.api.repository.ItemRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ItemRepository itemRepository;

	@Test
	void deveCadastrarItemComSucesso() throws Exception {
		ItemRequest request = new ItemRequest(
				"  Betoneira 400L  ",
				"  Equipamento para preparo de concreto  ",
				"  Construção  ",
				5
		);

		MvcResult result = mockMvc.perform(post("/itens")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nome").value("Betoneira 400L"))
				.andExpect(jsonPath("$.descricao").value("Equipamento para preparo de concreto"))
				.andExpect(jsonPath("$.categoria").value("Construção"))
				.andExpect(jsonPath("$.quantidadeTotal").value(5))
				.andExpect(jsonPath("$.ativo").value(true))
				.andReturn();

		Long idCriado = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
		assertThat(result.getResponse().getHeader("Location")).endsWith("/itens/" + idCriado);

		Item salvo = itemRepository.findById(idCriado).orElseThrow();
		assertThat(salvo.getNome()).isEqualTo("Betoneira 400L");
		assertThat(salvo.getDescricao()).isEqualTo("Equipamento para preparo de concreto");
		assertThat(salvo.getCategoria()).isEqualTo("Construção");
		assertThat(salvo.getQuantidadeTotal()).isEqualTo(5);
		assertThat(salvo.getAtivo()).isTrue();
	}

	@Test
	void deveCadastrarItemSemDescricao() throws Exception {
		ItemRequest request = new ItemRequest("Andaime Tubular", "   ", "Estrutura", 10);

		MvcResult result = mockMvc.perform(post("/itens")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Andaime Tubular"))
				.andExpect(jsonPath("$.descricao").value(nullValue()))
				.andExpect(jsonPath("$.categoria").value("Estrutura"))
				.andExpect(jsonPath("$.quantidadeTotal").value(10))
				.andExpect(jsonPath("$.ativo").value(true))
				.andReturn();

		Long idCriado = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
		assertThat(itemRepository.findById(idCriado).orElseThrow().getDescricao()).isNull();
	}

	@Test
	void deveRetornar400AoCadastrarItemComDadosInvalidos() throws Exception {
		ItemRequest request = new ItemRequest("   ", "", "   ", 0);

		mockMvc.perform(post("/itens")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/itens"))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItems("nome", "categoria", "quantidadeTotal")));
	}

	@Test
	void deveRetornar400ParaQuantidadeTotalNulaNegativaOuZero() throws Exception {
		List<ItemRequest> invalidos = List.of(
				new ItemRequest("Item Válido", null, "Categoria", null),
				new ItemRequest("Item Válido", null, "Categoria", 0),
				new ItemRequest("Item Válido", null, "Categoria", -1)
		);

		for (ItemRequest request : invalidos) {
			mockMvc.perform(post("/itens")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.fieldErrors[*].field", hasItems("quantidadeTotal")));
		}
	}

	@Test
	void deveRetornar400QuandoCorpoJsonForMalformado() throws Exception {
		mockMvc.perform(post("/itens")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\": \"Sem Fechamento\""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/itens"));
	}

	@Test
	void deveListarItensIncluindoDesativados() throws Exception {
		Item ativo = itemRepository.saveAndFlush(
				new Item("Mesa Redonda", "Mesa para eventos", "Mobiliário", 20)
		);

		Item inativo = new Item("Cadeira Gamer", "Cadeira ergonômica", "Mobiliário", 5);
		inativo.desativar();
		itemRepository.saveAndFlush(inativo);

		mockMvc.perform(get("/itens"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(ativo.getId()))
				.andExpect(jsonPath("$[0].nome").value("Mesa Redonda"))
				.andExpect(jsonPath("$[0].ativo").value(true))
				.andExpect(jsonPath("$[1].id").value(inativo.getId()))
				.andExpect(jsonPath("$[1].nome").value("Cadeira Gamer"))
				.andExpect(jsonPath("$[1].ativo").value(false));
	}

	@Test
	void deveBuscarItemPorIdComSucesso() throws Exception {
		Item salvo = itemRepository.saveAndFlush(
				new Item("Projetor Full HD", "Projetor para apresentações", "Audiovisual", 3)
		);

		mockMvc.perform(get("/itens/{id}", salvo.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.nome").value("Projetor Full HD"))
				.andExpect(jsonPath("$.descricao").value("Projetor para apresentações"))
				.andExpect(jsonPath("$.categoria").value("Audiovisual"))
				.andExpect(jsonPath("$.quantidadeTotal").value(3))
				.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void deveRetornar404ParaItemInexistenteEmGetPutEPatch() throws Exception {
		ItemRequest payload = new ItemRequest(
				"Item Teste",
				"Descrição válida",
				"Categoria",
				1
		);

		mockMvc.perform(get("/itens/{id}", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));

		mockMvc.perform(put("/itens/{id}", 999999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));

		mockMvc.perform(patch("/itens/{id}/desativar", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));
	}

	@Test
	void deveAtualizarItemComSucesso() throws Exception {
		Item salvo = itemRepository.saveAndFlush(
				new Item("Gerador 5kVA", "Gerador portátil", "Energia", 3)
		);

		ItemRequest request = new ItemRequest(
				"  Gerador 5kVA Silencioso  ",
				"  Gerador com isolamento acústico  ",
				"  Energia  ",
				5
		);

		mockMvc.perform(put("/itens/{id}", salvo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.nome").value("Gerador 5kVA Silencioso"))
				.andExpect(jsonPath("$.descricao").value("Gerador com isolamento acústico"))
				.andExpect(jsonPath("$.categoria").value("Energia"))
				.andExpect(jsonPath("$.quantidadeTotal").value(5))
				.andExpect(jsonPath("$.ativo").value(true));

		Item atualizado = itemRepository.findById(salvo.getId()).orElseThrow();
		assertThat(atualizado.getNome()).isEqualTo("Gerador 5kVA Silencioso");
		assertThat(atualizado.getDescricao()).isEqualTo("Gerador com isolamento acústico");
		assertThat(atualizado.getCategoria()).isEqualTo("Energia");
		assertThat(atualizado.getQuantidadeTotal()).isEqualTo(5);
		assertThat(atualizado.getAtivo()).isTrue();
	}

	@Test
	void devePreservarStatusDesativadoAoAtualizarItem() throws Exception {
		Item salvo = new Item("Projetor Antigo", "Projetor reserva", "Audiovisual", 2);
		salvo.desativar();
		itemRepository.saveAndFlush(salvo);

		ItemRequest request = new ItemRequest(
				"Projetor Antigo Revisado",
				"Projetor reserva revisado",
				"Audiovisual",
				3
		);

		mockMvc.perform(put("/itens/{id}", salvo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Projetor Antigo Revisado"))
				.andExpect(jsonPath("$.quantidadeTotal").value(3))
				.andExpect(jsonPath("$.ativo").value(false));

		assertThat(itemRepository.findById(salvo.getId()).orElseThrow().getAtivo()).isFalse();
	}

	@Test
	void deveRetornar400AoAtualizarItemComDadosInvalidos() throws Exception {
		Item salvo = itemRepository.saveAndFlush(
				new Item("Item Válido", null, "Categoria", 2)
		);

		ItemRequest invalido = new ItemRequest("", "   ", "", -1);

		mockMvc.perform(put("/itens/{id}", salvo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalido)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItems("nome", "categoria", "quantidadeTotal")));
	}

	@Test
	void deveDesativarItemSemExcluirRegistro() throws Exception {
		Item salvo = itemRepository.saveAndFlush(
				new Item("Caixa de Som", "Caixa amplificada Bluetooth", "Áudio", 6)
		);

		mockMvc.perform(patch("/itens/{id}/desativar", salvo.getId()))
				.andExpect(status().isNoContent());

		Item desativado = itemRepository.findById(salvo.getId()).orElseThrow();
		assertThat(desativado.getAtivo()).isFalse();
		assertThat(itemRepository.findByAtivoTrue())
				.extracting(Item::getId)
				.doesNotContain(salvo.getId());
	}

	@Test
	void deveDesativarItemJaDesativadoDeFormaIdempotente() throws Exception {
		Item salvo = new Item("Item Inativo", null, "Teste", 1);
		salvo.desativar();
		itemRepository.saveAndFlush(salvo);

		mockMvc.perform(patch("/itens/{id}/desativar", salvo.getId()))
				.andExpect(status().isNoContent());

		Item desativado = itemRepository.findById(salvo.getId()).orElseThrow();
		assertThat(desativado.getAtivo()).isFalse();
	}
}
