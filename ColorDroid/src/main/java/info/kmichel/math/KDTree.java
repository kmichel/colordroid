package info.kmichel.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KDTree<ElementType extends KDTreeElement<ElementType>> {

    final ElementType element;
    final KDTree<ElementType> left_child;
    final KDTree<ElementType> right_child;

    private KDTree(final ElementType element, final KDTree<ElementType> left_child, final KDTree<ElementType> right_child) {
        this.element = element;
        this.left_child = left_child;
        this.right_child = right_child;
    }

    public ElementType getNearestElement(final ElementType target) {
        // TODO: allow passing a preallocated NearestNeighborState
        final NearestNeighborState<ElementType> state = new NearestNeighborState<ElementType>(target);
        findNearestNode(this, 0, state);
        return state.best_node.element;
    }

    private static class NearestNeighborState<ElementType extends KDTreeElement<ElementType>> {
        final ElementType target;
        KDTree<ElementType> best_node;
        float best_squared_distance;

        NearestNeighborState(final ElementType target) {
            this.target = target;
            best_squared_distance = Float.POSITIVE_INFINITY;
        }

        void add(final KDTree<ElementType> node) {
            final float squared_distance = target.getSquaredDistance(node.element);
            if (squared_distance < best_squared_distance) {
                best_node = node;
                best_squared_distance = squared_distance;
            }
        }
    }

    private static <ElementType extends KDTreeElement<ElementType>>
    void findNearestNode(KDTree<ElementType> node, int depth, final NearestNeighborState<ElementType> state) {
        // TODO: is it possible/beneficial to remove entirely the recursion ?
        while (true) {
            if (node == null)
                return;

            final float delta = state.target.getDistanceOnAxis(node.element, depth);
            final KDTree<ElementType> near_child = delta < 0 ? node.left_child : node.right_child;
            final KDTree<ElementType> far_child = delta < 0 ? node.right_child : node.left_child;

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

    public static <ElementType extends KDTreeElement<ElementType>>
    KDTree<ElementType> buildTree(final List<ElementType> elements, final List<Comparator<ElementType>> comparators) {
        return buildTree(elements, comparators, 0);
    }

    private static <ElementType extends KDTreeElement<ElementType>>
    KDTree<ElementType> buildTree(final List<ElementType> elements, final List<Comparator<ElementType>> comparators, final int depth) {
        if (elements.isEmpty())
            return null;
        Collections.sort(elements, comparators.get(depth % comparators.size()));
        final int median_index = elements.size() / 2;
        return new KDTree<ElementType>(
                elements.get(median_index),
                buildTree(elements.subList(0, median_index), comparators, depth + 1),
                buildTree(elements.subList(median_index + 1, elements.size()), comparators, depth + 1));
    }
}
