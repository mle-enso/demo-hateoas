package demo.hateoas;

import java.util.Arrays;
import java.util.Locale;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ToString
@AllArgsConstructor
public enum PageMapping {
	OFFER_EN(Page.OFFER, Locale.UK, "offer"),
	OFFER_DE(Page.OFFER, Locale.GERMANY, "angebot"),

	LIST_EN(Page.LIST, Locale.UK, "list"),
	LIST_DE(Page.LIST, Locale.GERMANY, "liste");

	private Page page;
	private Locale locale;
	private String pathVar;

	public static PageMapping valueOf(Page page, Locale locale) {
		return Arrays.stream(values())
				.filter(mapping -> mapping.getPage() == page && mapping.getLocale().equals(locale))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"No page mapping found for page " + page + " and locale " + locale));
	}
}
