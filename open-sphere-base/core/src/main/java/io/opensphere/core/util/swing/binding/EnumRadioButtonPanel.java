package io.opensphere.core.util.swing.binding;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.ViewPanel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A panel that contains radio buttons, one for each value in an enum. The
 * buttons will be bi-directionally bound to a property.
 *
 * @param <E> The element type.
 */
public class EnumRadioButtonPanel<E extends Enum<E>> extends ViewPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param type The type.
     * @param prop The property.
     */
    public EnumRadioButtonPanel(Class<E> type, ObjectProperty<E> prop)
    {
        ButtonGroup buttonGroup = new ButtonGroup();

        ActionListener buttonListener = e -> prop.set(Enum.valueOf(type, e.getActionCommand()));
        EnumSet<E> allOf = EnumSet.allOf(type);
        for (E t : allOf)
        {
            JRadioButton btn = new JRadioButton(t.toString());
            btn.setActionCommand(t.name());
            buttonGroup.add(btn);
            btn.addActionListener(buttonListener);

            ChangeListener<? super E> propListener = new ChangeListener<>()
            {
                @Override
                public void changed(ObservableValue<? extends E> v, E o, E n)
                {
                    if (Objects.equals(n, t))
                    {
                        btn.setSelected(true);
                    }
                }
            };
            prop.addListener(propListener);
            propListener.changed(prop, null, prop.getValue());
        }

        Collection<Component> components = New.collection();
        JLabel label = new JLabel(prop.getName() + "    ");
        components.add(label);
        components.addAll(Collections.list(buttonGroup.getElements()));
        addRow(New.array(components, Component.class));
    }
}
