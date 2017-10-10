package org.nem.core.model;

import org.nem.core.model.mosaic.*;
import org.nem.core.model.primitive.*;

import java.math.BigInteger;

/**
 * Implementation for calculating and validating transaction fees before the fee fork.
 */
public class TransactionFeeCalculatorBeforeFork implements TransactionFeeCalculator {
	private static final Amount FEE_UNIT = Amount.fromNem(2);
	private static final long FEE_UNIT_NUM_NEM = FEE_UNIT.getNumNem();
	private static final int FEE_MULTIPLIER = 3;

	private final MosaicFeeInformationLookup mosaicFeeInformationLookup;

	/**
	 * Creates a default transaction fee calculator.
	 */
	public TransactionFeeCalculatorBeforeFork() {
		this(id -> null);
	}

	/**
	 * Creates a default transaction fee calculator.
	 *
	 * @param mosaicFeeInformationLookup The mosaic fee information lookup.
	 */
	public TransactionFeeCalculatorBeforeFork(final MosaicFeeInformationLookup mosaicFeeInformationLookup) {
		this.mosaicFeeInformationLookup = mosaicFeeInformationLookup;
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
				return this.calculateMinimumFee((TransferTransaction)transaction);

			case TransactionTypes.MULTISIG_AGGREGATE_MODIFICATION:
				return calculateMinimumFee((MultisigAggregateModificationTransaction)transaction);

			case TransactionTypes.PROVISION_NAMESPACE:
			case TransactionTypes.MOSAIC_DEFINITION_CREATION:
			case TransactionTypes.MOSAIC_SUPPLY_CHANGE:
				return FEE_UNIT.multiply(FEE_MULTIPLIER).multiply(18);
		}

		return FEE_UNIT.multiply(FEE_MULTIPLIER);
	}

	private Amount calculateMinimumFee(final TransferTransaction transaction) {
		final long messageFee = null == transaction.getMessage() ? 0 : Math.max(1, transaction.getMessageLength() / 16) * FEE_UNIT_NUM_NEM;
		if (transaction.getAttachment().getMosaics().isEmpty()) {
			final long numXem = transaction.getAmount().getNumNem();
			final long transferFee = calculateXemTransferFee(numXem);
			return Amount.fromNem(messageFee + transferFee);
		}

		final long transferFee = transaction.getAttachment().getMosaics().stream()
				.map(m -> {
					final MosaicFeeInformation information = this.mosaicFeeInformationLookup.findById(m.getMosaicId());
					if (null == information) {
						throw new IllegalArgumentException(String.format("unable to find fee information for '%s'", m.getMosaicId()));
					}

					return calculateXemEquivalent(transaction.getAmount(), m, information.getSupply(), information.getDivisibility());
				})
				.map(TransactionFeeCalculatorBeforeFork::calculateXemTransferFee)
				.reduce(0L, Long::sum);
		return Amount.fromNem(messageFee + (transferFee * 5) / 4);
	}

	private static long calculateXemTransferFee(final long numXem) {
		final long smallTransferPenalty = FEE_UNIT.multiply(5).getNumNem() - numXem;
		final long largeTransferFee = (long)(Math.atan(numXem / 150000.) * FEE_MULTIPLIER * 33);
		return Math.max(smallTransferPenalty, Math.max(FEE_UNIT_NUM_NEM, largeTransferFee));
	}

	private static long calculateXemEquivalent(final Amount amount, final Mosaic mosaic, final Supply supply, final int divisibility) {
		if (Supply.ZERO.equals(supply)) {
			return 0;
		}

		return BigInteger.valueOf(MosaicConstants.MOSAIC_DEFINITION_XEM.getProperties().getInitialSupply())
				.multiply(BigInteger.valueOf(mosaic.getQuantity().getRaw()))
				.multiply(BigInteger.valueOf(amount.getNumMicroNem()))
				.divide(BigInteger.valueOf(supply.getRaw()))
				.divide(BigInteger.TEN.pow(divisibility + 6))
				.longValue();
	}

	private static Amount calculateMinimumFee(final MultisigAggregateModificationTransaction transaction) {
		final int numModifications = transaction.getCosignatoryModifications().size();
		final int minCosignatoriesFee = null == transaction.getMinCosignatoriesModification() ? 0 : FEE_MULTIPLIER;
		return FEE_UNIT.multiply(5 + FEE_MULTIPLIER * numModifications + minCosignatoriesFee);
	}

	/**
	 * Determines whether the fee for the transaction at the specified block height is valid.
	 *
	 * @param transaction The transaction.
	 * @param blockHeight The block height.
	 * @return true if the transaction fee is valid; false otherwise.
	 */
	@Override
	public boolean isFeeValid(final Transaction transaction, final BlockHeight blockHeight) {
		final Amount minimumFee = this.calculateMinimumFee(transaction);
		final long FORK_HEIGHT = 92000;
		final Amount maxCacheFee = Amount.fromNem(1000); // 1000 xem is the maximum fee that helps push a transaction into the cache
		switch (transaction.getType()) {
			case TransactionTypes.MULTISIG_SIGNATURE:
				// minimumFee <= multisig signatures fee <= 1000
				// reason: during spam attack cosignatories must be able to get their signature into the cache.
				//         it is limited in order for the last cosignatory not to be able to drain the multisig account
				return 0 <= transaction.getFee().compareTo(minimumFee) && 0 >= transaction.getFee().compareTo(maxCacheFee);
		}

		return transaction.getFee().compareTo(minimumFee) >= 0;
	}
}
