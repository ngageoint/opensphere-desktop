package io.opensphere.core.util.swing;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * The Class FontComboBoxRenderer. Renders font in its native display style.
 */
public class FontComboBoxRenderer implements ListCellRenderer<FontWrapper>
{
    /** The underlying renderer. */
    private final BasicComboBoxRenderer myRenderer = new BasicComboBoxRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends FontWrapper> list, FontWrapper value, int index,
            boolean isSelected, boolean cellHasFocus)
    {
        Component c = myRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        c.setFont(value.getFont());
        return c;
    }
}
