package me.whereareiam.commandant.common;

import me.whereareiam.commandant.builder.PaginationBuilder;
import me.whereareiam.commandant.fixture.TestMessenger;
import me.whereareiam.commandant.fixture.TestSender;
import me.whereareiam.commandant.message.GeneratedCommandantMessages;
import me.whereareiam.commandant.message.PaginationMessages;
import me.whereareiam.keystone.Messenger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultPaginationBuilderTest {
	private static final PaginationMessages MESSAGES = new GeneratedCommandantMessages().pagination();

	private final Messenger<TestSender> messenger = TestMessenger.create();
	private final TestSender reader = TestSender.player("Alice");

	@Test
	void hidesTheLineWhenEverythingFitsOnOnePage() {
		assertEquals(Optional.empty(), pagination(false, false, false).build(5, 1, 7));
	}

	@Test
	void showsArrowsThatRunThePageCommandOfTheirPage() {
		show(pagination(false, false, false), 20, 2);

		assertEquals(List.of("Page 2 of 3 « Page 1 Page 3 »"), reader.lines());
		assertEquals(List.of("/arbor help 1", "/arbor help 3"), reader.clickable());
	}

	@Test
	void leavesOutArrowsWithNoPageToGoTo() {
		show(pagination(false, false, false), 10, 1);
		show(pagination(false, false, false), 10, 2);

		assertEquals(List.of("Page 1 of 2  Page 2 »", "Page 2 of 2 « Page 1 "), reader.lines());
		assertEquals(List.of("/arbor help 2", "/arbor help 1"), reader.clickable());
	}

	@Test
	void showsWhatItIsToldToOnASinglePage() {
		show(pagination(true, true, true), 0, 1);

		assertEquals(List.of("Page 1 of 1 « Page 1 Page 1 »"), reader.lines());
		assertEquals(List.of("/arbor help 1", "/arbor help 1"), reader.clickable());
	}

	@Test
	void arrowsRunNothingWithoutAPageCommand() {
		show(new DefaultPaginationBuilder(MESSAGES, null, false, false, false), 10, 1);

		assertEquals(List.of("Page 1 of 2  Page 2 »"), reader.lines());
		assertEquals(List.of(), reader.clickable());
	}

	private void show(PaginationBuilder pagination, int totalItems, int page) {
		messenger.send(reader, pagination.build(totalItems, page, 7).orElseThrow());
	}

	private PaginationBuilder pagination(boolean showIfOnePage, boolean showPreviousOnFirst, boolean showNextOnLast) {
		return new DefaultPaginationBuilder(MESSAGES, page -> "/arbor help " + page, showIfOnePage, showPreviousOnFirst, showNextOnLast);
	}
}
