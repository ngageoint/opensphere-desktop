package io.opensphere.core.util.swing.pie;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;

/**
 * The Class MultiLevelPieChartLegend.
 *
 * @param <T> the generic type
 */
public final class MultiLevelPieChartLegend<T>
{
    /** The data model. */
    private transient MultiLevelPieChartModel<T> myDataModel;

    /** The legend map. */
    private Map<Color, String> myLegendMap;

    /** The value comparator. */
    private final Comparator<T> myValueComparator;

    /** The legend bounds. */
    private Rectangle myLegendBounds;

    /** Text color. */
    private Color myTextColor;

    /** The renderer. */
    private final transient MultiLevelPieCellRenderer<T> myRenderer;

    /**
     * Instantiates a new multi level pie chart legend.
     *
     * @param model the model
     * @param renderer the renderer
     * @param valueComparator the value comparator
     */
    public MultiLevelPieChartLegend(MultiLevelPieChartModel<T> model, MultiLevelPieCellRenderer<T> renderer,
            Comparator<T> valueComparator)
    {
        myDataModel = model;
        myRenderer = renderer;
        myValueComparator = valueComparator;
        myLegendMap = buildLegendModel();
        myTextColor = UIManager.getColor("Label.foreground");
    }

    /**
     * Builds the legend model.
     *
     * @return the map
     */
    public Map<Color, String> buildLegendModel()
    {
        final Map<Color, List<T>> colorToDataMap = LazyMap.create(New.<Color, List<T>>map(), Color.class, New.<T>listFactory());
        for (int ring = 0, ringCount = myDataModel.getRingCount(); ring < ringCount; ring++)
        {
            for (int slice = 0, sliceCount = myDataModel.getSliceCount(); slice < sliceCount; slice++)
            {
                T value = myDataModel.getValueAt(slice, ring);
                Color color = myRenderer.getColor(value);
                if (color != null)
                {
                    colorToDataMap.get(color).add(value);
                }
            }
        }

        // Sort the values
        if (myValueComparator != null)
        {
            for (Map.Entry<Color, List<T>> entry : colorToDataMap.entrySet())
            {
                List<T> values = entry.getValue();
                Collections.sort(values, myValueComparator);
            }
        }

        // Sort the keys based on the values
        List<Color> sortedKeys = New.list(colorToDataMap.keySet());
        if (myValueComparator != null)
        {
            Collections.sort(sortedKeys, new Comparator<Color>()
            {
                @Override
                public int compare(Color o1, Color o2)
                {
                    T value1 = colorToDataMap.get(o1).get(0);
                    T value2 = colorToDataMap.get(o2).get(0);
                    return myValueComparator.compare(value1, value2);
                }
            });
        }

        // Create the sorted legend map
        Map<Color, String> legendMap = New.insertionOrderMap();
        for (Color key : sortedKeys)
        {
            List<T> values = colorToDataMap.get(key);
            String minValue = myRenderer.getLabel(values.get(0));
            String maxValue = myRenderer.getLabel(values.get(values.size() - 1));
            StringBuilder label = new StringBuilder();
            label.append(minValue);
            if (!minValue.equals(maxValue))
            {
                label.append('-');
                label.append(maxValue);
            }
            legendMap.put(key, label.toString());
        }

        return legendMap;
    }

    /**
     * Gets the legend bounds.
     *
     * @return the legend bounds
     */
    public Rectangle getLegendBounds()
    {
        return myLegendBounds;
    }

    /**
     * Paint legend.
     *
     * @param g the g
     * @param height the height
     */
    public void paintLegend(Graphics g, int height)
    {
        if (!myLegendMap.isEmpty())
        {
            // Determine max label width
            int maxLabelwidth = 0;
            for (String label : myLegendMap.values())
            {
                int labelWidth = (int)g.getFontMetrics().getStringBounds(label, g).getWidth();
                if (labelWidth > maxLabelwidth)
                {
                    maxLabelwidth = labelWidth;
                }
            }

            // Set constants
            final int boxSize = 20;
            final int pad = 4;
            final int legendWidth = boxSize + maxLabelwidth + 3 * pad;
            final int legendHeight = (boxSize + pad) * myLegendMap.size() + pad;
            final int legendX = pad;
            final int legendY = height - legendHeight - pad;
            final int boxX = legendX + pad;
            final int labelX = boxX + boxSize + pad;
            final int labelYOffset = g.getFont().getSize() - 1 + (boxSize - g.getFont().getSize()) / 2;

            // Draw border
            g.setColor(Color.BLACK);
            g.drawRect(legendX, legendY, legendWidth, legendHeight);
            g.drawRect(legendX - 1, legendY - 1, legendWidth + 2, legendHeight + 2);

            // Draw boxes and labels
            int y = legendY + pad;
            for (Map.Entry<Color, String> entry : myLegendMap.entrySet())
            {
                Color color = entry.getKey();
                String label = entry.getValue();

                // Draw box
                g.setColor(color);
                g.fillRect(boxX, y, boxSize, boxSize);

                // Draw label
                if (label != null)
                {
                    g.setColor(myTextColor);
                    g.drawString(label, labelX, y + labelYOffset);
                }

                y += boxSize + pad;
            }
            myLegendBounds = new Rectangle(legendX, legendY, legendWidth, legendHeight);
        }
    }

    /**
     * Sets the data model.
     *
     * @param model the new data model
     */
    public void setDataModel(MultiLevelPieChartModel<T> model)
    {
        myDataModel = model;
        myLegendMap = buildLegendModel();
    }

    /**
     * Sets the text color of the legend.
     *
     * @param color The color of the text.
     */
    public void setTextColor(Color color)
    {
        myTextColor = color;
    }
}
