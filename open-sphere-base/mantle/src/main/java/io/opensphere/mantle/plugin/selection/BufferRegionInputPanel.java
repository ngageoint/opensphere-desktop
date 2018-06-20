package io.opensphere.mantle.plugin.selection;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.length.Length;

/** An input panel in which a buffer region is configured. */
public class BufferRegionInputPanel extends JPanel
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = -2710737609798623773L;

    /** The Buffer distance text field. */
    private final JSpinner myDistanceInputField;

    /** The Buffer distance unit combo box. */
    private final JComboBox<String> myUnitInputField;

    /** The unit provider used to populate the unit input field. */
    private final UnitsProvider<Length> myUnitsProvider;

    /** The number model used in the spinner input. */
    private final SpinnerNumberModel mySpinnerModel;

    /** Consumer invoked upon change to the input field or unit provider. */
    private final Consumer<Length> myEditConsumer;

    /**
     * Instantiates a new buffer region input panel.
     *
     * @param initialDistance the initial distance
     * @param unitsProvider the units provider
     * @param validRange the valid range message
     * @param pEditConsumer The consumer method invoked when a change is made to
     *            the input field or unit provider.
     */
    public BufferRegionInputPanel(Length initialDistance, UnitsProvider<Length> unitsProvider, String validRange,
            Consumer<Length> pEditConsumer)
    {
        myEditConsumer = pEditConsumer;
        myUnitsProvider = unitsProvider;
        mySpinnerModel = new SpinnerNumberModel((int)initialDistance.getMagnitude(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        myDistanceInputField = new JSpinner(mySpinnerModel);
        myDistanceInputField.addChangeListener(e -> myEditConsumer.accept(getDistance()));
        myDistanceInputField.addMouseWheelListener(e -> mouseWheelMoved(e));

        myUnitInputField = new JComboBox<>(myUnitsProvider.getAvailableUnitsSelectionLabels(false));
        myUnitInputField.setSelectedItem(initialDistance.getSelectionLabel());
        myUnitInputField.addItemListener(e -> executeConditionally(e.getStateChange() == ItemEvent.SELECTED));

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(layout);

        JTextArea noteArea = new JTextArea(
                "Please note that buffer regions may not work for some shapes or over the 180 meridian."
                        + "  In addition, the distance from the source geometry to the buffer boundary will become"
                        + " less accurate for large areas and at higher latitudes.");
        noteArea.setBackground(getBackground());
        noteArea.setBorder(null);
        noteArea.setLineWrap(true);
        noteArea.setEditable(false);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(noteArea.getFont().deriveFont(Font.ITALIC, noteArea.getFont().getSize() - 1));

        c.gridwidth = 3;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(noteArea, c);
        add(noteArea);

        JLabel distanceLabel = new JLabel("Buffer Distance:");
        c.anchor = GridBagConstraints.WEST;
        c.ipadx = 5;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        c.gridx = 0;
        layout.setConstraints(distanceLabel, c);
        add(distanceLabel);

        c.weightx = 1.0;
        c.gridx++;
        layout.setConstraints(myDistanceInputField, c);
        add(myDistanceInputField);

        c.weightx = 0;
        c.gridx++;
        layout.setConstraints(myUnitInputField, c);
        add(myUnitInputField);

        Dimension preferredSize = new Dimension(myUnitInputField.getPreferredSize());
        preferredSize.height = myDistanceInputField.getPreferredSize().height;
        myUnitInputField.setPreferredSize(preferredSize);

        JLabel validRangeLabel = new JLabel(validRange);
        validRangeLabel.setFont(validRangeLabel.getFont().deriveFont(Font.ITALIC, validRangeLabel.getFont().getSize() - 1));

        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.NONE;
        c.gridy++;
        c.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(validRangeLabel, c);
        add(validRangeLabel);

        setPreferredSize(new Dimension(400, 100));
    }

    /**
     * Adjusts the value of the distance input field, based on the mouse wheel
     * movement.
     *
     * @param pEvent the event triggered by the mouse wheel movement.
     */
    private void mouseWheelMoved(MouseWheelEvent pEvent)
    {
        if (pEvent.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
        {
            int stepSize = mySpinnerModel.getStepSize().intValue();
            int currentValue = mySpinnerModel.getNumber().intValue();

            // negate the movement to get the value to go the right direction
            // (pushing the mouse wheel forward should increment the value,
            // pulling it backwards should decrement it).
            mySpinnerModel.setValue(Integer.valueOf(currentValue + -1 * stepSize * pEvent.getWheelRotation()));
        }
    }

    /**
     * Gets the distance.
     *
     * @return the distance
     * @throws NumberFormatException the number format exception
     */
    public Length getDistance() throws NumberFormatException
    {
        return Length.create(getDistanceUnit(), mySpinnerModel.getNumber().doubleValue());
    }

    /**
     * Gets the distance unit.
     *
     * @return the distance unit
     */
    private Class<? extends Length> getDistanceUnit()
    {
        return myUnitsProvider.getUnitsWithSelectionLabel((String)myUnitInputField.getSelectedItem());
    }

    /**
     * Reduces conditional execution to a one-liner.
     *
     * @param pExecutionCondition indicator of whether to execute
     */
    private void executeConditionally(boolean pExecutionCondition)
    {
        if (pExecutionCondition)
        {
            myEditConsumer.accept(getDistance());
        }
    }
}
