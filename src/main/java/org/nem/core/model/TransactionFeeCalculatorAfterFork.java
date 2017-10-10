package org.nem.core.model;

import org.nem.core.model.mosaic.MosaicFeeInformationLookup;
import org.nem.core.model.primitive.Amount;

/**
 * Implementation for calculating and validating transaction fees after the first fee fork.
 */
public class TransactionFeeCalculatorAfterFork extends AbstractTransactionFeeCalculator implements TransactionFeeCalculator {
	private static final Amount FEE_UNIT = Amount.fromNem(2);
	private static final int FEE_MULTIPLIER = 3;

	/**
	 * Creates a transaction fee calculator.
	 *
	 * @param mosaicFeeInformationLookup The mosaic fee information lookup.
	 */
	public TransactionFeeCalculatorAfterFork(final MosaicFeeInformationLookup mosaicFeeInformationLookup) {
		super(mosaicFeeInformationLookup);
	}

	/**
	 * Calculates the minimum fee for the specified transaction at the specified block height.
	 *
	 * @param transaction The transaction.
	 * @return The minimum fee.
	 */
	@Override
	public Amount calculateMinimumFee(final Transaction transaction) {
		switch (transaction.getType()) {
			case TransactionTypes.TRANSFER:
				return this.calculateMinimumFeeImpl((TransferTransaction)transaction);

			case TransactionTypes.MULTISIG_AGGREGATE_MODIFICATION:
				return this.calculateMinimumFeeImpl((MultisigAggregateModificationTransaction)transaction);

			case TransactionTypes.PROVISION_NAMESPACE:
			case TransactionTypes.MOSAIC_DEFINITION_CREATION:
			case TransactionTypes.MOSAIC_SUPPLY_CHANGE:
				return FEE_UNIT.multiply(10);
		}

		return FEE_UNIT.multiply(FEE_MULTIPLIER);
	}

	private Amount calculateMinimumFeeImpl(final MultisigAggregateModificationTransaction transaction) {
		final int numModifications = transaction.getCosignatoryModifications().size();
		final int minCosignatoriesFee = null == transaction.getMinCosignatoriesModification() ? 0 : FEE_MULTIPLIER;
		return FEE_UNIT.multiply(5 + FEE_MULTIPLIER * numModifications + minCosignatoriesFee);
	}
}
