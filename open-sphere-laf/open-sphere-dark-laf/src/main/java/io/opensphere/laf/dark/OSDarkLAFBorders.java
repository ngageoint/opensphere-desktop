package io.opensphere.laf.dark;

import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;

import io.opensphere.laf.dark.border.OSDarkLAFButtonBorder;
import io.opensphere.laf.dark.border.OSDarkLAFCellFocusBorder;
import io.opensphere.laf.dark.border.OSDarkLAFComboButtonBorder;
import io.opensphere.laf.dark.border.OSDarkLAFComboEditorBorder;
import io.opensphere.laf.dark.border.OSDarkLAFEmptyGenBorder;
import io.opensphere.laf.dark.border.OSDarkLAFGenBorder;
import io.opensphere.laf.dark.border.OSDarkLAFInternalFrameBorder;
import io.opensphere.laf.dark.border.OSDarkLAFMenuBarBorder;
import io.opensphere.laf.dark.border.OSDarkLAFMenuBorder;
import io.opensphere.laf.dark.border.OSDarkLAFPopupMenuBorder;
import io.opensphere.laf.dark.border.OSDarkLAFRolloverButtonBorder;
import io.opensphere.laf.dark.border.OSDarkLAFScrollPaneBorder;
import io.opensphere.laf.dark.border.OSDarkLAFTextFieldBorder;
import io.opensphere.laf.dark.border.OSDarkLAFThinGenBorder;
import io.opensphere.laf.dark.border.OSDarkLAFToolBarBorder;
import io.opensphere.laf.dark.border.OSDarkLAFToolTipBorder;

/**
 * A factory class in which borders for the look and feel are provided.
 */
public class OSDarkLAFBorders extends MetalBorders
{
    /**
     * The border used for buttons.
     */
    private static Border buttonBorder;

    /**
     * The border used for popup menus.
     */
    private static Border popupMenuBorder;

    /**
     * The border used for buttons when the user rolls over the component.
     */
    private static Border buttonRolloverBorder;

    /**
     * The border used for scroll panes.
     */
    private static Border scrollPaneBorder;

    /**
     * The border used for internal frames.
     */
    private static Border internalFrameBorder;

    /**
     * The border used for menu bars.
     */
    private static Border menuBarBorder;

    /**
     * The border used for tool bars.
     */
    private static Border toolBarBorder;

    /**
     * The border used for cells when the cell has focus.
     */
    private static Border cellFocusBorder;

    /**
     * The generic border used for items that don't have a specifically defined
     * border.
     */
    private static Border genericBorder;

    /**
     * An empty border.
     */
    private static Border emptyBorder;

    /**
     * The generic thin border used for items that don't specifically define
     * their own border.
     */
    private static Border generalThinBorder;

    /**
     * The border used for menus when they don't specifically define their own
     * border.
     */
    private static Border generalMenuBorder;

    /**
     * The border used for text fields.
     */
    private static Border generalTextFieldBorder;

    /**
     * The border used for combobox editors.
     */
    private static Border generalComboEditorBorder;

    /**
     * The border used for combobox buttons.
     */
    private static Border generalComboButtonBorder;

    /**
     * The border used for tooltips.
     */
    private static Border generalToolTipBorder;

    /**
     * Gets the value of the {@link #buttonBorder} field.
     *
     * @return the value stored in the {@link #buttonBorder} field.
     */
    public static Border getButtonBorder()
    {
        if (null == buttonBorder)
        {
            buttonBorder = new BorderUIResource.CompoundBorderUIResource(new OSDarkLAFButtonBorder(),
                    new BasicBorders.MarginBorder());
        }
        return buttonBorder;
    }

    /**
     * Gets the value of the {@link #cellFocusBorder} field.
     *
     * @return the value stored in the {@link #cellFocusBorder} field.
     */
    public static Border getCellFocusBorder()
    {
        if (null == cellFocusBorder)
        {
            cellFocusBorder = new OSDarkLAFCellFocusBorder();
        }
        return cellFocusBorder;
    }

    /**
     * Gets the value of the {@link #generalComboButtonBorder} field.
     *
     * @return the value stored in the {@link #generalComboButtonBorder} field.
     */
    public static Border getComboButtonBorder()
    {
        if (null == generalComboButtonBorder)
        {
            generalComboButtonBorder = new OSDarkLAFComboButtonBorder();
        }
        return generalComboButtonBorder;
    }

    /**
     * Gets the value of the {@link #generalComboEditorBorder} field.
     *
     * @return the value stored in the {@link #generalComboEditorBorder} field.
     */
    public static Border getComboEditorBorder()
    {
        if (null == generalComboEditorBorder)
        {
            generalComboEditorBorder = new OSDarkLAFComboEditorBorder();
        }
        return generalComboEditorBorder;
    }

