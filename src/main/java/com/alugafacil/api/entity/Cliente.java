package com.alugafacil.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
		name = "clientes",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_clientes_documento", columnNames = "documento"),
				@UniqueConstraint(name = "uk_clientes_email", columnNames = "email")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "O nome do cliente é obrigatório")
	@Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
	@Column(name = "nome", nullable = false, length = 150)
	private String nome;

	@NotBlank(message = "O documento do cliente é obrigatório")
	@Size(max = 20, message = "O documento deve ter no máximo 20 caracteres")
	@Column(name = "documento", nullable = false, length = 20, unique = true)
	private String documento;

	@NotBlank(message = "O telefone do cliente é obrigatório")
	@Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
	@Column(name = "telefone", nullable = false, length = 20)
	private String telefone;

	@NotBlank(message = "O e-mail do cliente é obrigatório")
	@Email(message = "O e-mail informado é inválido")
	@Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
	@Column(name = "email", nullable = false, length = 150, unique = true)
	private String email;

	public Cliente(String nome, String documento, String telefone, String email) {
		this.nome = nome;
		this.documento = documento;
		this.telefone = telefone;
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Cliente other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
