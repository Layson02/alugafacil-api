package com.alugafacil.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alugafacil.api.dto.ClienteRequest;
import com.alugafacil.api.entity.Cliente;
import com.alugafacil.api.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void deveCadastrarClienteComSucesso() throws Exception {
		ClienteRequest request = new ClienteRequest(
				"  Maria Silva  ",
				" 12345678901 ",
				" 83999990000 ",
				"maria.silva@alugafacil.com"
		);

		MvcResult result = mockMvc.perform(post("/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nome").value("Maria Silva"))
				.andExpect(jsonPath("$.documento").value("12345678901"))
				.andExpect(jsonPath("$.telefone").value("83999990000"))
				.andExpect(jsonPath("$.email").value("maria.silva@alugafacil.com"))
				.andReturn();

		Long idCriado = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
		assertThat(result.getResponse().getHeader("Location")).endsWith("/clientes/" + idCriado);
		assertThat(clienteRepository.findById(idCriado)).isPresent();
	}

	@Test
	void deveRetornar400AoCadastrarClienteComDadosInvalidos() throws Exception {
		ClienteRequest request = new ClienteRequest("   ", "", "   ", "email-invalido");

		mockMvc.perform(post("/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/clientes"))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItems("nome", "documento", "telefone", "email")));
	}

	@Test
	void deveRetornar400QuandoCorpoJsonForMalformado() throws Exception {
		mockMvc.perform(post("/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\": \"Sem Fechamento\""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/clientes"));
	}

	@Test
	void deveRetornar409AoCadastrarDocumentoOuEmailDuplicado() throws Exception {
		clienteRepository.saveAndFlush(
				new Cliente("Cliente Existente", "11122233344", "83991112222", "existente@alugafacil.com")
		);

		ClienteRequest documentoDuplicado = new ClienteRequest(
				"Outro Nome",
				"11122233344",
				"83993334444",
				"outro.email@alugafacil.com"
		);

		mockMvc.perform(post("/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(documentoDuplicado)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("documento")));

		ClienteRequest emailDuplicado = new ClienteRequest(
				"Outro Nome",
				"55566677788",
				"83993334444",
				"EXISTENTE@ALUGAFACIL.COM"
		);

		mockMvc.perform(post("/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(emailDuplicado)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("e-mail")));
	}

	@Test
	void deveListarClientesEFiltrarPorNome() throws Exception {
		Cliente maria = clienteRepository.saveAndFlush(
				new Cliente("Maria Oliveira", "22233344455", "83992223333", "maria.oliveira@teste.com")
		);
		Cliente joao = clienteRepository.saveAndFlush(
				new Cliente("João Souza", "33344455566", "83993334444", "joao.souza@teste.com")
		);
		Cliente anaMaria = clienteRepository.saveAndFlush(
				new Cliente("Ana Maria Costa", "44455566677", "83994445555", "ana.maria@teste.com")
		);

		mockMvc.perform(get("/clientes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].id").value(maria.getId()))
				.andExpect(jsonPath("$[1].id").value(joao.getId()))
				.andExpect(jsonPath("$[2].id").value(anaMaria.getId()));

		mockMvc.perform(get("/clientes").param("nome", "maria"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].nome", hasItems("Maria Oliveira", "Ana Maria Costa")));
	}

	@Test
	void deveBuscarClientePorIdComSucesso() throws Exception {
		Cliente salvo = clienteRepository.saveAndFlush(
				new Cliente("Carlos Pereira", "66677788899", "83996667777", "carlos.pereira@teste.com")
		);

		mockMvc.perform(get("/clientes/{id}", salvo.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.nome").value("Carlos Pereira"))
				.andExpect(jsonPath("$.documento").value("66677788899"))
				.andExpect(jsonPath("$.telefone").value("83996667777"))
				.andExpect(jsonPath("$.email").value("carlos.pereira@teste.com"));
	}

	@Test
	void deveRetornar404ParaClienteInexistenteEmGetPutEDelete() throws Exception {
		ClienteRequest payload = new ClienteRequest(
				"Cliente Teste",
				"12312312300",
				"83991231234",
				"teste@alugafacil.com"
		);

		mockMvc.perform(get("/clientes/{id}", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));

		mockMvc.perform(put("/clientes/{id}", 999999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));

		mockMvc.perform(delete("/clientes/{id}", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message", containsString("999999")));
	}

	@Test
	void deveAtualizarClienteComSucesso() throws Exception {
		Cliente salvo = clienteRepository.saveAndFlush(
				new Cliente("Pedro Santos", "77788899900", "83997778888", "pedro.santos@teste.com")
		);

		ClienteRequest mantendoChaves = new ClienteRequest(
				"  Pedro H. Santos  ",
				"77788899900",
				" 83997779999 ",
				"pedro.santos@teste.com"
		);

		mockMvc.perform(put("/clientes/{id}", salvo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(mantendoChaves)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.nome").value("Pedro H. Santos"))
				.andExpect(jsonPath("$.telefone").value("83997779999"));

		ClienteRequest novasChaves = new ClienteRequest(
				"Pedro Henrique Santos",
				"00099988877",
				"83990009999",
				"pedro.henrique@teste.com"
		);

		mockMvc.perform(put("/clientes/{id}", salvo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(novasChaves)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.documento").value("00099988877"))
				.andExpect(jsonPath("$.email").value("pedro.henrique@teste.com"));

		Cliente atualizado = clienteRepository.findById(salvo.getId()).orElseThrow();
		assertThat(atualizado.getNome()).isEqualTo("Pedro Henrique Santos");
		assertThat(atualizado.getDocumento()).isEqualTo("00099988877");
		assertThat(atualizado.getTelefone()).isEqualTo("83990009999");
		assertThat(atualizado.getEmail()).isEqualTo("pedro.henrique@teste.com");
	}

	@Test
	void deveRetornar400AoAtualizarClienteComDadosInvalidos() throws Exception {
		Cliente salvo = clienteRepository.saveAndFlush(
				new Cliente("Lucas Lima", "12121212121", "83991212121", "lucas.lima@teste.com")
		);

		ClienteRequest invalido = new ClienteRequest("", "   ", "", "nao-eh-email");

		mockMvc.perform(put("/clientes/{id}", salvo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalido)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItems("nome", "documento", "telefone", "email")));
	}

	@Test
	void deveRetornar409AoAtualizarParaDocumentoOuEmailDeOutroCliente() throws Exception {
		Cliente primeiro = clienteRepository.saveAndFlush(
				new Cliente("Cliente Um", "13131313131", "83991313131", "cliente.um@teste.com")
		);
		Cliente segundo = clienteRepository.saveAndFlush(
				new Cliente("Cliente Dois", "14141414141", "83991414141", "cliente.dois@teste.com")
		);

		ClienteRequest documentoDeOutro = new ClienteRequest(
				"Cliente Dois",
				primeiro.getDocumento(),
				"83991414141",
				"cliente.dois@teste.com"
		);

		mockMvc.perform(put("/clientes/{id}", segundo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(documentoDeOutro)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("documento")));

		ClienteRequest emailDeOutro = new ClienteRequest(
				"Cliente Dois",
				segundo.getDocumento(),
				"83991414141",
				"CLIENTE.UM@TESTE.COM"
		);

		mockMvc.perform(put("/clientes/{id}", segundo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(emailDeOutro)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("e-mail")));
	}

	@Test
	void deveRemoverClienteComSucesso() throws Exception {
		Cliente salvo = clienteRepository.saveAndFlush(
				new Cliente("Fernanda Alves", "15151515151", "83991515151", "fernanda.alves@teste.com")
		);

		mockMvc.perform(delete("/clientes/{id}", salvo.getId()))
				.andExpect(status().isNoContent());

		assertThat(clienteRepository.findById(salvo.getId())).isEmpty();
	}

	@Test
	void deveRetornar409AoRemoverClienteComReservaVinculada() throws Exception {
		Cliente salvo = clienteRepository.saveAndFlush(
				new Cliente("Roberto Dias", "16161616161", "83991616161", "roberto.dias@teste.com")
		);

		jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				salvo.getId(), "2026-12-01", "2026-12-05", "PENDENTE"
		);

		mockMvc.perform(delete("/clientes/{id}", salvo.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("reservas vinculadas")));
	}
}
