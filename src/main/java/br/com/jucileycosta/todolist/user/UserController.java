package br.com.jucileycosta.todolist.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

	private final IUserRepository userRepository;

	public UserController(IUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostMapping("/")
	public ResponseEntity create(@RequestBody UserModel userModel) {
		var user = userRepository.findByUsername(userModel.getUsername());

		if (Objects.nonNull(user)) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Usuário já existe.");
		}

		var passwordHashed = BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray());
		userModel.setPassword(passwordHashed);

		var userCreated = userRepository.save(userModel);
		return ResponseEntity.ok(userCreated);
	}
}
