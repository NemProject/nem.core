package org.nem.core.model;

import org.nem.core.model.mosaic.MosaicFeeInformationLookup;
import org.nem.core.model.primitive.Amount;

/**
 * Implementation for calculating and validating transaction fees since the second fee fork.
 */
public class FeeUnitAwareTransactionFeeCalculator extends AbstractTransactionFeeCalculator implements TransactionFeeCalculator {
	private final Amount feeUnit;

	/**
	 * Creates a transaction fee calculator.
	 *
	 * @param feeUnit The fee unit to use.
	 * @param mosaicFeeInformationLookup The mosaic fee information lookup.
	 */
	public FeeUnitAwareTransactionFeeCalculator(final Amount feeUnit, final MosaicFeeInformationLookup mosaicFeeInformationLookup) {
		super(mosaicFeeInformationLookup);
		this.feeUnit = feeUnit;
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
				return this.weightWithFeeUnit(this.calculateMinimumFeeImpl((TransferTransaction)transaction));

			case TransactionTypes.MULTISIG_AGGREGATE_MODIFICATION:
				return this.weightWithFeeUnit(Amount.fromNem(10));

			default:
				return this.weightWithFeeUnit(Amount.fromNem(3));
		}
	}

	private Amount weightWithFeeUnit(final Amount fee) {
		return Amount.fromMicroNem(this.feeUnit.getNumMicroNem() * fee.getNumNem());
	}
}
