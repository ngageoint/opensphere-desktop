package io.opensphere.imagery;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class LatLongCornerPanel.
 */
@SuppressWarnings("serial")
public class LatLongCornerPanel extends JPanel implements DocumentListener
{
    /** The Change listener. */
    private ChangeListener myChangeListener;

    /** The lower right lat field. */
    private JSpinner myLRLatField;

    /** The lower right lat label. */
    private JLabel myLRLatLabel;

    /** The lower right long field. */
    private JSpinner myLRLongField;

    /** The lower right long label. */
    private JLabel myLRLongLabel;

    /** The lower right panel. */
    private JPanel myLRPanel;

    /** The upper left lat field. */
    private JSpinner myULLatField;

    /** The upper left lat label. */
    private JLabel myULLatLabel;

    /** The upper left long field. */
    private JSpinner myULLongField;

    /** The upper left long label. */
    private JLabel myULLongLabel;

    /** The upper left panel. */
    private JPanel myULPanel;

    /**
     * Adds the change listener to spinner.
     *
     * @param spinner the spinner
     * @param listener the listener
     * @param panel the panel
     */
    private static void addChangeListenerToSpinner(JSpinner spinner, final ChangeListener listener, LatLongCornerPanel panel)
    {
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().getDocument().addDocumentListener(panel);
    }

    /**
     * Removes the change listener to spinner.
     *
     * @param spinner the spinner
     * @param listener the listener
     * @param panel the panel
     */
    private static void removeChangeListenerToSpinner(JSpinner spinner, final ChangeListener listener, LatLongCornerPanel panel)
    {
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().getDocument().removeDocumentListener(panel);
    }

    /**
     * Default constructor.
     */
    public LatLongCornerPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initialize(0, 0);
    }

    /**
     * Instantiates a new lat long corner panel.
     *
     * @param ulRightSpace the upper left right space
     * @param lrLeftSpace the lower right left space
     */
    public LatLongCornerPanel(int ulRightSpace, int lrLeftSpace)
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initialize(ulRightSpace, lrLeftSpace);
    }

    /**
     * Adds the change listener.
     *
     * @param listener the listener
     */
    public void addChangeListener(final ChangeListener listener)
    {
        myChangeListener = listener;
        LatLongCornerPanel.addChangeListenerToSpinner(myULLatField, listener, this);
        LatLongCornerPanel.addChangeListenerToSpinner(myULLongField, listener, this);
        LatLongCornerPanel.addChangeListenerToSpinner(myLRLatField, listener, this);
        LatLongCornerPanel.addChangeListenerToSpinner(myLRLongField, listener, this);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
    }

    /**
     * Gets the lR lat.
     *
     * @return the lR lat
     */
    public double getLRLat()
    {
        return ((Double)myLRLatField.getValue()).doubleValue();
    }

    /**
     * Gets the lR long.
     *
     * @return the lR long
     */
    public double getLRLong()
    {
        return ((Double)myLRLongField.getValue()).doubleValue();
    }

    /**
     * Gets the uL lat.
     *
     * @return the uL lat
     */
    public double getULLat()
    {
        return ((Double)myULLatField.getValue()).doubleValue();
    }

    /**
     * Gets the uL long.
     *
     * @return the uL long
     */
    public double getULLong()
    {
        return ((Double)myULLongField.getValue()).doubleValue();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        if (myChangeListener != null)
        {
            EventQueueUtilities.runOnEDT(() -> myChangeListener.stateChanged(new ChangeEvent(this)));
        }
    }

    /**
     * Removes the change listener.
     *
     * @param listener the listener
     */
    public void removeChangeListener(ChangeListener listener)
    {
        myChangeListener = null;
        LatLongCornerPanel.removeChangeListenerToSpinner(myULLatField, listener, this);
        LatLongCornerPanel.removeChangeListenerToSpinner(myULLongField, listener, this);
        LatLongCornerPanel.removeChangeListenerToSpinner(myLRLatField, listener, this);
        LatLongCornerPanel.removeChangeListenerToSpinner(myLRLongField, listener, this);
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
    }

    /**
     * overridden to set enabled status of all contained components.
     *
     * @param enabled the new enabled
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        myULPanel.setEnabled(enabled);
        myULLatLabel.setEnabled(enabled);
        myULLatField.setEnabled(enabled);
        myULLongLabel.setEnabled(enabled);
        myULLongField.setEnabled(enabled);
        myLRPanel.setEnabled(enabled);
        myLRLatLabel.setEnabled(enabled);
        myLRLatField.setEnabled(enabled);
        myLRLongLabel.setEnabled(enabled);
        myLRLongField.setEnabled(enabled);
    }

    /**
     * Sets the lR lat.
     *
     * @param lat the new lR lat
     */
    public void setLRLat(double lat)
    {
        myLRLatField.setValue(Double.valueOf(lat));
    }

    /**
     * Sets the lR long.
     *
     * @param lon the new lR long
     */
    public void setLRLong(double lon)
    {
        myLRLongField.setValue(Double.valueOf(lon));
    }

    /**
     * Sets the uL lat.
     *
     * @param lat the new uL lat
     */
    public void setULLat(double lat)
    {
        myULLatField.setValue(Double.valueOf(lat));
    }

    /**
     * Sets the uL long.
     *
     * @param lon the new uL long
     */
    public void setULLong(double lon)
    {
        myULLongField.setValue(Double.valueOf(lon));
    }

    /**
     * Initialize.
     *
     * @param ulRightSpace the upper left right space
     * @param lrLeftSpace the lower right left space
     */
    private void initialize(int ulRightSpace, int lrLeftSpace)
    {
        myULPanel = new JPanel();
        myULPanel.setLayout(new BoxLayout(myULPanel, BoxLayout.X_AXIS));
        myULPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, ulRightSpace),
                BorderFactory.createTitledBorder("Upper Left Corner")));
        myULPanel.add(myULLatLabel = new JLabel("Lat:"));
        myULPanel.add(myULLatField = new JSpinner(new SpinnerNumberModel(0.0, -90.0, 90.0, 0.1)));
        myULLatField.setEditor(new JSpinner.NumberEditor(myULLatField, "#0.0###"));
        myULPanel.add(Box.createHorizontalStrut(5));
        myULPanel.add(myULLongLabel = new JLabel("Lon:"));
        myULPanel.add(myULLongField = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.1)));
        myULLongField.setEditor(new JSpinner.NumberEditor(myULLongField, "##0.0###"));
        add(myULPanel);

        myLRPanel = new JPanel();
        myLRPanel.setLayout(new BoxLayout(myLRPanel, BoxLayout.X_AXIS));

        myLRPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, lrLeftSpace, 0, 0),
                BorderFactory.createTitledBorder("Lower Right Corner")));

        myLRPanel.add(myLRLatLabel = new JLabel("Lat:"));
        myLRPanel.add(myLRLatField = new JSpinner(new SpinnerNumberModel(0.0, -90.0, 90.0, 0.1)));
        myLRLatField.setEditor(new JSpinner.NumberEditor(myLRLatField, "#0.0###"));
        myLRPanel.add(Box.createHorizontalStrut(5));
        myLRPanel.add(myLRLongLabel = new JLabel("Lon:"));
        myLRPanel.add(myLRLongField = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.1)));
        myLRLongField.setEditor(new JSpinner.NumberEditor(myLRLongField, "##0.0###"));
        add(myLRPanel);
    }
}
