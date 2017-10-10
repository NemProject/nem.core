package org.nem.core.model.ncc;

import net.minidev.json.JSONObject;
import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.nem.core.model.Address;
import org.nem.core.serialization.*;
import org.nem.core.test.*;

public class SerializableAccountIdTest {

	@Test
	public void canCreateIdAroundValidAddress() {
		// Arrange:
		final Address address = Utils.generateRandomAddress();

		// Act:
		final SerializableAccountId accountId = new SerializableAccountId(address);

		// Assert:
		Assert.assertThat(getAddress(accountId), IsEqual.equalTo(address));
	}

	@Test
	public void cannotCreateIdAroundInvalidAddress() {
		// Arrange:
		final Address address = Address.fromEncoded("TBAD");

		// Assert:
		ExceptionAssert.assertThrows(v -> new SerializableAccountId(address), IllegalArgumentException.class);
	}

	@Test
	public void canCreateIdAroundValidAddressString() {
		// Arrange:
		final Address address = Utils.generateRandomAddress();

		// Act:
		final SerializableAccountId accountId = new SerializableAccountId(address.toString());

		// Assert:
		Assert.assertThat(getAddress(accountId), IsEqual.equalTo(address));
	}

	@Test
	public void cannotCreateIdAroundInvalidAddressString() {
		// Assert:
		ExceptionAssert.assertThrows(v -> new SerializableAccountId("TBAD"), IllegalArgumentException.class);
	}

	@Test
	public void canCreateIdAroundDeserializerWithValidAddress() {
		// Arrange:
		final Address address = Utils.generateRandomAddress();

		// Act:
		final SerializableAccountId accountId = createFromJson(address);

		// Assert:
		Assert.assertThat(getAddress(accountId), IsEqual.equalTo(address));
	}

	@Test
	public void cannotCreateIdAroundDeserializerWithInvalidAddress() {
		// Act:
		ExceptionAssert.assertThrows(v -> createFromJson(Address.fromEncoded("TBAD")), IllegalArgumentException.class);
	}

	@Test
	public void canSerializeAccountId() {
		// Arrange:
		final Address address = Utils.generateRandomAddress();
		final SerializableAccountId accountId = new SerializableAccountId(address);

		// Act:
		final JSONObject jsonObject = JsonSerializer.serializeToJson(accountId);

		// Assert:
		Assert.assertThat(jsonObject.size(), IsEqual.equalTo(1));
		Assert.assertThat(jsonObject.get("account"), IsEqual.equalTo(address.toString()));
	}

	private static SerializableAccountId createFromJson(final Address address) {
		final JSONObject jsonObject = new JSONObject();
		jsonObject.put("account", address.toString());

		// Act:
		return new SerializableAccountId(Utils.createDeserializer(jsonObject));
	}

	private static Address getAddress(final SerializableAccountId accountId) {
		final Deserializer deserializer = Utils.createDeserializer(JsonSerializer.serializeToJson(accountId));
		return Address.readFrom(deserializer, "account");
	}
}
