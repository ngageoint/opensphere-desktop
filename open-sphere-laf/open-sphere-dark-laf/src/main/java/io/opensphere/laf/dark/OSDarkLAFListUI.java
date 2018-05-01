package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicListUI;

public class OSDarkLAFListUI extends BasicListUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFListUI(pComponent);
    }

    public OSDarkLAFListUI(JComponent pComponent)
    {
        super();
    }

    @Override
    protected void paintCell(Graphics graph, int rowIndex, Rectangle rowBounds, ListCellRenderer<Object> cellRenderer,
            ListModel<Object> listModel, ListSelectionModel selectionModel, int leadIndex)
    {
        rowBounds.x += 1;
        super.paintCell(graph, rowIndex, rowBounds, cellRenderer, listModel, selectionModel, leadIndex);
        rowBounds.x -= 1;

        if (list.isSelectedIndex(rowIndex))
        {
            final Color originalColor = graph.getColor();

            graph.translate(rowBounds.x, rowBounds.y);

            final GradientPaint gradientPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, rowBounds.height,
                    OSDarkLAFUtils.getShadowColor());
            final Color backgroundColor = OpenSphereDarkLookAndFeel.getMenuSelectedBackground();

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setPaint(gradientPaint);
            graph2D.fillRect(0, 0, rowBounds.width - 1, rowBounds.height);

            graph.setColor(backgroundColor.darker());
            graph.drawRect(0, 0, rowBounds.width - 1, rowBounds.height - 1);

            graph.translate(-rowBounds.x, -rowBounds.y);
            graph.setColor(originalColor);
        }
    }
}
