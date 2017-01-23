package org.lhasalimited;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lhasalimited.vitic.backend.web.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
	
	@LocalServerPort
	private int port;

	@Value("${server.port}")
	private int mgt;

	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Test
	public void shouldReturn200WhenSendingRequestToController() throws Exception {
		String localHost="http://localhost:";
		String searchUri="/search";
		String searchUrl=localHost+this.port+searchUri;
		
		ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(searchUrl, Map.class);

		then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}