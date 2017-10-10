package org.nem.core.model;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.nem.core.model.mosaic.*;
import org.nem.core.model.primitive.*;
import org.nem.core.test.*;

import java.util.stream.IntStream;

@RunWith(Enclosed.class)
public class TransactionFeeCalculatorBeforeForkTest extends AbstractTransactionFeeCalculatorTest {
	private static final long FEE_UNIT = 2;

	@BeforeClass
	public static void setup() {
		DEFAULT_HEIGHT = new BlockHeight(100);
		final MosaicFeeInformationLookup lookup = AbstractTransactionFeeCalculatorTest.createMosaicFeeInformationLookup();
		NemGlobals.setTransactionFeeCalculator(new DefaultTransactionFeeCalculator(
				lookup,
				() -> DEFAULT_HEIGHT,
				new BlockHeight[] { DEFAULT_HEIGHT.next(), new BlockHeight(1_000_000_000L) }));
		setNamespaceAndMosaicRelatedDefaultFee(108_000_000);
		setTransactionDefaultFee(6_000_000);
		setMultisigSignatureMinimumFee(6_000_000);
	}

	@AfterClass
	public static void teardown() {
		Utils.resetGlobals();
	}

	//region calculateMinimumFee

	//region transfer

	public static class TransferMinimumFeeCalculation {
		private static final long SMALL_TRANSFER_PENALTY = 10;
		private static final long MIN_TRANSFER_FEE = FEE_UNIT;

		@Test
		public void feeIsCalculatedCorrectlyForEmptyTransfer() {
			// Assert:
			assertXemFee(0, 0, Amount.fromNem(SMALL_TRANSFER_PENALTY));
		}

		@Test
		public void feeIsCalculatedCorrectlyForPenalizedSmallTransfers() {
			// Assert:
			for (int i = 1; i < 9; ++i) {
				assertXemFee(i, 0, Amount.fromNem(SMALL_TRANSFER_PENALTY - i));
			}
		}

		@Test
		public void feeIsCalculatedCorrectlyForNonPenalizedSmallTransfers() {
			// Assert:
			for (int i = 9; i < 20; ++i) {
				assertXemFee(i, 0, Amount.fromNem(MIN_TRANSFER_FEE));
			}
		}

		@Test
		public void feeIsCalculatedCorrectlyNearTransferStepIncreases() {
			// Assert:
			assertXemFee(4546, 0, Amount.fromNem(2));
			assertXemFee(4547, 0, Amount.fromNem(3));
			assertXemFee(4548, 0, Amount.fromNem(3));

			assertXemFee(6063, 0, Amount.fromNem(3));
			assertXemFee(6064, 0, Amount.fromNem(4));
			assertXemFee(6065, 0, Amount.fromNem(4));
		}

		@Test
		public void feeIsCalculatedCorrectlyForLargeTransfers() {
			// Assert:
			assertXemFee(10000, 0, Amount.fromNem(6));
			assertXemFee(100000, 0, Amount.fromNem(58));
			assertXemFee(1000000, 0, Amount.fromNem(140));
			assertXemFee(2000000, 0, Amount.fromNem(148));
		}

		@Test
		public void feeIsCalculatedCorrectlyForLargeTransfersWithMessages() {
			// Assert:
			assertXemFee(10000, 96, Amount.fromNem(6 + 12));
			assertXemFee(100000, 128, Amount.fromNem(58 + 16));
			assertXemFee(1000000, 96, Amount.fromNem(140 + 12));
			assertXemFee(2000000, 128, Amount.fromNem(148 + 16));
		}

		@Test
		public void feeIsCalculatedCorrectlyForTransferWithSmallestMessage() {
			// Assert:
			assertXemFee(1200, 1, Amount.fromNem(MIN_TRANSFER_FEE + 2));
		}

		@Test
		public void feeIsCalculatedCorrectlyForTransferNearMessageStepIncreases() {
			// Assert:
			assertXemFee(1200, 31, Amount.fromNem(MIN_TRANSFER_FEE + 2));
			assertXemFee(1200, 32, Amount.fromNem(MIN_TRANSFER_FEE + 4));
			assertXemFee(1200, 33, Amount.fromNem(MIN_TRANSFER_FEE + 4));

			assertXemFee(1200, 63, Amount.fromNem(MIN_TRANSFER_FEE + 6));
			assertXemFee(1200, 64, Amount.fromNem(MIN_TRANSFER_FEE + 8));
			assertXemFee(1200, 65, Amount.fromNem(MIN_TRANSFER_FEE + 8));
		}

		@Test
		public void feeIsCalculatedCorrectlyForTransferWithLargeMessage() {
			// Assert:
			assertXemFee(1200, 96, Amount.fromNem(MIN_TRANSFER_FEE + 12));
			assertXemFee(1200, 128, Amount.fromNem(MIN_TRANSFER_FEE + 16));
			assertXemFee(1200, 256, Amount.fromNem(MIN_TRANSFER_FEE + 32));
		}

		@Test
		public void messageFeeIsBasedOnEncodedSize() {
			// Assert:
			assertMessageFee(96, 128, Amount.fromNem(SMALL_TRANSFER_PENALTY + 12));
			assertMessageFee(128, 96, Amount.fromNem(SMALL_TRANSFER_PENALTY + 16));
		}

		// region mosaic transfers

