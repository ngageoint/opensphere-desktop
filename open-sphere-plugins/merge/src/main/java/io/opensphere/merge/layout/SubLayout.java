package io.opensphere.merge.layout;

import javafx.scene.layout.Pane;

/** Nestable layout interface. */
public interface SubLayout
{
    /** Attach to the root pane (i.e., add children). */
    public void setRoot(Pane r);

    /** Get the width of this arrangement of subcomponents. */
    public double getWidth();

    /** Get the height of this arrangement of subcomponents. */
    public double getHeight();

    /** Layout the subcomponents within the resident root Pane. */
    public void doLayout(double x0, double y0, double dx, double dy);
}