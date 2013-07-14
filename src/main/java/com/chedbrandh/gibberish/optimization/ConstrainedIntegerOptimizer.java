package com.chedbrandh.gibberish.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * This is an optimizer for constrained integer problems. The algorithm will
 * find a local min for the problem. If Output compares equal for two Inputs,
 * then the Inputs are compared as a tiebreaker. This guarantees the algorithm
 * to be deterministic (given that the Problem always returns the same
 * starting Input, and that the order of which the direction iterator returns
 * directions is deterministic).
 *
 * The algorithm starts with some legal Input, finds a descent Direction by
 * iterating through all of them, the Direction is then followed until it is
 * no longer legal or no longer a descent Direction. The process is repeating
 * finding new descent Directions until no more can be found.
 *
 * Since we are talking about integers I don't know of a better way find
 * descent Directions other than by checking them all (a finite number of
 * them). Since there may potentially be many Directions I figured I might as
 * well just follow the first descent Direction I find, rather than looking for
 * the steepest one (could be costly).
 *
 * Disclaimer: Finally I would like to point out that I am no mathematician
 * and I give no guarantees to any of the claims I am making here. The
 * algorithm described may very well have an actual proper accurate name, and
 * the terminology I use is probably all bonkers.
 *
 * @param <Input> {@link ConstrainedIntegerOptimizer.Problem}
 * @param <Output> {@link ConstrainedIntegerOptimizer.Problem}
 * @param <Direction> {@link ConstrainedIntegerOptimizer.Problem}
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class ConstrainedIntegerOptimizer
        <Input extends Comparable<Input>, Output extends Comparable<Output>, Direction> {

    private final static Logger LOGGER =
            LoggerFactory.getLogger(ConstrainedIntegerOptimizer.class);

    private final Problem <Input, Output, Direction> problem;

    private Input currentInput;
    private Output currentOutput;
    private Direction currentDirection;

    /**
     * Creates an optimizer, no optimization is performed.
     * @param problem The problem to optimize.
     */
    public ConstrainedIntegerOptimizer(Problem<Input, Output, Direction> problem) {
        this.problem = problem;
    }

    /**
     * Perform optimization and return the Input value for the found min.
     * @return The Input value for the min.
     */
    public Input findMin() {
        // initialize currentInput and currentOutput
        currentInput = problem.getLegalStartingInput();
        if (problem.isIllegalInput(currentInput)) {
            throw new IllegalArgumentException("Provided starting input is not legal.");
        }
        currentOutput = problem.function(currentInput);
        LOGGER.debug("Starting optimization from input {} with output {}.",
                currentInput, currentOutput);

        // look for and follow new descent directions while they exist or limit is reached
        for (long i = 0; tryAllDirections(); i++){
            LOGGER.debug("Has followed descent directions {} time(s).", i + 1);
            if (i == problem.maxPasses()) {
                throw new IllegalArgumentException("Still finding descent " +
                        "directions after max number of descent directions followed.");
            }
        }
        LOGGER.debug("Finished optimization at input {} with output {}.",
                currentInput, currentOutput);
        return currentInput;
    }

    /**
     * Iterate over all Directions until either a descent Direction is found,
     * or no descent Directions exist. When a descent Direction is found, the
     * Direction is followed, and the current Input is updated, until the
     * Direction is either no longer legal, or no longer a descent Direction.
     *
     * @return Returns true if the current Input was updated, false otherwise,
     * implying no descent Directions exist and a minimum has been found.
     */
    private boolean tryAllDirections() {
        LOGGER.debug("Trying all directions.");
        Iterator<Direction> directionIterator = problem.getDirections();
        boolean didDescend = false;
        while(!didDescend && directionIterator.hasNext()) {
            currentDirection = directionIterator.next();
            LOGGER.debug("Trying direction {}.", currentDirection);
            while(tryDirection()) {
                didDescend = true;
            }
        }
        return didDescend;
    }

    /**
     * Move current Input one step in the current Direction if it leads to
     * a lower Output, or if the Output is the same but a lower Input.
     *
     * @return Returns true if current Input was updated.
     */
    private boolean tryDirection() {

        Input nextInput = problem.moveOneStep(currentInput, currentDirection);

        if (problem.isIllegalInput(nextInput)) {
            LOGGER.debug("Current direction leads beyond constraints to {}.", nextInput);
            return false;
        }

        int inputDiff = nextInput.compareTo(currentInput);
        if (inputDiff == 0) {
            throw new IllegalStateException("Two different inputs must never compare equal.");
        }

        Output nextOutput = problem.function(nextInput);
        int outputDiff = nextOutput.compareTo(currentOutput);

        if (outputDiff < 0 || (outputDiff == 0 && inputDiff < 0)) {
            currentInput = nextInput;
            currentOutput = nextOutput;
            LOGGER.debug("Followed current direction leading to input {} with output {}.",
                    currentInput, currentOutput);
            return true;
        }
        LOGGER.debug("Rejected current direction leading to input {} with output {}.",
                nextInput, nextOutput);
        return false;
    }

    /**
     * Class for defining a constrained integer problem.
     *
     * Input and Output must both be comparable. If Output compares equal for
     * two Inputs, then the Inputs are compared as a tiebreaker.
     *
     * @param <Input>       Must hold that (x.compareTo(y)==0) == (x.equals(y))
     * @param <Output>      Optimization problem output. E.g. a Double.
     * @param <Direction>   Way to move from one input to another. E.g. a vector.
     *
     * @author Christofer Hedbrandh (chedbrandh@gmail.com)
     * @since 1.0
     */
    public interface Problem <Input extends Comparable<Input>,
            Output extends Comparable<Output>, Direction> {

        /**
         * The function to optimize.
         *
         * @param input     Function input.
         * @return          Function output.
         */
        Output function(Input input);

        /**
         * Returns some legal starting Input. In order to guarantee determinism
         * of the optimization algorithm, the same Input must be returned.
         *
         * @return  A legal starting point.
         */
        Input getLegalStartingInput();

        /**
         * Return the resulting Input of moving some Input one step in some
         * Direction. E.g. moving vector (3,3) in the direction (1,0) one step
         * to (4,3).
         *
         * There is probably a better mathematical way to put this, but
         * "one step" is meant to be with regards to the input not the
         * direction, i.e. moving vector (3,3) in the direction (10,0) must not
         * give (13,3) because then all vectors in between are missed.
         *
         * @param input     Input value to move.
         * @param direction Direction to move input.
         * @return          New input value after moving one step in direction.
         */
        Input moveOneStep(Input input, Direction direction);

        /**
         * Determines whether an Input is legal or not. This is where the
         * constraints of the problem is specified. E.g. v1 &gt; 0 or v1 + v2 = 42
         *
         * @param input Input value to check for legality.
         * @return      True if input is illegal, false otherwise.
         */
        boolean isIllegalInput(Input input);

        /**
         * Get all directions. Any known illegal directions does not need to
         * (should not) be returned. Such as in the case of equality
         * constraints.
         *
         * @return  The direction iterator.
         */
        Iterator<Direction> getDirections();

        /**
         * Number of times new descent directions can be found and followed
         * before giving up.
         *
         * @return  The maximum number of passes allowed.
         */
        long maxPasses();
    }
}
