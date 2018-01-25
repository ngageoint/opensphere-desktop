package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;

public class OSDarkLAFProgressBarUI extends BasicProgressBarUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFProgressBarUI();
    }

    @Override
    public void paintIndeterminate(Graphics graph, JComponent comp)
    {
        final Graphics2D graph2D = (Graphics2D)graph;

        Rectangle aRectangle = new Rectangle();
        aRectangle = getBox(aRectangle);

        final Insets barInsets = progressBar.getInsets();
        final int xi = barInsets.left;
        final int yi = barInsets.top;
        final int xf = comp.getWidth() - barInsets.right;
        final int yf = comp.getHeight() - barInsets.bottom;

        graph2D.setColor(progressBar.getForeground());
        graph2D.fillRect(aRectangle.x, aRectangle.y, aRectangle.width, aRectangle.height);

        if (progressBar.getOrientation() != JProgressBar.HORIZONTAL)
        {
            GradientPaint grad = new GradientPaint(aRectangle.x, aRectangle.y, OSDarkLAFUtils.getActiveColor(), aRectangle.width,
                    aRectangle.y, OSDarkLAFUtils.getShadowColor());
            graph2D.setPaint(grad);
            graph2D.fill(aRectangle);
            grad = new GradientPaint(xi, yi, OSDarkLAFUtils.getShadowColor(), xf, yi, OSDarkLAFUtils.getActiveColor());
            graph2D.setPaint(grad);
            graph2D.fillRect(xi, yi, xf, aRectangle.y);
            graph2D.fillRect(xi, aRectangle.y + aRectangle.height, xf, yf);
        }
        else
        {
            GradientPaint grad = new GradientPaint(aRectangle.x, aRectangle.y, OSDarkLAFUtils.getActiveColor(), aRectangle.x,
                    aRectangle.height, OSDarkLAFUtils.getShadowColor());
            graph2D.setPaint(grad);
            graph2D.fill(aRectangle);
            grad = new GradientPaint(xi, yi, OSDarkLAFUtils.getShadowColor(), xi, yf, OSDarkLAFUtils.getActiveColor());
            graph2D.setPaint(grad);
            graph2D.fillRect(xi, yi, aRectangle.x, yf);
            graph2D.fillRect(aRectangle.x + aRectangle.width, yi, xf, yf);
        }
        paintString(graph2D, 0, 0, 0, 0, 0, barInsets);
    }

    @Override
    public void paintDeterminate(Graphics graph, JComponent jComp)
    {
        final Graphics2D graph2D = (Graphics2D)graph;

        final Insets barInsets = progressBar.getInsets();
        final int width = progressBar.getWidth() - (barInsets.left + barInsets.right);
        final int tall = progressBar.getHeight() - (barInsets.top + barInsets.bottom);
        final int length = getAmountFull(barInsets, width, tall);

        final int xi = barInsets.left;
        final int yi = barInsets.top;
        final int xf = xi + width;
        final int yf = yi + tall;
        final int xm = xi + length - 1;
        final int ym = yf - length;

        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL)
        {
            graph2D.setColor(progressBar.getForeground());
            graph2D.fillRect(xi, yi, xm, yf);

            GradientPaint gradientPaint = new GradientPaint(xi, yi, OSDarkLAFUtils.getActiveColor(), xi, yf,
                    OSDarkLAFUtils.getShadowColor());
            graph2D.setPaint(gradientPaint);
            graph2D.fillRect(xi, yi, xm, yf);

            gradientPaint = new GradientPaint(xm + 1, yi, OSDarkLAFUtils.getShadowColor(), xm + 1, yf,
                    OSDarkLAFUtils.getActiveColor());
            graph2D.setPaint(gradientPaint);
            graph2D.fillRect(xm + 1, yi, xf, yf);
        }
        else
        {
            graph2D.setColor(progressBar.getForeground());
            graph2D.fillRect(xi, ym, xf, yf);

            GradientPaint gradientPaint = new GradientPaint(xi, yi, OSDarkLAFUtils.getShadowColor(), xf, yi,
                    OSDarkLAFUtils.getActiveColor());
            graph2D.setPaint(gradientPaint);
            graph2D.fillRect(xi, yi, xf, ym);

            gradientPaint = new GradientPaint(xi, ym, OSDarkLAFUtils.getActiveColor(), xf, ym, OSDarkLAFUtils.getShadowColor());
            graph2D.setPaint(gradientPaint);
            graph2D.fillRect(xi, ym, xf, yf);
        }

        paintString(graph, 0, 0, 0, 0, 0, barInsets);
    }

    @Override
    protected void paintString(Graphics graph, int x, int y, int width, int height, int amountFull, Insets insets)
    {
        if (!progressBar.isStringPainted())
        {
            return;
        }

        final String pbText = progressBar.getString();

        final Point placementLoc = getStringPlacement(graph, pbText, insets.left, insets.top,
                progressBar.getWidth() - insets.left - insets.right, progressBar.getHeight() - insets.top - insets.bottom);
        graph.setFont(progressBar.getFont().deriveFont(Font.BOLD));

        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL)
        {
            if (!progressBar.getComponentOrientation().isLeftToRight())
            {
                placementLoc.x += progressBar.getFontMetrics(graph.getFont()).stringWidth(pbText);
            }
        }

        OSDarkLAFUtils.paintShadowTitle(graph, pbText, placementLoc.x, placementLoc.y, Color.white, Color.black, 1,
                OSDarkLAFUtils.WIDE, progressBar.getOrientation());
    }
}
