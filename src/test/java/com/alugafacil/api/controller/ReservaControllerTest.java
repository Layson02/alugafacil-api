package com.alugafacil.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alugafacil.api.dto.ReservaItemRequest;
import com.alugafacil.api.dto.ReservaRequest;
import com.alugafacil.api.entity.Cliente;
import com.alugafacil.api.entity.Item;
import com.alugafacil.api.entity.Reserva;
import com.alugafacil.api.entity.ReservaItem;
import com.alugafacil.api.entity.StatusReserva;
import com.alugafacil.api.repository.ClienteRepository;
import com.alugafacil.api.repository.ItemRepository;
import com.alugafacil.api.repository.ReservaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
class ReservaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ReservaRepository reservaRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Test
	void deveCriarReservaComSucesso() throws Exception {
		Cliente cliente = criarCliente();
		Item betoneira = criarItem("Betoneira", 10);
		Item andaime = criarItem("Andaime", 20);
		LocalDate inicio = LocalDate.now().plusDays(1);
		LocalDate fim = LocalDate.now().plusDays(5);

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				inicio,
				fim,
				List.of(
						new ReservaItemRequest(betoneira.getId(), 2),
						new ReservaItemRequest(andaime.getId(), 4)
				)
		);

		MvcResult result = mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.clienteId").value(cliente.getId()))
				.andExpect(jsonPath("$.clienteNome").value(cliente.getNome()))
				.andExpect(jsonPath("$.dataInicio").value(inicio.toString()))
				.andExpect(jsonPath("$.dataFim").value(fim.toString()))
				.andExpect(jsonPath("$.status").value("PENDENTE"))
				.andExpect(jsonPath("$.criadoEm").isNotEmpty())
				.andExpect(jsonPath("$.itens", hasSize(2)))
				.andExpect(jsonPath("$.itens[*].itemId",
						hasItems(betoneira.getId().intValue(), andaime.getId().intValue())))
				.andExpect(jsonPath("$.itens[*].itemNome", hasItems("Betoneira", "Andaime")))
				.andExpect(jsonPath("$.itens[*].quantidade", hasItems(2, 4)))
				.andReturn();

		Long idCriado = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
		assertThat(result.getResponse().getHeader("Location")).endsWith("/reservas/" + idCriado);

		Reserva salva = reservaRepository.findById(idCriado).orElseThrow();
		assertThat(salva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
		assertThat(salva.getCriadoEm()).isNotNull();
		assertThat(salva.getItens()).hasSize(2);
		assertThat(salva.getItens())
				.extracting(reservaItem -> reservaItem.getItem().getNome())
				.containsExactlyInAnyOrder("Betoneira", "Andaime");
	}

	@Test
	void deveRetornar404QuandoClienteNaoExistir() throws Exception {
		Item item = criarItem("Item", 5);

		ReservaRequest request = new ReservaRequest(
				999999L,
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of(new ReservaItemRequest(item.getId(), 1))
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));
	}

	@Test
	void deveRetornar404QuandoItemNaoExistir() throws Exception {
		Cliente cliente = criarCliente();

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of(new ReservaItemRequest(999999L, 1))
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));
	}

	@Test
	void deveRetornar409QuandoItemEstiverInativo() throws Exception {
		Cliente cliente = criarCliente();
		Item item = new Item("Item Inativo", null, "Categoria", 5);
		item.desativar();
		itemRepository.saveAndFlush(item);

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of(new ReservaItemRequest(item.getId(), 1))
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("não está ativo")));
	}

	@Test
	void deveRetornar400QuandoDataFimForMenorQueDataInicio() throws Exception {
		Cliente cliente = criarCliente();
		Item item = criarItem("Item", 5);

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(5),
				LocalDate.now().plusDays(1),
				List.of(new ReservaItemRequest(item.getId(), 1))
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("periodoValido")));
	}

	@Test
	void deveRetornar400QuandoListaDeItensEstiverVazia() throws Exception {
		Cliente cliente = criarCliente();

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of()
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("itens")));
	}

	@Test
	void deveRetornar400QuandoListaDeItensForNula() throws Exception {
		Cliente cliente = criarCliente();

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				null
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("itens")));
	}

	@Test
	void deveRetornar400QuandoQuantidadeForNulaZeroOuNegativa() throws Exception {
		Cliente cliente = criarCliente();
		Item item = criarItem("Item", 10);
		LocalDate inicio = LocalDate.now().plusDays(1);
		LocalDate fim = LocalDate.now().plusDays(2);

		List<ReservaRequest> invalidos = List.of(
				new ReservaRequest(cliente.getId(), inicio, fim,
						List.of(new ReservaItemRequest(item.getId(), null))),
				new ReservaRequest(cliente.getId(), inicio, fim,
						List.of(new ReservaItemRequest(item.getId(), 0))),
				new ReservaRequest(cliente.getId(), inicio, fim,
						List.of(new ReservaItemRequest(item.getId(), -1)))
		);

		for (ReservaRequest request : invalidos) {
			mockMvc.perform(post("/reservas")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.fieldErrors[*].field", hasItem(containsString("quantidade"))));
		}
	}

	@Test
	void deveRetornar400QuandoItemIdForNulo() throws Exception {
		Cliente cliente = criarCliente();

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of(new ReservaItemRequest(null, 1))
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem(containsString("itemId"))));
	}

	@Test
	void deveRetornar400QuandoMesmoItemForInformadoMaisDeUmaVez() throws Exception {
		Cliente cliente = criarCliente();
		Item item = criarItem("Item", 10);

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of(
						new ReservaItemRequest(item.getId(), 1),
						new ReservaItemRequest(item.getId(), 2)
				)
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("itensSemDuplicidade")))
				.andExpect(jsonPath("$.fieldErrors[*].message", hasItem(containsString("mesmo item"))));
	}

	@Test
	void deveListarReservasOrdenadasPorId() throws Exception {
		Cliente cliente = criarCliente();
		Item item = criarItem("Item", 10);

		Reserva primeira = salvarReserva(
				cliente,
				item,
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				1
		);
		Reserva segunda = salvarReserva(
				cliente,
				item,
				LocalDate.now().plusDays(3),
				LocalDate.now().plusDays(4),
				2
		);

		mockMvc.perform(get("/reservas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(primeira.getId()))
				.andExpect(jsonPath("$[0].status").value("PENDENTE"))
				.andExpect(jsonPath("$[1].id").value(segunda.getId()))
				.andExpect(jsonPath("$[1].status").value("PENDENTE"));
	}

	@Test
	void deveBuscarReservaPorIdComSucesso() throws Exception {
		Cliente cliente = criarCliente();
		Item item = criarItem("Betoneira", 10);
		LocalDate inicio = LocalDate.now().plusDays(1);
		LocalDate fim = LocalDate.now().plusDays(3);

		Reserva salva = salvarReserva(cliente, item, inicio, fim, 2);

		mockMvc.perform(get("/reservas/{id}", salva.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salva.getId()))
				.andExpect(jsonPath("$.clienteId").value(cliente.getId()))
				.andExpect(jsonPath("$.clienteNome").value(cliente.getNome()))
				.andExpect(jsonPath("$.dataInicio").value(inicio.toString()))
				.andExpect(jsonPath("$.dataFim").value(fim.toString()))
				.andExpect(jsonPath("$.status").value("PENDENTE"))
				.andExpect(jsonPath("$.criadoEm").isNotEmpty())
				.andExpect(jsonPath("$.itens", hasSize(1)))
				.andExpect(jsonPath("$.itens[0].itemId").value(item.getId()))
				.andExpect(jsonPath("$.itens[0].itemNome").value(item.getNome()))
				.andExpect(jsonPath("$.itens[0].quantidade").value(2));
	}

	@Test
	void naoDevePersistirReservaQuandoAlgumItemForInvalido() throws Exception {
		Cliente cliente = criarCliente();
		Item valido = criarItem("Item Válido", 5);
		long totalAntes = reservaRepository.count();

		ReservaRequest request = new ReservaRequest(
				cliente.getId(),
				LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2),
				List.of(
						new ReservaItemRequest(valido.getId(), 1),
						new ReservaItemRequest(999999L, 1)
				)
		);

		mockMvc.perform(post("/reservas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

		assertThat(reservaRepository.count()).isEqualTo(totalAntes);
	}

	@Test
	void deveRetornar404ParaReservaInexistente() throws Exception {
		mockMvc.perform(get("/reservas/{id}", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));
	}

	private Cliente criarCliente() {
		String sufixo = UUID.randomUUID().toString().substring(0, 8);
		return clienteRepository.saveAndFlush(new Cliente(
				"Cliente " + sufixo,
				"doc" + sufixo,
				"8399" + sufixo,
				"cliente." + sufixo + "@teste.com"
		));
	}

	private Item criarItem(String nome, int quantidadeTotal) {
		return itemRepository.saveAndFlush(new Item(
				nome,
				"Descrição de " + nome,
				"Categoria",
				quantidadeTotal
		));
	}

	private Reserva salvarReserva(Cliente cliente, Item item, LocalDate inicio, LocalDate fim, int quantidade) {
		Reserva reserva = Reserva.builder()
				.cliente(cliente)
				.dataInicio(inicio)
				.dataFim(fim)
				.build();

		ReservaItem reservaItem = ReservaItem.builder()
				.item(item)
				.quantidade(quantidade)
				.build();

		reserva.adicionarItem(reservaItem);
		return reservaRepository.saveAndFlush(reserva);
	}
}
