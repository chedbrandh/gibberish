package com.chedbrandh.gibberish.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.chedbrandh.gibberish.WordProvider;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Direction;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Input;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Output;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TotalMeanWordLengthProblemTest {

    private static final WordProvider PROVIDER_1 = new WordProvider(ImmutableList.of(
            "a", "b", "c", "d", "e", "f", "g", "h"), "");

    private static final WordProvider PROVIDER_2 = new WordProvider(ImmutableList.of(
            "a", "b", "cde", "fgh", "ijklmn", "opqrst", "abcdef", "ghijkl"), "");

    private static final WordProvider PROVIDER_3 = new WordProvider(ImmutableList.of(
            "a", "b"), "");

    private ConstrainedIntegerOptimizer<Input, Output, Direction> optimizer;
    private TotalMeanWordLengthProblem problem;

    @Test
    public void testConstantOutput() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_1, PROVIDER_1);

        problem = new TotalMeanWordLengthProblem(providers, 9);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(3, 3, 3), optimizer.findMin().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 7);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(1, 3, 3), optimizer.findMin().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 5);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(1, 1, 3), optimizer.findMin().bitDistribution);
    }

    @Test
    public void testIncreasingOutput() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_2, PROVIDER_2, PROVIDER_2);

        problem = new TotalMeanWordLengthProblem(providers, 9);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(3, 3, 3), optimizer.findMin().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 7);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(2, 2, 3), optimizer.findMin().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 5);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(1, 2, 2), optimizer.findMin().bitDistribution);
    }

    @Test
    public void testMixedOutput() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2);

        problem = new TotalMeanWordLengthProblem(providers, 6);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(3, 3), optimizer.findMin().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 4);
        optimizer = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(ImmutableList.of(3, 1), optimizer.findMin().bitDistribution);
    }

    @Test
    public void testFunction() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2);
        problem = new TotalMeanWordLengthProblem(providers, 6);

        assertEquals(2.0, problem.function(new Input(ImmutableList.of(0, 0))).totalMeanLength, 0);
        assertEquals(2.0, problem.function(new Input(ImmutableList.of(1, 0))).totalMeanLength, 0);
        assertEquals(2.0, problem.function(new Input(ImmutableList.of(2, 0))).totalMeanLength, 0);
        assertEquals(2.0, problem.function(new Input(ImmutableList.of(3, 0))).totalMeanLength, 0);

        assertEquals(2.0, problem.function(new Input(ImmutableList.of(0, 1))).totalMeanLength, 0);
        assertEquals(2.0, problem.function(new Input(ImmutableList.of(1, 1))).totalMeanLength, 0);
        assertEquals(2.0, problem.function(new Input(ImmutableList.of(2, 1))).totalMeanLength, 0);
        assertEquals(2.0, problem.function(new Input(ImmutableList.of(3, 1))).totalMeanLength, 0);

        assertEquals(3.0, problem.function(new Input(ImmutableList.of(0, 2))).totalMeanLength, 0);
        assertEquals(3.0, problem.function(new Input(ImmutableList.of(1, 2))).totalMeanLength, 0);
        assertEquals(3.0, problem.function(new Input(ImmutableList.of(2, 2))).totalMeanLength, 0);
        assertEquals(3.0, problem.function(new Input(ImmutableList.of(3, 2))).totalMeanLength, 0);

        assertEquals(5.0, problem.function(new Input(ImmutableList.of(0, 3))).totalMeanLength, 0);
        assertEquals(5.0, problem.function(new Input(ImmutableList.of(1, 3))).totalMeanLength, 0);
        assertEquals(5.0, problem.function(new Input(ImmutableList.of(2, 3))).totalMeanLength, 0);
        assertEquals(5.0, problem.function(new Input(ImmutableList.of(3, 3))).totalMeanLength, 0);
    }

    @Test
    public void testGetLegalStartingInput() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2);

        problem = new TotalMeanWordLengthProblem(providers, 2);
        assertEquals(ImmutableList.of(1, 1), problem.getLegalStartingInput().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 3);
        assertEquals(ImmutableList.of(2, 1), problem.getLegalStartingInput().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 4);
        assertEquals(ImmutableList.of(3, 1), problem.getLegalStartingInput().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 5);
        assertEquals(ImmutableList.of(3, 2), problem.getLegalStartingInput().bitDistribution);

        problem = new TotalMeanWordLengthProblem(providers, 6);
        assertEquals(ImmutableList.of(3, 3), problem.getLegalStartingInput().bitDistribution);

        problem = new TotalMeanWordLengthProblem(
                ImmutableList.of(PROVIDER_3, PROVIDER_1), 4);
        assertEquals(ImmutableList.of(1, 3), problem.getLegalStartingInput().bitDistribution);
    }

    @Test
    public void testMoveOneStep() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2);
        problem = new TotalMeanWordLengthProblem(providers, 2);

        assertEquals(ImmutableList.of(2, 2), problem.moveOneStep(
                new Input(ImmutableList.of(1, 3)), new Direction(1, 0)).bitDistribution);

        assertEquals(ImmutableList.of(3, 1), problem.moveOneStep(
                new Input(ImmutableList.of(2, 2)), new Direction(1, 0)).bitDistribution);

        assertEquals(ImmutableList.of(1, 3), problem.moveOneStep(
                new Input(ImmutableList.of(2, 2)), new Direction(0, 1)).bitDistribution);
    }

    @Test
    public void testIsLegalInput() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2);

        problem = new TotalMeanWordLengthProblem(providers, 6);
        assertFalse(problem.isIllegalInput(new Input(ImmutableList.of(3, 3))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(2, 4))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(4, 2))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(4, 3))));

        problem = new TotalMeanWordLengthProblem(providers, 4);
        assertFalse(problem.isIllegalInput(new Input(ImmutableList.of(1, 3))));
        assertFalse(problem.isIllegalInput(new Input(ImmutableList.of(2, 2))));
        assertFalse(problem.isIllegalInput(new Input(ImmutableList.of(3, 1))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(4, 0))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(0, 4))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(2, 3))));

        problem = new TotalMeanWordLengthProblem(providers, 3);
        assertFalse(problem.isIllegalInput(new Input(ImmutableList.of(1, 2))));
        assertFalse(problem.isIllegalInput(new Input(ImmutableList.of(2, 1))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(3, 0))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(0, 3))));
        assertTrue(problem.isIllegalInput(new Input(ImmutableList.of(2, 3))));
    }

    @Test
    public void testGetDirectionsTwoDimensions() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2);
        problem = new TotalMeanWordLengthProblem(providers, 6);


        Set<Direction> expectedDirections = ImmutableSet.of(
                new Direction(0, 1), new Direction(1, 0));
        Set<Direction> actualDirections = ImmutableSet.copyOf(problem.getDirections());
        assertEquals(expectedDirections, actualDirections);
    }

    @Test
    public void testGetDirectionsThreeDimensions() throws Exception {
        List<WordProvider> providers = ImmutableList.of(PROVIDER_1, PROVIDER_2, PROVIDER_3);
        problem = new TotalMeanWordLengthProblem(providers, 6);

        Set<Direction> expectedDirections = ImmutableSet.of(
                new Direction(0, 1), new Direction(0, 2),
                new Direction(1, 0), new Direction(1, 2),
                new Direction(2, 0), new Direction(2, 1));
        Set<Direction> actualDirections = ImmutableSet.copyOf(problem.getDirections());
        assertEquals(expectedDirections, actualDirections);
    }
}
