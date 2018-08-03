package io.opensphere.overlay.query;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.FontIconEnum;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.SplitButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 * 
 */
public class QuerySelectorSplitButton extends SplitButton
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = 6753950820522867830L;

    private final ObjectProperty<SelectionMode> myCurrentSelectionMode;

    /** The property in which the toggle state is maintained. */
    private final BooleanProperty myToggledProperty;

    private static final Map<SelectionMode, SelectionModeMetadata> myModeIcons = Map.of(SelectionMode.BOUNDING_BOX,
            new SelectionModeMetadata("Box", AwesomeIconRegular.SQUARE, SelectionMode.BOUNDING_BOX), SelectionMode.CIRCLE,
            new SelectionModeMetadata("Circle", AwesomeIconRegular.CIRCLE, SelectionMode.CIRCLE), SelectionMode.POLYGON,
            new SelectionModeMetadata("Polygon", AwesomeIconRegular.STAR, SelectionMode.POLYGON), SelectionMode.LINE,
            new SelectionModeMetadata("Line", AwesomeIconSolid.LONG_ARROW_ALT_RIGHT, SelectionMode.LINE));

    /**
     * @param text
     * @param icon
     * @param drawSplit
     */
    public QuerySelectorSplitButton()
    {
        super(null, new GenericFontIcon(AwesomeIconRegular.SQUARE, IconUtil.DEFAULT_ICON_FOREGROUND), true);

        myCurrentSelectionMode = new ConcurrentObjectProperty<>(SelectionMode.BOUNDING_BOX);
        myCurrentSelectionMode.addListener((obs, ov, nv) ->
        {
            setIcon(myModeIcons.get(nv).getDefaultIcon());
        });

        myToggledProperty = new ConcurrentBooleanProperty(false);
        myToggledProperty.addListener((obs, ov, nv) ->
        {
            super.setSelected(nv);
            if (nv)
            {
                setIcon(myModeIcons.get(myCurrentSelectionMode.get()).getSelectedIcon());
            }
            else
            {
                setIcon(myModeIcons.get(myCurrentSelectionMode.get()).getDefaultIcon());
            }

        });

        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.BOUNDING_BOX));
        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.CIRCLE));
        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.POLYGON));
        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.LINE));

        addSeparator();
        addMenuItem(new JMenuItem("Choose Area", new GenericFontIcon(AwesomeIconSolid.LIST, IconUtil.DEFAULT_ICON_FOREGROUND)));
        addMenuItem(new JMenuItem("Enter Coordinates",
                new GenericFontIcon(AwesomeIconSolid.CALCULATOR, IconUtil.DEFAULT_ICON_FOREGROUND)));
        addMenuItem(
                new JMenuItem("Country Border", new GenericFontIcon(AwesomeIconSolid.GLOBE, IconUtil.DEFAULT_ICON_FOREGROUND)));
        addMenuItem(new JMenuItem("Whole World", new GenericFontIcon(AwesomeIconRegular.MAP, IconUtil.DEFAULT_ICON_FOREGROUND)));
    }

    /**
     * Gets the value of the {@link #myCurrentSelectionMode} field.
     *
     * @return the value stored in the {@link #myCurrentSelectionMode} field.
     */
    public ObjectProperty<SelectionMode> currentSelectionModeProperty()
    {
        return myCurrentSelectionMode;
    }

    /**
     * Gets the value of the {@link #myToggledProperty} field.
     *
     * @return the value stored in the {@link #myToggledProperty} field.
     */
    public BooleanProperty toggledProperty()
    {
        return myToggledProperty;
    }

    private void createIconModifyingMenuItem(SelectionModeMetadata metadata)
    {
        JMenuItem item = new JMenuItem(metadata.getText());
        item.setIcon(metadata.getDefaultIcon());
        item.setSelectedIcon(metadata.getSelectedIcon());
        item.addActionListener(e ->
        {
            setIcon(metadata.getSelectedIcon());
            toggledProperty().set(true);
            currentSelectionModeProperty().set(metadata.getMode());
        });

        addMenuItem(item);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.AbstractButton#setSelected(boolean)
     */
    @Override
    public void setSelected(boolean b)
    {
        // intercepts the super implementation to bind behavior to the toggled
        // property.
        toggledProperty().set(b);
    }

    private static class SelectionModeMetadata
    {
        private String myText;

        private Icon myDefaultIcon;

        private Icon mySelectedIcon;

        private SelectionMode myMode;

        /**
         * @param text
         * @param selectedIcon
         * @param deselectedIcon
         * @param mode
         */
        public SelectionModeMetadata(String text, FontIconEnum icon, SelectionMode mode)
        {
            super();
            myText = text;
            myDefaultIcon = new GenericFontIcon(icon, IconUtil.DEFAULT_ICON_FOREGROUND);
            mySelectedIcon = new GenericFontIcon(icon, IconUtil.ICON_SELECTION_FOREGROUND);
            myMode = mode;
        }

        /**
         * Gets the value of the {@link #myText} field.
         *
         * @return the value stored in the {@link #myText} field.
         */
        public String getText()
        {
            return myText;
        }

        /**
         * Gets the value of the {@link #mySelectedIcon} field.
         *
         * @return the value stored in the {@link #mySelectedIcon} field.
         */
        public Icon getSelectedIcon()
        {
            return mySelectedIcon;
        }

        /**
         * Gets the value of the {@link #myDefaultIcon} field.
         *
         * @return the value stored in the {@link #myDefaultIcon} field.
         */
        public Icon getDefaultIcon()
        {
            return myDefaultIcon;
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
}
