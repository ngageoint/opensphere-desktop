package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.Popup;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

public class OSDarkLAFPopupMenuUI extends BasicPopupMenuUI
{
    private static Robot aRobot;

    private static Kernel menuKernel;

    private BufferedImage backBlurImage;

    private BufferedImage backImage;

    private MyPopupListener myPopupListener;

    private static final int WIDTH_HEIGHT_CONSTANT = 3;

    private static final int W_H_CONST_SQUARED = WIDTH_HEIGHT_CONSTANT * WIDTH_HEIGHT_CONSTANT;

    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        if (aRobot == null)
        {
            try
            {
                aRobot = new Robot();
            }
            catch (final Exception e)
            {
            }
        }

        if (menuKernel == null)
        {
            final float[] elements = new float[W_H_CONST_SQUARED];
            for (int i = 0; i < elements.length; i++)
            {
                elements[i] = .1f;
            }
            final int m = WIDTH_HEIGHT_CONSTANT / 2 + 1;
            elements[m * m] = 0.2f;

            menuKernel = new Kernel(WIDTH_HEIGHT_CONSTANT, WIDTH_HEIGHT_CONSTANT, elements);
        }

        return new OSDarkLAFPopupMenuUI();
    }

    @Override
    public void update(Graphics graph, JComponent jComp)
    {
        if (backBlurImage != null)
        {
            graph.drawImage(backBlurImage, 0, 0, null);
        }

        if (OSDarkLAFUtils.getMenuOpacity() > 5)
        {
            final Color endColor = new Color(jComp.getBackground().getRed(), jComp.getBackground().getGreen(),
                    jComp.getBackground().getBlue(), OSDarkLAFUtils.getMenuOpacity());
            graph.setColor(endColor);
            graph.fillRect(0, 0, jComp.getWidth() - 4, jComp.getHeight() - 4);
        }
    }

    protected BufferedImage surpriseEnd(JPopupMenu jPopupMU, Rectangle rectangle, int transparency)
    {
        BufferedImage image = null;

        try
        {
            image = aRobot.createScreenCapture(rectangle);
        }
        catch (final Throwable e)
        {
            image = new BufferedImage(rectangle.width, rectangle.height, BufferedImage.TYPE_INT_ARGB);
            final Graphics g = image.getGraphics();
            g.setColor(OSDarkLAFUtils.opacitizeColor(jPopupMU.getBackground(), transparency));
            g.fillRect(0, 0, rectangle.width, rectangle.height);
            g.dispose();
        }

        return image;
    }

    @Override
    public Popup getPopup(JPopupMenu jPopupMU, int x, int y)
    {
        final Dimension muDim = jPopupMU.getPreferredSize();
        final Rectangle muRect = new Rectangle(x, y, muDim.width, muDim.height);
        backImage = surpriseEnd(jPopupMU, muRect, 0);

        if (OSDarkLAFUtils.getMenuOpacity() > 250)
        {
            backBlurImage = backImage;
        }
        else
        {
            final Rectangle clearRectangle = new Rectangle(x - WIDTH_HEIGHT_CONSTANT, y - WIDTH_HEIGHT_CONSTANT,
                    muDim.width + 2 * WIDTH_HEIGHT_CONSTANT, muDim.height + 2 * WIDTH_HEIGHT_CONSTANT);

            final BufferedImage transparentBackground = surpriseEnd(jPopupMU, clearRectangle, OSDarkLAFUtils.getMenuOpacity());

            backBlurImage = new BufferedImage(muDim.width, muDim.height, BufferedImage.TYPE_INT_ARGB);
            final BufferedImage tempFondo = transparentBackground.getSubimage(0, 0, transparentBackground.getWidth(),
                    transparentBackground.getHeight());

            final ConvolveOp convolveOp = new ConvolveOp(menuKernel, ConvolveOp.EDGE_NO_OP, null);
            convolveOp.filter(transparentBackground, tempFondo);
            convolveOp.filter(tempFondo, transparentBackground);
            convolveOp.filter(transparentBackground, tempFondo);

            final Graphics graph = backBlurImage.getGraphics();
            graph.drawImage(backImage, 0, 0, null);
            graph.drawImage(
                    tempFondo.getSubimage(WIDTH_HEIGHT_CONSTANT, WIDTH_HEIGHT_CONSTANT, muDim.width - 5, muDim.height - 5), 0, 0,
                    null);
        }
        return super.getPopup(jPopupMU, x, y);
    }

    @Override
    public void installDefaults()
    {
        super.installDefaults();
        popupMenu.setOpaque(false);
        popupMenu.setBorder(OSDarkLAFBorders.getPopupMenuBorder());
    }

    @Override
    public void uninstallDefaults()
    {
        super.uninstallDefaults();
        LookAndFeel.installBorder(popupMenu, "PopupMenu.border");
        popupMenu.setOpaque(true);
    }

    @Override
    public void installListeners()
    {
        super.installListeners();
        myPopupListener = new MyPopupListener(popupMenu);
        popupMenu.addPopupMenuListener(myPopupListener);
    }

    @Override
    public void uninstallListeners()
    {
        super.uninstallListeners();
        popupMenu.removePopupMenuListener(myPopupListener);
    }

    private class MyPopupListener implements PopupMenuListener
    {
        JPopupMenu parentJPopupMenu;

        public MyPopupListener(JPopupMenu popupMU)
        {
            parentJPopupMenu = popupMU;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent ev)
        {
            if (null == backImage)
            {
                return;
            }

            final Graphics graph = parentJPopupMenu.getRootPane().getGraphics();

            final Point parentLocOnScreen = parentJPopupMenu.getLocationOnScreen();
            final Point rootPaneLocOnScreen = parentJPopupMenu.getRootPane().getLocationOnScreen();

            graph.drawImage(backImage, parentLocOnScreen.x - rootPaneLocOnScreen.x, parentLocOnScreen.y - rootPaneLocOnScreen.y,
                    null);
            backImage = null;
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent ev)
        {
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent ev)
        {
        }
    }
}
