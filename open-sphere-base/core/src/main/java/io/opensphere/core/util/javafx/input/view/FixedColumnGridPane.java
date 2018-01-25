package io.opensphere.core.util.javafx.input.view;

import javafx.scene.layout.GridPane;

/**
 * An extension to the standard {@link GridPane} in which the number of columns is fixed at instantiation.
 */
public class FixedColumnGridPane extends GridPane
{
    /**
     * The number of columns used in the grid.
     */
    private final int myColumnCount;

    /**
     * Creates a new grid pane, accepting the number of columns to align in the grid.
     *
     * @param pColumnCount The number of columns used in the grid.
     */
    public FixedColumnGridPane(int pColumnCount)
    {
        myColumnCount = pColumnCount;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.layout.GridPane#computePrefWidth(double)
     */
    @Override
    protected double computePrefWidth(double height)
    {
        final double width = super.computePrefWidth(height);

        // RT-30903: Make sure width snaps to pixel when divided by
        // number of columns. GridPane doesn't do this with percentage
        // width constraints. See GridPane.adjustColumnWidths().
        final int nCols = myColumnCount;
        final double snaphgap = snapSpace(getHgap());
        final double left = snapSpace(getInsets().getLeft());
        final double right = snapSpace(getInsets().getRight());
        final double hgaps = snaphgap * (nCols - 1);
        final double contentWidth = width - left - right - hgaps;
        return ((snapSize(contentWidth / nCols)) * nCols) + left + right + hgaps;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.layout.GridPane#layoutChildren()
     */
    @Override
    protected void layoutChildren()
    {
        // Prevent AssertionError in GridPane
        if (getWidth() > 0 && getHeight() > 0)
        {
            super.layoutChildren();
        }
    }
}
