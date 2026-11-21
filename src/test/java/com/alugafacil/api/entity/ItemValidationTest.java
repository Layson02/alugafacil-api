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

class ItemValidationTest {

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
	void deveValidarItemComDadosValidos() {
		Item itemCompleto = Item.builder()
				.nome("Betoneira 400L")
				.descricao("Equipamento para preparo de concreto")
				.categoria("Construção")
				.quantidadeTotal(5)
				.build();

		assertThat(itemCompleto.getAtivo()).isTrue();
		assertThat(validator.validate(itemCompleto)).isEmpty();

		Item itemSemDescricao = new Item("Andaime Tubular", null, "Estrutura", 10);

		assertThat(itemSemDescricao.getAtivo()).isTrue();
		assertThat(validator.validate(itemSemDescricao)).isEmpty();
	}

	@Test
	void deveRejeitarCamposObrigatoriosNulosOuEmBranco() {
		Item itemNulo = new Item();
		Set<ConstraintViolation<Item>> violacoesNulo = validator.validate(itemNulo);

		assertThat(violacoesNulo)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactlyInAnyOrder("nome", "categoria", "quantidadeTotal");

		Item itemEmBranco = Item.builder()
				.nome("   ")
				.categoria("   ")
				.quantidadeTotal(null)
				.ativo(null)
				.build();
		Set<ConstraintViolation<Item>> violacoesEmBranco = validator.validate(itemEmBranco);

		assertThat(violacoesEmBranco)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactlyInAnyOrder("nome", "categoria", "quantidadeTotal", "ativo");
	}

	@Test
	void deveRejeitarQuantidadeTotalMenorOuIgualAZero() {
		Item itemQuantidadeZero = Item.builder()
				.nome("Furadeira Industrial")
				.categoria("Ferramentas")
				.quantidadeTotal(0)
				.build();

		Set<ConstraintViolation<Item>> violacoesZero = validator.validate(itemQuantidadeZero);

		assertThat(violacoesZero)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactly("quantidadeTotal");

		Item itemQuantidadeNegativa = new Item("Furadeira Industrial", null, "Ferramentas", -5);

		Set<ConstraintViolation<Item>> violacoesNegativa = validator.validate(itemQuantidadeNegativa);

		assertThat(violacoesNegativa)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactly("quantidadeTotal");
	}

	@Test
	void deveRejeitarCamposAcimaDoTamanhoMaximo() {
		Item item = Item.builder()
				.nome("A".repeat(151))
				.descricao("B".repeat(501))
				.categoria("C".repeat(101))
				.quantidadeTotal(3)
				.build();

		Set<ConstraintViolation<Item>> violations = validator.validate(item);

		assertThat(violations)
				.extracting(v -> v.getPropertyPath().toString())
				.containsExactlyInAnyOrder("nome", "descricao", "categoria");
	}
}
