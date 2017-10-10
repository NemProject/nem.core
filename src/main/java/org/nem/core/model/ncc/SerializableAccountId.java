package org.nem.core.model.ncc;

import org.nem.core.model.Address;
import org.nem.core.serialization.*;

/**
 * A serializable account id.
 * <br>
 * This is done purposefully (instead of having AccountId directly implement SerializableEntity)
 * to make it explicit that derived classes of AccountId will most likely not serialize completely.
 */
public class SerializableAccountId extends AccountId implements SerializableEntity {

	/**
	 * Creates a new account id.
	 *
	 * @param address The account address.
	 */
	public SerializableAccountId(final Address address) {
		super(address);
	}

	/**
	 * Creates a new account id.
	 *
	 * @param address The account address string.
	 */
	public SerializableAccountId(final String address) {
		super(address);
	}
	/**
	 * Creates a new account id.
	 *
	 * @param deserializer The deserializer.
	 */

	public SerializableAccountId(final Deserializer deserializer) {
		super(deserializer);
	}

	@Override
	public void serialize(final Serializer serializer) {
		Address.writeTo(serializer, "account", this.getAddress());
	}
}
