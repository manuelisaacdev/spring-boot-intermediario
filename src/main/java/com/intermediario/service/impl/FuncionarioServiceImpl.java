package com.intermediario.service.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.intermediario.dto.FuncionarioEmailDTO;
import com.intermediario.dto.FuncionarioSenhaDTO;
import com.intermediario.exception.EntityNotFoundException;
import com.intermediario.exception.IntegrityException;
import com.intermediario.exception.UnauthorizedException;
import com.intermediario.model.Funcionario;
import com.intermediario.model.Pais;
import com.intermediario.model.Telefone;
import com.intermediario.repository.FuncionarioRepository;
import com.intermediario.service.AbstractService;
import com.intermediario.service.FuncionarioService;
import com.intermediario.storage.StorageService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class FuncionarioServiceImpl extends AbstractServiceImpl<Funcionario, UUID, FuncionarioRepository> implements FuncionarioService {
	private final PasswordEncoder encoder;
	private final StorageService storageService;
	private final AbstractService<Pais, UUID> paisService;

	public FuncionarioServiceImpl(FuncionarioRepository repository, HttpServletRequest request,
			MessageSource messageSource, PasswordEncoder encoder, StorageService storageService,
			AbstractService<Pais, UUID> paisService) {
		super(repository, request, messageSource);
		this.encoder = encoder;
		this.storageService = storageService;
		this.paisService = paisService;
	}

	@Override
	public Funcionario findByEmail(String email) throws EntityNotFoundException {
		return this.repository.findByEmail(email)
		.orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("Entity.notfound", null, request.getLocale())));
	}
	
	@Override
	public Funcionario create(UUID idPais, Funcionario funcionario, Telefone telefone, Optional<MultipartFile> fotoPerfil) {
		funcionario.setTelefones(List.of(telefone));
		funcionario.setPais(paisService.findById(idPais));
		funcionario.setSenha(encoder.encode(funcionario.getSenha()));
		if (fotoPerfil.isPresent()) {
			funcionario.setFotoPerfil(storageService.store(fotoPerfil.get()));
		}
		try {			
			return super.save(funcionario);
		} catch (DataIntegrityViolationException e) {
			storageService.delete(funcionario.getFotoPerfil());
			throw new IntegrityException(messageSource.getMessage("employee.email.already-exists", new String[]{funcionario.getEmail()}, request.getLocale()));
		}
	}
	
	@Override
	public List<Funcionario> create(List<Funcionario> funcionarios) {
		var paises = paisService.findAll();
		SecureRandom random = new SecureRandom();
		funcionarios = funcionarios.stream().map(funcionario -> {
			funcionario.setSenha(encoder.encode(funcionario.getSenha()));
			funcionario.setPais(paises.get(random.nextInt(paises.size() - 1)));
			return funcionario;
		}).toList();
		for (Funcionario funcionario : funcionarios) {
			super.save(funcionario);
		}
		return funcionarios;
	}
	
	@Override
	public Funcionario update(UUID idFuncionario, UUID idPais, Funcionario funcionario) {
		Funcionario entity = this.findById(idFuncionario);
		entity.setNome(funcionario.getNome());
		entity.setGenero(funcionario.getGenero());
		entity.setMorada(funcionario.getMorada());
		entity.setPais(paisService.findById(idPais));
		entity.setDataNascimento(funcionario.getDataNascimento());
		entity.setBilheteIdentidade(funcionario.getBilheteIdentidade());
		return super.save(entity);
	}
	
	@Override
	public Funcionario updateEmail(UUID idFuncionario, FuncionarioEmailDTO updateEmailDTO) throws UnauthorizedException {
		var entity = this.findById(idFuncionario);
		if (!encoder.matches(updateEmailDTO.getSenha(), entity.getSenha())) {
			throw new UnauthorizedException(messageSource.getMessage("unauthorized", null, request.getLocale()));
		}
		entity.setEmail(updateEmailDTO.getEmail());
		return super.save(entity);
	}
	
	@Override
	public Funcionario updatePassword(UUID idFuncionario, FuncionarioSenhaDTO senhaDTO) throws UnauthorizedException {
		var entity = this.findById(idFuncionario);
		if (!encoder.matches(senhaDTO.getAntiga(), entity.getSenha())) {
			throw new UnauthorizedException(messageSource.getMessage("employee.password.invalid", null, request.getLocale()));
		}
		entity.setSenha(encoder.encode(senhaDTO.getNova()));
		return super.save(entity);
	}
	
	@Override
	public Funcionario updateProfilePhoto(UUID idFuncionario, MultipartFile fotoPerfil) {
		var entity = this.findById(idFuncionario);
		String arquivo = storageService.store(fotoPerfil);
		storageService.delete(entity.getFotoPerfil());
		entity.setFotoPerfil(arquivo);
		return super.save(entity);
	}
	
	@Override
	public Funcionario deleteById(UUID id) {
		Funcionario funcionario = super.deleteById(id);
		storageService.delete(funcionario.getFotoPerfil());
		return funcionario;	
	}
}
