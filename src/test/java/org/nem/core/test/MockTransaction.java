package org.nem.core.test;

import org.nem.core.model.*;
import org.nem.core.model.observers.*;
import org.nem.core.model.primitive.Amount;
import org.nem.core.serialization.*;
import org.nem.core.time.TimeInstant;

import java.util.*;
import java.util.function.Consumer;

/**
 * A mock Transaction implementation.
 */
public class MockTransaction extends Transaction {
	private static final int TYPE = 124;
	private static final int VERSION = 1;
	public static final TimeInstant TIMESTAMP = new TimeInstant(1122448);
	public static final TimeInstant DEADLINE = TIMESTAMP.addHours(2);
	public static final Amount DEFAULT_FEE = Amount.fromNem(6);

	private int customField;

	private Collection<Account> otherAccounts = new ArrayList<>();
	private Collection<Transaction> childTransactions = new ArrayList<>();

	private Consumer<TransactionObserver> transferAction = o -> NotificationUtils.notifyDebit(o, this.getSigner(), this.getFee());

	private final List<Notification> notifications = new ArrayList<>();
	private int numTransferCalls;

	/**
	 * Creates a mock transaction.
	 */
	public MockTransaction() {
		this(Utils.generateRandomAccount());
	}

	/**
	 * Creates a mock transaction.
	 *
	 * @param sender The transaction sender's account.
	 */
	public MockTransaction(final Account sender) {
		this(sender, 0);
	}

	/**
	 * Creates a mock transaction.
	 *
	 * @param type The transaction type.
	 * @param sender The transaction sender's account.
	 */
	public MockTransaction(final int type, final Account sender) {
		super(type, VERSION, TIMESTAMP, sender);
		this.setDeadline(DEADLINE);
	}

	/**
	 * Creates a mock transaction.
	 *
	 * @param timeStamp The transaction's timestamp.
	 */
	public MockTransaction(final TimeInstant timeStamp) {
		this(0, timeStamp);
	}

	/**
	 * Creates a mock transaction.
	 *
	 * @param sender The transaction sender's account.
	 * @param customField The initial custom field value.
	 */
	public MockTransaction(final Account sender, final int customField) {
		super(TYPE, VERSION, TIMESTAMP, sender);
		this.customField = customField;
		this.setDeadline(DEADLINE);
	}

	/**
	 * Creates a mock transaction.
	 *
	 * @param customField The initial custom field value.
	 * @param timeStamp The transaction timestamp.
	 */
	public MockTransaction(final int customField, final TimeInstant timeStamp) {
		this(Utils.generateRandomAccount(), customField, timeStamp);
	}

	/**
	 * Creates a mock transaction.
	 *
	 * @param sender The transaction sender's account.
	 * @param customField The initial custom field value.
	 * @param timeStamp The transaction timestamp.
	 */
	public MockTransaction(final Account sender, final int customField, final TimeInstant timeStamp) {
		super(TYPE, VERSION, timeStamp, sender);
		this.customField = customField;
		this.setDeadline(timeStamp.addHours(2));
	}

	/**
	 * Creates a mock transaction.
	 * This overload is intended to be used for comparison tests.
	 *
	 * @param type The transaction type.
	 * @param customField The initial custom field value.
	 * @param timeStamp The transaction timestamp.
	 */
	public MockTransaction(final int type, final int customField, final TimeInstant timeStamp) {
		super(type, VERSION, timeStamp, Utils.generateRandomAccount());
		this.customField = customField;
		this.setDeadline(timeStamp.addHours(2));
	}

	/**
	 * Creates a mock transaction.
	 * This overload is intended to be used for comparison tests.
	 *
	 * @param type The transaction type.
	 * @param version The transaction version.
	 * @param timeStamp The transaction timestamp.
	 * @param fee The transaction fee.
	 */
	public MockTransaction(final int type, final int version, final TimeInstant timeStamp, final long fee) {
		super(type, version, timeStamp, Utils.generateRandomAccount());
		this.setFee(new Amount(fee));
		this.setDeadline(timeStamp.addHours(2));
	}

	/**
	 * Deserializes a MockTransaction.
	 *
	 * @param deserializer The deserializer to use.
	 */
	public MockTransaction(final Deserializer deserializer) {
		super(deserializer.readInt("type"), DeserializationOptions.VERIFIABLE, deserializer);
		this.customField = deserializer.readInt("customField");
	}

	/**
	 * Gets the number of times transfer was called.
	 *
	 * @return The number of times transfer was called.
	 */
	public int getNumTransferCalls() {
		return this.numTransferCalls;
	}

	/**
	 * Gets the custom field value.
	 *
	 * @return The custom field value.
	 */
	public int getCustomField() {
		return this.customField;
	}

	/**
	 * Sets an action that should be executed when transfer is called.
	 *
	 * @param transferAction The action.
	 */
	public void setTransferAction(final Consumer<TransactionObserver> transferAction) {
		this.transferAction = transferAction;
	}

	/**
	 * Adds a notification that gets fired when transfer is called.
	 *
	 * @param notification The notification.
	 */
	public void addNotification(final Notification notification) {
		this.notifications.add(notification);
	}

	/**
	 * Sets the other accounts.
	 *
	 * @param accounts The other accounts.
	 */
	public void setOtherAccounts(final Collection<Account> accounts) {
		this.otherAccounts = accounts;
	}

	/**
	 * Sets the child transactions.
	 *
	 * @param transactions The child transactions.
	 */
	public void setChildTransactions(final Collection<Transaction> transactions) {
		this.childTransactions = transactions;
	}

	@Override
	protected Collection<Account> getOtherAccounts() {
		return this.otherAccounts;
	}

	@Override
	public Collection<Transaction> getChildTransactions() {
		return this.childTransactions;
	}

	@Override
	protected void serializeImpl(final Serializer serializer) {
		super.serializeImpl(serializer);
		serializer.writeInt("customField", this.customField);
	}

	@Override
	protected void transfer(final TransactionObserver observer, final TransactionExecutionState state) {
		this.transferAction.accept(observer);
		this.notifications.forEach(observer::notify);
		++this.numTransferCalls;
	}

	@Override
	public String toString() {
		return String.format("Mock %d", this.customField);
	}
}