package io.opensphere.controlpanels.layers.layersets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import io.opensphere.controlpanels.layers.layersets.LayerSetController.LayerSetControllerListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.mantle.data.ActiveGroupEntry;

/**
 * The panel that contains the buttons for manipulating layer sets.
 */
@SuppressWarnings("serial")
public class LayerSetButtonPanel extends AbstractHUDPanel implements LayerSetControllerListener, ActionListener
{
    /** The add layer set menu item. */
    private JMenuItem myAddLayerSetMenuItem;

    /** The controller. */
    private final transient LayerSetController myController;

    /** The default layer set menu item. */
    private LayerSetMenuItem myDefaultSetMenuItem;

    /** The manage layer sets menu item. */
    private transient JMenuItem myManageLayerSetsMenuItem;

    /** The split button. */
    private transient SplitButton mySplitButton;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new layer set button panel.
     *
     * @param toolbox the {@link Toolbox}
     */
    public LayerSetButtonPanel(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
        myController = new LayerSetController(toolbox);
        setBackground(getBackgroundColor());
        setLayout(new BorderLayout());
        add(getSplitButtonInternal(), BorderLayout.CENTER);
        buildMenuOptions();
        myController.addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (Utilities.sameInstance(e.getSource(), getManageLayerSetsMenuItem()))
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.layer-manager.button-row.manage-layer-set");
            myController.showLayerSetManager();
        }
        else if (Utilities.sameInstance(e.getSource(), getAddLayerSetMenuItem()))
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.layer-manager.button-row.add-layer-set");
            myController.saveCurrentSet();
        }
        else if (Utilities.sameInstance(e.getSource(), getDefaultSetMenuItem()))
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.layer-manager.button-row.select-default-layer-set");
            myController.restoreDefaultActiveSet();
        }
        else if (e.getSource() instanceof LayerSetMenuItem)
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.layer-manager.button-row.select-layer-set");
            myController.activateLayerSet(e.getActionCommand(), true);
        }
    }

    /**
     * Gets the import file button.
     *
     * @return the import file button
     */
    public SplitButton getSplitButton()
    {
        return getSplitButtonInternal();
    }

    @Override
    public void layerSetsChanged()
    {
        buildMenuOptions();
    }

    /**
     * Builds the file import menu options.
     */
    private void buildMenuOptions()
    {
        getSplitButton().removeAll();
        getSplitButton().add(getAddLayerSetMenuItem());
        getSplitButton().add(getManageLayerSetsMenuItem());

        getSplitButton().add(new JSeparator(SwingConstants.HORIZONTAL));
        getSplitButton().add(getDefaultSetMenuItem());

        List<String> setNames = myController.getSavedSetNames();
        if (setNames != null && !setNames.isEmpty())
        {
            Set<String> availableIds = myController.getAvailableGroupIds();
            for (String name : setNames)
            {
                List<ActiveGroupEntry> entries = myController.getSavedSetLayers(name);
                boolean someActive = false;
                for (ActiveGroupEntry entry : entries)
                {
                    if (availableIds.contains(entry.getId()))
                    {
                        someActive = true;
                        break;
                    }
                }
                LayerSetMenuItem item = new LayerSetMenuItem(name);
                item.addActionListener(this);
                setEnabled(someActive);
                getSplitButton().add(item);
            }
        }
    }

    /**
     * Gets the menu item.
     *
     * @return the add layer set menu item
     */
    private JMenuItem getAddLayerSetMenuItem()
    {
        if (myAddLayerSetMenuItem == null)
        {
            myAddLayerSetMenuItem = new JMenuItem("Add Layer Set...");
            myAddLayerSetMenuItem.addActionListener(this);
        }
        return myAddLayerSetMenuItem;
    }

    /**
     * Gets the menu item.
     *
     * @return the default layer set menu item
     */
    private LayerSetMenuItem getDefaultSetMenuItem()
    {
        if (myDefaultSetMenuItem == null)
        {
            myDefaultSetMenuItem = new LayerSetMenuItem(LayerSetController.DEFAULT_LAYER_SET_NAME);
            myDefaultSetMenuItem.addActionListener(this);
        }
        return myDefaultSetMenuItem;
    }

    /**
     * Gets the menu item.
     *
     * @return the manage layer sets menu item
     */
    private JMenuItem getManageLayerSetsMenuItem()
    {
        if (myManageLayerSetsMenuItem == null)
        {
            myManageLayerSetsMenuItem = new JMenuItem("Manage Layer Sets...");
            myManageLayerSetsMenuItem.addActionListener(this);
        }
        return myManageLayerSetsMenuItem;
    }

    /**
     * Gets the import file button.
     *
     * @return the import file button
     */
    private SplitButton getSplitButtonInternal()
    {
        if (mySplitButton == null)
        {
            mySplitButton = new SplitButton(null, null);
            IconUtil.setIcons(mySplitButton, IconType.STAR);
            mySplitButton.setToolTipText("Add a layer set");
            mySplitButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.layer-manager.button-row.add-layer-set");
                    myController.saveCurrentSet();
                }
            });
        }
        return mySplitButton;
    }

    /**
     * Marker class for a layer set menu item.
     */
    public static class LayerSetMenuItem extends JMenuItem
    {
        /**
         * Instantiates a new layer set menu item.
         *
         * @param name the name
         */
        public LayerSetMenuItem(String name)
        {
            super(name);
            setActionCommand(name);
        }
    }
}
