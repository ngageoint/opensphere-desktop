package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * A length AbstractStyleParameterEditorPanel controlled by a column in the data.
 */
public class ColumnLengthParameterEditorPanel extends AbstractStyleParameterEditorPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The units combo box. */
    private JComboBox<Class<? extends Length>> myUnitsCombo;

    /**
     * Constructor.
     *
     * @param label the {@link PanelBuilder}
     * @param style the style
     * @param paramKey the param key
     * @param unitOptions the unit options
     */
    public ColumnLengthParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            Collection<Class<? extends Length>> unitOptions)
    {
        super(label, style, paramKey);

        myUnitsCombo = new JComboBox<>();
        for (Class<? extends Length> unit : unitOptions)
        {
            myUnitsCombo.addItem(unit);
        }
        BasicComboBoxRenderer renderer = new BasicComboBoxRenderer();
        myUnitsCombo.setRenderer(new ListCellRenderer<Class<? extends Length>>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends Class<? extends Length>> list,
                    Class<? extends Length> value, int index, boolean isSelected, boolean cellHasFocus)
            {
                String displayValue = Length.create(value, 0.).getShortLabel(false);
                return renderer.getListCellRendererComponent(list, displayValue, index, isSelected, cellHasFocus);
            }
        });
//        myUnitsCombo.addActionListener(e -> handleValueChange());

        GridBagPanel panel = new GridBagPanel();
        panel.setInsets(0, 4, 0, 0).fillNone().add(myUnitsCombo);

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);

        update();
    }

    @Override
    public final void update()
    {
        assert EventQueue.isDispatchThread();

        Object value = getParamValue();
        if (value instanceof Length)
        {
            myUnitsCombo.setSelectedItem(value.getClass());
        }
    }
}
