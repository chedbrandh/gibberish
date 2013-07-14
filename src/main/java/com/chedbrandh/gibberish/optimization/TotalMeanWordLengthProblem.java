package com.chedbrandh.gibberish.optimization;

import com.chedbrandh.gibberish.WordProvider;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Direction;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Input;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * There are a certain number of word providers. Each word provider must have
 * a certain number of bits that it covers. The number of bits, for each word
 * provider, add up to the total number of bits. Given some word provider and
 * some number of bits, a mean word length can be calculated. The mean word
 * length values, for each word provider, add up to the total mean word length.
 *
 * The optimization problem to solve here, is finding the min of the
 * total-mean-length function, given the constraint that the total number of
 * bits is fixed, and all word providers must have at least one, and at most
 * bitCoverage(), number of bits assigned.
 *
 * Every word provider thus introduce a new dimension. A legal vector (Input)
 * in space is defined by the number of bits each word provider is assigned
 * (these bits adding up to equal the specified total number of bits). A legal
 * Direction from a legal Input, one step, thus implies removing a bit assigned
 * to one word provider, and adding a bit to another (number of bits assigned
 * must still be at least one, and at most the "bit coverage" of the word
 * provider).
 *
 * In order to assure a global minimum, the meanWordLength() for each provider
 * must have a 0 &le; gradient.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class TotalMeanWordLengthProblem
        implements ConstrainedIntegerOptimizer.Problem<Input, Output, Direction> {

    // max number of iterations to run the optimizer before giving up
    public static final int MAX_PASSES = 1000;

    // total-mean-lengths differing by less than this are considered equal
    public static final double DOUBLE_TOLERANCE = 0.00001;

    // let optimizer start direction always be moving a bit from index 1 to 2
    private static final Direction START_DIRECTION = new Direction(0, 1);

    /*
    Comparator for lists of integers. Stating at index 0, the difference
    between the first non-equal pair of integers is returned, or zero if
    lists are equal.
     */
    private static final Comparator<List<Integer>> INTEGER_LIST_COMPARATOR =
            (list1, list2) -> {
                Preconditions.checkArgument(list1.size() == list2.size());
                for (int i = 0; i < list1.size(); i++) {
                    int compare = list1.get(i) - list2.get(i);
                    if (compare != 0) {
                        return compare;
                    }
                }
                return 0;
            };

    private final ImmutableList<WordProvider> wordProviders;
    private final int totalNumBits;

    /**
     * Constructor for the optimization problem.
     *
     * @param wordProviders  The word providers involved. Must be at least two,
     *                       and no more than the total number of bits to be
     *                       distributed.
     * @param totalNumBits   The number of bits to distribute over the word
     *                       providers.
     */
    public TotalMeanWordLengthProblem(Iterable<WordProvider> wordProviders,
                                      int totalNumBits) {
        ImmutableList<WordProvider> wordProvidersList = ImmutableList.copyOf(wordProviders);
        Preconditions.checkArgument(1 < wordProvidersList.size(),
                "Must provide at least two word providers.");
        Preconditions.checkArgument(wordProvidersList.size() <= totalNumBits, "The number of " +
                "total bits must be at least equal to the number of word providers.");
        this.wordProviders = wordProvidersList;
        this.totalNumBits = totalNumBits;
    }

    /**
     * The number of word providers.
     */
    private int numWordProviders() {
        return wordProviders.size();
    }

    /**
     * Function to optimize. The sum of the mean word length of all word
     * providers, given some bit distribution.
     */
    @Override
    public Output function(Input input) {
        double sum = 0;
        for (int i = 0; i < numWordProviders(); i++) {
            int numWords = Ints.checkedCast(LongMath.pow(2, input.bitDistribution.get(i)));
            sum += wordProviders.get(i).meanWordLength(numWords);
        }
        return new Output(sum);
    }

    /**
     * Finds some legal starting point.
     *
     * First assigns the minimum one bit to each word provider. Then, for each
     * word provider, assigns as many of the remaining bits as possible,
     * without exceeding the "bit coverage" of the provider, until all bits
     * have been assigned.
     */
    @Override
    public Input getLegalStartingInput() {
        // initialize bit distribution list with ones
        List<Integer> bitDistribution =
                Lists.newArrayList(Collections.nCopies(numWordProviders(), 1));
        int remainingBits = totalNumBits - numWordProviders();
        for (int i = 0; remainingBits > 0 && i < numWordProviders(); i ++) {
            WordProvider wordProvider = wordProviders.get(i);
            // assign to provider as many additional remaining bits as allowed
            int providerNumBits = Math.min(remainingBits + 1, wordProvider.bitCoverage());
            bitDistribution.set(i, providerNumBits);
            remainingBits = remainingBits - providerNumBits + 1;
        }
        if (remainingBits > 0) {
            throw new IllegalArgumentException("Incomplete bit coverage. " +
                    "Missing coverage for last " + remainingBits + " bit(s).");
        }
        return new Input(bitDistribution);
    }

    /**
     * Adjust bit distribution by removing a bit from one provider and adding
     * to another.
     */
    @Override
    public Input moveOneStep(Input input, Direction direction) {
        ArrayList<Integer> newBitDistribution = Lists.newArrayList(input.bitDistribution);
        newBitDistribution.set(direction.index1, newBitDistribution.get(direction.index1) - 1);
        newBitDistribution.set(direction.index2, newBitDistribution.get(direction.index2) + 1);
        return new Input(newBitDistribution);
    }

    /**
     * A bit distribution is legal if all constraints are fulfilled. This means
     * the sum of all distributed bits equal to the specified total, all
     * word providers have at least one bit assigned, and at most it's total
     * bit coverage.
     */
    @Override
    public boolean isIllegalInput(Input input) {
        boolean isLegal = true;
        int sumNumBits = 0;
        for (int i = 0; isLegal && i < numWordProviders(); i++) {
            int numBits = input.bitDistribution.get(i);
            sumNumBits += numBits;
            int bitCoverage = wordProviders.get(i).bitCoverage();
            isLegal = 1 <= numBits && numBits <= bitCoverage;
        }
        return !isLegal || sumNumBits != totalNumBits;
    }

    /**
     * Returns an iterator of all possible direction.
     */
    @Override
    public Iterator<Direction> getDirections() {

        return new Iterator<Direction>() {

            // set to null when all directions have been exhausted
            private Direction nextDirection = START_DIRECTION;

            @Override
            public boolean hasNext() {
                return nextDirection != null;
            }

            @Override
            public Direction next() {
                if (nextDirection == null) {
                    throw new NoSuchElementException();
                }
                Direction result = nextDirection;
                updateNextDirection();
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void updateNextDirection() {
                int newIndex1 = nextDirection.index1;
                int newIndex2 = nextDirection.index2;
                do {
                    newIndex1 = (newIndex1 + 1) % numWordProviders();
                    if (newIndex1 == 0) {
                        newIndex2 = (newIndex2 + 1) % numWordProviders();
                    }
                } while (newIndex1 == newIndex2);

                Direction newDirection = new Direction(newIndex1, newIndex2);

                // return null if returned to start direction, indicating no more elements
                nextDirection = newDirection.equals(START_DIRECTION) ? null : newDirection;
            }
        };
    }

    @Override
    public long maxPasses() {
        return MAX_PASSES;
    }

    /**
     * A holder of a list of integers representing the number of bits assigned
     * to each word provider.
     *
     * @author Christofer Hedbrandh (chedbrandh@gmail.com)
     * @since 1.0
     */
    public static class Input implements Comparable<Input> {
        public final List<Integer> bitDistribution;

        public Input(List<Integer> bitDistribution) {
            this.bitDistribution  = bitDistribution;
        }

        @Override
        public int compareTo(Input other) {
            Preconditions.checkArgument(bitDistribution.size() == other.bitDistribution.size());
            return INTEGER_LIST_COMPARATOR.compare(bitDistribution, other.bitDistribution);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Input.class)
                    .add("bitDistribution", bitDistribution)
                    .toString();
        }
    }

    /**
     * A holder of a total-mean-length double but with a compare method
     * that does a fuzzy double compare.
     *
     * @author Christofer Hedbrandh (chedbrandh@gmail.com)
     * @since 1.0
     */
    public static class Output implements Comparable<Output> {
        public final double totalMeanLength;

        public Output(double totalMeanLength) {
            this.totalMeanLength = totalMeanLength;
        }

        @Override
        public int compareTo(Output other) {
            return DoubleMath.fuzzyCompare(
                    totalMeanLength, other.totalMeanLength, DOUBLE_TOLERANCE);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Output.class)
                    .add("totalMeanLength", totalMeanLength)
                    .toString();
        }
    }

    /**
     * The data structure for a direction, i.e. removing a bit from one word
     * provider and adding to another, is represented by the two word provider
     * indices.
     *
     * E.g. in mathematical terms, for a problem in 5 dimensions, direction
     * {index1: 1, index2: 3} would be the vector (0, -1, 0, 1, 0).
     *
     * @author Christofer Hedbrandh (chedbrandh@gmail.com)
     * @since 1.0
     */
    public static class Direction {
        public final int index1;
        public final int index2;

        public Direction(int index1, int index2) {
            this.index1 = index1;
            this.index2 = index2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Direction other = (Direction) o;
            return Objects.equal(index1, other.index1) && Objects.equal(index2, other.index2);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(index1, index2);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Direction.class)
                    .add("index1", index1)
                    .add("index2", index2)
                    .toString();
        }
    }
}
