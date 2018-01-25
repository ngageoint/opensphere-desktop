package io.opensphere.imagery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * The Class ImageryDataGroupInfo.
 */
public class ImageryDataGroupInfo extends DefaultDataGroupInfo
{
    /** The ContextMenuProvider. */
    private final ContextMenuProvider<DataGroupContextKey> myContextMenuProvider = new ContextMenuProvider<DataGroupContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(final String contextId, final DataGroupContextKey key)
        {
            if (Utilities.sameInstance(key.getDataGroup(), ImageryDataGroupInfo.this))
            {
                if (key.getDataType() != null)
                {
                    return getDataTypeMenu(key);
                }
                else
                {
                    return getDataGroupMenu(key);
                }
            }
            return null;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }

        /**
         * Get the menu for a data group.
         *
         * @param key The menu context key.
         * @return The menu items.
         */
        private List<JMenuItem> getDataGroupMenu(DataGroupContextKey key)
        {
            List<JMenuItem> items = New.list();
            JMenuItem zoomToGroupItem = new JMenuItem("Zoom To Group");
            zoomToGroupItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (myImagerySourceGroup != null)
                    {
                        myImagerySourceGroup.zoomToGroup(getToolbox());
                    }
                }
            });
            items.add(zoomToGroupItem);

            JMenuItem centerOnGroupItem = new JMenuItem("Center On Group");
            centerOnGroupItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (myImagerySourceGroup != null)
                    {
                        myImagerySourceGroup.centerOnGroup(getToolbox(), false);
                    }
                }
            });
            items.add(centerOnGroupItem);

            JMenuItem clearGroupCache = new JMenuItem("Clear Image Group Cache");
            clearGroupCache.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (myImagerySourceGroup != null)
                    {
                        if (myImagerySourceGroup.getImageryEnvoy() != null)
                        {
                            myImagerySourceGroup.getImageryEnvoy().clearImageCache();
                        }
                        else
                        {
                            myImagerySourceGroup.cleanCache(getToolbox());
                        }
                    }
                }
            });
            items.add(clearGroupCache);

            if (myImagerySourceGroup != null)
            {
                final boolean isShown = myImagerySourceGroup.isGroupBoundsVisible();

                JMenuItem showHideGroupBoundsMI = new JMenuItem(isShown ? "Hide Boundary Rectangle" : "Show Boundary Rectangle");
                showHideGroupBoundsMI.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (myImagerySourceGroup != null)
                        {
                            myImagerySourceGroup.showGroupBounds(getToolbox(), !isShown);
                        }
                    }
                });
                items.add(showHideGroupBoundsMI);
            }
            return items;
        }

        /**
         * Get the menu for a single data type.
         *
         * @param key The menu context key.
         * @return The menu items.
         */
        private List<JMenuItem> getDataTypeMenu(DataGroupContextKey key)
        {
            final ImageryDataTypeInfo dti = (ImageryDataTypeInfo)key.getDataType();
            List<JMenuItem> menuItems = New.list();
            JMenuItem zoomToImageItem = new JMenuItem("Zoom To Image");
            zoomToImageItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    dti.getImageryFileSource().zoomToImage(getToolbox());
                }
            });
            menuItems.add(zoomToImageItem);

            JMenuItem centerOnItem = new JMenuItem("Center On Image");
            centerOnItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    dti.getImageryFileSource().centerOnImage(getToolbox(), false);
                }
            });
            menuItems.add(centerOnItem);

            JMenuItem clearCacheItem = new JMenuItem("Clear Image Cache");
            clearCacheItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (dti.getImagerySourceGroup() != null && dti.getImagerySourceGroup().getImageryEnvoy() != null)
                    {
                        myImagerySourceGroup.getImageryEnvoy().clearImageCache(Collections.singleton(dti.getTypeKey()));
                    }
                    else
                    {
                        dti.getImageryFileSource().cleanCache(getToolbox().getDataRegistry());
                    }
                }
            });
            menuItems.add(clearCacheItem);
            return menuItems;
        }
    };

    /** The Imagery source group. */
    private final ImagerySourceGroup myImagerySourceGroup;

    /**
     * Instantiates a new imagery data group info.
     *
     * @param rootNode the root node
     * @param aToolbox the a toolbox
     * @param id the id
     * @param displayName the display name
     * @param sourceGroup the source group
     */
    public ImageryDataGroupInfo(boolean rootNode, Toolbox aToolbox, String id, String displayName, ImagerySourceGroup sourceGroup)
    {
        super(rootNode, aToolbox, "Imagery", id, displayName);
        myImagerySourceGroup = sourceGroup;
        // This is never de-registered, it relies on the context manager using a
        // weak reference.
        ContextActionManager manager = aToolbox.getUIRegistry().getContextActionManager();
        manager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                myContextMenuProvider);
    }

    /**
     * Gets the imagery source group.
     *
     * @return the imagery source group
     */
    public final ImagerySourceGroup getImagerySourceGroup()
    {
        return myImagerySourceGroup;
    }
}
