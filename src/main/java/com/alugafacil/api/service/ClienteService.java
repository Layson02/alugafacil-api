package com.alugafacil.api.service;

import com.alugafacil.api.dto.ClienteRequest;
import com.alugafacil.api.dto.ClienteResponse;
import com.alugafacil.api.entity.Cliente;
import com.alugafacil.api.exception.ConflictException;
import com.alugafacil.api.exception.ResourceNotFoundException;
import com.alugafacil.api.repository.ClienteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository clienteRepository;

	@Transactional
	public ClienteResponse criar(ClienteRequest request) {
		String nome = request.nome().trim();
		String documento = request.documento().trim();
		String telefone = request.telefone().trim();
		String email = request.email().trim();

		if (clienteRepository.existsByDocumento(documento)) {
			throw new ConflictException("Já existe um cliente cadastrado com o documento informado");
		}
		if (clienteRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("Já existe um cliente cadastrado com o e-mail informado");
		}

		Cliente cliente = Cliente.builder()
				.nome(nome)
				.documento(documento)
				.telefone(telefone)
				.email(email)
				.build();

		Cliente salvo = clienteRepository.saveAndFlush(cliente);
		return ClienteResponse.fromEntity(salvo);
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listar(String nome) {
		List<Cliente> clientes = (nome != null && !nome.isBlank())
				? clienteRepository.findByNomeContainingIgnoreCase(nome.trim())
				: clienteRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

		return clientes.stream()
				.map(ClienteResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {
		Cliente cliente = buscarEntidadePorId(id);
		return ClienteResponse.fromEntity(cliente);
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		Cliente cliente = buscarEntidadePorId(id);

		String nome = request.nome().trim();
		String documento = request.documento().trim();
		String telefone = request.telefone().trim();
		String email = request.email().trim();

		if (clienteRepository.existsByDocumentoAndIdNot(documento, id)) {
			throw new ConflictException("Já existe um cliente cadastrado com o documento informado");
		}
		if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
			throw new ConflictException("Já existe um cliente cadastrado com o e-mail informado");
		}

		cliente.setNome(nome);
		cliente.setDocumento(documento);
		cliente.setTelefone(telefone);
		cliente.setEmail(email);

		Cliente atualizado = clienteRepository.saveAndFlush(cliente);
		return ClienteResponse.fromEntity(atualizado);
	}

	@Transactional
	public void remover(Long id) {
		Cliente cliente = buscarEntidadePorId(id);
		try {
			clienteRepository.delete(cliente);
			clienteRepository.flush();
		} catch (DataIntegrityViolationException ex) {
			throw new ConflictException("Não é possível remover o cliente pois existem reservas vinculadas");
		}
	}

	private Cliente buscarEntidadePorId(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o id: " + id));
	}
}
