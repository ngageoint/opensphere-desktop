package io.opensphere.mantle.data.geom.style.dialog;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataTypeNodeUserObject.
 */
public class DataTypeNodeUserObject
{
    /** The Checked. */
    private boolean myChecked;

    /** The DGI. */
    private final DataGroupInfo myDGI;

    /** The Display name. */
    private final String myDisplayName;

    /** The DTI. */
    private final DataTypeInfo myDTI;

    /** The Node key. */
    private final String myNodeKey;

    /** The Node listener. */
    private final NodeListener myNodeListener;

    /** The Node type. */
    private final NodeType myNodeType;

    /** The Selected. */
    private boolean mySelected;

    /**
     * Creates the node key.
     *
     * @param type the {@link NodeType}
     * @param dgi the {@link DataGroupInfo} (can be null)
     * @param dti the {@link DataTypeInfo} (can be null)
     * @param displayName the display name (can be null)(but not reccomended )
     * @return the string
     */
    public static String createNodeKey(NodeType type, DataGroupInfo dgi, DataTypeInfo dti, String displayName)
    {
        Utilities.checkNull(type, "type");
        StringBuilder sb = new StringBuilder(type.name());
        if (dgi != null)
        {
            sb.append("@:@DGI_ID[");
            sb.append(dgi.getId());
            sb.append(']');
        }
        if (dti != null)
        {
            sb.append("@:@DTI_ID[");
            sb.append(dti.getTypeKey());
            sb.append(']');
        }
        if (displayName != null)
        {
            sb.append("@:@");
            sb.append(displayName);
        }
        return sb.toString();
    }

