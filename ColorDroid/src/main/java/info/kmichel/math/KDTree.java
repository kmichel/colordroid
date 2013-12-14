package info.kmichel.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import info.kmichel.colordroid.NamedLuvColor;

// TODO: genericize
public class KDTree {

    final NamedLuvColor element;
    final KDTree left_child;
    final KDTree right_child;

    private KDTree(final NamedLuvColor element, final KDTree left_child, final KDTree right_child) {
        this.element = element;
        this.left_child = left_child;
        this.right_child = right_child;
    }

    public NamedLuvColor getNearestElement(final NamedLuvColor target) {
        // TODO: allow passing a preallocated NearestNeighborState
        final NearestNeighborState state = new NearestNeighborState(target);
        findNearestNode(this, 0, state);
        return state.best_node.element;
    }

    private static class NearestNeighborState {
        final NamedLuvColor target;
        KDTree best_node;
        float best_squared_distance;

        NearestNeighborState(final NamedLuvColor target) {
            this.target = target;
            best_squared_distance = Float.POSITIVE_INFINITY;
        }

        void add(final KDTree node) {
            final float squared_distance = target.getSquaredDistance(node.element);
            if (squared_distance < best_squared_distance) {
                best_node = node;
                best_squared_distance = squared_distance;
            }
        }
    }

    private static void findNearestNode(KDTree node, int depth, final NearestNeighborState state) {
        // TODO: is it possible/beneficial to remove entirely the recursion ?
        while (true) {
            if (node == null)
                return;

            final float delta = state.target.getDistanceOnAxis(node.element, depth);
            final KDTree near_child = delta < 0 ? node.left_child : node.right_child;
            final KDTree far_child = delta < 0 ? node.right_child : node.left_child;

            findNearestNode(near_child, depth + 1, state);
            state.add(node);
            // This variable change and the while loop are a tail-call optimisation :)
            if (delta * delta < state.best_squared_distance) {
                node = far_child;
                depth += 1;
            } else
                return;
        }
    }

    public static KDTree buildTree(final List<NamedLuvColor> elements, final List<Comparator<NamedLuvColor>> comparators) {
        return buildTree(elements, comparators, 0);
    }

    private static KDTree buildTree(final List<NamedLuvColor> elements, final List<Comparator<NamedLuvColor>> comparators, final int depth) {
        if (elements.isEmpty())
            return null;
        Collections.sort(elements, comparators.get(depth % comparators.size()));
        final int median_index = elements.size() / 2;
        return new KDTree(
                elements.get(median_index),
                buildTree(elements.subList(0, median_index), comparators, depth + 1),
                buildTree(elements.subList(median_index + 1, elements.size()), comparators, depth + 1));
    }
}