		// mosaic definition data used for the following tests: supply = 100_000_000, divisibility = 3
		// supply ratio: 8_999_999_999 / 100_000_000 ≈ 90
		// divisibility ratio = 1_000_000 / 1_000 = 1000
		// 1000 / 90 = 11.11..., so transferring a quantity of 12 is roughly like transferring 1 xem
		// In comparison to a xem transfer, equivalent mosaic transfers have 25% higher fees (rounded)

		// note that xem as a mosaic transfer is also 25% higher than a regular xem transfer because
		// it takes up more space in the db; this is validated by
		// feesForMosaicTransfersAreTwentyFivePercentHigherThanEquivalentXemTransfers

		@Test
		public void feeIsCalculatedCorrectlyForPenalizedSmallMosaicTransfers() {
			// Assert:
			for (int i = 1; i < 9; ++i) {
				assertSingleMosaicFee(1, 0, i * 12L, Amount.fromNem((SMALL_TRANSFER_PENALTY - i) * 5 / 4));
			}
		}

		@Test
		public void feeIsCalculatedCorrectlyForNonPenalizedSmallMosaicTransfers() {
			// Assert:
			for (int i = 9; i < 20; ++i) {
				assertSingleMosaicFee(1, 0, i * 12L, Amount.fromNem(MIN_TRANSFER_FEE * 5 / 4));
			}
		}

		@Test
		public void feeIsCalculatedCorrectlyNearMosaicTransferStepIncreases() {
			// Assert:
			// 2 -> 3 roughly at 50.5 units
			assertSingleMosaicFee(1, 0, 50_000L, Amount.fromNem(2 * 5 / 4));
			assertSingleMosaicFee(1, 0, 51_000L, Amount.fromNem(3 * 5 / 4));
			assertSingleMosaicFee(1, 0, 52_000L, Amount.fromNem(3 * 5 / 4));

			// 3 -> 5 roughly at 67.3 units
			assertSingleMosaicFee(1, 0, 67_000L, Amount.fromNem(3 * 5 / 4));
			assertSingleMosaicFee(1, 0, 68_000L, Amount.fromNem(4 * 5 / 4));
			assertSingleMosaicFee(1, 0, 69_000L, Amount.fromNem(4 * 5 / 4));
		}

		@Test
		public void feeIsCalculatedCorrectlyForLargeMosaicTransfers() {
			// Assert:
			assertSingleMosaicFee(1, 0, 111_000L, Amount.fromNem(6 * 5 / 4));
			assertSingleMosaicFee(1, 0, 1_110_000L, Amount.fromNem(58 * 5 / 4));
			assertSingleMosaicFee(1, 0, 11_100_000L, Amount.fromNem(140 * 5 / 4));
			assertSingleMosaicFee(1, 0, 22_200_000L, Amount.fromNem(148 * 5 / 4));
		}

		@Test
		public void feeIsCalculatedCorrectlyForMosaicTransfersWithAmountOtherThanOne() {
			// Assert:
			assertSingleMosaicFee(1, 0, 1_000_000L, Amount.fromNem(53 * 5 / 4));
			assertSingleMosaicFee(2, 0, 500_000L, Amount.fromNem(53 * 5 / 4));
			assertSingleMosaicFee(5, 0, 200_000L, Amount.fromNem(53 * 5 / 4));
			assertSingleMosaicFee(10, 0, 100_000L, Amount.fromNem(53 * 5 / 4));
			assertSingleMosaicFee(500, 0, 2_000L, Amount.fromNem(53 * 5 / 4));
			assertSingleMosaicFee(10_000, 0, 100L, Amount.fromNem(53 * 5 / 4));
			assertSingleMosaicFee(1_000_000, 0, 1L, Amount.fromNem(53 * 5 / 4));
		}

		@Test
		public void messageFeeIsAddedToMosaicTransferFee() {
			// Assert:
			assertSingleMosaicFee(1, 31, 1_000_000L, Amount.fromNem(53 * 5 / 4 + 2));
			assertSingleMosaicFee(1, 47, 1_000_000L, Amount.fromNem(53 * 5 / 4 + 4));
			assertSingleMosaicFee(1, 63, 1_000_000L, Amount.fromNem(53 * 5 / 4 + 6));
			assertSingleMosaicFee(1, 79, 1_000_000L, Amount.fromNem(53 * 5 / 4 + 8));
		}

		@Test
		public void feesAreAddedWhenTransferringSeveralMosaics() {
			// Arrange: mosaic definitions are (100M, 3), (200M, 4), (300M, 5)
			final Transaction transaction = createTransferWithMosaics(1, 0, 111_000L, 11_100_000L, 1_110_000_000L);

			// Assert:
			assertTransactionFee(transaction, Amount.fromNem((6 + 31 + 113) * 5 / 4));
		}

		@Test
		public void feesForMosaicTransfersAreTwentyFivePercentHigherThanEquivalentXemTransfers() {
			// Assert:
			IntStream.range(0, 100).forEach(i -> assertXemTransferToMosaicTransferFeeRatio(1000 * i));
		}

		@Test
		public void feesForMosaicTransfersAreTenIfMosaicSupplyIsZero() {
			// Arrange:
			final TransferTransaction transaction = createTransfer(5, null);
			final MosaicId mosaicId = Utils.createMosaicId("foo", "zero supply");
			transaction.getAttachment().addMosaic(mosaicId, Quantity.fromValue(1_000_000));

			// Assert:
			// - zero supply means zero xem equivalent and therefore a penalty fee of 10 * 1.25 xem = 12 xem
			assertTransactionFee(transaction, Amount.fromNem(12));
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
}