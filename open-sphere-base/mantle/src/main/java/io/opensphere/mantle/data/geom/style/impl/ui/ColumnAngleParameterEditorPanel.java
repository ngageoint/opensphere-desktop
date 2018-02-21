package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * An angle AbstractStyleParameterEditorPanel controlled by a column in the data.
 */
public class ColumnAngleParameterEditorPanel extends AbstractStyleParameterEditorPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ColumnAngleParameterEditorPanel.class);

    /** The multiplier parameter key. */
    private final String myMultiplierKey;

    /** The columns combo box. */
    private final JComboBox<String> myColumnsCombo;

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
     */
    public ColumnAngleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String columnKey,
            String multiplierKey, Collection<? extends String> columns)
    {
        super(label, style, columnKey);
        myMultiplierKey = multiplierKey;

        myColumnsCombo = new JComboBox<>(new ListComboBoxModel<>(CollectionUtilities.sort(columns)));
        myColumnsCombo.setToolTipText("The column value to use for angle.");

        myMultiplierField = new JTextField();
        myMultiplierField.setColumns(4);
        myMultiplierField.setToolTipText("The amount by which to multiple the column value.");

        GridBagPanel panel = new GridBagPanel();
        panel.setInsets(0, 0, 0, 4);
        panel.add(myColumnsCombo);
        String unitPreLabel = (String)myPanelBuilder.getOtherParameter("unitPreLabel");
        if (unitPreLabel != null)
        {
            panel.add(new JLabel(unitPreLabel));
        }
        panel.add(new JLabel("deg"));
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
                handleMultiplierChange();
            }
        });
    }

    @Override
    public final void update()
    {
        assert EventQueue.isDispatchThread();

        SwingUtilities.setComboBoxValue(myColumnsCombo, (String)getParamValue());

        Integer multiplierParamValue = (Integer)myStyle.getStyleParameter(myMultiplierKey).getValue();
        SwingUtilities.setTextFieldValue(myMultiplierField, multiplierParamValue.toString());
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
     * Handles a change in the multiplier value.
     */
    private void handleMultiplierChange()
    {
        try
        {
            int magnitude = Integer.parseInt(myMultiplierField.getText());
            setParamValue(myMultiplierKey, Integer.valueOf(magnitude));
        }
        catch (NumberFormatException e)
        {
            LOGGER.debug(e);
        }
    }
}
