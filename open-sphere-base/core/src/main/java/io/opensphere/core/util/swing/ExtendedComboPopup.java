package io.opensphere.core.util.swing;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.plaf.basic.BasicComboPopup;

/**
 * The popup menu that can exceed the width of the combo box.
 */
public class ExtendedComboPopup extends BasicComboPopup
{
    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The combo box.
     */
    private final ExtendedComboBox<?> myComboBox;

    /**
     * Constructs the popup that can exceed the width of the combo box.
     *
     * @param combo The combo box.
     */
    public ExtendedComboPopup(ExtendedComboBox<?> combo)
    {
        super(combo);
        myComboBox = combo;
    }

    @Override
    public void show()
    {
        Dimension size = myComboBox.getPopupSize();
        size.setSize(size.width, getPopupHeightForRowCount(myComboBox.getMaximumRowCount()));

        Rectangle popupBounds = computePopupBounds(0, myComboBox.getBounds().height, size.width, size.height);
        scroller.setMaximumSize(popupBounds.getSize());
        scroller.setPreferredSize(popupBounds.getSize());
        scroller.setMinimumSize(popupBounds.getSize());

        list.invalidate();

        int selectedIndex = comboBox.getSelectedIndex();
        if (selectedIndex == -1)
        {
            list.clearSelection();
        }
        else
        {
            list.setSelectedIndex(selectedIndex);
        }

        list.ensureIndexIsVisible(list.getSelectedIndex());
        setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

        show(myComboBox, popupBounds.x, popupBounds.y);
    }
}
