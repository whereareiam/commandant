package me.whereareiam.commandant.model;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TranslatableDescriptionTest {
	@Test
	void rendersAsTheTranslatableComponentOfItsKey() {
		TranslatableDescription description = TranslatableDescription.of("arbor.command.status.description");

		assertEquals(Component.translatable("arbor.command.status.description"), description.asComponent());
		assertEquals("arbor.command.status.description", description.textDescription());
		assertFalse(description.isEmpty());
	}
}
