package com.intermediario.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.intermediario.dto.FuncionarioDTO;
import com.intermediario.dto.FuncionarioEmailDTO;
import com.intermediario.dto.FuncionarioSenhaDTO;
import com.intermediario.dto.FuncionarioUpdateDTO;
import com.intermediario.dto.TelefoneDTO;
import com.intermediario.model.Funcionario;
import com.intermediario.model.Genero;
import com.intermediario.model.Pais;
import com.intermediario.model.Telefone;
import com.intermediario.service.FuncionarioService;
import com.intermediario.util.BaseController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/funcionarios")
public class FuncionarioController extends BaseController {
	private final FuncionarioService funcionarioService;
	
	@GetMapping
	public ResponseEntity<List<Funcionario>> findAll(
			@RequestParam(required = false) UUID pais,
			@RequestParam(required = false) String nome, 
			@RequestParam(required = false) String email, 
			@RequestParam(required = false) Genero genero, 
			@RequestParam(required = false) String morada, 
			@RequestParam(required = false) String bilheteIdentidade,
			@RequestParam(required = false) LocalDate dataNascimento, 
			@RequestParam(defaultValue = "nome") String orderBy, 
			@RequestParam(defaultValue = "ASC") Direction direction) {
		return super.ok(funcionarioService.findAll(
			Example.of(
				Funcionario.builder()
				.nome(nome)
				.email(email)
				.genero(genero)
				.morada(morada)
				.dataNascimento(dataNascimento)
				.bilheteIdentidade(bilheteIdentidade)
				.pais(Pais.builder().id(pais).build())
				.build(),
				ExampleMatcher.matching()
				.withMatcher("nome", matcher -> matcher.contains().ignoreCase())
				.withMatcher("email", matcher -> matcher.contains().ignoreCase())
			), 
			orderBy, direction
		));
	}

	@GetMapping("/{idFuncionario}")
	public ResponseEntity<Funcionario> findById(@PathVariable UUID idFuncionario) {
		return super.ok(funcionarioService.findById(idFuncionario));
	}
	
	@GetMapping("/contador")
	public ResponseEntity<Long> count() {
		return super.ok(funcionarioService.count());
	}
	
	@GetMapping("/paginacao")
	public ResponseEntity<Page<Funcionario>> pagination(
			@RequestParam int page, @RequestParam int size,
			@RequestParam(required = false) UUID pais,
			@RequestParam(required = false) String nome, 
			@RequestParam(required = false) String email, 
			@RequestParam(required = false) Genero genero, 
			@RequestParam(required = false) String morada, 
			@RequestParam(required = false) String bilheteIdentidade,
			@RequestParam(required = false) LocalDate dataNascimento, 
			@RequestParam(defaultValue = "nome") String orderBy, 
			@RequestParam(defaultValue = "ASC") Direction direction) {
		return super.ok(funcionarioService.pagination(
				page, size,
			Example.of(
				Funcionario.builder()
				.nome(nome)
				.email(email)
				.genero(genero)
				.morada(morada)
				.dataNascimento(dataNascimento)
				.bilheteIdentidade(bilheteIdentidade)
				.pais(Pais.builder().id(pais).build())
				.build(),
				ExampleMatcher.matching()
				.withMatcher("nome", matcher -> matcher.contains().ignoreCase())
				.withMatcher("email", matcher -> matcher.contains().ignoreCase())
				.withMatcher("bilheteIdentidade", matcher -> matcher.contains().ignoreCase())
			), 
			orderBy, direction
		));
	}
	
	@PostMapping("/{idPais}")
	public ResponseEntity<Funcionario> create(@PathVariable UUID idPais, @Valid FuncionarioDTO funcionarioDTO, @Valid TelefoneDTO telefoneDTO, @RequestParam Optional<MultipartFile> fotoPerfil) {
		var funcionario = Funcionario.builder().build();
		BeanUtils.copyProperties(funcionarioDTO, funcionario);
		return super.created(funcionarioService.create(idPais, funcionario, Telefone.builder().numero(telefoneDTO.getNumero()).build(), fotoPerfil));
	}
	
	@PostMapping("/lista")
	public ResponseEntity<List<Funcionario>> create(@RequestBody @Valid List<FuncionarioDTO> funcionariosDTO) {
		return super.created(funcionarioService.create(funcionariosDTO.stream().map(funcionarioDTO -> {
			Funcionario funcionario = Funcionario.builder().build();
			BeanUtils.copyProperties(funcionarioDTO, funcionario);
			funcionario.setTelefones(List.of(Telefone.builder().numero(funcionarioDTO.getNumero()).build()));
			return funcionario;
		}).toList()));
	}
	
	@PutMapping("/{idFuncionario}/{idPais}")
	public ResponseEntity<Funcionario>  update(@PathVariable UUID idFuncionario, @PathVariable UUID idPais, @RequestBody @Valid FuncionarioUpdateDTO funcionarioUpdateDTO) {
		var funcionario = Funcionario.builder().build();
		BeanUtils.copyProperties(funcionarioUpdateDTO, funcionario);
		return super.ok(funcionarioService.update(idFuncionario, idPais, funcionario));
	}
	
	@PatchMapping("/email/{idFuncionario}")
	public ResponseEntity<Funcionario> updateEmail(@PathVariable UUID idFuncionario, @RequestBody @Valid FuncionarioEmailDTO funcionarioEmailDTO) {
		return super.ok(funcionarioService.updateEmail(idFuncionario, funcionarioEmailDTO));
	}
	
	@PatchMapping("/senha/{idFuncionario}")
	public ResponseEntity<Funcionario> updatePassword(@PathVariable UUID idFuncionario, @RequestBody @Valid FuncionarioSenhaDTO funcionarioSenhaDTO) {
		return super.ok(funcionarioService.updatePassword(idFuncionario, funcionarioSenhaDTO));
	}
	
	@PatchMapping("/fotoPerfil/{idFuncionario}")
	public ResponseEntity<Funcionario> updateProfilePhoto(@PathVariable UUID idFuncionario, @RequestParam MultipartFile fotoPerfil) {
		return super.ok(funcionarioService.updateProfilePhoto(idFuncionario, fotoPerfil));
	}

	@DeleteMapping("/{idFuncionario}")
	public ResponseEntity<Funcionario>  delete(@PathVariable UUID idFuncionario) {
		return super.ok(funcionarioService.deleteById(idFuncionario));
	}
}
