package demo.hatoas;

import static org.hamcrest.CoreMatchers.is;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import io.restassured.RestAssured;

@SpringBootTest(classes = DemoApplication.class)
public class HateoasControllerTest extends AbstractTestNGSpringContextTests {
	@Value("${local.server.port}")
	int port;

	@Test
	public void listInEnglish() {
		RestAssured
				.given()
				.baseUri("http://localhost").port(port)
				.when()
				.get("/list")
				.then()
				.statusCode(200)
				.body("[0]", is(""))
				.body("[1]", is(""));
	}
}
