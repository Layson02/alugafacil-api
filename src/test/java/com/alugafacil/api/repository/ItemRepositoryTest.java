package com.alugafacil.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.alugafacil.api.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ItemRepositoryTest {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void deveSalvarEConsultarItemPorId() {
		Item completo = Item.builder()
				.nome("Betoneira 400L")
				.descricao("Equipamento para mistura de concreto")
				.categoria("Construção")
				.quantidadeTotal(4)
				.build();

		Item semDescricao = new Item("Andaime Tubular", null, "Estrutura", 12);

		Item salvoCompleto = itemRepository.saveAndFlush(completo);
		Item salvoSemDescricao = itemRepository.saveAndFlush(semDescricao);
		entityManager.clear();

		assertThat(salvoCompleto.getId()).isNotNull().isPositive();
		assertThat(salvoSemDescricao.getId()).isNotNull().isPositive();

		Optional<Item> encontradoCompleto = itemRepository.findById(salvoCompleto.getId());
		assertThat(encontradoCompleto).isPresent();
		assertThat(encontradoCompleto.get().getNome()).isEqualTo("Betoneira 400L");
		assertThat(encontradoCompleto.get().getDescricao()).isEqualTo("Equipamento para mistura de concreto");
		assertThat(encontradoCompleto.get().getCategoria()).isEqualTo("Construção");
		assertThat(encontradoCompleto.get().getQuantidadeTotal()).isEqualTo(4);
		assertThat(encontradoCompleto.get().getAtivo()).isTrue();

		Optional<Item> encontradoSemDescricao = itemRepository.findById(salvoSemDescricao.getId());
		assertThat(encontradoSemDescricao).isPresent();
		assertThat(encontradoSemDescricao.get().getNome()).isEqualTo("Andaime Tubular");
		assertThat(encontradoSemDescricao.get().getDescricao()).isNull();
		assertThat(encontradoSemDescricao.get().getCategoria()).isEqualTo("Estrutura");
		assertThat(encontradoSemDescricao.get().getQuantidadeTotal()).isEqualTo(12);
		assertThat(encontradoSemDescricao.get().getAtivo()).isTrue();
	}

	@Test
	void deveConsultarItensPorNomeCategoriaEStatusAtivo() {
		Item cadeiraDobravel = itemRepository.saveAndFlush(
				new Item("Cadeira Dobrável", "Cadeira plástica branca", "Mobiliário", 50)
		);

		Item cadeiraGamer = new Item("Cadeira Gamer", "Cadeira ergonômica", "Mobiliário", 10);
		cadeiraGamer.desativar();
		itemRepository.saveAndFlush(cadeiraGamer);

		Item caixaSom = itemRepository.saveAndFlush(
				new Item("Caixa de Som JBL", "Caixa amplificada Bluetooth", "Áudio", 6)
		);
		entityManager.clear();

		List<Item> ativos = itemRepository.findByAtivoTrue();
		assertThat(ativos)
				.extracting(Item::getId)
				.containsExactlyInAnyOrder(cadeiraDobravel.getId(), caixaSom.getId());

		List<Item> porNome = itemRepository.findByNomeContainingIgnoreCase("cadeira");
		assertThat(porNome)
				.extracting(Item::getNome)
				.containsExactlyInAnyOrder("Cadeira Dobrável", "Cadeira Gamer");

		List<Item> porNomeAtivos = itemRepository.findByNomeContainingIgnoreCaseAndAtivoTrue("cadeira");
		assertThat(porNomeAtivos)
				.extracting(Item::getNome)
				.containsExactly("Cadeira Dobrável");

		List<Item> porCategoria = itemRepository.findByCategoriaIgnoreCase("mobiliário");
		assertThat(porCategoria)
				.extracting(Item::getNome)
				.containsExactlyInAnyOrder("Cadeira Dobrável", "Cadeira Gamer");

		List<Item> porCategoriaAtivos = itemRepository.findByCategoriaIgnoreCaseAndAtivoTrue("mobiliário");
		assertThat(porCategoriaAtivos)
				.extracting(Item::getNome)
				.containsExactly("Cadeira Dobrável");

		assertThat(itemRepository.findByCategoriaIgnoreCase("Inexistente")).isEmpty();
	}

	@Test
	void deveAtualizarEDesativarItemSemExclusaoFisica() {
		Item salvo = itemRepository.saveAndFlush(
				new Item("Gerador 5kVA", "Gerador portátil a gasolina", "Energia", 3)
		);
		Long id = salvo.getId();

		salvo.setNome("Gerador 5kVA Silencioso");
		salvo.setDescricao("Gerador portátil com isolamento acústico");
		salvo.setQuantidadeTotal(5);
		salvo.desativar();
		itemRepository.saveAndFlush(salvo);
		entityManager.clear();

		Item desativado = itemRepository.findById(id).orElseThrow();
		assertThat(desativado.getNome()).isEqualTo("Gerador 5kVA Silencioso");
		assertThat(desativado.getDescricao()).isEqualTo("Gerador portátil com isolamento acústico");
		assertThat(desativado.getQuantidadeTotal()).isEqualTo(5);
		assertThat(desativado.getAtivo()).isFalse();
		assertThat(itemRepository.findByAtivoTrue())
				.extracting(Item::getId)
				.doesNotContain(id);

		desativado.ativar();
		itemRepository.saveAndFlush(desativado);
		entityManager.clear();

		Item reativado = itemRepository.findById(id).orElseThrow();
		assertThat(reativado.getAtivo()).isTrue();
		assertThat(itemRepository.findByAtivoTrue())
				.extracting(Item::getId)
				.contains(id);
	}

	@Test
	void deveImpedirPersistenciaDeItemComNomeEmBranco() {
		Item invalido = new Item("   ", "Descrição válida", "Ferramentas", 2);

		assertThatThrownBy(() -> itemRepository.saveAndFlush(invalido))
				.isInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void deveImpedirPersistenciaDeItemComQuantidadeZero() {
		Item invalido = new Item("Parafusadeira", null, "Ferramentas", 0);

		assertThatThrownBy(() -> itemRepository.saveAndFlush(invalido))
				.isInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void deveImpedirPersistenciaDeItemComQuantidadeNegativa() {
		Item invalido = new Item("Parafusadeira", null, "Ferramentas", -2);

		assertThatThrownBy(() -> itemRepository.saveAndFlush(invalido))
				.isInstanceOf(ConstraintViolationException.class);
	}
}
