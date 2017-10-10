package org.nem.core.model;

import org.junit.Test;
import org.mockito.Mockito;
import org.nem.core.model.primitive.BlockHeight;

import java.util.function.Supplier;

public class DefaultTransactionFeeCalculatorTest {

	// region calculateMinimumFee

	@Test
	public void calculatorDelegatesToCorrectCalculatorForHeightsBeforeFirstForkHeight() {
		assertMinimumFeeCalculatorBeforeFirstFork(1L, 1234L);
		assertMinimumFeeCalculatorBeforeFirstFork(10L, 1234L);
		assertMinimumFeeCalculatorBeforeFirstFork(100L, 1234L);
		assertMinimumFeeCalculatorBeforeFirstFork(1000L, 1234L);
		assertMinimumFeeCalculatorBeforeFirstFork(1233L, 1234L);
	}

	@Test
	public void calculatorDelegatesToCorrectCalculatorAtFirstForkHeight() {
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(1L, 1L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(10L, 10L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(100L, 100L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(1000L, 1000L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(1234L, 1234L);
	}

	@Test
	public void calculatorDelegatesToCorrectCalculatorForHeightsAfterFirstForkHeightButBeforeSecondForkHeight() {
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(2L, 1L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(1235L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(2000L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(10000L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(1_000_000L, 1234L);
	}

	@Test
	public void calculatorDelegatesToCorrectCalculatorAtSecondForkHeight() {
		assertMinimumFeeCalculatorAtAndAfterSecondFork(1234L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterSecondFork(2000L, 2000L);
		assertMinimumFeeCalculatorAtAndAfterSecondFork(10000L, 10000L);
		assertMinimumFeeCalculatorAtAndAfterSecondFork(1_000_000L, 1_000_000L);
	}

	@Test
	public void calculatorDelegatesToCorrectCalculatorAfterSecondForkHeight() {
		assertMinimumFeeCalculatorAtAndAfterSecondFork(1235L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterSecondFork(2000L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterSecondFork(10000L, 1234L);
		assertMinimumFeeCalculatorAtAndAfterSecondFork(1_000_000L, 1234L);
	}

	private static void assertMinimumFeeCalculatorBeforeFirstFork(final long testHeight, final long forkHeight) {
		// Arrange:
		final TestContext context = new TestContext(
				() -> new BlockHeight(testHeight),
				new BlockHeight(forkHeight),
				new BlockHeight(999_999_999_999L));
		final Transaction transaction = Mockito.mock(TransferTransaction.class);

		// Act:
		context.calculator.calculateMinimumFee(transaction);

		// Assert:
		Mockito.verify(context.calculator1, Mockito.only()).calculateMinimumFee(transaction);
		Mockito.verify(context.calculator2, Mockito.never()).calculateMinimumFee(Mockito.any());
		Mockito.verify(context.calculator3, Mockito.never()).calculateMinimumFee(Mockito.any());
	}

	private static void assertMinimumFeeCalculatorAtAndAfterFirstForkButBeforeSecondFork(final long testHeight, final long forkHeight) {
		// Arrange:
		final TestContext context = new TestContext(
				() -> new BlockHeight(testHeight),
				new BlockHeight(forkHeight),
				new BlockHeight(999_999_999_999L));
		final Transaction transaction = Mockito.mock(Transaction.class);

		// Act:
		context.calculator.calculateMinimumFee(transaction);

		// Assert:
		Mockito.verify(context.calculator1, Mockito.never()).calculateMinimumFee(Mockito.any());
		Mockito.verify(context.calculator2, Mockito.only()).calculateMinimumFee(transaction);
		Mockito.verify(context.calculator3, Mockito.never()).calculateMinimumFee(Mockito.any());
	}

	private static void assertMinimumFeeCalculatorAtAndAfterSecondFork(final long testHeight, final long forkHeight) {
		// Arrange:
		final TestContext context = new TestContext(
				() -> new BlockHeight(testHeight),
				new BlockHeight(1000L),
				new BlockHeight(forkHeight));
		final Transaction transaction = Mockito.mock(Transaction.class);

		// Act:
		context.calculator.calculateMinimumFee(transaction);

		// Assert:
		Mockito.verify(context.calculator1, Mockito.never()).calculateMinimumFee(Mockito.any());
		Mockito.verify(context.calculator2, Mockito.never()).calculateMinimumFee(Mockito.any());
		Mockito.verify(context.calculator3, Mockito.only()).calculateMinimumFee(transaction);
	}

	// endregion

	// region isFeeValid

	@Test
	public void isFeeValidUsesCorrectCalculatorForHeightsBeforeFirstForkHeight() {
		assertIsFeeValidCalculatorBeforeFirstFork(1L, 1234L);
		assertIsFeeValidCalculatorBeforeFirstFork(10L, 1234L);
		assertIsFeeValidCalculatorBeforeFirstFork(100L, 1234L);
		assertIsFeeValidCalculatorBeforeFirstFork(1000L, 1234L);
		assertIsFeeValidCalculatorBeforeFirstFork(1233L, 1234L);
	}

	@Test
	public void isFeeValidUsesCorrectCalculatorAtFirstForkHeight() {
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1L, 1L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(10L, 10L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(100L, 100L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1000L, 1000L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1234L, 1234L);
	}

	@Test
	public void isFeeValidUsesCorrectCalculatorForHeightsAfterFirstForkHeightButBeforeSecondForkHeight() {
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(2L, 1L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1235L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(2000L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(10000L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1_000_000L, 1234L);
	}

	@Test
	public void isFeeValidUsesCorrectCalculatorAtSecondForkHeight() {
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1234L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(2000L, 2000L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(10000L, 10000L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1_000_000L, 1_000_000L);
	}

	@Test
	public void isFeeValidUsesCorrectCalculatorAfterSecondForkHeight() {
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1235L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(2000L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(10000L, 1234L);
		assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(1_000_000L, 1234L);
	}

	private static void assertIsFeeValidCalculatorBeforeFirstFork(final long testHeight, final long forkHeight) {
		// Arrange:
		final TestContext context = new TestContext(
				() -> new BlockHeight(testHeight),
				new BlockHeight(forkHeight),
				new BlockHeight(999_999_999_999L));
		final Transaction transaction = Mockito.mock(Transaction.class);

		// Act:
		context.calculator.isFeeValid(transaction, new BlockHeight(testHeight));

		// Assert:
		Mockito.verify(context.calculator1, Mockito.only()).isFeeValid(transaction, new BlockHeight(testHeight));
		Mockito.verify(context.calculator2, Mockito.never()).isFeeValid(Mockito.any(), Mockito.any());
		Mockito.verify(context.calculator3, Mockito.never()).isFeeValid(Mockito.any(), Mockito.any());
	}

	private static void assertIsFeeValidCalculatorAtAndAfterFirstForkButBeforeSecondFork(final long testHeight, final long forkHeight) {
		// Arrange:
		final TestContext context = new TestContext(
				() -> new BlockHeight(testHeight),
				new BlockHeight(forkHeight),
				new BlockHeight(999_999_999_999L));
		final Transaction transaction = Mockito.mock(Transaction.class);

		// Act:
		context.calculator.isFeeValid(transaction, new BlockHeight(testHeight));

		// Assert:
		Mockito.verify(context.calculator1, Mockito.never()).isFeeValid(Mockito.any(), Mockito.any());
		Mockito.verify(context.calculator2, Mockito.only()).isFeeValid(transaction, new BlockHeight(testHeight));
		Mockito.verify(context.calculator3, Mockito.never()).isFeeValid(Mockito.any(), Mockito.any());
	}

	private static void assertIsFeeValidCalculatorAtAndAfterSecondFork(final long testHeight, final long forkHeight) {
		// Arrange:
		final TestContext context = new TestContext(
				() -> new BlockHeight(testHeight),
				new BlockHeight(1000L),
				new BlockHeight(forkHeight));
		final Transaction transaction = Mockito.mock(Transaction.class);

		// Act:
		context.calculator.isFeeValid(transaction, new BlockHeight(testHeight));

		// Assert:
		Mockito.verify(context.calculator1, Mockito.never()).isFeeValid(Mockito.any(), Mockito.any());
		Mockito.verify(context.calculator2, Mockito.never()).isFeeValid(Mockito.any(), Mockito.any());
		Mockito.verify(context.calculator3, Mockito.only()).isFeeValid(transaction, new BlockHeight(testHeight));
	}

	// endregion

	private static class TestContext {
		private final TransactionFeeCalculatorBeforeFork calculator1 = Mockito.mock(TransactionFeeCalculatorBeforeFork.class);
		private final TransactionFeeCalculatorAfterFork calculator2 = Mockito.mock(TransactionFeeCalculatorAfterFork.class);
		private final FeeUnitAwareTransactionFeeCalculator calculator3 = Mockito.mock(FeeUnitAwareTransactionFeeCalculator.class);
		private final DefaultTransactionFeeCalculator calculator;

		public TestContext(final Supplier<BlockHeight> heightSupplier, final BlockHeight forkHeight1, final BlockHeight forkHeight2) {
			this.calculator = new DefaultTransactionFeeCalculator(
					heightSupplier,
					new BlockHeight[] { forkHeight1, forkHeight2 },
					new TransactionFeeCalculator[] { this.calculator1, this.calculator2, this.calculator3 });
		}
	}
}
