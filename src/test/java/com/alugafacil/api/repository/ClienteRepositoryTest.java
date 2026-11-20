package com.alugafacil.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.alugafacil.api.entity.Cliente;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ClienteRepositoryTest {

	@Autowired
	private ClienteRepository clienteRepository;

	@Test
	void deveSalvarEConsultarClientePorId() {
		Cliente cliente = Cliente.builder()
				.nome("Carlos Pereira")
				.documento("11122233344")
				.telefone("83991112222")
				.email("carlos.pereira@teste.com")
				.build();

		Cliente salvo = clienteRepository.saveAndFlush(cliente);

		assertThat(salvo.getId()).isNotNull().isPositive();

		Optional<Cliente> encontrado = clienteRepository.findById(salvo.getId());
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getNome()).isEqualTo("Carlos Pereira");
		assertThat(encontrado.get().getDocumento()).isEqualTo("11122233344");
		assertThat(encontrado.get().getTelefone()).isEqualTo("83991112222");
		assertThat(encontrado.get().getEmail()).isEqualTo("carlos.pereira@teste.com");
	}

	@Test
	void deveConsultarClientePorDocumentoEmailENome() {
		Cliente maria = clienteRepository.saveAndFlush(
				new Cliente("Maria Oliveira", "22233344455", "83992223333", "maria.oliveira@teste.com")
		);
		clienteRepository.saveAndFlush(
				new Cliente("Ana Maria Costa", "33344455566", "83993334444", "ana.costa@teste.com")
		);

		assertThat(clienteRepository.findByDocumento("22233344455"))
				.isPresent()
				.get()
				.extracting(Cliente::getId)
				.isEqualTo(maria.getId());

		assertThat(clienteRepository.findByEmail("ana.costa@teste.com"))
				.isPresent()
				.get()
				.extracting(Cliente::getNome)
				.isEqualTo("Ana Maria Costa");

		assertThat(clienteRepository.existsByDocumento("22233344455")).isTrue();
		assertThat(clienteRepository.existsByDocumento("00000000000")).isFalse();

		assertThat(clienteRepository.existsByEmail("maria.oliveira@teste.com")).isTrue();
		assertThat(clienteRepository.existsByEmail("naoexiste@teste.com")).isFalse();

		List<Cliente> encontrados = clienteRepository.findByNomeContainingIgnoreCase("maria");
		assertThat(encontrados)
				.extracting(Cliente::getDocumento)
				.containsExactlyInAnyOrder("22233344455", "33344455566");

		assertThat(clienteRepository.findByDocumento("99999999999")).isEmpty();
		assertThat(clienteRepository.findByEmail("inexistente@teste.com")).isEmpty();
	}

	@Test
	void deveAtualizarERemoverCliente() {
		Cliente salvo = clienteRepository.saveAndFlush(
				new Cliente("Pedro Santos", "44455566677", "83994445555", "pedro.santos@teste.com")
		);
		Long id = salvo.getId();

		salvo.setNome("Pedro H. Santos");
		salvo.setTelefone("83994446666");
		clienteRepository.saveAndFlush(salvo);

		Cliente atualizado = clienteRepository.findById(id).orElseThrow();
		assertThat(atualizado.getNome()).isEqualTo("Pedro H. Santos");
		assertThat(atualizado.getTelefone()).isEqualTo("83994446666");

		clienteRepository.deleteById(id);
		clienteRepository.flush();

		assertThat(clienteRepository.findById(id)).isEmpty();
	}

	@Test
	void deveImpedirPersistenciaDeClienteComCamposInvalidos() {
		Cliente invalido = new Cliente("   ", "55566677788", "83995556666", "invalido");

		assertThatThrownBy(() -> clienteRepository.saveAndFlush(invalido))
				.isInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void deveImpedirCadastroDeDocumentoDuplicado() {
		clienteRepository.saveAndFlush(
				new Cliente("Primeiro Cliente", "66677788899", "83996667777", "primeiro@teste.com")
		);

		Cliente duplicado = new Cliente("Segundo Cliente", "66677788899", "83996668888", "segundo@teste.com");

		assertThatThrownBy(() -> clienteRepository.saveAndFlush(duplicado))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveImpedirCadastroDeEmailDuplicado() {
		clienteRepository.saveAndFlush(
				new Cliente("Cliente Um", "77788899900", "83997778888", "mesmo.email@teste.com")
		);

		Cliente duplicado = new Cliente("Cliente Dois", "88899900011", "83998889999", "mesmo.email@teste.com");

		assertThatThrownBy(() -> clienteRepository.saveAndFlush(duplicado))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
