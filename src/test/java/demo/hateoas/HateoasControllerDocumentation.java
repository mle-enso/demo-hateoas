package demo.hateoas;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

import java.lang.reflect.Method;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class HateoasControllerDocumentation extends AbstractTestNGSpringContextTests {
	private ManualRestDocumentation restDocumentation = new ManualRestDocumentation();

	@BeforeMethod
	public void setUp(Method method) {
		this.spec = new RequestSpecBuilder()
				.addFilter(documentationConfiguration(this.restDocumentation))
				.setBaseUri("http://localhost")
				.setPort(port)
				.build();
		this.restDocumentation.beforeTest(getClass(), method.getName());
	}

	@AfterMethod
	public void tearDown() {
		this.restDocumentation.afterTest();
	}

	@LocalServerPort
	int port;

	private RequestSpecification spec;

	@DataProvider(name = "urlPermutations")
	public static Object[][] urlPermutations() {
		return new Object[][] {
				{ "list", "offer", "en_GB" },
				{ "liste", "angebot", "de_DE" },
				{ "offer", "list", "en_GB" },
				{ "angebot", "liste", "de_DE" } };
	}

	@Test(dataProvider = "urlPermutations")
	public void urls(String path, String nextLink, String locale) {
		given(spec)
				.when().filter(document("hateoas",
						preprocessResponse(prettyPrint()),
						requestParameters(parameterWithName("locale").description(
								"The locale for the current request, to determine the correct path variable during validation")),
						responseFields(
								fieldWithPath("currentPathVar")
										.description("The path variable used for the current request"),
								fieldWithPath("nextLink")
										.description("Locale-specific link to the other controller endpoint"))))
				.param("locale", locale)
				.get(path)
				.then()
				.statusCode(200)
				.body("currentPathVar", is(path))
				.body("nextLink", is("http://localhost:" + port + "/" + nextLink + "?locale=" + locale));
	}

	@Test
	public void xForwardedFor() {
		given(spec)
				.when().filter(document("x-forwarded-host",
						preprocessResponse(prettyPrint()),
						requestParameters(parameterWithName("locale").description(
								"The locale for the current request, to determine the correct path variable during validation")),
						requestHeaders(headerWithName("X-Forwarded-Host")
								.description("The host for which the link shall be created.")),
						responseFields(
								fieldWithPath("currentPathVar")
										.description("The path variable used for the current request"),
								fieldWithPath("nextLink")
										.description("Locale-specific link to the other controller endpoint"))))
				.param("locale", "en_GB")
				.header("X-Forwarded-Host", "test.de:9090")
				.get("list")
				.then()
				.statusCode(200)
				.body("currentPathVar", is("list"))
				.body("nextLink", is("http://test.de:9090/offer?locale=en_GB"));
	}

	@Test
	public void invalidPathMapping() {
		given(spec)
				.when()
				.param("locale", "de_DE")
				.get("list")
				.then()
				.statusCode(400)
				.body("[0]", is("list is no valid path variable for page LIST and locale de_DE"));
	}

	@Test
	public void invalidPathVar() {
		given(spec)
				.when()
				.param("locale", "en_GB")
				.get("muell")
				.then()
				.statusCode(404);
	}
}
