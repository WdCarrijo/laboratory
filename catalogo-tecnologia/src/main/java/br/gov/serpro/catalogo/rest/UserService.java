package br.gov.serpro.catalogo.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.spi.validation.ValidateRequest;

import br.gov.frameworkdemoiselle.resteasy.util.ValidationException;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import br.gov.serpro.catalogo.entity.User;
import br.gov.serpro.catalogo.persistence.UserDAO;
import br.gov.serpro.catalogo.security.LDAPAuthenticator;

@ValidateRequest
@Path("/api/user")
@Produces(APPLICATION_JSON)
public class UserService {

	@Inject
	private UserDAO userDAO;
	
	@Inject
	private LDAPAuthenticator ldapAuthenticator;
	
	@GET
	public List<User> listar() {
		return userDAO.findAll();
	}
	
	@GET
	@Path("/{id}")
	public User carregar(@NotNull @PathParam("id") Long id) {
		return userDAO.load(id);
	}
	
	@GET
	@Path("/cpf/{cpf}")
	public User carregar(@NotNull @PathParam("cpf") String cpf) throws Exception {
		if (userDAO.loadByCPF(cpf) != null){
			ValidationException ve = new ValidationException();
			ve.addViolation(null, "Usuário já cadastrado na base.");
			throw ve;
		}
		
		return ldapAuthenticator.searchUserByCPF(cpf);
	}
	
	@PUT
	@Transactional
	public void alterar(@Valid User user) {
		userDAO.update(user);
	}
	
	@POST
	@Transactional
	public void inserir(@Valid User user) {
		userDAO.insert(user);
	}
}