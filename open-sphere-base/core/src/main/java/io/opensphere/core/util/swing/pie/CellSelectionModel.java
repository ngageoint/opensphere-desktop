package io.opensphere.core.util.swing.pie;

import java.awt.Point;
import java.util.Collection;

/**
 * Cell selection model interface.
 */
public interface CellSelectionModel
{
    /**
     * Adds the change listener.
     *
     * @param listener the listener
     */
    void addChangeListener(ChangeListener listener);

    // ========== Selection section ==========

    /**
     * Adds a selected cell.
     *
     * @param cell the cell
     * @param source the source of the change
     */
    void addSelectedCell(Point cell, Object source);

    /**
     * Gets the mouse-over cell.
     *
     * @return the mouse-over cell, or null
     */
    Point getMouseOverCell();

    /**
     * Gets the selected cells.
     *
     * @return the selected cells
     */
    Collection<Point> getSelectedCells();

    /**
     * Determines if the cell is selected.
     *
     * @param cell the cell
     * @return whether the cell is selected
     */
    boolean isCellSelected(Point cell);

    /**
     * Determines if the cell is the mouse-over cell.
     *
     * @param cell the cell
     * @return whether the cell is the mouse-over cell
     */
    boolean isMouseOverCell(Point cell);

    /**
     * Removes the change listener.
     *
     * @param listener the listener
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Removes a selected cell.
     *
     * @param cell the cell
     * @param source the source of the change
     */
    void removeSelectedCell(Point cell, Object source);

    /**
     * Sets the anchor cell.
     *
     * @param cell the cell
     */
    void setAnchorCell(Point cell);

    /**
     * Sets the final cell.
     *
     * @param cell the cell
     * @param keepSelected True if the previous selections should be kept, false
     *            otherwise.
     * @param source the source of the change
     */
    void setFinalCell(Point cell, boolean keepSelected, Object source);

    // ========== Mouse over section ==========

    /**
     * Sets the mouse-over cell.
     *
     * @param cell the cell
     * @param source the source of the change
     */
    void setMouseOverCell(Point cell, Object source);

    /**
     * Sets the selected cell.
     *
     * @param cell the cell
     * @param source the source of the change
     */
    void setSelectedCell(Point cell, Object source);

    /**
     * Sets the selected cells.
     *
     * @param cells the cells
     * @param source the source of the change
     */
    void setSelectedCells(Collection<? extends Point> cells, Object source);

    // ========== Listeners section ==========

    /**
     * Sets the selection mode.
     *
     * @param selectionMode the selection mode
     * @param source the source of the change
     */
    void setSelectionMode(SelectionMode selectionMode, Object source);

    /**
     * Selection mode.
     */
    enum SelectionMode
    {
        /** Individual cells are selected. */
        CELL,

        /** Slices are selected. */
        SLICE,

        /** Rings are selected. */
        RING
    }
}
