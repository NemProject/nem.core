package org.nem.core.model;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.nem.core.model.mosaic.*;
import org.nem.core.model.primitive.*;
import org.nem.core.test.*;

@RunWith(Enclosed.class)
public class FeeUnitAwareTransactionFeeCalculatorTest extends AbstractTransactionFeeCalculatorTest {
	private static Amount FEE_UNIT = Amount.fromMicroNem(50_000);

	@BeforeClass
	public static void setup() {
		DEFAULT_HEIGHT = new BlockHeight(100);
		final MosaicFeeInformationLookup lookup = AbstractTransactionFeeCalculatorTest.createMosaicFeeInformationLookup();
		NemGlobals.setTransactionFeeCalculator(new DefaultTransactionFeeCalculator(
				lookup,
				() -> DEFAULT_HEIGHT,
				new BlockHeight[] { new BlockHeight(1), DEFAULT_HEIGHT.prev() }));
		setNamespaceAndMosaicRelatedDefaultFee(weightWithFeeUnit(Amount.fromNem(3)).getNumMicroNem());
		setTransactionDefaultFee(weightWithFeeUnit(Amount.fromNem(3)).getNumMicroNem());
		setMultisigSignatureMinimumFee(weightWithFeeUnit(Amount.fromNem(3)).getNumMicroNem());
		setMultisigAggregateModificationFeeCalculation(
				(numModification, hasMinCosignatoriesModification) -> weightWithFeeUnit(Amount.fromNem(10)));
	}

	@AfterClass
	public static void teardown() {
		// note that we need to reset the fee calculator since JUnit isn't calling setup()
		// for other fee calculator test classes for some reason
		setMultisigAggregateModificationFeeCalculation(MultisigAggregateModificationMinimumFeeCalculation::calculateExpectedFee);
		Utils.resetGlobals();
	}

	private static Amount weightWithFeeUnit(final Amount fee) {
		return Amount.fromMicroNem(FEE_UNIT.getNumMicroNem() * fee.getNumNem());
	}

	//region calculateMinimumFee

	//region transfer

	public static class TransferMinimumFeeCalculation {
		private static final long MIN_TRANSFER_FEE = 1;

		@Test
		public void feeIsCalculatedCorrectlyForEmptyTransfer() {
			// Assert:
			assertXemFee(0, 0, weightWithFeeUnit(Amount.fromNem(1)));
		}

		@Test
		public void feeIsCalculatedCorrectlyNearTransferStepIncreases() {
			// Assert: fee is initially 1 and increased every 10k xem until is reaches a max fee of 25 xem
			final long step = 10_000;
			for (int i = 0; i < 26; ++i) {
				final long amount = i * step;
				final long fee = Math.max(1, Math.min(25, amount / step));
				assertXemFee(amount, 0, weightWithFeeUnit(Amount.fromNem(fee)));
				assertXemFee(amount + 1, 0, weightWithFeeUnit(Amount.fromNem(fee)));
				assertXemFee(amount + 100, 0, weightWithFeeUnit(Amount.fromNem(fee)));
				assertXemFee(amount + step - 1, 0, weightWithFeeUnit(Amount.fromNem(fee)));
			}
		}

		@Test
		public void feeIsCappedAtTwentyFiveXem() {
			// Assert:
			assertXemFee(250_000, 0, weightWithFeeUnit(Amount.fromNem(25)));
			assertXemFee(250_001, 0, weightWithFeeUnit(Amount.fromNem(25)));
			assertXemFee(500_000, 0, weightWithFeeUnit(Amount.fromNem(25)));
			assertXemFee(1_000_000, 0, weightWithFeeUnit(Amount.fromNem(25)));
			assertXemFee(10_000_000, 0, weightWithFeeUnit(Amount.fromNem(25)));
			assertXemFee(100_000_000, 0, weightWithFeeUnit(Amount.fromNem(25)));
			assertXemFee(1_000_000_000, 0, weightWithFeeUnit(Amount.fromNem(25)));
		}

		@Test
		public void feeIsCalculatedCorrectlyForLargeTransfersWithMessages() {
			// Assert:
			assertXemFee(10000, 96, weightWithFeeUnit(Amount.fromNem(1 + 4)));
			assertXemFee(100000, 128, weightWithFeeUnit(Amount.fromNem(10 + 5)));
			assertXemFee(1000000, 96, weightWithFeeUnit(Amount.fromNem(25 + 4)));
			assertXemFee(2000000, 128, weightWithFeeUnit(Amount.fromNem(25 + 5)));
		}

