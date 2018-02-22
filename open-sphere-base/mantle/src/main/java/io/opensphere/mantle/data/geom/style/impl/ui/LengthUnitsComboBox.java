package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.swing.ListComboBoxModel;

/** A combo box for length units. */
public class LengthUnitsComboBox extends JComboBox<Class<? extends Length>>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param unitOptions the unit options
     */
    public LengthUnitsComboBox(Collection<Class<? extends Length>> unitOptions)
    {
        super(new ListComboBoxModel<>(unitOptions));

        BasicComboBoxRenderer renderer = new BasicComboBoxRenderer();
        setRenderer(new ListCellRenderer<Class<? extends Length>>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends Class<? extends Length>> list,
                    Class<? extends Length> value, int index, boolean isSelected, boolean cellHasFocus)
            {
                String displayValue = Length.create(value, 0.).getShortLabel(false);
                return renderer.getListCellRendererComponent(list, displayValue, index, isSelected, cellHasFocus);
            }
        });

        StringBuilder tooltip = new StringBuilder(32);
        tooltip.append("The length units. (");
        for (Class<? extends Length> unit : unitOptions)
        {
            Length length = Length.create(unit, 0.);
            String shortLabel = length.getShortLabel(false);
            String longLabel = Length.create(unit, 0.).getLongLabel(false);
            tooltip.append(shortLabel).append('=').append(longLabel).append(", ");
        }
        tooltip.setLength(tooltip.length() - 2);
        tooltip.append(')');
        setToolTipText(tooltip.toString());
    }
}
