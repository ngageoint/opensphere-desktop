package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopIconUI;

/**
 * OpenSphere Dark Look and Feel for a minimized window on a desktop.
 */
public class OSDarkLAFDesktopIconUI extends BasicDesktopIconUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFDesktopIconUI();
    }

    boolean iconHasFocus;

    private final int iconWidth = UIManager.getInt("OSDarkLAFDesktopIcon.width");

    private final int iconHeight = UIManager.getInt("OSDarkLAFDesktopIcon.height");

    private final int bigIconWidth = UIManager.getInt("OSDarkLAFDesktopIconBig.width");

    private final int bigIconHeight = UIManager.getInt("OSDarkLAFDesktopIconBig.height");

    private final DummyMouseListener dummyMouseListener;

    private Icon resizedIcon;

    private Icon antIcon;

    public OSDarkLAFDesktopIconUI()
    {
        super();
        dummyMouseListener = new DummyMouseListener();
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        LookAndFeel.uninstallBorder(desktopIcon);
    }

    @Override
    public void update(Graphics graph, JComponent jComp)
    {
        paint(graph, jComp);
    }

    protected String getTitle(String titleString, FontMetrics fMetrics, int length)
    {
        if (titleString == null || titleString.isEmpty())
        {
            return "";
        }

        final int lTit = fMetrics.stringWidth(titleString);
        if (lTit <= length)
        {
            return titleString;
        }

        int ellipsisWidth = fMetrics.stringWidth("...");
        if (length - ellipsisWidth <= 0)
        {
            return "";
        }

        int i = 1;
        do
        {
            final String tmp = titleString.substring(0, i++) + "...";
            ellipsisWidth = fMetrics.stringWidth(tmp);
        }
        while (ellipsisWidth < length);

        return titleString.substring(0, i - 1) + "...";
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        if (frame.getFrameIcon() != antIcon)
        {
            antIcon = frame.getFrameIcon();
            resizedIcon = OSDarkLAFUtils.rescaleIcon(antIcon, bigIconWidth, bigIconHeight);
        }
        String iconTitle = frame.getTitle();

        int x = 0;
        if (null != resizedIcon)
        {
            x = (iconWidth - resizedIcon.getIconWidth()) / 2;
            resizedIcon.paintIcon(jComp, graph, x, 2);
        }

        graph.setFont(UIManager.getFont("DesktopIcon.font"));
        final FontMetrics fMetrics = graph.getFontMetrics();

        if (iconHasFocus)
        {
            int y = 0;
            String secondaryTitle = getTitle(iconTitle, fMetrics, iconWidth - 10);
            while (secondaryTitle.length() > 0)
            {
                if (secondaryTitle.endsWith("..."))
                {
                    secondaryTitle = secondaryTitle.substring(0, secondaryTitle.length() - 3);
                }

                final Rectangle2D secondaryTitleStringBounds = fMetrics.getStringBounds(secondaryTitle, graph);
                x = (int)(iconWidth - secondaryTitleStringBounds.getWidth()) / 2;
                y += secondaryTitleStringBounds.getHeight();

                OSDarkLAFUtils.paintShadowTitleWide(graph, secondaryTitle, x, y, Color.white);

                iconTitle = iconTitle.substring(secondaryTitle.length());
                secondaryTitle = getTitle(iconTitle, fMetrics, iconWidth - 10);
            }
        }
        else
        {
            iconTitle = getTitle(iconTitle, fMetrics, iconWidth - 10);
            final Rectangle2D titleBounds = fMetrics.getStringBounds(iconTitle, graph);
            x = (int)(iconWidth - titleBounds.getWidth()) / 2;
            OSDarkLAFUtils.paintShadowTitleWide(graph, iconTitle, x, iconHeight - OSDarkLAFUtils.TITLE_SHADOW_THICKNESS,
                    Color.white);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent comp)
    {
        return getMinimumSize(comp);
    }

    @Override
    public Dimension getMaximumSize(JComponent comp)
    {
        return getMinimumSize(comp);
    }

    @Override
    public Dimension getMinimumSize(JComponent comp)
    {
        return new Dimension(iconWidth, iconHeight);
    }

    @Override
    protected void installComponents()
    {
    }

    @Override
    protected void uninstallComponents()
    {
    }

    @Override
    protected void installListeners()
    {
        super.installListeners();

        if (frame != null)
        {
            desktopIcon.addMouseMotionListener(dummyMouseListener);
            desktopIcon.addMouseListener(dummyMouseListener);
        }
    }

    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        desktopIcon.removeMouseMotionListener(dummyMouseListener);
        desktopIcon.removeMouseListener(dummyMouseListener);
    }

    private class DummyMouseListener extends MouseInputAdapter
    {
        void updateUI(MouseEvent ev)
        {
            if (null != desktopIcon)
            {
                desktopIcon.getDesktopPane().updateUI();
            }
        }

        @Override
        public void mouseExited(MouseEvent event)
        {
            iconHasFocus = false;
            updateUI(event);
        }

        @Override
        public void mouseEntered(MouseEvent event)
        {
            iconHasFocus = true;
            updateUI(event);
        }

        @Override
        public void mouseReleased(MouseEvent event)
        {
            updateUI(event);
        }

        @Override
        public void mousePressed(MouseEvent event)
        {
            updateUI(event);
        }

        @Override
        public void mouseDragged(MouseEvent event)
        {
            updateUI(event);
        }
    }
}