		@Test
		public void feeIsCalculatedCorrectlyForTransferWithSmallestMessage() {
			// Assert:
			assertXemFee(1200, 1, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 1)));
		}

		@Test
		public void feeIsCalculatedCorrectlyForTransferNearMessageStepIncreases() {
			// Assert:
			assertXemFee(1200, 31, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 1)));
			assertXemFee(1200, 32, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 2)));
			assertXemFee(1200, 33, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 2)));

			assertXemFee(1200, 63, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 2)));
			assertXemFee(1200, 64, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 3)));
			assertXemFee(1200, 65, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 3)));
		}

		@Test
		public void feeIsCalculatedCorrectlyForTransferWithLargeMessage() {
			// Assert:
			assertXemFee(1200, 96, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 4)));
			assertXemFee(1200, 128, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 5)));
			assertXemFee(1200, 256, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 9)));
			assertXemFee(1200, 320, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 11)));
		}

		@Test
		public void messageFeeIsBasedOnEncodedSize() {
			// Assert:
			assertMessageFee(96, 128, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 4)));
			assertMessageFee(128, 96, weightWithFeeUnit(Amount.fromNem(MIN_TRANSFER_FEE + 5)));
		}

		// region mosaic transfers

		// region small business mosaics

		// - A so-called small business mosaic has divisibility of 0 and a max supply of 10000
		// - It is always charged 1 xem fee no matter how many mosaics are transferred
		// - Mosaic 'small business x' has divisibility 0 and supply x * 1000 for x > 0
		// - Mosaic 'small business 0' has divisibility 1 and supply 1000

		@Test
		public void transfersOfManyMosaicsWithDivisibilityZeroAndLowSupplyHaveMinimumFee() {
			// Arrange:
			for (int i = 1; i <= 10; ++i) {
				// Arrange:
				final TransferTransaction transaction = createTransfer(1, null);
				final MosaicId mosaicId = Utils.createMosaicId("foo", String.format("small business %d", i));
				transaction.getAttachment().addMosaic(mosaicId, Quantity.fromValue(i * 1_000));

				// Assert:
				assertTransactionFee(transaction, weightWithFeeUnit(Amount.fromNem(1)));
			}
		}

		@Test
		public void transfersOfManyMosaicsWithDivisibilityZeroAndSupplyAboveTenThousandDoesNotHaveMinimumFee() {
			// Arrange:
			// - A supply of 11000 means it is not a small business mosaic
			final TransferTransaction transaction = createTransfer(1, null);
			final MosaicId mosaicId = Utils.createMosaicId("foo", "small business 11");
			transaction.getAttachment().addMosaic(mosaicId, Quantity.fromValue(1_000));

			// Assert:
			assertTransactionFee(transaction, weightWithFeeUnit(Amount.fromNem(4)));
		}

		@Test
		public void transfersOfManyMosaicsWithDivisibilityLargerThanZeroDoesNotHaveMinimumFee() {
			// Arrange:
			// - A divisibility of 1 means it is not a small business mosaic
			final TransferTransaction transaction = createTransfer(1, null);
			final MosaicId mosaicId = Utils.createMosaicId("foo", "small business 0");
			transaction.getAttachment().addMosaic(mosaicId, Quantity.fromValue(1_000));

			// Assert:
			assertTransactionFee(transaction, weightWithFeeUnit(Amount.fromNem(3)));
		}

		// endregion

		// region other mosaics

		// mosaic definition data used for the following tests: supply = 100_000_000, divisibility = 3
		// supply ratio: 8_999_999_999 / 100_000_000 ≈ 90
		// divisibility ratio = 1_000_000 / 1_000 = 1000
		// 1000 / 90 = 11.11..., so transferring a quantity of 12 is roughly like transferring 1 xem
		// Adjustment for the fee is 9 xem due to the lower supply and divisibility

		@Test
		public void feeIsCalculatedCorrectlyNearMosaicTransferStepIncreases() {
			// Assert:
			// Minimum fee for low amounts
			assertSingleMosaicFee(1, 0, 12L, weightWithFeeUnit(Amount.fromNem(1))); // ~ 1 xem
			assertSingleMosaicFee(1, 0, 111_000L, weightWithFeeUnit(Amount.fromNem(1))); // ~9_999 xem

			// 1 -> 2 roughly at 1222.222 units
			assertSingleMosaicFee(1, 0, 1_222_000L, weightWithFeeUnit(Amount.fromNem(1)));
			assertSingleMosaicFee(1, 0, 1_223_000L, weightWithFeeUnit(Amount.fromNem(2))); // ~ 110_000 xem
			assertSingleMosaicFee(1, 0, 1_224_000L, weightWithFeeUnit(Amount.fromNem(2)));

			// 2 -> 3 roughly at 1333.333 units
			assertSingleMosaicFee(1, 0, 1_333_000L, weightWithFeeUnit(Amount.fromNem(2)));
			assertSingleMosaicFee(1, 0, 1_334_000L, weightWithFeeUnit(Amount.fromNem(3))); // ~ 120_000 xem
			assertSingleMosaicFee(1, 0, 1_335_000L, weightWithFeeUnit(Amount.fromNem(3)));

			// 3 -> 4 roughly at 1444.444 units
			assertSingleMosaicFee(1, 0, 1_444_000L, weightWithFeeUnit(Amount.fromNem(3)));
			assertSingleMosaicFee(1, 0, 1_445_000L, weightWithFeeUnit(Amount.fromNem(4))); // ~ 130_000 xem
			assertSingleMosaicFee(1, 0, 1_446_000L, weightWithFeeUnit(Amount.fromNem(4)));
		}

		@Test
		public void feeIsCalculatedCorrectlyForLargeMosaicTransfers() {
			// Assert:
			assertSingleMosaicFee(1, 0, 2_112_000L, weightWithFeeUnit(Amount.fromNem(10))); // ~ 190_000 xem
			assertSingleMosaicFee(1, 0, 2_445_000L, weightWithFeeUnit(Amount.fromNem(13))); // ~ 220_000 xem
			assertSingleMosaicFee(1, 0, 2_778_000L, weightWithFeeUnit(Amount.fromNem(16))); // ~ 250_000 xem
			assertSingleMosaicFee(1, 0, 3_000_000L, weightWithFeeUnit(Amount.fromNem(16)));
			assertSingleMosaicFee(1, 0, 10_000_000L, weightWithFeeUnit(Amount.fromNem(16)));
			assertSingleMosaicFee(1, 0, 100_000_000L, weightWithFeeUnit(Amount.fromNem(16)));
		}

		@Test
		public void feeIsCalculatedCorrectlyForMosaicTransfersWithAmountOtherThanOne() {
			// Assert:
			assertSingleMosaicFee(1, 0, 2_112_000L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(2, 0, 1_056_000L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(5, 0, 422_400L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(10, 0, 211_200L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(100, 0, 21_120L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(1_000, 0, 2_112L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(21_120, 0, 100L, weightWithFeeUnit(Amount.fromNem(10)));
			assertSingleMosaicFee(2_112_000, 0, 1L, weightWithFeeUnit(Amount.fromNem(10)));
		}

		@Test
		public void messageFeeIsAddedToMosaicTransferFee() {
			// Assert:
			assertSingleMosaicFee(1, 15, 2_112_000L, weightWithFeeUnit(Amount.fromNem(10 + 1)));
			assertSingleMosaicFee(1, 32, 2_112_000L, weightWithFeeUnit(Amount.fromNem(10 + 2)));
			assertSingleMosaicFee(1, 96, 2_112_000L, weightWithFeeUnit(Amount.fromNem(10 + 4)));
			assertSingleMosaicFee(1, 160, 2_112_000L, weightWithFeeUnit(Amount.fromNem(10 + 6)));
		}

		@Test
		public void feesAreAddedWhenTransferringSeveralMosaics() {
			// Arrange: mosaic definitions are (100M, 3), (200M, 4), (300M, 5)
			final Transaction transaction = createTransferWithMosaics(1, 0, 2_000_000L, 50_000_000L, 800_000_000L);

			// Assert:
			assertTransactionFee(transaction, weightWithFeeUnit(Amount.fromNem(8 + 16 + 19)));
		}

		@Test
		public void feesForMosaicTransfersAreOneIfMosaicSupplyIsZero() {
			// Arrange:
			final TransferTransaction transaction = createTransfer(5, null);
			final MosaicId mosaicId = Utils.createMosaicId("foo", "zero supply");
			transaction.getAttachment().addMosaic(mosaicId, Quantity.fromValue(1_000_000));

			// Assert:
			// - zero supply means zero xem equivalent and therefore a fee of 1 xem
			assertTransactionFee(transaction, weightWithFeeUnit(Amount.fromNem(1)));
		}

		@Test
		public void feesCannotBeCalculatedForUnknownMosaic() {
			// Arrange:
			final TransferTransaction transaction = createTransfer(1L, null);
			transaction.getAttachment().addMosaic(Utils.createMosaic("foo", "tokens"));
			final TransactionFeeCalculator calculator = createCalculator();

			// Act:
			ExceptionAssert.assertThrows(
					v -> calculator.calculateMinimumFee(transaction),
					IllegalArgumentException.class);
		}

		// endregion
	}

	//endregion

	//endregion
}
