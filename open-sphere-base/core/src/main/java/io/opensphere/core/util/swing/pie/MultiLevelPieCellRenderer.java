package io.opensphere.core.util.swing.pie;

import java.awt.Color;

/**
 * Renderer interface for MultiLevelPieChart.
 *
 * @param <T> The type of the data
 */
public interface MultiLevelPieCellRenderer<T>
{
    /**
     * Gets the color of the cell.
     *
     * @param value the cell value
     * @return the color, or null
     */
    Color getColor(T value);

    /**
     * Gets the label text for the cell.
     *
     * @param value the cell value
     * @return the label text, or null
     */
    String getLabel(T value);
}
