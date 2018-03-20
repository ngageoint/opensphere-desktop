package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.text.NumberFormat;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * A length AbstractStyleParameterEditorPanel controlled by a column in the data.
 */
public class ColumnLengthParameterEditorPanel extends AbstractStyleParameterEditorPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ColumnLengthParameterEditorPanel.class);

    /** The multiplier parameter key. */
    private final String myMultiplierKey;

    /** The columns combo box. */
    private final JComboBox<String> myColumnsCombo;

    /** The units combo box. */
    private final LengthUnitsComboBox myUnitsCombo;

    /** The multiplier text field. */
    private final JTextField myMultiplierField;

    /**
     * Constructor.
     *
     * @param label the {@link PanelBuilder}
     * @param style the style
     * @param columnKey the column key
     * @param multiplierKey the multiplier key
     * @param columns the columns
     * @param unitOptions the unit options
     */
    public ColumnLengthParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String columnKey,
            String multiplierKey, Collection<? extends String> columns, Collection<Class<? extends Length>> unitOptions)
    {
        super(label, style, columnKey);
        myMultiplierKey = multiplierKey;

        myColumnsCombo = new JComboBox<>(new ListComboBoxModel<>(CollectionUtilities.sort(columns)));
        myColumnsCombo.setToolTipText("The column value to use for length.");
        ComponentUtilities.setPreferredHeight(myColumnsCombo, 24);

        myUnitsCombo = new LengthUnitsComboBox(unitOptions);
        ComponentUtilities.setPreferredHeight(myUnitsCombo, 24);

        myMultiplierField = new JTextField();
        ComponentUtilities.setMinimumWidth(myMultiplierField, 50);
        ComponentUtilities.setPreferredWidth(myMultiplierField, 50);
        myMultiplierField.setToolTipText("The amount by which to multiple the column value.");

        GridBagPanel panel = new GridBagPanel();
        panel.setInsets(0, 0, 0, 4);
        panel.add(myColumnsCombo);
        String unitPreLabel = (String)myPanelBuilder.getOtherParameter("unitPreLabel");
        if (unitPreLabel != null)
        {
            panel.add(new JLabel(unitPreLabel));
        }
        panel.add(myUnitsCombo);
        panel.add(new JLabel("x"));
        panel.add(myMultiplierField);
        panel.fillHorizontalSpace();

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);

        update();

        myColumnsCombo.addActionListener(e -> handleColumnChange());
        myMultiplierField.getDocument().addDocumentListener(new DocumentListenerAdapter()
        {
            @Override
            public void updateAction(DocumentEvent e)
            {
                handleLengthChange();
            }
        });
        myUnitsCombo.addActionListener(e -> handleLengthChange());
    }

    @Override
    public final void update()
    {
        assert EventQueue.isDispatchThread();

        SwingUtilities.setComboBoxValue(myColumnsCombo, (String)getParamValue());

        Length multiplierParamValue = (Length)myStyle.getStyleParameter(myMultiplierKey).getValue();
        SwingUtilities.setComboBoxValue(myUnitsCombo, multiplierParamValue.getClass());
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        String multiplierText = format.format((int)multiplierParamValue.getMagnitude());
        SwingUtilities.setTextFieldValue(myMultiplierField, multiplierText);
    }

    /**
     * Handles a change in the column.
     */
    private void handleColumnChange()
    {
        String column = (String)myColumnsCombo.getSelectedItem();
        setParamValue(column);
    }

    /**
     * Handles a change in one of the length values.
     */
    private void handleLengthChange()
    {
        try
        {
            Length value = getLength();
            setParamValue(myMultiplierKey, value);
        }
        catch (NumberFormatException e)
        {
            LOGGER.debug(e);
        }
    }

    /**
     * Gets the current length value from the UI.
     *
     * @return the length value
     */
    private Length getLength()
    {
        int magnitude = Integer.parseInt(myMultiplierField.getText());
        @SuppressWarnings("unchecked")
        Class<? extends Length> unit = (Class<? extends Length>)myUnitsCombo.getSelectedItem();
        Length value = Length.create(unit, magnitude);
        return value;
    }
}
