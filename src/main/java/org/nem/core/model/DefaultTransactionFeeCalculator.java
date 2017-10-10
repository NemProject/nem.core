package org.nem.core.model;

import org.nem.core.model.mosaic.MosaicFeeInformationLookup;
import org.nem.core.model.primitive.*;

import java.util.function.Supplier;

/**
 * Default implementation for calculating and validating transaction fees.
 */
public class DefaultTransactionFeeCalculator implements TransactionFeeCalculator {
	private static Amount FEE_UNIT_SECOND_FORK = Amount.fromMicroNem(50_000L);

	private final Supplier<BlockHeight> heightSupplier;
	private final BlockHeight[] forkHeights;
	private final TransactionFeeCalculator[] calculators;

	public DefaultTransactionFeeCalculator(
			final MosaicFeeInformationLookup mosaicFeeInformationLookup,
			final Supplier<BlockHeight> heightSupplier,
			final BlockHeight[] forkHeights) {
		this(
				heightSupplier,
				forkHeights,
				new TransactionFeeCalculator[] {
						new TransactionFeeCalculatorBeforeFork(mosaicFeeInformationLookup),
						new TransactionFeeCalculatorAfterFork(mosaicFeeInformationLookup),
						new FeeUnitAwareTransactionFeeCalculator(FEE_UNIT_SECOND_FORK, mosaicFeeInformationLookup)
				});
	}

	public DefaultTransactionFeeCalculator(
			final Supplier<BlockHeight> heightSupplier,
			final BlockHeight[] forkHeights,
			final TransactionFeeCalculator[] calculators) {
		if (forkHeights.length + 1 != calculators.length) {
			throw new RuntimeException("number of fee forks mismatch number of fee calculators");
		}

		this.heightSupplier = heightSupplier;
		this.forkHeights = forkHeights;
		this.calculators = calculators;
	}

	@Override
	public Amount calculateMinimumFee(Transaction transaction) {
		return this.getCalculator(this.heightSupplier.get()).calculateMinimumFee(transaction);
	}

	@Override
	public boolean isFeeValid(Transaction transaction, BlockHeight blockHeight) {
		return this.getCalculator(blockHeight).isFeeValid(transaction, blockHeight);
	}

	private TransactionFeeCalculator getCalculator(final BlockHeight blockHeight) {
		for (int i = 0; i < this.forkHeights.length; ++i) {
			if (blockHeight.compareTo(this.forkHeights[i]) < 0) {
				return this.calculators[i];
			}
		}

		return this.calculators[this.forkHeights.length];
	}
}
