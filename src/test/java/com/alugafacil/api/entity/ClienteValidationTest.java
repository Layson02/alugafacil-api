package com.alugafacil.api.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClienteValidationTest {

	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void closeValidatorFactory() {
		if (validatorFactory != null) {
			validatorFactory.close();
		}
	}

	@Test
	void deveValidarClienteComDadosValidos() {
		Cliente cliente = Cliente.builder()
				.nome("Maria Silva")
				.documento("12345678901")
				.telefone("83999990000")
				.email("maria.silva@alugafacil.com")
				.build();

		Set<ConstraintViolation<Cliente>> violations = validator.validate(cliente);

		assertThat(violations).isEmpty();
	}

	@Test
	void deveRejeitarCamposObrigatoriosNulosOuEmBranco() {
		Cliente clienteNulo = new Cliente();
		Set<ConstraintViolation<Cliente>> violacoesNulo = validator.validate(clienteNulo);

		assertThat(violacoesNulo)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactlyInAnyOrder("nome", "documento", "telefone", "email");

		Cliente clienteEmBranco = new Cliente("   ", "   ", "   ", "   ");
		Set<ConstraintViolation<Cliente>> violacoesEmBranco = validator.validate(clienteEmBranco);

		assertThat(violacoesEmBranco)
				.extracting(v -> v.getPropertyPath().toString())
				.contains("nome", "documento", "telefone", "email");
	}

	@Test
	void deveRejeitarEmailComFormatoInvalido() {
		Cliente cliente = Cliente.builder()
				.nome("João Souza")
				.documento("98765432100")
				.telefone("83988887777")
				.email("email-invalido")
				.build();

		Set<ConstraintViolation<Cliente>> violations = validator.validate(cliente);

		assertThat(violations)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactly("email");
	}

	@Test
	void deveRejeitarCamposAcimaDoTamanhoMaximo() {
		Cliente cliente = Cliente.builder()
				.nome("A".repeat(151))
				.documento("1".repeat(21))
				.telefone("9".repeat(21))
				.email("a".repeat(145) + "@teste.com")
				.build();

		Set<ConstraintViolation<Cliente>> violations = validator.validate(cliente);

		assertThat(violations)
				.extracting(v -> v.getPropertyPath().toString())
				.contains("nome", "documento", "telefone", "email");
	}
}
