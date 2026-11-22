package com.alugafacil.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
		@NotBlank(message = "O nome do cliente é obrigatório")
		@Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
		String nome,

		@NotBlank(message = "O documento do cliente é obrigatório")
		@Size(max = 20, message = "O documento deve ter no máximo 20 caracteres")
		String documento,

		@NotBlank(message = "O telefone do cliente é obrigatório")
		@Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
		String telefone,

		@NotBlank(message = "O e-mail do cliente é obrigatório")
		@Email(message = "O e-mail informado é inválido")
		@Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
		String email
) {
}
