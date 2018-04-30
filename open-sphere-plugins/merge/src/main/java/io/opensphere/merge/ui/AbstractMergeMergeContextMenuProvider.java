package io.opensphere.merge.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenuItem;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.crust.DataTypeChecker;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.ContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.merge.controller.MergeController;
import io.opensphere.merge.model.JoinModel;

/**
 * An abstract implementation of a menu provider, used to abstract functions
 * common to single- and multi-selection menu providers. When only a single
 * layer is selected, the menu items are displayed, but disabled, and are
 * populated with tool tips explaining how to enable them.
 *
 * @param <CONTEXT_KEY_TYPE> the data type for which the concrete menu provider
 *            implementation is written. Must be an implementation of the
 *            {@link ContextKey} interface.
 */
public abstract class AbstractMergeMergeContextMenuProvider<CONTEXT_KEY_TYPE extends ContextKey>
        implements ContextMenuProvider<CONTEXT_KEY_TYPE>
{
    /** Message displayed to users when no data are selected. */
    protected static final String NO_DATA_MSG = "No features found for the selected layers.  "
            + "Please load data in the selected layers and try again.";

    /** Used to get the element counts. */
    private final DataElementCache myCache;

    /** The controller that drives the merge. */
    private final MergeController myMergeController;

    /** Manager for layers formed by joining. */
    private JoinManager myJoinManager;

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /** Registered callback for creating a new join config. */
    private Consumer<JoinModel> myJoinListener;

    /**
     * Constructs a new merge context menu provider.
     *
     * @param toolbox The system toolbox.
     * @param mergeController The merge controller.
     */
    public AbstractMergeMergeContextMenuProvider(Toolbox toolbox, MergeController mergeController)
    {
        myToolbox = toolbox;
        myCache = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class).getDataElementCache();
        myMergeController = mergeController;
    }

    /**
     * Sets the value of the {@link #myJoinListener} field.
     *
     * @param joinListener the value to store in the {@link #myJoinListener}
     *            field.
     */
    public void setJoinListener(Consumer<JoinModel> joinListener)
    {
        myJoinListener = joinListener;
    }

    /**
     * Sets the value of the {@link #myJoinManager} field.
     *
     * @param joinManager the value to store in the {@link #myJoinManager}
     *            field.
     */
    public void setJoinManager(JoinManager joinManager)
    {
        myJoinManager = joinManager;
    }

    /**
     * Gets the value of the {@link #myJoinListener} field.
     *
     * @return the value stored in the {@link #myJoinListener} field.
     */
    public Consumer<JoinModel> getJoinListener()
    {
        return myJoinListener;
    }

    /**
     * Gets the value of the {@link #myJoinManager} field.
     *
     * @return the value stored in the {@link #myJoinManager} field.
     */
    public JoinManager getJoinManager()
    {
        return myJoinManager;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, CONTEXT_KEY_TYPE key)
    {
        Collection<DataTypeInfo> dataTypes = getDataTypes(key);
        List<Component> mergeMenus = New.list(2);
        if (!dataTypes.isEmpty())
        {
            JMenuItem mergeMenuItem = SwingUtilities.newMenuItem("Merge...", e -> mergeRequest(dataTypes));
            JMenuItem joinMenuItem = SwingUtilities.newMenuItem("Join...", e -> joinRequest(dataTypes));

            mergeMenus.add(mergeMenuItem);
            mergeMenus.add(joinMenuItem);

            if (dataTypes.size() == 1)
            {
                mergeMenuItem.setEnabled(false);
                mergeMenuItem.setToolTipText("Select two or more layers to enable merge.");
                joinMenuItem.setEnabled(false);
                joinMenuItem.setToolTipText("Select two or more layers to enable join.");
            }
        }
        return mergeMenus;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.control.action.ContextMenuProvider#getPriority()
     */
    @Override
    public int getPriority()
    {
        return 20;
    }

    /**
     * Launches the merge ui.
     *
     * @param dataTypes The data types to merge.
     */
    protected void mergeRequest(Collection<DataTypeInfo> dataTypes)
    {
        if (!hasData(dataTypes))
        {
            Notify.info(NO_DATA_MSG, Method.POPUP);
            return;
        }

        MergeUI.showMergeDialog(dataTypes, myToolbox, myMergeController);
    }

    /**
     * Handles user selection of the "Join..." menu item.
     *
     * @param dataTypes those DataTypeInfo instances selected for joining
     */
    protected void joinRequest(Collection<? extends DataTypeInfo> dataTypes)
    {
        if (!hasData(dataTypes))
        {
            Notify.info(NO_DATA_MSG, Method.POPUP);
            return;
        }

        JoinGui gui = new JoinGui();
        JFXDialog dialog = GuiUtil.okCancelDialog(myToolbox, "Join Layers");
        dialog.setFxNode(GuiUtil.vScroll(gui.getMainPane()));
        dialog.setAcceptEar(() -> ThreadUtilities.runCpu(() -> handleJoin(gui.getModel())));
        dialog.setSize(new Dimension(450, 450));
        gui.setup(myToolbox, dialog);
        gui.setData(dataTypes);
        dialog.setVisible(true);
    }

    /**
     * Perform the join operation as instructed by the user.
     *
     * @param m a model containing the join parameters
     */
    protected void handleJoin(JoinModel m)
    {
        myJoinManager.handleJoin(m);
        if (myJoinListener != null)
        {
            myJoinListener.accept(m);
        }
    }

    /**
     * Checks to see if the layers have data to merge.
     *
     * @param dataTypes The layers to check for data.
     * @return True if at least one layer has data to merge
     */
    protected boolean hasData(Collection<? extends DataTypeInfo> dataTypes)
    {
        for (DataTypeInfo dataType : dataTypes)
        {
            if (myCache.getElementCountForType(dataType) > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the data types for the context key.
     *
     * @param key the context key
     * @return the data types
     */
    private Collection<DataTypeInfo> getDataTypes(CONTEXT_KEY_TYPE key)
    {
        Collection<DataTypeInfo> dataTypes = New.list(key.getDataTypes());
        for (DataGroupInfo group : key.getDataGroups())
        {
            dataTypes.addAll(group.getMembers(false));
        }

        List<DataTypeInfo> featureTypes = New.list();
        for (DataTypeInfo layer : dataTypes)
        {
            if (!featureTypes.contains(layer) && DataTypeChecker.isFeatureType(layer))
            {
                featureTypes.add(layer);
            }
        }

        return featureTypes;
    }
}
