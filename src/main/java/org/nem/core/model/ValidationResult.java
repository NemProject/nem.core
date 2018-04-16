package org.nem.core.model;

import java.util.Iterator;

/**
 * Possible validation results.
 */
public enum ValidationResult {

	//region general

	/**
	 * Validation is neutral.
	 */
	NEUTRAL(0),

	/**
	 * Validation succeeded.
	 */
	SUCCESS(1),

	/**
	 * Validation failed for an unknown reason.
	 */
	FAILURE_UNKNOWN(2),

	/**
	 * Validation failed because the deadline passed.
	 */
	FAILURE_PAST_DEADLINE(3),

	/**
	 * Validation failed because the deadline is too far in the future.
	 */
	FAILURE_FUTURE_DEADLINE(4),

	/**
	 * Validation failed because the account had an insufficient balance.
	 */
	FAILURE_INSUFFICIENT_BALANCE(5),

	/**
	 * Validation failed because the message is too large.
	 */
	FAILURE_MESSAGE_TOO_LARGE(6),

	/**
	 * Validation failed because the transaction hash is already known.
	 */
	FAILURE_HASH_EXISTS(7),

	/**
	 * Validation failed because the verification of the signature failed.
	 */
	FAILURE_SIGNATURE_NOT_VERIFIABLE(8),

	/**
	 * Validation failed because the time stamp is too far in the past.
	 */
	FAILURE_TIMESTAMP_TOO_FAR_IN_PAST(9),

	/**
	 * Validation failed because the time stamp is too far in the future.
	 */
	FAILURE_TIMESTAMP_TOO_FAR_IN_FUTURE(10),

	/**
	 * Validation failed because the block had an ineligible signer.
	 * This usually occurs when remote harvesting is in the process of being activated or deactivated.
	 */
	FAILURE_INELIGIBLE_BLOCK_SIGNER(11),

	/**
	 * Validation failed because the entity cannot be used because the nodes are out of sync.
	 */
	FAILURE_ENTITY_UNUSABLE_OUT_OF_SYNC(12),

	/**
	 * Validation failed because the chain score is inferior to our score.
	 */
	FAILURE_CHAIN_SCORE_INFERIOR(13),

	/**
	 * Validation failed because the chain could not be validated.
	 */
	FAILURE_CHAIN_INVALID(14),

	/**
	 * Validation failed because there are too many transactions in a block.
	 */
	FAILURE_TOO_MANY_TRANSACTIONS(15),

	/**
	 * Validation failed because a block contained a self-signed transaction.
	 */
	FAILURE_SELF_SIGNED_TRANSACTION(16),

	/**
	 * Validation failed because a transaction has an insufficient fee.
	 */
	FAILURE_INSUFFICIENT_FEE(17),

	/**
	 * Validation failed because a transaction originated from the nemesis account after the nemesis block.
	 */
	FAILURE_NEMESIS_ACCOUNT_TRANSACTION_AFTER_NEMESIS_BLOCK(18),

	/**
	 * Transaction was rejected because the debtor is not allowed to put another transaction into the cache.
	 */
	FAILURE_TRANSACTION_CACHE_TOO_FULL(19),

	/**
	 * Entity was rejected because it has the wrong network specified.
	 */
	FAILURE_WRONG_NETWORK(20),

	/**
	 * Block was rejected because it was harvested by a blocked account (typically a reserved NEM fund).
	 */
	FAILURE_CANNOT_HARVEST_FROM_BLOCKED_ACCOUNT(21),

	/**
	 * Validation failed because an entity had an invalid version.
	 */
	FAILURE_ENTITY_INVALID_VERSION(22),

	//endregion

	//region forks 4x

	/**
	 * Validation failed because V2 multisig aggregate modification transactions are not allowed before the (first) fork height.
	 */
	FAILURE_MULTISIG_V2_AGGREGATE_MODIFICATION_BEFORE_FORK(41),

	/**
	 * Validation failed, because new transaction types (namespace, mosaic creation, mosaic supply, transfer mosaic)
	 * are not allowed before the second fork height.
	 */
	FAILURE_TRANSACTION_BEFORE_SECOND_FORK(42),

	//endregion

	//region importance 6x

	/**
	 * Validation failed because remote harvesting account has a pre-existing balance transfer.
	 */
	FAILURE_DESTINATION_ACCOUNT_IN_USE(62),

	/**
	 * Validation failed because previous importance transfer change is in progress.
	 */
	FAILURE_IMPORTANCE_TRANSFER_IN_PROGRESS(63),

	/**
	 * Validation failed because importance transfer activation was attempted while already activated.
	 */
	FAILURE_IMPORTANCE_TRANSFER_NEEDS_TO_BE_DEACTIVATED(64),

	/**
	 * Validation failed because importance transfer deactivation was attempted while already deactivated.
	 */
	FAILURE_IMPORTANCE_TRANSFER_IS_NOT_ACTIVE(65),

