package me.whereareiam.commandant.common;

import me.whereareiam.commandant.builder.PaginationBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static me.whereareiam.commandant.builder.PaginationBuilder.Text;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultPaginationBuilderTest {
	private static final String PREFIX = "arbor.help.pagination";

	@Test
	void everyTextGetsTheArgumentsItDeclares() {
		PaginationBuilder pagination = pagination(false, false, false);

		Map<String, Set<String>> declared = Rendered.declared(PREFIX, Text.values());
		assertEquals(declared, Rendered.arguments(pagination.build(20, 2, 7).orElseThrow()));
	}

	@Test
	void hidesTheLineWhenEverythingFitsOnOnePage() {
		PaginationBuilder pagination = pagination(false, false, false);

		assertEquals(Optional.empty(), pagination.build(5, 1, 7));
	}

	@Test
	void showsArrowsThatRunThePageCommandOfTheirPage() {
		PaginationBuilder pagination = pagination(false, false, false);

		assertEquals(
				"arbor.help.pagination.line(current=2, max=3, "
						+ "previous=arbor.help.pagination.previous(page=1)[click /arbor help 1], "
						+ "next=arbor.help.pagination.next(page=3)[click /arbor help 3])",
				Rendered.of(pagination.build(20, 2, 7).orElseThrow())
		);
	}

	@Test
	void leavesOutArrowsWithNoPageToGoTo() {
		PaginationBuilder pagination = pagination(false, false, false);

		assertEquals(
				"arbor.help.pagination.line(current=1, max=2, previous=, "
						+ "next=arbor.help.pagination.next(page=2)[click /arbor help 2])",
				Rendered.of(pagination.build(10, 1, 7).orElseThrow())
		);
		assertEquals(
				"arbor.help.pagination.line(current=2, max=2, "
						+ "previous=arbor.help.pagination.previous(page=1)[click /arbor help 1], next=)",
				Rendered.of(pagination.build(10, 2, 7).orElseThrow())
		);
	}

	@Test
	void showsWhatItIsToldToOnASinglePage() {
		PaginationBuilder pagination = pagination(true, true, true);

		assertEquals(
				"arbor.help.pagination.line(current=1, max=1, "
						+ "previous=arbor.help.pagination.previous(page=1)[click /arbor help 1], "
						+ "next=arbor.help.pagination.next(page=1)[click /arbor help 1])",
				Rendered.of(pagination.build(0, 1, 7).orElseThrow())
		);
	}

	@Test
	void arrowsRunNothingWithoutAPageCommand() {
		PaginationBuilder pagination = new DefaultPaginationBuilder(PREFIX, Rendered.ARGUMENTS, null, false, false, false);

		assertEquals(
				"arbor.help.pagination.line(current=1, max=2, previous=, next=arbor.help.pagination.next(page=2))",
				Rendered.of(pagination.build(10, 1, 7).orElseThrow())
		);
	}

	private PaginationBuilder pagination(boolean showIfOnePage, boolean showPreviousOnFirst, boolean showNextOnLast) {
		return new DefaultPaginationBuilder(
				PREFIX,
				Rendered.ARGUMENTS,
				page -> "/arbor help " + page,
				showIfOnePage,
				showPreviousOnFirst,
				showNextOnLast
		);
	}
}
