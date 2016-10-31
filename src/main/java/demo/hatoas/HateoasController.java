package demo.hatoas;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;
import java.util.Locale;

import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

@RestController
public class HateoasController {
	@RequestMapping("/{pathVar:list|liste}")
	public List<String> list(@PathVariable String pathVar, @RequestParam Locale locale) {
		validateCurrentPathVar(Page.LIST, pathVar, locale);
		Link offerLink = linkTo(methodOn(HateoasController.class).offer(pathVar, locale)).withRel("offer");
		return Lists.newArrayList("current pathVar " + pathVar, "link to offer " + offerLink.getHref());
	}

	@RequestMapping("/{pathVar:offer|angebot}")
	public List<String> offer(@PathVariable String pathVar, @RequestParam Locale locale) {
		validateCurrentPathVar(Page.OFFER, pathVar, locale);
		Link listLink = linkTo(methodOn(HateoasController.class).offer(pathVar, locale)).withRel("list");
		return Lists.newArrayList("current pathVar " + pathVar, "link to list " + listLink);
	}

	private void validateCurrentPathVar(Page page, String pathVar, Locale locale) {
		PageMapping mapping = PageMapping.valueOf(page, locale);
		if (!mapping.getPathVar().equals(pathVar))
			throw new IllegalArgumentException(
					pathVar + " is no valid path variable for page " + page + " and locale " + locale);
	}

}