	/**
	 * Validation failed because transaction is using remote account in an improper way.
	 */
	FAILURE_TRANSACTION_NOT_ALLOWED_FOR_REMOTE(66),

	//endregion

	//region multisig 7x 8x

	/**
	 * Validation failed because signer is not a cosigner of given multisig account.
	 */
	FAILURE_MULTISIG_NOT_A_COSIGNER(71),

	/**
	 * Validation failed because the cosignatories attached to a multisig transaction were invalid.
	 */
	FAILURE_MULTISIG_INVALID_COSIGNERS(72),

	/**
	 * Validation failed because a multisig signature was not associated with any known multisig transaction.
	 */
	FAILURE_MULTISIG_NO_MATCHING_MULTISIG(73),

	/**
	 * Validation failed because multisig account tried to make transaction that is not allowed.
	 */
	FAILURE_TRANSACTION_NOT_ALLOWED_FOR_MULTISIG(74),

	/**
	 * Validation failed because signer is already a cosigner of given multisig account.
	 */
	FAILURE_MULTISIG_ALREADY_A_COSIGNER(75),

	/**
	 * Validation failed because a multisig signature is attached to an incorrect multisig transaction.
	 */
	FAILURE_MULTISIG_MISMATCHED_SIGNATURE(76),

	/**
	 * Validation failed because a multisig modification contained multiple deletes.
	 */
	FAILURE_MULTISIG_MODIFICATION_MULTIPLE_DELETES(77),

	/**
	 * Validation failed because a multisig modification contained redundant cosignatory modifications.
	 */
	FAILURE_MULTISIG_MODIFICATION_REDUNDANT_MODIFICATIONS(78),

	/**
	 * Validation failed because conflicting multisig modification is present.
	 */
	FAILURE_CONFLICTING_MULTISIG_MODIFICATION(79),

	/**
	 * Validation failed because a multisig modification would result in a multisig account having too many cosigners.
	 */
	FAILURE_TOO_MANY_MULTISIG_COSIGNERS(80),

	/**
	 * Validation failed because a multisig modification would result in a multisig account being a cosigner.
	 */
	FAILURE_MULTISIG_ACCOUNT_CANNOT_BE_COSIGNER(81),

	/**
	 * Validation failed because the minimum number of cosignatories is larger than the number of cosignatories.
	 */
	FAILURE_MULTISIG_MIN_COSIGNATORIES_OUT_OF_RANGE(82),

	//endregion

	//region block chain validator 10x 11x

	/**
	 * Validation failed because received chain has too many blocks.
	 */
	FAILURE_MAX_CHAIN_SIZE_EXCEEDED(101),

	/**
	 * Validation failed because a block was received with an unexpected height.
	 */
	FAILURE_BLOCK_UNEXPECTED_HEIGHT(102),

	/**
	 * Validation failed because an unverifiable block was received.
	 */
	FAILURE_BLOCK_UNVERIFIABLE(103),

	/**
	 * Validation failed because a block was received that is not a hit.
	 */
	FAILURE_BLOCK_NOT_HIT(104),

	/**
	 * Validation failed because an unverifiable transaction was received.
	 */
	FAILURE_TRANSACTION_UNVERIFIABLE(105),

	/**
	 * Validation failed because an incoming chain contained a transaction more than once.
	 */
	FAILURE_TRANSACTION_DUPLICATE_IN_CHAIN(106),

	//endregion

	//region namespace 12x

	/**
	 * Validation failed because the namespace is unknown.
	 */
	FAILURE_NAMESPACE_UNKNOWN(121),

	/**
	 * Validation failed because the namespace already exists.
	 */
	FAILURE_NAMESPACE_ALREADY_EXISTS(122),

	/**
	 * Validation failed because the namespace has expired.
	 */
	FAILURE_NAMESPACE_EXPIRED(123),

	/**
	 * Validation failed because the transaction signer is not the owner of the namespace.
	 */
	FAILURE_NAMESPACE_OWNER_CONFLICT(124),

	/**
	 * Validation failed because the name for the namespace is invalid.
	 */
	FAILURE_NAMESPACE_INVALID_NAME(125),

	/**
	 * Validation failed because the specified namespace rental fee sink is invalid.
	 */
	FAILURE_NAMESPACE_INVALID_RENTAL_FEE_SINK(126),

	/**
	 * Validation failed because the specified rental fee is invalid.
	 */
	FAILURE_NAMESPACE_INVALID_RENTAL_FEE(127),

	/**
	 * Validation failed because the provision was done too early.
	 */
	FAILURE_NAMESPACE_PROVISION_TOO_EARLY(128),

	/**
	 * Validation failed because the namespace contains a reserved part and is not claimable.
	 */
	FAILURE_NAMESPACE_NOT_CLAIMABLE(129),

	//endregion

	//region mosaic 14x

	/**
	 * Validation failed because the mosaic is unknown.
	 */
	FAILURE_MOSAIC_UNKNOWN(141),

