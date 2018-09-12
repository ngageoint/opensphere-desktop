package io.opensphere.merge.ui;

import java.awt.Dimension;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.merge.controller.MergeController;
import io.opensphere.merge.model.JoinModel;
import io.opensphere.merge.model.MergeModel;
import io.opensphere.merge.model.MergePrefs;
import io.opensphere.merge.model.MergePrefs.Join;

/**
 * Facilitates management of existing join/merge configurations.
 */
public class ConfigGui
{
    /** System Toolbox. */
    private final Toolbox myToolbox;

    /** The DataGroupController. */
    private final DataGroupController myDataGroupController;

    /** Used to get the element counts. */
    private final DataElementCache myDataCache;

    /** This GUI's host dialog. */
    private final JFXDialog myDialog;

    /** Manager for layers formed by joining. */
    private final JoinManager myJoinManager;

    /** The merge controller. */
    private final MergeController myMergeController;

    /** GUI root. */
    private final ScrollPane myMainPane;

    /** Persistent data model. */
    private MergePrefs myPreferences;

    /** Rows; each one represents a join configuration. */
    private final ObservableList<ConfigUiRow> myDataRows = FXCollections.observableArrayList();

    /** Callback for saving (this class does not handle persistence). */
    private final Runnable mySaveCallback;

    /**
     * Constructs ConfigGui.
     *
     * @param tb the toolbox
     * @param jm the join manager
     * @param mergeController the merge controller
     * @param callback the save callback
     */
    public ConfigGui(Toolbox tb, JoinManager jm, MergeController mergeController, Runnable callback)
    {
        myToolbox = tb;
        myJoinManager = jm;
        myMergeController = mergeController;
        mySaveCallback = callback;

        MantleToolbox mtb = tb.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myDataGroupController = mtb.getDataGroupController();
        myDataCache = mtb.getDataElementCache();

        ListView<ConfigUiRow> listView = new ListView<>(myDataRows);
        listView.setCellFactory(view -> new MergeFormatCell());
        // Ignore all events that the ListView would otherwise handle.
        listView.setSelectionModel(null);
        listView.setEventDispatcher(null);

        myMainPane = GuiUtil.vScroll(listView);

        myDialog = GuiUtil.okDialog(tb, "Joins/Merges");
        myDialog.setSize(new Dimension(450, 450));
    }

    /** Show the dialog, creating it if necessary. */
    public void show()
    {
        if (!myDialog.isVisible())
        {
            // Closing the window clears the scene, so we'll need to do this
            // every time.
            myDialog.setFxNode(myMainPane);
            myDialog.setVisible(true);
            Quantify.collectMetric("mist3d.menu-bar.edit.joins-merges");
        }
    }

    /** Inform the interested party, if any, of a change to persist. */
    private void fireSave()
    {
        if (mySaveCallback != null)
        {
            mySaveCallback.run();
        }
    }

    /**
     * Introduce the persistent data model.
     *
     * @param p data model
     */
    public void setData(MergePrefs p)
    {
        myPreferences = p;
        myPreferences.getMerges().addListener((ListChangeListener<MergeModel>)c ->
        {
            Platform.runLater(() -> handleMergeModelChange(c));
        });
        myDataRows.clear();
        myPreferences.getJoins().stream().map(ConfigUiRow::new).forEach(myDataRows::add);
        myPreferences.getMerges().stream().map(ConfigUiRow::new).forEach(myDataRows::add);
    }

    /**
     * Adds a new Join to the data list.
     *
     * @param join the join to add
     */
    public void addJoin(MergePrefs.Join join)
    {
        myDataRows.add(new ConfigUiRow(join));
    }

    /**
     * Adds a new merge to the data list.
     *
     * @param merge the merge to add
     */
    public void addMerge(MergeModel merge)
    {
        myDataRows.add(new ConfigUiRow(merge));
    }

