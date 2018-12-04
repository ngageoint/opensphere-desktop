package io.opensphere.myplaces.specific;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.FontIconEnum;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.kml.gx.Track;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.DataTypeInfoMyPlaceChangedEvent;

/**
 * Provides a set of menu items common to points to be used in the
 * DataGroupContext and the GeometryContext.
 */
public abstract class MyPointsMenuItemProvider implements ActionListener
{
    /** The Menu components. */
    private final Map<ItemType, JMenuItem> myMenuItems = New.map();

    /**
     * Instantiates a new places context menu provider.
     */
    public MyPointsMenuItemProvider()
    {
        EventQueueUtilities.invokeLater(() ->
        {
            for (ItemType type : ItemType.values())
            {
                JMenuItem item = new JMenuItem(type.toString());
                if (type.getIcon() != null)
                {
                    item.setIcon(type.getIcon());
                }
                item.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                item.addActionListener(e -> actionPerformed(e));
                myMenuItems.put(type, item);
            }
        });
    }

    /**
     * Adds menu items for the map point and the callout bubble.
     *
     * @param menuItems the menu items
     * @param result the result
     * @param type the type
     */
    public void addMenuItems(List<Component> menuItems, Placemark result, ItemType type)
    {
        if (!(result.getGeometry() instanceof Track) && (type.equals(ItemType.HIDE_POINT) || type.equals(ItemType.SHOW_POINT)))
        {
            for (Data data : result.getExtendedData().getData())
            {
                if (data.getName().equals(Constants.IS_FEATURE_ON_ID))
                {
                    if (Boolean.parseBoolean(data.getValue()))
                    {
                        menuItems.add(getMenuItems().get(ItemType.HIDE_POINT));
                    }
                    else
                    {
                        menuItems.add(getMenuItems().get(ItemType.SHOW_POINT));
                    }
                    break;
                }
            }
        }

        if (type.equals(ItemType.HIDE_BUBBLE) || type.equals(ItemType.SHOW_BUBBLE))
        {
            for (Data data : result.getExtendedData().getData())
            {
                if (data.getName().equals(Constants.IS_ANNOHIDE_ID))
                {
                    if (Boolean.parseBoolean(data.getValue()))
                    {
                        menuItems.add(getMenuItems().get(ItemType.SHOW_BUBBLE));
                    }
                    else
                    {
                        menuItems.add(getMenuItems().get(ItemType.HIDE_BUBBLE));
                    }
                    break;
                }
            }
        }
    }

    /**
     * Do menu item action.
     *
     * @param dataType The data type.
     * @param mark the mark
     * @param dataParam the data param
     * @param value the value
     */
    public void doMenuItemAction(DataTypeInfo dataType, Placemark mark, String dataParam, boolean value)
    {
        if (mark == null)
        {
            return;
        }
        // find the "Data" called dataParam and set its boolean value
        mark.getExtendedData().getData().stream().filter(d -> d.getName().equals(dataParam)).findFirst()
                .ifPresent(d -> d.setValue(String.valueOf(value)));
        dataType.fireChangeEvent(new DataTypeInfoMyPlaceChangedEvent(dataType, this));
    }

    /**
     * Gets the menu items.
     *
     * @return the menu items
     */
    public Map<ItemType, JMenuItem> getMenuItems()
    {
        return myMenuItems;
    }

    /**
     * The Enum ItemType.
     */
    public enum ItemType
    {
        /** The EDIT item. */
        EDIT("Edit Place", AwesomeIconSolid.PENCIL_ALT, true, false, false, true),

        /** The DELETE item. */
        DELETE("Remove Feature", AwesomeIconSolid.TIMES, true, false, true, true),

        /** The HIDE_POINT item. */
        HIDE_POINT("Hide Feature", AwesomeIconSolid.EYE_SLASH, false, false, false, true),

        /** The SHOW_POINT item. */
        SHOW_POINT("Show Feature", AwesomeIconSolid.EYE, false, false, false, true),

        /** The HIDE_BUBBLE item. */
        HIDE_BUBBLE("Hide Bubble", AwesomeIconSolid.EYE_SLASH, false, false, false, true),

        /** The SHOW_BUBBLE item. */
        SHOW_BUBBLE("Show Bubble", AwesomeIconSolid.EYE, false, false, false, true);

        /** The Label. */
        private final String myLabel;

        /** The Label. */
        private final Icon myIcon;

        /** If this item is required or not. */
        private boolean myIsRequired;

        /** The Is group. */
        private boolean myIsGroup;

        /**
         * Indicates if the data group info is needed.
         */
        private boolean myNeedsDataGroup;

        /**
         * Indicates if the data type info is needed.
         */
        private boolean myNeedsDataType;

        /**
         * Instantiates a new item type.
         *
         * @param label the label
         * @param required the required
         * @param isGroup the is group
         * @param needsDataGroup Indicates if the data group info is needed.
         * @param needsDataType Indicates if the data type info is needed.
         */
        ItemType(String label, FontIconEnum icon, boolean required, boolean isGroup, boolean needsDataGroup,
                boolean needsDataType)
        {
            myLabel = label;
            myIcon = new GenericFontIcon(icon, Color.WHITE);
            myIsRequired = required;
            myIsGroup = isGroup;
            myNeedsDataGroup = needsDataGroup;
            myNeedsDataType = needsDataType;
        }

        /**
         * Checks if is group.
         *
         * @return true, if is group
         */
        public boolean isGroup()
        {
            return myIsGroup;
        }

        /**
         * Indicates if the data group info is needed.
         *
         * @return True if the data group is needed false otherwise.
         */
        public boolean isNeedsDataGroup()
        {
            return myNeedsDataGroup;
        }

        /**
         * Indicates if the data type info is needed.
         *
         * @return True if the data type is needed false otherwise.
         */
        public boolean isNeedsDataType()
        {
            return myNeedsDataType;
        }

        /**
         * Checks if is required.
         *
         * @return true, if is required
         */
        public boolean isRequired()
        {
            return myIsRequired;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }

        /**
         * Gets the value of the {@link #myIcon} field.
         *
         * @return the value stored in the {@link #myIcon} field.
         */
        public Icon getIcon()
        {
            return myIcon;
        }
    }
}
