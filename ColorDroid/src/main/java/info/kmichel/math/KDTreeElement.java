package info.kmichel.math;

public interface KDTreeElement<Type extends KDTreeElement<Type>> {
    float getSquaredDistance(final Type element);

    float getDistanceOnAxis(final Type color, final int axis);
}