    /**
     * Removes a row from the data list.
     *
     * @param row the row to remove
     */
    public void removeRow(ConfigUiRow row)
    {
        if (row.getJoin() != null)
        {
            myDataRows.removeIf(r -> row.getJoin().equals(r.getJoin()));
        }
        else
        {
            myDataRows.removeIf(r -> row.getMerge().equals(r.getMerge()));
        }
    }

    /**
     * Replaces the Join in the data list at the given index with a new one.
     *
     * @param index the index to replace
     * @param join the new join
     */
    public void replaceJoin(int index, MergePrefs.Join join)
    {
        myDataRows.set(index, new ConfigUiRow(join));
    }

    /**
     * Handles a change in the merge model.
     *
     * @param change the change
     */
    void handleMergeModelChange(ListChangeListener.Change<? extends MergeModel> change)
    {
        while (change.next())
        {
            for (MergeModel merge : change.getRemoved())
            {
                myDataRows.removeIf(r -> merge.equals(r.getMerge()));
            }
            change.getAddedSubList().stream().map(ConfigUiRow::new).forEach(myDataRows::add);
        }
    }

    /** A ListCell for viewing Merge/Join items. */
    private class MergeFormatCell extends ListCell<ConfigUiRow>
    {
        /** Reference to the preference object. */
        public ConfigUiRow myItem;

        /** Constructor. Does nothing. */
        public MergeFormatCell()
        {
        }

        @Override
        protected void updateItem(ConfigUiRow item, boolean empty)
        {
            super.updateItem(item, empty);

            setText(null);
            setGraphic(null);

            if (!empty && item != null)
            {
                myItem = item;

                // Set label style.
                setContentDisplay(ContentDisplay.RIGHT);
                setEllipsisString("...");
                setGraphicTextGap(25);

                setPrefWidth(0);
                setMaxWidth(Control.USE_PREF_SIZE);

                // Construct buttons.
                HBox buttonBox = new HBox(5);

                Button updateButton = new Button("Update");
                updateButton.setOnAction((evt) -> ThreadUtilities.runCpu(() -> update()));

                Button editButton = new Button("Edit");
                editButton.setOnAction((evt) -> EventQueueUtilities.runOnEDT(() -> edit()));

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction((evt) -> ThreadUtilities.runCpu(() -> delete()));

                buttonBox.getChildren().addAll(updateButton, editButton, deleteButton);

                // Init label.
                setText(item.getName());
                setGraphic(buttonBox);
            }
        }

        /**
         * Verify that layers are available before edit or update.
         *
         * @param editOnly when true, only require metadata for editing.
         * @return true if existing resources are sufficient to proceed
         */
        private boolean checkLayers(boolean editOnly)
        {
            String effect;
            if (editOnly)
            {
                effect = "Cannot edit:";
            }
            else
            {
                effect = "Cannot calculate join:";
            }

            // check all participating layers for existence of metadata
            for (MergePrefs.LayerParam lp : myItem.getJoin().params)
            {
                DataTypeInfo t = myDataGroupController.findMemberById(lp.typeKey);
                // Mantle must be aware of the layer
                if (t == null)
                {
                    return croak(lp.typeKey + " is not loaded.", effect);
                }
                String disp = t.getDisplayName();
                // The layer must have metadata (can this fail?)
                if (t.getMetaDataInfo() == null)
                {
                    return croak(disp + " has no metadata.", effect);
                }
                // for editing, this is all that is necessary
                if (editOnly)
                {
                    continue;
                }
                // for updating, the layer needs to be active ...
                if (!t.getParent().activationProperty().isActive())
                {
                    return croak(disp + " is not active.", effect);
                }
                // ... and actually have some records
                if (myDataCache.getElementCountForType(t) <= 0)
                {
                    return croak(disp + " is empty.", effect);
                }
            }
            return true;
        }

