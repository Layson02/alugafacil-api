package com.alugafacil.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "itens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "O nome do item é obrigatório")
	@Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
	@Column(name = "nome", nullable = false, length = 150)
	private String nome;

	@Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
	@Column(name = "descricao", length = 500)
	private String descricao;

	@NotBlank(message = "A categoria do item é obrigatória")
	@Size(max = 100, message = "A categoria deve ter no máximo 100 caracteres")
	@Column(name = "categoria", nullable = false, length = 100)
	private String categoria;

	@NotNull(message = "A quantidade total do item é obrigatória")
	@Positive(message = "A quantidade total deve ser maior que zero")
	@Column(name = "quantidade_total", nullable = false)
	private Integer quantidadeTotal;

	@Builder.Default
	@NotNull(message = "O status ativo do item é obrigatório")
	@Column(name = "ativo", nullable = false)
	private Boolean ativo = Boolean.TRUE;

	public Item(String nome, String descricao, String categoria, Integer quantidadeTotal) {
		this.nome = nome;
		this.descricao = descricao;
		this.categoria = categoria;
		this.quantidadeTotal = quantidadeTotal;
		this.ativo = Boolean.TRUE;
	}

	public void desativar() {
		this.ativo = Boolean.FALSE;
	}

	public void ativar() {
		this.ativo = Boolean.TRUE;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Item other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
