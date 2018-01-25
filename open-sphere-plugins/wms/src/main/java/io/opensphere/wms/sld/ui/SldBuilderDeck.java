package io.opensphere.wms.sld.ui;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.wms.sld.SldRegistry;

/**
 * JPanel whose layout is a card layout so that the individual symbolizer panels
 * can be shown easily.
 */
public final class SldBuilderDeck extends JPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Point panel. */
    private PointSymbolizerPanel myPointPanel;

    /** The Polygon panel. */
    private PolygonSymbolizerPanel myPolygonPanel;

    /** The Line panel. */
    private JPanel myLinePanel;

    /** The Card layout. */
    private final CardLayout myCardLayout;

    /** The Sld registry. */
    private final SldRegistry mySldRegistry;

    /** The Selected panel. */
    private AbstractSymbolizer mySelectedPanel;

    /**
     * Create the panel.
     *
     * @param sldRegistry the sld registry
     */
    public SldBuilderDeck(SldRegistry sldRegistry)
    {
        mySldRegistry = sldRegistry;
        myCardLayout = new CardLayout();
        setLayout(myCardLayout);
        add(getPointSymbolizerPanel(), PanelType.POINT.toString());
        add(getLinePanel(), PanelType.LINE.toString());
        add(getPolygonSymbolizerPanel(), PanelType.POLY.toString());
        myCardLayout.show(this, PanelType.POINT.toString());
    }

    /**
     * Gets the point panel.
     *
     * @return the point panel
     */
    public PointSymbolizerPanel getPointSymbolizerPanel()
    {
        if (myPointPanel == null)
        {
            myPointPanel = new PointSymbolizerPanel(mySldRegistry);
        }
        return myPointPanel;
    }

    /**
     * Gets the polygon symbolizer panel.
     *
     * @return the polygon symbolizer panel
     */
    public PolygonSymbolizerPanel getPolygonSymbolizerPanel()
    {
        if (myPolygonPanel == null)
        {
            myPolygonPanel = new PolygonSymbolizerPanel(mySldRegistry);
        }
        return myPolygonPanel;
    }

    /**
     * Gets the selected panel.
     *
     * @return the selected panel
     */
    public AbstractSymbolizer getSelectedPanel()
    {
        return mySelectedPanel;
    }

    /**
     * Show panel.
     *
     * @param type the type
     */
    public void showPanel(PanelType type)
    {
        switch (type)
        {
            case POINT:
                mySelectedPanel = getPointSymbolizerPanel();
                break;

            case LINE:
//                mySelectedPanel = getLinePanel();
                break;

            case POLY:
                mySelectedPanel = getPolygonSymbolizerPanel();
                break;

            default:
                break;
        }
        myCardLayout.show(this, type.toString());
    }

    /**
     * Gets the line panel.
     *
     * @return the line panel
     */
    private JPanel getLinePanel()
    {
        if (myLinePanel == null)
        {
            myLinePanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            myLinePanel.add(new JLabel("Line panel here:"), gbc);
        }
        return myLinePanel;
    }

    /**
     * The Enum PanelType.
     */
    public enum PanelType
    {
        /** The POINT symbolizer type panel. */
        POINT("Point"),

        /** The LINE symbolizer type panel. */
        LINE("Line"),

        /** The POLYGON symbolizer type panel. */
        POLY("Polygon");

        /** The Title. */
        private String myTitle;

        /**
         * Instantiates a new panel type.
         *
         * @param title the title
         */
        PanelType(String title)
        {
            myTitle = title;
        }

        @Override
        public String toString()
        {
            return myTitle;
        }
    }
}
