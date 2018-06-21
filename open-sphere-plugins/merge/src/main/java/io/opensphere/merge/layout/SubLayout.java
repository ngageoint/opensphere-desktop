package io.opensphere.merge.layout;

import javafx.scene.layout.Pane;

/** Nestable layout interface. */
public interface SubLayout
{
    /**
     * Attach to the root pane (i.e., add children).
     *
     * @param r root pane
     */
    public void setRoot(Pane r);

    /**
     * Get the width of this arrangement of subcomponents.
     *
     * @return the width
     */
    public double getWidth();

    /**
     * Get the height of this arrangement of subcomponents.
     *
     * @return the height
     */
    public double getHeight();

    /**
     * Layout the subcomponents within the resident root Pane.
     *
     * @param x0 origin x
     * @param y0 origin y
     * @param dx distance x
     * @param dy distance y
     */
    public void doLayout(double x0, double y0, double dx, double dy);
}