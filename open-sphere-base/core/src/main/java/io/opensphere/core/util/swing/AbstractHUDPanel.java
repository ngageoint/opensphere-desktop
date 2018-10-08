package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Colors;

/**
 * The Class AbstractHUDPanel. This class defines some common background colors
 * for transparency and highlighting. It also contains common methods useful for
 * creating panels.
 */
public class AbstractHUDPanel extends JPanel implements PreferenceChangeListener
{
    /** The Component background color. */
    public static final Color ourComponentBackgroundColor = Colors.LF_SECONDARY3;

    /** The default HUD background color. */
    public static final Color ourDefaultHUDBackgroundColor = ColorUtilities.opacitizeColor(ourComponentBackgroundColor, 240);

    /** The Border color. */
    public static final Color ourBorderColor = new Color(200, 200, 255, 255);

    /** The highlight color for the border. */
    public static final Color ourBorderHighlightColor = new Color(100, 100, 120);

    /** The Border transparency color. */
    public static final Color ourBorderTransparencyColor = new Color(200, 200, 255, 200);

    /** The Opaque background color. */
    private static final Color ourOpaqueBackgroundColor = Color.DARK_GRAY.darker();

    /** The Read image error message. */
    private static final String ourReadImageErrorMessage = "IOException reading images.";

    /** The Title font. */
    private static final Font ourTitleFont = new Font("Dialog", Font.BOLD, 12);

    /** The Top level panel dimension. */
    private static final Dimension ourTopLevelPanelDim = new Dimension(224, 580);

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Background color. */
    private Color myBackgroundColor = ourDefaultHUDBackgroundColor;

    /** The topic for HUD background color preference. */
    public static final String ourHUDBackgroundColorKey = "background_color";

    /** The preferences registry. */
    private transient PreferencesRegistry myPreferencesRegistry;

    /**
     * This method creates and returns a JScrollPane with the following
     * attributes.
     *
     * @param pScrollablePanel the scrollable panel that the JScrollPane will
     *            use
     * @return the created JScrollPane
     */
    public static JScrollPane getJScrollPane(JComponent pScrollablePanel)
    {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setOpaque(false);
//        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        if (pScrollablePanel != null)
        {
            scrollPane.setViewportView(pScrollablePanel);
            scrollPane.getViewport().setOpaque(false);
            // The main panel should have the background color drawn
            scrollPane.getViewport().getView().setBackground(new Color(0, 0, 0, 0));
        }

        return scrollPane;
    }

    /**
     * Gets the read image error message.
     *
     * @return the read image error message
     */
    public static String getReadImageErrorMessage()
    {
        return ourReadImageErrorMessage;
    }

    /**
     * Creates a new <code>AbstractHUDPanel</code> with a double buffer and a
     * flow layout.
     */
    public AbstractHUDPanel()
    {
        super();
    }

    /**
     * Create a new buffered AbstractHUDPanel with the specified layout manager.
     *
     * @param layout The LayoutManager to use.
     */
    public AbstractHUDPanel(LayoutManager layout)
    {
        super(layout);
    }

    /**
     * Creates a new <code>AbstractHUDPanel</code> with a double buffer and a
     * flow layout.
     *
     * @param prefReg The preferences registry used to access preferences.
     */
    public AbstractHUDPanel(PreferencesRegistry prefReg)
    {
        this(new FlowLayout(), prefReg);
    }

    /**
     * Create a new buffered AbstractHUDPanel with the specified layout manager.
     *
     * @param layout The LayoutManager to use.
     * @param prefReg The preferences registry used to access preferences.
     */
    public AbstractHUDPanel(LayoutManager layout, PreferencesRegistry prefReg)
    {
        super(layout);
        if (prefReg != null)
        {
            myPreferencesRegistry = prefReg;
            myBackgroundColor = new Color(
                    prefReg.getPreferences(AbstractHUDPanel.class).getInt(ourHUDBackgroundColorKey, myBackgroundColor.getRGB()),
                    true);

            prefReg.getPreferences(AbstractHUDPanel.class).addPreferenceChangeListener(AbstractHUDPanel.ourHUDBackgroundColorKey,
                    this);
        }
    }

    @Override
    public void doLayout()
    {
        // For some reason setting all of the components to non-opaque avoids
        // some of the bad rendering artifacts when the frame is popped out.
        // Doing this here helps to avoid missing components which may be added
        // after creation. This will not cover the cases where a component is
        // added but the panel layout is not changed.
        ComponentUtilities.setContentsOpaque(this, false);
        super.doLayout();
    }

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor()
    {
        return myBackgroundColor;
    }

    /**
     * Gets the border color.
     *
     * @return the border color
     */
    public Color getBorderColor()
    {
        return ourBorderColor;
    }

    /**
     * Gets the border highlight color.
     *
     * @return the border highlight color
     */
    public Color getBorderHighlightColor()
    {
        return ourBorderHighlightColor;
    }

    /**
     * Gets the border transparency color.
     *
     * @return the border transparency color
     */
    public Color getBorderTransparencyColor()
    {
        return ourBorderTransparencyColor;
    }

    /**
     * Gets the image icon.
     *
     * @param pURL the uRL
     * @return the image icon
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ImageIcon getImageIcon(URL pURL) throws IOException
    {
        ImageIcon icon = null;
        icon = new ImageIcon(ImageIO.read(pURL));
        return icon;
    }

    /**
     * Gets the opaque background color.
     *
     * @return the opaque background color
     */
    public Color getOpaqueBackgroundColor()
    {
        return ourOpaqueBackgroundColor;
    }

    /**
     * Gets the title font.
     *
     * @return the title font
     */
    public Font getTitleFont()
    {
        return ourTitleFont;
    }

    /**
     * Gets the top level panel dim.
     *
     * @return the top level panel dim
     */
    public Dimension getTopLevelPanelDim()
    {
        return ourTopLevelPanelDim;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        myBackgroundColor = new Color(myPreferencesRegistry.getPreferences(AbstractHUDPanel.class)
                .getInt(ourHUDBackgroundColorKey, myBackgroundColor.getRGB()), true);

        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                setBackground(myBackgroundColor);
            }
        });
    }

    @Override
    public void setBackground(Color color)
    {
        super.setBackground(color);
        myBackgroundColor = color;
    }

    /**
     * Sets the components size, preferred size, and minimum size.
     *
     * @param pComp the comp
     * @param pDim the dim
     */
    public void setComponentSize(JComponent pComp, Dimension pDim)
    {
        pComp.setSize(pDim);
        pComp.setPreferredSize(pComp.getSize());
        pComp.setMinimumSize(pComp.getSize());
    }

    @Override
    protected void paintComponent(Graphics graphic)
    {
        if (!isOpaque() && !hasHUDPanelAncestor(this) && graphic instanceof Graphics2D)
        {
            Graphics2D g2d = (Graphics2D)graphic;
            g2d.setPaint(myBackgroundColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        super.paintComponent(graphic);
    }

    /**
     * Determine whether the given container has an ancestor which is an
     * AbstractHUDPanel.
     *
     * @param container The container to check for HUD panel ancestors.
     * @return true if the container has an ancestor which is an
     *         AbstractHUDPanel.
     */
    private boolean hasHUDPanelAncestor(Container container)
    {
        Container parent = container.getParent();
        if (parent != null)
        {
            if (parent instanceof AbstractHUDPanel)
            {
                return true;
            }
            return hasHUDPanelAncestor(parent);
        }

        return false;
    }
}
