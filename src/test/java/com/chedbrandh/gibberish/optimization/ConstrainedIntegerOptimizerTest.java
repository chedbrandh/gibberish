package com.chedbrandh.gibberish.optimization;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class ConstrainedIntegerOptimizerTest {

    private ConstrainedIntegerOptimizer<Integer, Double, Direction> undertest;
    private ConstrainedIntegerOptimizer.Problem<Integer, Double, Direction> problem;

    @Test
    public void testFindMin() throws Exception {
        problem = new Quadratic(0, -10, 10);
        undertest = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(0, (int)undertest.findMin());

        problem = new Quadratic(10, 0, 20);
        undertest = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(10, (int)undertest.findMin());
    }

    @Test
    public void testFindMinBeyondLower() throws Exception {
        problem = new Quadratic(0, 5, 10);
        undertest = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(5, (int)undertest.findMin());
    }

    @Test
    public void testFindMinBeyondUpper() throws Exception {
        problem = new Quadratic(0, -10, -5);
        undertest = new ConstrainedIntegerOptimizer<>(problem);
        assertEquals(-5, (int)undertest.findMin());
    }

    private static class Quadratic
            implements ConstrainedIntegerOptimizer.Problem<Integer, Double, Direction> {

        private final int min;
        private final int lowerBound;
        private final int upperBound;

        public Quadratic(int min, int lowerBound, int upperBound) {
            Preconditions.checkArgument(lowerBound <= upperBound);
            this.min = min;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public Double function(Integer integer) {
            return (integer - (double)min) * (integer - (double)min);
        }

        @Override
        public Integer getLegalStartingInput() {
            return lowerBound;
        }

        @Override
        public Integer moveOneStep(Integer integer, Direction direction) {
            return integer + (direction == Direction.GREATER ? 1 : -1);
        }

        @Override
        public boolean isIllegalInput(Integer integer) {
            return lowerBound > integer || integer > upperBound;
        }

        @Override
        public Iterator<Direction> getDirections() {
            return ImmutableList.of(Direction.GREATER, Direction.LESSER).iterator();
        }

        @Override
        public long maxPasses() {
            return 999;
        }
    }

    private enum Direction {
        GREATER,
        LESSER
    }
}
