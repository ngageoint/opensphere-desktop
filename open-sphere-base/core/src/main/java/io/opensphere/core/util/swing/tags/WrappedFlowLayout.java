package io.opensphere.core.util.swing.tags;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;

/**
 * An extended flow layout that places components in potentially multiple rows,
 * depending on the width of the parent component, and centers each component
 * vertically within its row.
 */
public class WrappedFlowLayout extends FlowLayout
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = 4974639223335338969L;

    /**
     * Constructs a new <code>FlowLayout</code> with a centered alignment and a
     * default 5-unit horizontal and vertical gap.
     */
    public WrappedFlowLayout()
    {
        super();
    }

    /**
     * Constructs a new <code>FlowLayout</code> with the specified alignment and
     * a default 5-unit horizontal and vertical gap. The value of the alignment
     * argument must be one of <code>FlowLayout.LEFT</code>,
     * <code>FlowLayout.RIGHT</code>, <code>FlowLayout.CENTER</code>,
     * <code>FlowLayout.LEADING</code>, or <code>FlowLayout.TRAILING</code>.
     *
     * @param align the alignment value
     */
    public WrappedFlowLayout(int align)
    {
        super(align);
    }

    /**
     * Creates a new flow layout manager with the indicated alignment and the
     * indicated horizontal and vertical gaps.
     * <p>
     * The value of the alignment argument must be one of
     * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
     * <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
     * <code>FlowLayout.TRAILING</code>.
     *
     * @param align the alignment value
     * @param hgap the horizontal gap between components and between the
     *            components and the borders of the <code>Container</code>
     * @param vgap the vertical gap between components and between the
     *            components and the borders of the <code>Container</code>
     */
    public WrappedFlowLayout(int align, int hgap, int vgap)
    {
        super(align, hgap, vgap);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.FlowLayout#layoutContainer(java.awt.Container)
     */
    @Override
    public void layoutContainer(Container container)
    {
        super.layoutContainer(container);

        int componentCount = container.getComponentCount();

        if (componentCount > 0)
        {
            Dimension containerSize = container.getSize();

            int currentRowWidth = 0;
            int[] rowIndexes = new int[componentCount];
            // this is a worst-case prediction, as there will never be more rows
            // than components:
            int[] rowHeights = new int[componentCount];
            Component[] components = container.getComponents();
            int currentMaxRowHeight = 0;
            int currentRow = 0;
            int accumulatedGap = 0;
            for (int i = 0; i < componentCount; i++)
            {
                Component component = components[i];
                accumulatedGap += getHgap();
                int componentWidth = component.getSize().width;
                int componentHeight = component.getSize().height;

                if (currentRowWidth + componentWidth + accumulatedGap > containerSize.width)
                {
                    // start a new row. First, capture the max height for the
                    // previous row, and store it:
                    rowHeights[currentRow] = currentMaxRowHeight;
                    currentRow++;
                    currentMaxRowHeight = 0;
                    currentRowWidth = 0;
                    accumulatedGap = 0;
                }
                currentRowWidth += componentWidth;
                currentMaxRowHeight = Math.max(componentHeight, currentMaxRowHeight);
                rowIndexes[i] = currentRow;
            }

            // capture the last row:
            rowHeights[currentRow] = currentMaxRowHeight;

            int rowCount = currentRow + 1;
            int[] rowOffsets = new int[rowCount];

            // account for extra space:
            int accumulatedVerticalOffset = 0;
            for (int row = 0; row < rowCount; row++)
            {
                rowHeights[row] = Math.max(containerSize.height / rowCount, rowHeights[row]);
                rowOffsets[row] = accumulatedVerticalOffset;
                accumulatedVerticalOffset += rowHeights[row] + getVgap();
            }

            for (int i = 0; i < componentCount; i++)
            {
                Component component = components[i];
                int rowMidpoint = rowHeights[rowIndexes[i]] / 2;
                Point p = component.getLocation();
                Dimension componentSize = component.getSize();

                p.y = rowOffsets[rowIndexes[i]] + rowMidpoint - componentSize.height / 2;
                component.setLocation(p);
            }
        }
    }
}
