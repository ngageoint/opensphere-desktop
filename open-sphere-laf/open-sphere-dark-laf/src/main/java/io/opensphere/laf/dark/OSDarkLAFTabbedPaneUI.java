package io.opensphere.laf.dark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class OSDarkLAFTabbedPaneUI extends BasicTabbedPaneUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFTabbedPaneUI();
    }

    private Color selectColor;

    private Polygon myShape;

    private final int inclTab = 12;

    private int rollover = -1;

    private final int carpetWidth = 18;

    private final int focusWidthH = 0;

    private int antRollover = -1;

    private CustomMouseEffectListener myCustomMouseListener;

    @Override
    protected void paintTabArea(Graphics graph, int tabPlacement, int selectedIndex)
    {
        if (runCount > 1)
        {
            final int[] lines = new int[runCount];
            for (int i = 0; i < runCount; i++)
            {
                lines[i] = rects[tabRuns[i]].y + (tabPlacement == TOP ? maxTabHeight : 0);
            }

            Arrays.sort(lines);

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (tabPlacement != TOP)
            {
                int row = 0;
                for (int i = 0; i < lines.length - 1; i++, row++)
                {
                    final Polygon carpetPoly = new Polygon();
                    carpetPoly.addPoint(0, lines[i]);
                    carpetPoly.addPoint(tabPane.getWidth() - 2 * row - 1, lines[i]);
                    carpetPoly.addPoint(tabPane.getWidth() - 2 * row - 1, lines[i + 1] - 3);
                    carpetPoly.addPoint(tabPane.getWidth() - 2 * row - 3, lines[i + 1]);
                    carpetPoly.addPoint(0, lines[i + 1]);
                    carpetPoly.addPoint(0, lines[i]);
                    graph2D.setColor(adjustAlphaByFactor(row + 2));
                    graph2D.fillPolygon(carpetPoly);
                    graph2D.setColor(darkShadow.darker());
                    graph2D.drawPolygon(carpetPoly);
                }
            }
            else
            {
                int row = runCount;
                for (int i = 0; i < lines.length - 1; i++, row--)
                {
                    final Polygon carpetPoly = new Polygon();
                    carpetPoly.addPoint(0, lines[i]);
                    carpetPoly.addPoint(tabPane.getWidth() - 2 * row - 2, lines[i]);
                    carpetPoly.addPoint(tabPane.getWidth() - 2 * row, lines[i] + 3);

                    if (i < lines.length - 2)
                    {
                        carpetPoly.addPoint(tabPane.getWidth() - 2 * row, lines[i + 1]);
                        carpetPoly.addPoint(0, lines[i + 1]);
                    }
                    else
                    {
                        carpetPoly.addPoint(tabPane.getWidth() - 2 * row, lines[i] + rects[selectedIndex].height);
                        carpetPoly.addPoint(0, lines[i] + rects[selectedIndex].height);
                    }

                    carpetPoly.addPoint(0, lines[i]);
                    graph2D.setColor(adjustAlphaByFactor(row));
                    graph2D.fillPolygon(carpetPoly);
                    graph2D.setColor(darkShadow.darker());
                    graph2D.drawPolygon(carpetPoly);
                }
            }
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        super.paintTabArea(graph, tabPlacement, selectedIndex);
    }

    @Override
    protected void paintTabBackground(Graphics graph, int tabPlacement, int tabIndex, int x, int y, int width, int height,
            boolean isSelected)
    {
        final Graphics2D graph2D = (Graphics2D)graph;
        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradShadowPaint;

        int[] xp = null;
        int[] yp = null;
        switch (tabPlacement)
        {
            case RIGHT:
                yp = new int[] { y, y + height - 3, y + height - 3, y, y };
                xp = new int[] { x, x, x + width - 2, x + width - 2, x };
                gradShadowPaint = new GradientPaint(x, y, OSDarkLAFUtils.getActiveColor(), x, y + height,
                        OSDarkLAFUtils.getShadowColor());
                break;
            case LEFT:
                yp = new int[] { y, y + height - 3, y + height - 3, y, y };
                xp = new int[] { x, x, x + width, x + width, x };
                gradShadowPaint = new GradientPaint(x, y, OSDarkLAFUtils.getActiveColor(), x, y + height,
                        OSDarkLAFUtils.getShadowColor());
                break;
            case BOTTOM:
                yp = new int[] { y, y + height - 3, y + height, y + height, y + height - 1, y + height - 3, y, y };
                xp = new int[] { x, x, x + 3, x + width - inclTab - 6, x + width - inclTab - 2, x + width - inclTab,
                    x + width - 3, x };
                gradShadowPaint = new GradientPaint(x, y, OSDarkLAFUtils.getActiveColor(), x, y + height,
                        OSDarkLAFUtils.getShadowColor());
                break;
            case TOP:
            default:
                yp = new int[] { y + height, y + 3, y, y, y + 1, y + 3, y + height, y + height };
                xp = new int[] { x, x, x + 3, x + width - inclTab - 6, x + width - inclTab - 2, x + width - inclTab, x + width,
                    x };
                gradShadowPaint = new GradientPaint(x, y, OSDarkLAFUtils.getActiveColor(), x, y + height,
                        OSDarkLAFUtils.getShadowColor());
                break;
        }

        myShape = new Polygon(xp, yp, xp.length);

        if (isSelected)
        {
            graph2D.setColor(selectColor);
        }
        else
        {
            graph2D.setColor(tabPane.getBackgroundAt(tabIndex));
        }

        graph2D.fill(myShape);

        if (runCount > 1)
        {
            graph2D.setColor(adjustAlphaByFactor(getRunForTab(tabPane.getTabCount(), tabIndex) - 1));
            graph2D.fill(myShape);
        }

        if (tabIndex == rollover)
        {
            graph2D.setColor(OSDarkLAFUtils.getRolloverColor());
            graph2D.fill(myShape);
        }

        graph2D.setPaint(gradShadowPaint);
        graph2D.fill(myShape);
        graph2D.setColor(OSDarkLAFUtils.getShadowColor());
        graph2D.draw(myShape);
        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        rollover = -1;
        selectColor = OpenSphereDarkLookAndFeel.getFocusColor();
        tabAreaInsets.right = carpetWidth;
    }

    @Override
    protected void installListeners()
    {
        super.installListeners();
        myCustomMouseListener = new CustomMouseEffectListener();
        tabPane.addMouseMotionListener(myCustomMouseListener);
        tabPane.addMouseListener(myCustomMouseListener);
    }

    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        tabPane.removeMouseMotionListener(myCustomMouseListener);
        tabPane.removeMouseListener(myCustomMouseListener);
    }

    @Override
    protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect,
            Rectangle iconRect, Rectangle textRect, boolean isSelected)
    {
        final Rectangle tabRectPeq = new Rectangle(tabRect);
        tabRectPeq.width -= inclTab;
        super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRectPeq, iconRect, textRect, isSelected);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight)
    {
        if (tabPlacement == RIGHT || tabPlacement == LEFT)
        {
            return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
        }
        else
        {
            return focusWidthH + super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
        }
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics fontMetrics)
    {
        return 8 + inclTab + super.calculateTabWidth(tabPlacement, tabIndex, fontMetrics);
    }

    @Override
    protected void paintTabBorder(Graphics graph, int tabPlacement, int tabIndex, int x, int y, int width, int height,
            boolean isSelected)
    {
    }

    @Override
    protected void paintFocusIndicator(Graphics graph, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
            Rectangle textRect, boolean isSelected)
    {
        if (isSelected && tabPane.hasFocus())
        {
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Stroke previousStroke = graph2D.getStroke();
            graph2D.setStroke(new BasicStroke(2.0f));
            graph2D.setColor(UIManager.getColor("ScrollBar.thumbShadow"));
            graph2D.drawPolygon(myShape);
            graph2D.setStroke(previousStroke);
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    protected Color adjustAlphaByFactor(int factor)
    {
        int alpha = 0;
        if (factor >= 0)
        {
            alpha = 50 + (factor > 7 ? 70 : 8 * factor);
        }
        return new Color(0, 0, 0, alpha);
    }

    public class CustomMouseEffectListener extends MouseAdapter implements MouseMotionListener
    {
        @Override
        public void mouseExited(MouseEvent e)
        {
            rollover = -1;
            tabPane.repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            rollover = tabForCoordinate(tabPane, e.getX(), e.getY());
            if (antRollover == rollover && rollover == -1)
            {
                return;
            }

            tabPane.repaint();
            antRollover = rollover;
        }
    }
}
