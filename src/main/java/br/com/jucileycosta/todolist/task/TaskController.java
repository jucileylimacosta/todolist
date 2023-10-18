package br.com.jucileycosta.todolist.task;

import br.com.jucileycosta.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

	private ITaskRepository taskRepository;

	public TaskController(ITaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@PostMapping("/")
	public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
		var idUser = (UUID) request.getAttribute("idUser");
		taskModel.setIdUser(idUser);

		var currentDate = LocalDateTime.now();
		if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("A data de início e ou a data de término da tarefa devem ser maior do que a data atual.");
		}

		if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("A data de início deve ser menor do que a data de término da tarefa.");
		}

		var taskCreated = taskRepository.save(taskModel);
		return ResponseEntity.ok(taskCreated);
	}

	@GetMapping("/")
	public List<TaskModel> list(HttpServletRequest request) {
		var idUser = (UUID) request.getAttribute("idUser");
		var tasks = taskRepository.findByIdUser(idUser);

		return tasks;
	}

	@PutMapping("/{id}")
	public ResponseEntity update(@PathVariable UUID id,
							@RequestBody TaskModel taskModel,
					   		HttpServletRequest request) {

		var task= taskRepository.findById(id).orElse(null);

		if(Objects.isNull(task)) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Tarefa não encontrada.");
		}

		var idUser = (UUID) request.getAttribute("idUser");

		if(!task.getIdUser().equals(idUser)) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Usuário não tem permissão para alterar essa tarefa.");
		}

		Utils.copyNonNullProperties(taskModel, task);

		var taskUpdated = taskRepository.save(task);

		return ResponseEntity.ok(taskUpdated);
	}
}
