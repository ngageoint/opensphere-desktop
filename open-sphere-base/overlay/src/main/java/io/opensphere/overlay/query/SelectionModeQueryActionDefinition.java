package io.opensphere.overlay.query;

import io.opensphere.core.util.FontIconEnum;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.GenericFontIcon;

/** A definition of a query action that modifies the current selection mode. */
public class SelectionModeQueryActionDefinition extends AbstractQueryActionDefinition
{
    /** The selection mode associated with the query action. */
    private final SelectionMode myMode;

    /**
     * Creates a new definition of a query action that can modify a selection
     * mode.
     * 
     * @param label the label to store in the definition.
     * @param icon the icon to store in the definition.
     * @param mode the selection mode associated with the definition.
     */
    public SelectionModeQueryActionDefinition(String label, FontIconEnum icon, SelectionMode mode)
    {
        super(new GenericFontIcon(icon, IconUtil.DEFAULT_ICON_FOREGROUND), label);
        selectedIconProperty().set(new GenericFontIcon(icon, IconUtil.ICON_SELECTION_FOREGROUND));
        rolloverIconProperty().set(new GenericFontIcon(icon, IconUtil.DEFAULT_ICON_ROLLOVER));
        myMode = mode;
    }

    /**
     * Gets the value of the {@link #myMode} field.
     *
     * @return the value stored in the {@link #myMode} field.
     */
    public SelectionMode getMode()
    {
        return myMode;
    }
}