    /**
     * Gets the value of the {@link #emptyBorder} field.
     *
     * @return the value stored in the {@link #emptyBorder} field.
     */
    public static Border getEmptyGenBorder()
    {
        if (null == emptyBorder)
        {
            emptyBorder = new OSDarkLAFEmptyGenBorder();
        }
        return emptyBorder;
    }

    /**
     * Gets the value of the {@link #genericBorder} field.
     *
     * @return the value stored in the {@link #genericBorder} field.
     */
    public static Border getGenBorder()
    {
        if (null == genericBorder)
        {
            genericBorder = new OSDarkLAFGenBorder();
        }
        return genericBorder;
    }

    /**
     * Gets the value of the {@link #generalMenuBorder} field.
     *
     * @return the value stored in the {@link #generalMenuBorder} field.
     */
    public static Border getGenMenuBorder()
    {
        if (null == generalMenuBorder)
        {
            generalMenuBorder = new OSDarkLAFMenuBorder();
        }
        return generalMenuBorder;
    }

    /**
     * Gets the value of the {@link #internalFrameBorder} field.
     *
     * @return the value stored in the {@link #internalFrameBorder} field.
     */
    public static Border getInternalFrameBorder()
    {
        if (null == internalFrameBorder)
        {
            internalFrameBorder = new OSDarkLAFInternalFrameBorder();
        }
        return internalFrameBorder;
    }

    /**
     * Gets the value of the {@link #menuBarBorder} field.
     *
     * @return the value stored in the {@link #menuBarBorder} field.
     */
    public static Border getMenuBarBorder()
    {
        if (null == menuBarBorder)
        {
            menuBarBorder = new OSDarkLAFMenuBarBorder();
        }
        return menuBarBorder;
    }

    /**
     * Gets the value of the {@link #popupMenuBorder} field.
     *
     * @return the value stored in the {@link #popupMenuBorder} field.
     */
    public static Border getPopupMenuBorder()
    {
        if (null == popupMenuBorder)
        {
            popupMenuBorder = new OSDarkLAFPopupMenuBorder();
        }
        return popupMenuBorder;
    }

    /**
     * Gets the value of the {@link #buttonRolloverBorder} field.
     *
     * @return the value stored in the {@link #buttonRolloverBorder} field.
     */
    public static Border getRolloverButtonBorder()
    {
        if (null == buttonRolloverBorder)
        {
            buttonRolloverBorder = new OSDarkLAFRolloverButtonBorder();
        }
        return buttonRolloverBorder;
    }

    /**
     * Gets the value of the {@link #scrollPaneBorder} field.
     *
     * @return the value stored in the {@link #scrollPaneBorder} field.
     */
    public static Border getScrollPaneBorder()
    {
        if (null == scrollPaneBorder)
        {
            scrollPaneBorder = new OSDarkLAFScrollPaneBorder();
        }
        return scrollPaneBorder;
    }

    /**
     * Returns a border instance for a text component.
     */
    /**
     * Gets the value of the {@link #generalTextFieldBorder} field.
     *
     * @return the value stored in the {@link #generalTextFieldBorder} field.
     */
    public static Border getTextFieldBorder()
    {
        if (null == generalTextFieldBorder)
        {
            generalTextFieldBorder = new OSDarkLAFTextFieldBorder();
        }
        return generalTextFieldBorder;
    }

    /**
     * Gets the value of the {@link #generalThinBorder} field.
     *
     * @return the value stored in the {@link #generalThinBorder} field.
     */
    public static Border getThinGenBorder()
    {
        if (null == generalThinBorder)
        {
            generalThinBorder = new OSDarkLAFThinGenBorder();
        }
        return generalThinBorder;
    }

    /**
     * Gets the value of the {@link #toolBarBorder} field.
     *
     * @return the value stored in the {@link #toolBarBorder} field.
     */
    public static Border getToolBarBorder()
    {
        if (null == toolBarBorder)
        {
            toolBarBorder = new OSDarkLAFToolBarBorder();
        }
        return toolBarBorder;
    }

    /**
     * Gets the value of the {@link #generalToolTipBorder} field.
     *
     * @return the value stored in the {@link #generalToolTipBorder} field.
     */
    public static Border getToolTipBorder()
    {
        if (null == generalToolTipBorder)
        {
            generalToolTipBorder = new OSDarkLAFToolTipBorder();
        }
        return generalToolTipBorder;
    }
}
