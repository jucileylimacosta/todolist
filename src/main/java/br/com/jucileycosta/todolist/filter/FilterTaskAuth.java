package br.com.jucileycosta.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.jucileycosta.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

	private IUserRepository userRepository;

	public FilterTaskAuth(IUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		// Verificar rota
		var servletPath = request.getServletPath();
		if (servletPath.startsWith("/tasks/")) {
			// Pegar a autenticação (usuario e senha)
			var authorization = request.getHeader("Authorization");
			var authEncoded = authorization.substring("Basic".length()).trim();

			byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
			var authString = new String(authDecoded);
			String[] credentials = authString.split(":");
			String username = credentials[0];
			String password = credentials[1];

			// Validar usuário
			var user = userRepository.findByUsername(username);
			if (Objects.isNull(user)) {
				response.sendError(401);
			} else {
				// Validar senha
				var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
				if (passwordVerify.verified) {
					request.setAttribute("idUser", user.getId());
					chain.doFilter(request, response);
				} else {
					response.sendError(401);
				}
			}
		} else {
			chain.doFilter(request, response);
		}
	}
}
