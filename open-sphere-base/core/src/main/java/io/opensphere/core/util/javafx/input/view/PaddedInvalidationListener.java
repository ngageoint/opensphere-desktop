package io.opensphere.core.util.javafx.input.view;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;

/**
 * An invalidation listener that preserves the padding of the to-be-invalidated region.
 */
public class PaddedInvalidationListener implements InvalidationListener
{
    /**
     * This boolean protects against unwanted recursion.
     */
    private boolean myRounding;

    /**
     * The region to invalidate.
     */
    private final Region myTargetRegion;

    /**
     * Creates a new invalidation listener.
     *
     * @param pTargetRegion the region to invalidate.
     *
     */
    public PaddedInvalidationListener(Region pTargetRegion)
    {
        myTargetRegion = pTargetRegion;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.beans.InvalidationListener#invalidated(javafx.beans.Observable)
     */
    @Override
    public void invalidated(Observable observable)
    {
        if (!myRounding)
        {
            Insets padding = myTargetRegion.getPadding();
            Insets rounded = new Insets(Math.round(padding.getTop()), Math.round(padding.getRight()),
                    Math.round(padding.getBottom()), Math.round(padding.getLeft()));
            if (!rounded.equals(padding))
            {
                myRounding = true;
                myTargetRegion.setPadding(rounded);
                myRounding = false;
            }
        }
    }
}
