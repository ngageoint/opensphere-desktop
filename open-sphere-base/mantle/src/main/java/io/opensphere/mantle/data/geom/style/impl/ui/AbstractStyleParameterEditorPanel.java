package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public abstract class AbstractStyleParameterEditorPanel extends JPanel
{
    /** The Constant PANEL_HEIGHT. */
    public static final String PANEL_HEIGHT = "PANEL_HEIGHT";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(AbstractStyleParameterEditorPanel.class);

    /** The points feature icon. */
    private static ImageIcon ourAlertIcon;

    /** The our features light outline icon. */
    private static ImageIcon ourAlertOverIcon;

    /** The our features dark outline icon. */
    private static ImageIcon ourAlertPressIcon;

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Alert icon. */
    private JButton myAlertButton;

    /** The Control panel. */
    protected JPanel myControlPanel;

    /** The Label. */
    @SuppressWarnings("PMD.SingularField")
    private JLabel myLabel;

    /** The Label panel. */
    @SuppressWarnings("PMD.SingularField")
    private JPanel myLabelPanel;

    /** The Panel builder. */
    protected PanelBuilder myPanelBuilder;

    /** The Style. */
    protected MutableVisualizationStyle myStyle;

    /** The Style parameter. */
    private String myStyleParameterKey;

    /** The Value to label map. */
    private final Map<Object, String> myValueToAlertMap = New.map();

    /** If true, the name label is placed above the editor components. */
    private boolean nameAbove;

    /** Optional sibling components to be layed out after this one. */
    private final List<AbstractStyleParameterEditorPanel> mySiblingComponents = New.list();

    static
    {
        try
        {
            ourAlertIcon = new ImageIcon(ImageIO.read(AbstractStyleParameterEditorPanel.class.getResource("/images/alert.png")));
            ourAlertOverIcon = new ImageIcon(
                    ImageIO.read(AbstractStyleParameterEditorPanel.class.getResource("/images/alert-rollover.png")));
            ourAlertPressIcon = new ImageIcon(
                    ImageIO.read(AbstractStyleParameterEditorPanel.class.getResource("/images/alert-press.png")));
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to load image icons for AddDataLeafNodePanel. " + e);
        }
    }

    /**
     * Default constructor.  All initialization is deferred.
     */
    public AbstractStyleParameterEditorPanel()
    {
    }

    /**
     * Constructor with stuff passed in as parameters.  It calls setStuff to
     * install the references to the stuff.
     * @param builder a badly-named, glorified struct
     * @param style a glorified map of String to Object, with event support
     * @param paramKey the name of the parameter edited by this ASPEP
     */
    public AbstractStyleParameterEditorPanel(PanelBuilder builder, MutableVisualizationStyle style, String paramKey)
    {
        setStuff(builder, style, paramKey);
        setupAspep();
    }

    /**
     * Specify whether the name label should be placed above (vs. left of) the
     * editor components.
     * @param b true if and only if the label is to be placed above
     */
    public void setNameAbove(boolean b)
    {
        nameAbove = b;
    }

    /**
     * Installs references to the stuff used by this class.
     * @param builder a badly-named, glorified struct
     * @param style a glorified map of String to Object, with event support
     * @param paramKey the name of the parameter edited by this ASPEP
     */
    public void setStuff(PanelBuilder builder,
            MutableVisualizationStyle style, String paramKey)
    {
        myPanelBuilder = builder;
        myStyle = style;
        myStyleParameterKey = paramKey;
    }

    /**
     * Initialize the GUI for this class.  This method should be called before
     * subclasses attempt to add their own GUI components.
     */
    public void setupAspep()
    {
        setLayout(new BorderLayout());

        myAlertButton = new JButton(ourAlertIcon);
        myAlertButton.setRolloverIcon(ourAlertOverIcon);
        myAlertButton.setPressedIcon(ourAlertPressIcon);
        myAlertButton.setContentAreaFilled(false);
        myAlertButton.setMargin(new Insets(0, 0, 0, 0));
        myAlertButton.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        myAlertButton.setVisible(false);
        myAlertButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(AbstractStyleParameterEditorPanel.this, myAlertButton.getToolTipText(), "Warning",
                        JOptionPane.WARNING_MESSAGE, null);
            }
        });

        myLabelPanel = new JPanel();
        myLabelPanel.setLayout(new BoxLayout(myLabelPanel, BoxLayout.X_AXIS));
        myLabelPanel.add(myAlertButton);
        if (myPanelBuilder.getIndent() > 0)
        {
            myLabelPanel.add(Box.createHorizontalStrut(myPanelBuilder.getIndent()));
        }
        myLabel = new JLabel(myPanelBuilder.getLabel());
        myLabelPanel.add(myLabel);
        if (myPanelBuilder.getTrailPadding() > 0)
        {
            myLabelPanel.add(Box.createHorizontalStrut(myPanelBuilder.getTrailPadding()));
        }
        if (myPanelBuilder.getWidth() > 0 && myPanelBuilder.getHeight() > 0)
        {
            myLabelPanel.setMinimumSize(new Dimension(myPanelBuilder.getWidth(), myPanelBuilder.getHeight()));
        }

        int panelHeight = getPanelHeightFromBuilder();
        myControlPanel = new JPanel();
        ComponentUtilities.setMinimumHeight(myControlPanel, panelHeight);

        if (nameAbove)
        {
            add(myLabelPanel, BorderLayout.NORTH);
        }
        else
        {
            add(myLabelPanel, BorderLayout.WEST);
        }
        add(myControlPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    }

    /**
     * Gets the modifiable list of sibling components.
     *
     * @return the list of sibling components
     */
    public List<AbstractStyleParameterEditorPanel> getSiblingComponents()
    {
        return mySiblingComponents;
    }

    /**
     * Gets the minimum width of the control panel, in pixels. Defaults to 80.
     *
     * @return the minimum width of the control panel.
     */
    protected int getMinimumControlPanelWidth()
    {
        return 80;
    }

    /**
     * Gets the panel height from builder, because you wouldn't have guessed
     * that from the name.
     *
     * @return the panel height from builder
     */
    protected int getPanelHeightFromBuilder()
    {
        Object value = myPanelBuilder.getOtherParameter(PANEL_HEIGHT);
        return value instanceof Number ? ((Number)value).intValue() : 40;
    }

    /**
     * Gets the parameter.
     *
     * @return the parameter
     */
    protected final VisualizationStyleParameter getParameter()
    {
        return myStyle.getStyleParameter(myStyleParameterKey);
    }

    /**
     * Get the value from the default VisualizationStyleParameter.
     * @return the default parameter value
     */
    protected Object getParamValue()
    {
        return getParameter().getValue();
    }

    /**
     * Set the value of the default parameter.
     * @param val the new value
     */
    protected void setParamValue(Object val)
    {
        myStyle.setParameter(myStyleParameterKey, val, this);
    }

    /**
     * Set the value associated with the specified parameter key.
     * @param key a String identifier for the parameter
     * @param val the new value
     */
    protected void setParamValue(String key, Object val)
    {
        myStyle.setParameter(key, val, this);
    }

    /**
     * Gets the value to alert message map.
     *
     * @return the value to alert message map
     */
    public Map<Object, String> getValueToAlertMap()
    {
        return myValueToAlertMap;
    }

    /**
     * Show message.
     *
     * @param value the value
     */
    protected void showMessage(final Object value)
    {
        String label = myValueToAlertMap.get(value);
        if (value == null || label == null)
        {
            myAlertButton.setVisible(false);
        }
        else
        {
            myAlertButton.setVisible(true);
            myAlertButton.setToolTipText(label);
        }
    }

    /**
     * Update the panel and all siblings.
     */
    public void updateAll()
    {
        update();
        for (AbstractStyleParameterEditorPanel sibling : mySiblingComponents)
        {
            sibling.update();
        }
    }

    /**
     * Update.
     */
    protected abstract void update();
}
