package app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import app.model.Token;

@RestController
@RequestMapping("/api/login")
public class LoginController {

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<Token> login(String email, String password) {
		return new ResponseEntity<Token>(new Token(""), HttpStatus.NOT_IMPLEMENTED);
	}
	
}