	/**
	 * Validation failed because the modification of the existing mosaic is not allowed.
	 */
	FAILURE_MOSAIC_MODIFICATION_NOT_ALLOWED(142),

	/**
	 * Validation failed because the transaction signer is not the creator of the mosaic.
	 */
	FAILURE_MOSAIC_CREATOR_CONFLICT(143),

	/**
	 * Validation failed because the mosaic supply is immutable.
	 */
	FAILURE_MOSAIC_SUPPLY_IMMUTABLE(144),

	/**
	 * Validation failed because the maximum overall mosaic supply is exceeded.
	 */
	FAILURE_MOSAIC_MAX_SUPPLY_EXCEEDED(145),

	/**
	 * Validation failed because the resulting mosaic supply would be negative.
	 */
	FAILURE_MOSAIC_SUPPLY_NEGATIVE(146),

	/**
	 * Validation failed because the mosaic is not transferable.
	 */
	FAILURE_MOSAIC_NOT_TRANSFERABLE(147),

	/**
	 * Validation failed because the divisibility of the mosaic is violated.
	 */
	FAILURE_MOSAIC_DIVISIBILITY_VIOLATED(148),

	/**
	 * Validation failed because conflicting mosaic creation is present.
	 */
	FAILURE_CONFLICTING_MOSAIC_CREATION(149),

	/**
	 * Validation failed because the mosaic creation fee sink is invalid.
	 */
	FAILURE_MOSAIC_INVALID_CREATION_FEE_SINK(150),

	/**
	 * Validation failed because the specified creation fee is invalid.
	 */
	FAILURE_MOSAIC_INVALID_CREATION_FEE(151),

	/**
	 * Validation failed because a transfer transaction had too many attached mosaic transfers.
	 */
	FAILURE_TOO_MANY_MOSAIC_TRANSFERS(152),

	/**
	 * Validation failed because the mosaic levy is not transferable.
	 */
	FAILURE_MOSAIC_LEVY_NOT_TRANSFERABLE(153),

	/**
	 * Validation failed because the mosaic levy is unknown.
	 */
	FAILURE_MOSAIC_LEVY_UNKNOWN(154);

	//endregion

	private final int value;

	ValidationResult(final int value) {
		this.value = value;
	}

	/**
	 * Creates a validation result given a raw value.
	 *
	 * @param value The value.
	 * @return The validation result if the value is known.
	 * @throws IllegalArgumentException if the value is unknown.
	 */
	public static ValidationResult fromValue(final int value) {
		for (final ValidationResult result : values()) {
			if (result.getValue() == value) {
				return result;
			}
		}

		throw new IllegalArgumentException("Invalid validation result: " + value);
	}

	/**
	 * Gets a value indicating whether or not this result indicates success.
	 *
	 * @return true if this result indicates success.
	 */
	public boolean isSuccess() {
		return this == ValidationResult.SUCCESS;
	}

	/**
	 * Gets a value indicating whether or not this result indicates an error.
	 *
	 * @return true if this result indicates an error.
	 */
	public boolean isFailure() {
		switch (this) {
			case NEUTRAL:
				return false;
		}

		return !this.isSuccess();
	}

	/**
	 * Gets the underlying integer representation of the result.
	 *
	 * @return The underlying value.
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Aggregates an iterator of validation results. This implementation short-circuits on the first failure.
	 *
	 * @param resultIterator The results to aggregate.
	 * @return The aggregated result.
	 */
	public static ValidationResult aggregate(final Iterator<ValidationResult> resultIterator) {
		boolean isNeutral = false;
		while (resultIterator.hasNext()) {
			final ValidationResult result = resultIterator.next();
			if (ValidationResult.NEUTRAL == result) {
				isNeutral = true;
			} else if (ValidationResult.SUCCESS != result) {
				return result;
			}
		}

		return isNeutral ? ValidationResult.NEUTRAL : ValidationResult.SUCCESS;
	}

	/**
	 * Aggregates an iterator of validation results. This implementation does not short-circuit on the first failure.
	 *
	 * @param resultIterator The results to aggregate.
	 * @return The aggregated result.
	 */
	public static ValidationResult aggregateNoShortCircuit(final Iterator<ValidationResult> resultIterator) {
		boolean isNeutral = false;
		ValidationResult firstFailureResult = ValidationResult.SUCCESS;
		while (resultIterator.hasNext()) {
			final ValidationResult result = resultIterator.next();
			if (ValidationResult.NEUTRAL == result) {
				isNeutral = true;
			} else if (ValidationResult.SUCCESS != result && ValidationResult.SUCCESS == firstFailureResult) {
				firstFailureResult = result;
			}
		}

		if (firstFailureResult.isFailure()) {
			return firstFailureResult;
		}

		return isNeutral ? ValidationResult.NEUTRAL : ValidationResult.SUCCESS;
	}
}
