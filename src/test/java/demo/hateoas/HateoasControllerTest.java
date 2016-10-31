package demo.hateoas;

import static org.hamcrest.CoreMatchers.is;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class HateoasControllerTest extends AbstractTestNGSpringContextTests {
	@Value("${local.server.port}")
	int port;

	@DataProvider(name = "urls")
	public Object[][] urlPermutations() {
		return new Object[][] {
				{ "list", "offer", "en_GB" },
				{ "liste", "angebot", "de_DE" },
				{ "offer", "list", "en_GB" },
				{ "angebot", "liste", "de_DE" }
		};
	}

	@Test(dataProvider = "urls")
	public void urls(String path, String nextLink, String locale) {
		RestAssured
				.given()
				.baseUri("http://localhost").port(port)
				.when()
				.param("locale", locale)
				.get(path)
				.then()
				.statusCode(200)
				.body("currentPathVar", is(path))
				.body("nextLink", is("http://localhost:" + port + "/" + nextLink + "?locale=" + locale));
	}

	@Test
	public void invalidPathMapping() {
		RestAssured
				.given()
				.baseUri("http://localhost").port(port)
				.when()
				.param("locale", "de_DE")
				.get("list")
				.then()
				.statusCode(400)
				.body("[0]", is("list is no valid path variable for page LIST and locale de_DE"));
	}

	@Test
	public void invalidPathVar() {
		RestAssured
				.given()
				.baseUri("http://localhost").port(port)
				.when()
				.param("locale", "en_GB")
				.get("muell")
				.then()
				.statusCode(404);
	}
}
