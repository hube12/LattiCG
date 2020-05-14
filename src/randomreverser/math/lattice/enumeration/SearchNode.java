package randomreverser.math.lattice.enumeration;

import randomreverser.math.component.BigFraction;
import randomreverser.math.component.BigMatrix;
import randomreverser.math.component.BigVector;
import randomreverser.math.optimize.Optimize;

import java.math.BigInteger;
import java.util.*;

class SearchNode {
    private final int size;
    private final int depth;

    private final BigMatrix inverse;
    private final BigVector origin;
    private final BigVector fixed;
    private final Optimize constraints;
    private final List<Integer> order;

    private Spliterator<BigVector> spliterator;

    public SearchNode(int size, int depth, BigMatrix inverse, BigVector origin, BigVector fixed, Optimize constraints, List<Integer> order) {
        this.size = size;
        this.depth = depth;
        this.inverse = inverse;
        this.origin = origin;
        this.fixed = fixed;
        this.constraints = constraints;
        this.order = order;
    }

    private SearchNode createChild(int index, BigInteger i) {
        BigVector gradient = this.inverse.getRow(index);
        BigFraction offset = this.origin.get(index);
        BigFraction value = new BigFraction(i);

        Optimize nextOptimize = this.constraints.withStrictBound(gradient, value.add(offset));
        BigVector nextFixed = this.fixed.add(BigVector.basis(this.size, index, value));

        return new SearchNode(this.size, this.depth + 1, this.inverse, this.origin, nextFixed, nextOptimize, this.order);
    }

    private void initialize() {
        if (this.depth == this.size) {
            this.spliterator = Collections.singleton(this.fixed).spliterator();
            return;
        }

        int index = this.order.get(this.depth);

        Deque<SearchNode> children = new LinkedList<>();

        BigVector gradient = this.inverse.getRow(index);
        BigFraction offset = this.origin.get(index);

        // make copies since if we try to do max after min, we force the
        // optimizer to retrace its steps
        BigInteger min = this.constraints.copy().minimize(gradient).getSecond().subtract(offset).ceil();
        BigInteger max = this.constraints.copy().maximize(gradient).getSecond().subtract(offset).floor();

        BigInteger lower = min.add(max).shiftRight(1);
        BigInteger upper = lower.add(BigInteger.ONE);
        boolean either = true;

        while (either) {
            either = false;

            if (lower.compareTo(min) >= 0) {
                children.add(this.createChild(index, lower));
                lower = lower.subtract(BigInteger.ONE);
                either = true;
            }

            if (upper.compareTo(max) <= 0) {
                children.add(this.createChild(index, upper));
                upper = upper.add(BigInteger.ONE);
                either = true;
            }
        }

        this.spliterator = new SearchSpliterator(children);
    }

    public Spliterator<BigVector> spliterator() {
        if (this.spliterator == null) {
            this.initialize();
        }

        return this.spliterator;
    }
}
