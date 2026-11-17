package me.whereareiam.commandant.model;

import me.whereareiam.semantica.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TranslatableDescriptionTest {
	@Test
	void isItsMessageAndShowsItsKeyAsText() {
		Message message = Message.of("arbor.command.status.description");
		TranslatableDescription description = TranslatableDescription.of(message);

		assertEquals(message, description.getMessage());
		assertEquals("arbor.command.status.description", description.textDescription());
		assertFalse(description.isEmpty());
	}
}