    /**
     * Decomposes the node key into a map that is accessed by the
     * {@link NodeKeyComponent} enum.
     *
     * Note that some values may not exist as {@link NodeKeyComponent}
     * .DATA_GROUP_INFO_ID and {@link NodeKeyComponent}.DATA_TYPE_INFO_ID are
     * optional.
     *
     * @param nodeKey the node key
     * @return the {@link Map} of {@link NodeKeyComponent} to value.
     *
     */
    public static Map<NodeKeyComponent, String> decomposeNodeKey(String nodeKey)
    {
        Map<NodeKeyComponent, String> map = New.map(4);
        if (StringUtils.isNotEmpty(nodeKey))
        {
            String[] parts = nodeKey.split("@:@");
            if (parts != null && parts.length > 0)
            {
                map.put(NodeKeyComponent.NODE_TYPE, parts[0]);
                if (parts.length > 1)
                {
                    for (int i = 1; i < parts.length; i++)
                    {
                        String val = parts[i];
                        if (val != null)
                        {
                            if (val.startsWith("DGI_ID["))
                            {
                                map.put(NodeKeyComponent.DATA_GROUP_INFO_ID, val.substring(7, val.length() - 1));
                            }
                            else if (val.startsWith("DTI_ID["))
                            {
                                map.put(NodeKeyComponent.DATA_TYPE_INFO_ID, val.substring(7, val.length() - 1));
                            }
                            else
                            {
                                map.put(NodeKeyComponent.DISPLAY_NAME, val);
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * Instantiates a new node user object proxy.
     *
     * @param displayName the display name for the node.
     * @param nodeType the {@link NodeType}
     * @param dgi the {@link DataGroupInfo} reference if applicable ( may be
     *            null ).
     * @param dti the {@link DataTypeInfo} reference if applicable (may be
     *            null).
     * @param listener the {@link NodeListener}
     */
    public DataTypeNodeUserObject(String displayName, NodeType nodeType, DataGroupInfo dgi, DataTypeInfo dti,
            NodeListener listener)
    {
        myDisplayName = displayName;
        myNodeType = nodeType;
        myDGI = dgi;
        myDTI = dti;
        myNodeKey = createNodeKey(myNodeType, myDGI, myDTI, myDisplayName);
        myNodeListener = listener;
    }

    /**
     * Instantiates a new node user object proxy.
     *
     * @param displayName the display name for the node.
     * @param nodeType the {@link NodeType}
     * @param listener the {@link NodeListener}
     */
    public DataTypeNodeUserObject(String displayName, NodeType nodeType, NodeListener listener)
    {
        this(displayName, nodeType, null, null, listener);
    }

    /**
     * Gets the data group info.
     *
     * @return the data group info
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDGI;
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return myDTI;
    }

    /**
     * Gets the node type.
     *
     * @return the node type
     */
    public NodeType getNodeType()
    {
        return myNodeType;
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    public boolean isSelected()
    {
        return mySelected;
    }

    /**
     * Sets the selected.
     *
     * @param selected the new selected
     */
    public void setSelected(boolean selected)
    {
        if (mySelected != selected)
        {
            mySelected = selected;
            fireNodeSelectStateChagned(selected);
        }
    }

    /**
     * Sets the selected no event.
     *
     * @param selected the new selected no event
     */
    public void setSelectedNoEvent(boolean selected)
    {
        mySelected = selected;
    }

    @Override
    public String toString()
    {
        return myDisplayName;
    }

    /**
     * Fire node check state changed.
     *
     * @param checked true if checked, false if not.
     */
    protected void fireNodeCheckStateChanged(final boolean checked)
    {
        if (myNodeListener != null)
        {
            EventQueueUtilities.runOnEDTAndWait(() -> myNodeListener.nodeCheckStateChanged(DataTypeNodeUserObject.this, checked));
        }
    }

    /**
     * Fire node select state changed.
     *
     * @param selected true if selected, false if not
     */
    protected void fireNodeSelectStateChagned(final boolean selected)
    {
        if (myNodeListener != null)
        {
            EventQueueUtilities.runOnEDTAndWait(() -> myNodeListener.nodeSelectStateChanged(DataTypeNodeUserObject.this, selected));
        }
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    protected String getDisplayName()
    {
        return myDisplayName;
    }

    /**
     * Gets the node key.
     *
     * @return the node key
     */
    protected String getNodeKey()
    {
        return myNodeKey;
    }

    /**
     * Checks if is checked.
     *
     * @return true, if is checked
     */
    protected boolean isChecked()
    {
        return myChecked;
    }

    /**
     * Sets the checked.
     *
     * @param checked the new checked
     * @param event the event
     */
    protected void setChecked(boolean checked, boolean event)
    {
        if (myChecked != checked)
        {
            myChecked = checked;
            if (event)
            {
                fireNodeCheckStateChanged(checked);
            }
        }
    }

    /**
     * The Enum NodeKeyComponent.
     */
    public enum NodeKeyComponent
    {
        /** The DATA_GROUP_INFO_ID. */
        DATA_GROUP_INFO_ID,

        /** The DATA_TYPE_INFO_ID. */
        DATA_TYPE_INFO_ID,

        /** The DISPLAY_NAME. */
        DISPLAY_NAME,

        /** The NODE_TYPE. */
        NODE_TYPE
    }

    /**
     * Listener interface for node changes.
     */
    public interface NodeListener
    {
        /**
         * Node check state changed.
         *
         * @param node the node
         * @param checked the checked
         */
        void nodeCheckStateChanged(DataTypeNodeUserObject node, boolean checked);

        /**
         * Node select state changed.
         *
         * @param node the node
         * @param selected the selected
         */
        void nodeSelectStateChanged(DataTypeNodeUserObject node, boolean selected);
    }

    /**
     * The Enum NodeType.
     */
    public enum NodeType
    {
        /** The COLLECTION_ROOT. */
        COLLECTION_ROOT,

        /** The DEFAULT_ROOT_FEATURE. */
        DEFAULT_ROOT_FEATURE,

        /** The DEFAULT_ROOT_TILE. */
        DEFAULT_ROOT_TILE,

        /** The root type for heatmap groups. */
        DEFAULT_ROOT_HEATMAP,

        /** The FEATURE_TYPE_LEAF. */
        FEATURE_TYPE_LEAF,

        /** The TILE_TYPE_LEAF. */
        TILE_TYPE_LEAF,

        /** The type for heatmap leaves. */
        HEATMAP_TYPE_LEAF;

        /**
         * Checks if is collection root.
         *
         * @return true, if is collection root
         */
        boolean isCollectionRoot()
        {
            return this == COLLECTION_ROOT;
        }

        /**
         * Checks if is default type.
         *
         * @return true, if is default type
         */
        boolean isDefaultType()
        {
            return this == DEFAULT_ROOT_TILE || this == DEFAULT_ROOT_FEATURE || this == NodeType.DEFAULT_ROOT_HEATMAP;
        }

        /**
         * Checks if is leaf type.
         *
         * @return true, if is leaf type
         */
        boolean isLeafType()
        {
            return this == FEATURE_TYPE_LEAF || this == TILE_TYPE_LEAF || this == NodeType.HEATMAP_TYPE_LEAF;
        }
    }
}
