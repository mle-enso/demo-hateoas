package demo.hateoas;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;



@SpringBootTest(classes = DemoApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class HateoasControllerDocumentation extends AbstractTestNGSpringContextTests {
	private ManualRestDocumentation restDocumentation = new ManualRestDocumentation("target/generated-snippets");

	@BeforeMethod
	public void setUp(Method method) {
		restDocumentation.beforeTest(getClass(), method.getName());
	}

	@AfterMethod
	public void tearDown() {
		restDocumentation.afterTest();
	}

	public RequestSpecification getPlainRequestSpec() {
		return new RequestSpecBuilder().addFilter(documentationConfiguration(restDocumentation).snippets().withEncoding("UTF-8")).build();
	}

	@Value("${local.server.port}")
	int port;

	@DataProvider(name = "urls")
	public Object[][] urlPermutations() {
		return new Object[][] {
			{ "list", "offer", "en_GB" },
			{ "liste", "angebot", "de_DE" },
			{ "offer", "list", "en_GB" },
			{ "angebot", "liste", "de_DE" } };
	}

	@Test(dataProvider = "urls")
	public void urls(String path, String nextLink, String locale) {
		given(getPlainRequestSpec())
		.baseUri("http://localhost").port(port)
		.when()
		.filter(document("hateoas",
				preprocessResponse(prettyPrint()),
				requestParameters(parameterWithName("locale").description("The locale for the current request, to determine the correct path variable during validation")),
				responseFields(
						fieldWithPath("currentPathVar").description("The path variable used for the current request"),
						fieldWithPath("nextLink").description("Locale-specific link to the other controller endpoint"))))
		.param("locale", locale)
		.get(path)
		.then()
		.statusCode(200)
		.body("currentPathVar", is(path))
		.body("nextLink", is("http://localhost:" + port + "/" + nextLink + "?locale=" + locale));
	}

	@Test
	public void invalidPathMapping() {
		given(getPlainRequestSpec())
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
		given(getPlainRequestSpec())
		.baseUri("http://localhost")
		.port(port)
		.when()
		.param("locale", "en_GB")
		.get("muell")
		.then()
		.statusCode(404);
	}
}