        /**
         * Fail and display a popup notification to the user.
         *
         * @param cause reason there is a problem
         * @param effect user-friendly explanation
         * @return false
         */
        private boolean croak(String cause, String effect)
        {
            Notify.info(effect + "\n" + cause, Method.POPUP);
            return false;
        }

        /** Recalculate this join/merge, if possible. */
        private void update()
        {
            if (myItem.getJoin() != null)
            {
                if (!checkLayers(false))
                {
                    return;
                }
                myJoinManager.handleJoin(myItem.getJoin());
            }
            else
            {
                myMergeController.removeMerge(myItem.getMerge());

                myMergeController.setModel(myItem.getMerge());
                myMergeController.performMerge();
            }
        }

        /** Spawn the config editor for this join/merge, if possible. */
        private void edit()
        {
            if (myItem.getJoin() != null)
            {
                editJoin();
            }
            else
            {
                editMerge();
            }
        }

        /** Spawn the config editor for this join, if possible. */
        private void editJoin()
        {
            // only edit if metadata are available
            if (!checkLayers(true))
            {
                return;
            }
            JoinGui gui = new JoinGui();
            JFXDialog edDialog = GuiUtil.okCancelDialog(myDialog, "Edit Join");
            edDialog.setFxNode(GuiUtil.vScroll(gui.getMainPane()));
            edDialog.setAcceptListener(() -> acceptJoinEdits(gui.getModel()));
            edDialog.setSize(new Dimension(450, 450));
            gui.setup(myToolbox, edDialog);
            gui.setData(myItem.getJoin());
            edDialog.setVisible(true);
        }

        /** Spawn the config editor for this merge, if possible. */
        private void editMerge()
        {
            MergeModel merge = myItem.getMerge();
            MergeUI.showMergeDialog(merge, myToolbox, null, () -> acceptMergeEdits(merge));
        }

        /**
         * Incorporate edits into the persistent model.
         *
         * @param m editor model
         */
        private void acceptJoinEdits(JoinModel m)
        {
            MergePrefs.Join newModel = myPreferences.editJoinModel(myItem.getJoin(), m);
            fireSave();

            FXUtilities.runOnFXThread(() -> replaceJoin(getIndex(), newModel));
        }

        /**
         * Incorporate edits into the persistent model.
         *
         * @param merge merge model
         */
        private void acceptMergeEdits(MergeModel merge)
        {
            // Only need to save in case the name changed.
            fireSave();

            // Trigger a change to the UI model
            Platform.runLater(() ->
            {
                for (int i = 0; i < myDataRows.size(); i++)
                {
                    ConfigUiRow row = myDataRows.get(i);
                    if (merge.equals(row.getMerge()))
                    {
                        myDataRows.set(i, row);
                        break;
                    }
                }
            });
        }

        /** Delete this join config. */
        private void delete()
        {
            myPreferences.delete(myItem.getName());
            fireSave();

            FXUtilities.runOnFXThread(() -> removeRow(myItem));
        }
    }

    /** Model for a UI row. */
    private static class ConfigUiRow
    {
        /** The join. */
        private final MergePrefs.Join myJoin;

        /** The merge. */
        private final MergeModel myMerge;

        /**
         * Constructor.
         *
         * @param join the join
         */
        public ConfigUiRow(Join join)
        {
            myJoin = join;
            myMerge = null;
        }

        /**
         * Constructor.
         *
         * @param merge the merge
         */
        public ConfigUiRow(MergeModel merge)
        {
            myJoin = null;
            myMerge = merge;
        }

        /**
         * Gets the join.
         *
         * @return the join
         */
        public MergePrefs.Join getJoin()
        {
            return myJoin;
        }

        /**
         * Gets the merge.
         *
         * @return the merge
         */
        public MergeModel getMerge()
        {
            return myMerge;
        }

        /**
         * Gets the name of the row.
         *
         * @return the name
         */
        public String getName()
        {
            return myJoin != null ? myJoin.name : myMerge.getNewLayerName().get();
        }
    }
}
