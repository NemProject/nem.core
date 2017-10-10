package org.nem.core.model;

import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.nem.core.messages.PlainMessage;
import org.nem.core.model.mosaic.*;
import org.nem.core.model.primitive.*;
import org.nem.core.test.*;
import org.nem.core.time.TimeInstant;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class AbstractTransactionFeeCalculatorTest {
	private static final long FEE_UNIT = 2;
	protected static BlockHeight DEFAULT_HEIGHT;

	// The default fee for namespace and mosaic related transactions
	protected static void setNamespaceAndMosaicRelatedDefaultFee(final long fee) {
		ProvisionNamespaceMinimumFeeCalculation.setDefaultFee(fee);
		MosaicDefinitionCreationMinimumFeeCalculation.setDefaultFee(fee);
		MosaicSupplyChangeMinimumFeeCalculation.setDefaultFee(fee);
	}

	protected static void setMultisigAggregateModificationFeeCalculation(final BiFunction<Integer, Boolean, Amount> calculator) {
		MultisigAggregateModificationMinimumFeeCalculation.setFeeCalculation(calculator);
	}

	// The default fee transactions
	protected static void setTransactionDefaultFee(final long fee) {
		DefaultMinimumFeeCalculation.setDefaultFee(fee);
	}

	// The minimum fee for signature transactions
	protected static void setMultisigSignatureMinimumFee(final long fee) {
		MultisigSignatureIsValidCalculation.setMultisigSignatureMinimumFee(fee);
	}

	protected static void assertXemFee(final long amount, final int messageSize, final Amount expectedFee) {
		// Arrange:
		final Message message = 0 == messageSize ? null : new PlainMessage(new byte[messageSize]);
		final Transaction transaction = createTransfer(amount, message);

		// Assert:
		assertTransactionFee(transaction, expectedFee);
	}

	protected static void assertSingleMosaicFee(final long amount, final int messageSize, final long quantity, final Amount expectedFee) {
		// Arrange:
		final Transaction transaction = createTransferWithMosaics(amount, messageSize, quantity);

		// Assert:
		assertTransactionFee(transaction, expectedFee);
	}

	protected static void assertMessageFee(final int encodedMessageSize, final int decodedMessageSize, final Amount expectedFee) {
		// Arrange:
		final Transaction transaction = createTransferWithMockMessage(encodedMessageSize, decodedMessageSize);

		// Assert:
		assertTransactionFee(transaction, expectedFee);
	}

	protected static Transaction createTransferWithMockMessage(final int encodedMessageSize, final int decodedMessageSize) {
		// Arrange:
		final MockMessage message = new MockMessage(7);
		message.setEncodedPayload(new byte[encodedMessageSize]);
		message.setDecodedPayload(new byte[decodedMessageSize]);
		return createTransfer(0, message);
	}

	protected static Transaction createTransferWithMosaics(final long amount, final int messageSize, final long... quantities) {
		final Message message = 0 == messageSize ? null : new PlainMessage(new byte[messageSize]);
		final TransferTransaction transaction = createTransfer(amount, message);
		IntStream.range(0, quantities.length)
				.forEach(i -> transaction.getAttachment().addMosaic(Utils.createMosaicId(i + 1), Quantity.fromValue(quantities[i])));
		return transaction;
	}

	protected static void assertXemTransferToMosaicTransferFeeRatio(final long amount) {
		// Arrange:
		final TransactionFeeCalculator calculator = createCalculator();
		final TransferTransaction xemTransfer = createTransfer(amount, null);
		final long xemFee = calculator.calculateMinimumFee(xemTransfer).getNumNem();
		final TransferTransaction mosaicTransfer = createTransfer(1, null);
		mosaicTransfer.getAttachment().addMosaic(MosaicConstants.MOSAIC_ID_XEM, Quantity.fromValue(amount * Amount.MICRONEMS_IN_NEM));
		final long mosaicFee = calculator.calculateMinimumFee(mosaicTransfer).getNumNem();

		// Assert:
		Assert.assertThat(mosaicFee, IsEqual.equalTo((xemFee * 5) / 4));
	}

	//region calculateMinimumFee

	//region multisig aggregate modification

	public static class MultisigAggregateModificationMinimumFeeCalculation {
		private static final Boolean MIN_COSIGNATORIES_MODIFICATION_PRESENT = true;
		private static BiFunction<Integer, Boolean, Amount> CALCULATOR = MultisigAggregateModificationMinimumFeeCalculation::calculateExpectedFee;

		@Test
		public void feeIsCalculatedCorrectlyForSingleCosignatoryModificationWithoutMinCosignatoriesModification() {
			// Assert:
			assertFee(1, !MIN_COSIGNATORIES_MODIFICATION_PRESENT, CALCULATOR.apply(1, false));
		}

		@Test
		public void feeIsCalculatedCorrectlyForMultipleCosignatoryModificationsWithoutMinCosignatoriesModification() {
			// Assert:
			for (int i = 2; i < 10; ++i) {
				assertFee(i, !MIN_COSIGNATORIES_MODIFICATION_PRESENT, CALCULATOR.apply(i, false));
			}
		}

		@Test
		public void feeIsCalculatedCorrectlyForZeroCosignatoryModificationsWithMinCosignatoriesModification() {
			// Assert:
			assertFee(0, MIN_COSIGNATORIES_MODIFICATION_PRESENT, CALCULATOR.apply(0, true));
		}

		@Test
		public void feeIsCalculatedCorrectlyForSingleCosignatoryModificationWithMinCosignatoriesModification() {
			// Assert:
			assertFee(1, MIN_COSIGNATORIES_MODIFICATION_PRESENT, CALCULATOR.apply(1, true));
		}

		@Test
		public void feeIsCalculatedCorrectlyForMultipleCosignatoryModificationsWithMinCosignatoriesModification() {
			// Assert:
			for (int i = 2; i < 10; ++i) {
				assertFee(i, MIN_COSIGNATORIES_MODIFICATION_PRESENT, CALCULATOR.apply(i, true));
			}
		}

		protected static Amount calculateExpectedFee(final int numModification, boolean hasMinCosignatoriesModification) {
			return Amount.fromNem((5 + 3 * numModification + (hasMinCosignatoriesModification ? 3 : 0)) * FEE_UNIT);
		}

		private static void setFeeCalculation(final BiFunction<Integer, Boolean, Amount> newCalculator) {
			CALCULATOR = newCalculator;
		}

		private static void assertFee(
				final int numModifications,
				final boolean minCosignatoriesModificationPresent,
				final Amount expectedFee) {
			// Arrange:
			final Transaction transaction = createMultisigAggregateModification(numModifications, minCosignatoriesModificationPresent ? 3 : null);

			// Assert:
			assertTransactionFee(transaction, expectedFee);
		}
	}

	//endregion

	//region other transactions

	private static abstract class DefaultMinimumFeeCalculation {
		private static long DEFAULT_FEE = 6_000_000;

		@Test
		public void feeIsDefaultFee() {
			// Arrange:
			final Transaction transaction = this.createTransaction();

			// Assert:
			assertTransactionFee(transaction, Amount.fromMicroNem(this.expectedFee()));
		}

		protected abstract Transaction createTransaction();

		protected long expectedFee() {
			return DEFAULT_FEE;
		}

		private static void setDefaultFee(final long fee) {
			DEFAULT_FEE = fee;
		}
	}

	public static class ImportanceTransferMinimumFeeCalculation extends DefaultMinimumFeeCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createImportanceTransfer();
		}
	}

	public static class MultisigMinimumFeeCalculation extends DefaultMinimumFeeCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMultisigTransfer();
		}
	}

	public static class MultisigSignatureMinimumFeeCalculation extends DefaultMinimumFeeCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMultisigSignature();
		}
	}

	public static class ProvisionNamespaceMinimumFeeCalculation extends DefaultMinimumFeeCalculation {
		protected static long DEFAULT_FEE;

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createProvisionNamespaceTransaction();
		}

		@Override
		protected long expectedFee() {
			return DEFAULT_FEE;
		}

		private static void setDefaultFee(final long fee) {
			DEFAULT_FEE = fee;
		}
	}

	public static class MosaicDefinitionCreationMinimumFeeCalculation extends DefaultMinimumFeeCalculation {
		protected static long DEFAULT_FEE;

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMosaicDefinitionCreationTransaction();
		}

		@Override
		protected long expectedFee() {
			return DEFAULT_FEE;
		}

		private static void setDefaultFee(final long fee) {
			DEFAULT_FEE = fee;
		}
	}

	public static class MosaicSupplyChangeMinimumFeeCalculation extends DefaultMinimumFeeCalculation {
		protected static long DEFAULT_FEE;

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMosaicSupplyChangeTransaction();
		}

		@Override
		protected long expectedFee() {
			return DEFAULT_FEE;
		}

		private static void setDefaultFee(final long fee) {
			DEFAULT_FEE = fee;
		}
	}

	//endregion

	//endregion

	//region isFeeValid

	private static abstract class DefaultIsValidCalculation {

		@Test
		public void feeBelowMinimumFeeIsNotValid() {
			// Arrange:
			final Transaction transaction = this.createTransaction();

			// Act:
			final boolean isValid = isRelativeMinimumFeeValid(transaction, -1);

			// Assert:
			Assert.assertThat(isValid, IsEqual.equalTo(false));
		}

		@Test
		public void feeEqualToMinimumFeeIsValid() {
			// Arrange:
			final Transaction transaction = this.createTransaction();

			// Act:
			final boolean isValid = isRelativeMinimumFeeValid(transaction, 0);

			// Assert:
			Assert.assertThat(isValid, IsEqual.equalTo(true));
		}

		@Test
		public void feeAboveMinimumFeeIsValid() {
			// Arrange:
			final Transaction transaction = this.createTransaction();

			// Act:
			final boolean isValid = isRelativeMinimumFeeValid(transaction, 1);

			// Assert:
			Assert.assertThat(isValid, IsEqual.equalTo(true));
		}

		protected abstract Transaction createTransaction();
	}

	public static class TransferIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return createTransfer(100, null);
		}
	}

	public static class MultisigAggregateModificationWithoutMinCosignatoriesModificationIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return createMultisigAggregateModification(5, null);
		}
	}

	public static class MultisigAggregateModificationWithMinCosignatoriesModificationIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return createMultisigAggregateModification(5, 3);
		}
	}

	public static class ImportanceTransferIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createImportanceTransfer();
		}
	}

	public static class MultisigIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMultisigTransfer();
		}
	}

	public static class ProvisionNamespaceIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createProvisionNamespaceTransaction();
		}
	}

	public static class MosaicDefinitionCreationIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMosaicDefinitionCreationTransaction();
		}
	}

	public static class MosaicSupplyChangeIsValidCalculation extends DefaultIsValidCalculation {

		@Override
		protected Transaction createTransaction() {
			return RandomTransactionFactory.createMosaicSupplyChangeTransaction();
		}
	}

	public static class MultisigSignatureIsValidCalculation {
		private static long MINIMUM_FEE = 6_000_000;
		private static final int FORK_HEIGHT = 92000;

		@Test
		public void feeBelowMinimumFeeIsNotValid() {
			// Arrange:
			final Transaction transaction = RandomTransactionFactory.createMultisigSignature();
			// Act:
			final boolean isValid = isRelativeMinimumFeeValid(transaction, -1);

			// Assert:
			Assert.assertThat(isValid, IsEqual.equalTo(false));
		}

		@Test
		public void feeEqualToMinimumFeeIsValid() {
			// Arrange:
			final Transaction transaction = RandomTransactionFactory.createMultisigSignature();

			// Act:
			final boolean isValid = isRelativeMinimumFeeValid(transaction, 0);

			// Assert:
			Assert.assertThat(isValid, IsEqual.equalTo(true));
		}

		@Test
		public void feeAboveMinimumFeeUpToOneThousandXemIsValid() {
			// Assert:
			final long[] heights = new long[] { 100, 1000, FORK_HEIGHT + 1, FORK_HEIGHT + 10, FORK_HEIGHT + 1000 };
			assertFeeAboveMinimumFeeUpToOneThousandXemHasExpectedValidityAtHeights(heights, true);
		}

		public static void assertFeeAboveMinimumFeeUpToOneThousandXemHasExpectedValidityAtHeight(final long height, final boolean expectedResult) {
			// Arrange:
			final Transaction transaction = RandomTransactionFactory.createMultisigSignature();

			// Assert:
			assertFeeValidationResult(transaction, MINIMUM_FEE + 1, height, expectedResult);
			assertFeeValidationResult(transaction, 10_000_000, height, expectedResult);
			assertFeeValidationResult(transaction, 100_000_000, height, expectedResult);
			assertFeeValidationResult(transaction, 1000_000_000, height, expectedResult);
		}

		public static void assertFeeAboveMinimumFeeUpToOneThousandXemHasExpectedValidityAtHeights(final long[] heights, final boolean expectedResult) {
			// Assert:
			for (final long height : heights) {
				assertFeeAboveMinimumFeeUpToOneThousandXemHasExpectedValidityAtHeight(height, expectedResult);
			}
		}

		@Test
		public void feeAboveOneThousandXemIsInvalid() {
			// Arrange:
			final Transaction transaction = RandomTransactionFactory.createMultisigSignature();

			// Assert:
			assertFeeValidationResult(transaction, 1001_000_000, false);
		}

		private static void setMultisigSignatureMinimumFee(final long fee) {
			MINIMUM_FEE = fee;
		}
	}

	//endregion

	//region factories

	protected static TransferTransaction createTransfer(final long amount, final Message message) {
		return new TransferTransaction(
				TimeInstant.ZERO,
				Utils.generateRandomAccount(),
				Utils.generateRandomAccount(),
				Amount.fromNem(amount),
				new TransferTransactionAttachment(message));
	}

	private static Transaction createMultisigAggregateModification(final int numModifications, final Integer minCosignatories) {
		final Collection<MultisigCosignatoryModification> modifications = new ArrayList<>();
		for (int i = 0; i < numModifications; ++i) {
			modifications.add(new MultisigCosignatoryModification(MultisigModificationType.AddCosignatory, Utils.generateRandomAccount()));
		}

		return new MultisigAggregateModificationTransaction(
				TimeInstant.ZERO,
				Utils.generateRandomAccount(),
				modifications,
				null == minCosignatories ? null : new MultisigMinCosignatoriesModification(minCosignatories));
	}

	//endregion

	//region other helpers

	protected static void assertTransactionFee(final Transaction transaction, final Amount expectedFee) {
		// Act:
		final Amount fee = createCalculator().calculateMinimumFee(transaction);

		// Assert:
		Assert.assertThat(fee, IsEqual.equalTo(expectedFee));
	}

	private static boolean isRelativeMinimumFeeValid(final Transaction transaction, final int delta) {
		// Arrange:
		Amount minimumFee = createCalculator().calculateMinimumFee(transaction);

		if (delta < 0) {
			minimumFee = minimumFee.subtract(Amount.fromMicroNem(-1 * delta));
		} else if (delta > 0) {
			minimumFee = minimumFee.add(Amount.fromMicroNem(delta));
		}

		transaction.setFee(minimumFee);

		// Act:
		return createCalculator().isFeeValid(transaction, DEFAULT_HEIGHT);
	}

	private static void assertFeeValidationResult(
			final Transaction transaction,
			final long fee,
			final boolean expectedResult) {
		assertFeeValidationResult(transaction, fee, Long.MAX_VALUE, expectedResult);
	}

	private static void assertFeeValidationResult(
			final Transaction transaction,
			final long fee,
			final long height,
			final boolean expectedResult) {
		// Arrange:
		transaction.setFee(Amount.fromMicroNem(fee));

		// Act:
		final boolean isValid = createCalculator().isFeeValid(transaction, new BlockHeight(height));

		// Assert:
		Assert.assertThat(
				String.format("fee: %d, height: %d", fee, height),
				isValid,
				IsEqual.equalTo(expectedResult));
	}

	protected static MosaicFeeInformationLookup createMosaicFeeInformationLookup() {
		return id -> {
			if (id.getName().equals("xem")) {
				return new MosaicFeeInformation(Supply.fromValue(8_999_999_999L), 6);
			}

			if (id.getName().equals("zero supply")) {
				return new MosaicFeeInformation(Supply.ZERO, 3);
			}

			if (id.getName().startsWith("small business 0")) {
				return new MosaicFeeInformation(Supply.fromValue(1_000), 1);
			}

			if (id.getName().startsWith("small business ")) {
				final int multiplier = Integer.parseInt(id.getName().substring(15));
				return new MosaicFeeInformation(Supply.fromValue(multiplier * 1_000), 0);
			}

			if (!id.getName().startsWith("name")) {
				return null;
			}

			final int multiplier = Integer.parseInt(id.getName().substring(4));
			final int divisibilityChange = multiplier - 1;
			return new MosaicFeeInformation(
					Supply.fromValue(100_000_000 * multiplier),
					3 + divisibilityChange);
		};
	}

	protected static TransactionFeeCalculator createCalculator() {
		return NemGlobals.getTransactionFeeCalculator();
	}

	//endregion
}
