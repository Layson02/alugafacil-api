package com.alugafacil.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AlugafacilApiApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		assertThat(jdbcTemplate).isNotNull();
	}

	@Test
	void deveExecutarMigrationInicialECriarTodasAsTabelas() {
		List<String> tabelasEsperadas = List.of(
				"clientes",
				"itens",
				"reservas",
				"reserva_itens",
				"retiradas",
				"devolucoes"
		);

		for (String tabela : tabelasEsperadas) {
			Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
			assertThat(count).as("Tabela %s deve existir após a migration inicial", tabela).isNotNull();
		}
	}

	@Test
	void deveValidarRelacionamentosERestricoesDoBanco() {
		jdbcTemplate.update(
				"INSERT INTO clientes (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
				"Cliente Teste", "12345678901", "83999990000", "cliente@teste.com"
		);
		Long clienteId = jdbcTemplate.queryForObject(
				"SELECT id FROM clientes WHERE documento = ?", Long.class, "12345678901"
		);

		jdbcTemplate.update(
				"INSERT INTO itens (nome, descricao, categoria, quantidade_total, ativo) VALUES (?, ?, ?, ?, ?)",
				"Betoneira 400L", "Equipamento para obra", "Construção", 5, true
		);
		Long itemId = jdbcTemplate.queryForObject(
				"SELECT id FROM itens WHERE nome = ?", Long.class, "Betoneira 400L"
		);

		jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				clienteId, "2026-12-01", "2026-12-05", "CONFIRMADA"
		);
		Long reservaId = jdbcTemplate.queryForObject(
				"SELECT id FROM reservas WHERE cliente_id = ?", Long.class, clienteId
		);

		jdbcTemplate.update(
				"INSERT INTO reserva_itens (reserva_id, item_id, quantidade) VALUES (?, ?, ?)",
				reservaId, itemId, 2
		);

		jdbcTemplate.update(
				"INSERT INTO retiradas (reserva_id, observacao) VALUES (?, ?)",
				reservaId, "Retirado sem avarias"
		);

		jdbcTemplate.update(
				"INSERT INTO devolucoes (reserva_id, observacao) VALUES (?, ?)",
				reservaId, "Devolvido no prazo"
		);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO itens (nome, categoria, quantidade_total, ativo) VALUES (?, ?, ?, ?)",
				"Item Inválido", "Construção", 0, true
		)).isInstanceOf(DataIntegrityViolationException.class);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO reserva_itens (reserva_id, item_id, quantidade) VALUES (?, ?, ?)",
				reservaId, itemId, 0
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveImpedirDatasInvalidasStatusInvalidoEDuplicidades() {
		jdbcTemplate.update(
				"INSERT INTO clientes (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
				"Maria Souza", "98765432100", "83988887777", "maria@teste.com"
		);
		Long clienteId = jdbcTemplate.queryForObject(
				"SELECT id FROM clientes WHERE documento = ?", Long.class, "98765432100"
		);

		// Documento duplicado deve falhar (uk_clientes_documento)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO clientes (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
				"Outra Pessoa", "98765432100", "83911112222", "outra@teste.com"
		)).isInstanceOf(DataIntegrityViolationException.class);

		// E-mail duplicado deve falhar (uk_clientes_email)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO clientes (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
				"Outra Pessoa", "11122233344", "83911112222", "maria@teste.com"
		)).isInstanceOf(DataIntegrityViolationException.class);

		// Data fim anterior à data início deve falhar (ck_reservas_periodo)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				clienteId, "2026-12-10", "2026-12-05", "PENDENTE"
		)).isInstanceOf(DataIntegrityViolationException.class);

		// Status fora do domínio permitido deve falhar (ck_reservas_status)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				clienteId, "2026-12-01", "2026-12-05", "STATUS_INEXISTENTE"
		)).isInstanceOf(DataIntegrityViolationException.class);

		// Reserva para cliente inexistente deve falhar (fk_reservas_cliente)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				999999L, "2026-12-01", "2026-12-05", "PENDENTE"
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveImpedirMaisDeUmaRetiradaOuDevolucaoParaMesmaReserva() {
		jdbcTemplate.update(
				"INSERT INTO clientes (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
				"Carlos Lima", "55566677788", "83977776666", "carlos@teste.com"
		);
		Long clienteId = jdbcTemplate.queryForObject(
				"SELECT id FROM clientes WHERE documento = ?", Long.class, "55566677788"
		);

		jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				clienteId, "2026-12-10", "2026-12-12", "RETIRADA"
		);
		Long reservaId = jdbcTemplate.queryForObject(
				"SELECT id FROM reservas WHERE cliente_id = ?", Long.class, clienteId
		);

		jdbcTemplate.update(
				"INSERT INTO retiradas (reserva_id, observacao) VALUES (?, ?)",
				reservaId, "Primeira retirada"
		);

		// Segunda retirada para a mesma reserva deve falhar (uk_retiradas_reserva)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO retiradas (reserva_id, observacao) VALUES (?, ?)",
				reservaId, "Retirada duplicada"
		)).isInstanceOf(DataIntegrityViolationException.class);

		jdbcTemplate.update(
				"INSERT INTO devolucoes (reserva_id, observacao) VALUES (?, ?)",
				reservaId, "Primeira devolução"
		);

		// Segunda devolução para a mesma reserva deve falhar (uk_devolucoes_reserva)
		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO devolucoes (reserva_id, observacao) VALUES (?, ?)",
				reservaId, "Devolução duplicada"
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void devePreencherValoresPadraoEPermitirReservaNoMesmoDia() {
		jdbcTemplate.update(
				"INSERT INTO clientes (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
				"Ana Paula", "44433322211", "83966665555", "ana@teste.com"
		);
		Long clienteId = jdbcTemplate.queryForObject(
				"SELECT id FROM clientes WHERE documento = ?", Long.class, "44433322211"
		);

		// Omitindo coluna 'ativo' para validar DEFAULT 1
		jdbcTemplate.update(
				"INSERT INTO itens (nome, categoria, quantidade_total) VALUES (?, ?, ?)",
				"Andaime Tubular", "Estrutura", 10
		);
		Boolean itemAtivo = jdbcTemplate.queryForObject(
				"SELECT ativo FROM itens WHERE nome = ?", Boolean.class, "Andaime Tubular"
		);
		assertThat(itemAtivo).isTrue();

		// Reserva com data_fim == data_inicio (locação de 1 dia) e criado_em automático
		jdbcTemplate.update(
				"INSERT INTO reservas (cliente_id, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)",
				clienteId, "2026-12-15", "2026-12-15", "PENDENTE"
		);
		Object criadoEm = jdbcTemplate.queryForObject(
				"SELECT criado_em FROM reservas WHERE cliente_id = ?", Object.class, clienteId
		);
		assertThat(criadoEm).isNotNull();
	}

	@Test
	void deveRegistrarHistoricoDaMigrationNoFlyway() {
		Integer migrationsComSucesso = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND script = 'V1__create_initial_schema.sql' AND success = 1",
				Integer.class
		);
		assertThat(migrationsComSucesso).isEqualTo(1);
	}

}
