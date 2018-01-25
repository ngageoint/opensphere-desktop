package io.opensphere.merge.layout;

import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/** Base class for subcomponent layouts. */
public class BaseLayout
{
    /** The root Pane that is the ancestor of all managed subcomponents. */
    protected Pane root = new LayoutPane();
    {
        chListen(root.widthProperty(), v -> setWidth(v.doubleValue()));
        chListen(root.heightProperty(), v -> setHeight(v.doubleValue()));
    }

    /** The current width. */
    protected double width;

    /** The current height. */
    protected double height;

    /**
     * Assign the current width and update the layout accordingly.
     *
     * @param w the width
     */
    private void setWidth(double w)
    {
        width = w;
        doLayout();
    }

    /**
     * Assign the current height and update the layout accordingly.
     *
     * @param h the height
     */
    private void setHeight(double h)
    {
        height = h;
        doLayout();
    }

    /**
     * Gets the root Pane.
     *
     * @return the root
     */
    public Pane getRoot()
    {
        return root;
    }

    /**
     * Layout the subcomponents. By default, this method does nothing, but
     * subclasses will probably override to provide some functionality.
     */
    protected void doLayout()
    {
    }

    /**
     * Measure the preferred height of this layout.
     *
     * @return the height
     */
    protected double layoutHeight()
    {
        return 0.0;
    }

    /**
     * Measure the preferred width of this layout.
     *
     * @return the width
     */
    protected double layoutWidth()
    {
        return 0.0;
    }

    /**
     * Replace one child Node with another. This method is useful for layouts
     * that have specific slots (e.g., "top" and "bottom"). When a new
     * subcomponent is introduced, it replaces the previous occupant of its
     * intended slot.
     *
     * @param oldN the old subcomponent (which may be null)
     * @param newN the new subcomponent (which may be null)
     * @return the new subcomponent
     */
    protected Node replace(Node oldN, Node newN)
    {
        if (oldN != null)
        {
            root.getChildren().remove(oldN);
        }
        if (newN != null)
        {
            root.getChildren().add(newN);
        }
        return newN;
    }

    /**
     * Type of the managed root Pane. It delegates to the containing class for
     * calculations of preferred dimensions and subcomponent layout.
     */
    private class LayoutPane extends Pane
    {
        @Override
        protected double computePrefHeight(double d)
        {
            return layoutHeight();
        }

        @Override
        protected double computePrefWidth(double d)
        {
            return layoutWidth();
        }

        @Override
        protected void layoutChildren()
        {
            // screw this crap
        }
    }

    /**
     * Removes some of the stupidity inherent in the handling of ObservableValue
     * change events (cf. chEar), covering 99.5% of use cases.
     *
     * @param obs an ObservableValue of some kind.
     * @param ear a Consumer for values assumed by <i>obs</i>
     * @param<T> type of value in <i>obs</i>
     */
    private static <T> void chListen(ObservableValue<T> obs, Consumer<T> ear)
    {
        obs.addListener(chEar(ear));
    }

    /**
     * Converts a simple Consumer to a ChangeListener for use in handling events
     * from an ObservableValue.
     *
     * @param ear listener
     * @param<T> the type of value to be consumed
     * @return a ChangeListener derived from the simpler <i>ear</i>
     */
    private static <T> ChangeListener<T> chEar(Consumer<T> ear)
    {
        return (obs, old, val) -> ear.accept(val);
    }
}