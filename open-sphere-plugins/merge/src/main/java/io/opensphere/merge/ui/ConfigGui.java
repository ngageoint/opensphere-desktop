package io.opensphere.merge.ui;

import java.awt.Dimension;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.merge.model.JoinModel;
import io.opensphere.merge.model.MergePrefs;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

/**
 * Facilitates management of existing join configurations.
 */
public class ConfigGui
{
    /** System Toolbox. */
    private Toolbox myToolbox;

    /** The DataGroupController. */
    private DataGroupController myDataGroupController;

    /** Used to get the element counts. */
    private DataElementCache myDataCache;

    /** This GUI's host dialog. */
    private JFXDialog myDialog;

    /** Manager for layers formed by joining. */
    private JoinManager myJoinManager;

    /** GUI root. */
    private final ListView<MergePrefs.Join> myMainPane;

    /** Persistent data model. */
    private MergePrefs myPreferences;

    /** Rows; each one represents a join configuration. */
    private final ObservableList<MergePrefs.Join> myDataRows = new ObservableMergeList<>();

    /** Callback for saving (this class does not handle persistence). */
    private Runnable mySaveCallback;

    /**
     * Constructs ConfigGui.
     *
     * @param tb
     * @param jm
     * @param callback
     */
    public ConfigGui(Toolbox tb, JoinManager jm, Runnable callback)
    {
        myToolbox = tb;
        myJoinManager = jm;
        mySaveCallback = callback;

        MantleToolbox mtb = tb.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myDataGroupController = mtb.getDataGroupController();
        myDataCache = mtb.getDataElementCache();

        myMainPane = new ListView<MergePrefs.Join>(myDataRows);
        myMainPane.setCellFactory((view) ->
        {
            return new MergeFormatCell();
        });
        myMainPane.setSelectionModel(null);

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
            myDialog.setFxNode(GuiUtil.vScroll(myMainPane));
            myDialog.setVisible(true);
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
        myDataRows.clear();

        myDataRows.addAll(myPreferences.getJoins());
    }

    /**
     * @param j
     */
    public void addJoin(MergePrefs.Join j)
    {
        myDataRows.add(j);
    }

    /** A ListCell for viewing Merge/Join items. */
    private class MergeFormatCell extends ListCell<MergePrefs.Join>
    {
        /** Name String. */
        public String myName;

        /** Reference to the preference object. */
        public MergePrefs.Join myItem;

        /** Constructor. Does nothing. */
        public MergeFormatCell()
        {
        }

        @Override
        protected void updateItem(MergePrefs.Join item, boolean empty)
        {
            super.updateItem(item, empty);

            setText(null);
            setGraphic(null);

            setTextAlignment(TextAlignment.JUSTIFY);
            setContentDisplay(ContentDisplay.RIGHT);

            if (!empty && item != null)
            {
                HBox myLabelBox = new HBox();

                myItem = item;
                myName = item.name;

                Button updateButton = new Button("Update");
                updateButton.setOnAction((evt) -> ThreadUtilities.runCpu(() -> update()));

                Button editButton = new Button("Edit");
                editButton.setOnAction((evt) -> EventQueueUtilities.runOnEDT(() -> edit()));

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction((evt) -> ThreadUtilities.runCpu(() -> delete()));

                ObservableList<Node> children = myLabelBox.getChildren();
                children.addAll(updateButton, editButton, deleteButton);

                setText(myName);
                setGraphic(myLabelBox);
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
            for (MergePrefs.LayerParam lp : myItem.params)
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

        /** Recalculate this join, if possible. */
        private void update()
        {
            if (!checkLayers(false))
            {
                return;
            }
            myJoinManager.handleJoin(myItem);
        }

        /** Spawn the config editor for this join, if possible. */
        private void edit()
        {
            // only edit if metadata are available
            if (!checkLayers(true))
            {
                return;
            }
            JoinGui gui = new JoinGui();
            JFXDialog edDialog = GuiUtil.okCancelDialog(myDialog, "Edit Join");
            edDialog.setFxNode(GuiUtil.vScroll(gui.getMainPane()));
            edDialog.setAcceptEar(() -> acceptEdits(gui.getModel()));
            edDialog.setSize(new Dimension(450, 450));
            gui.setup(myToolbox, edDialog);
            gui.setData(myItem);
            edDialog.setVisible(true);
        }

        /**
         * Incorporate edits into the persistent model.
         *
         * @param m editor model
         */
        private void acceptEdits(JoinModel m)
        {
            MergePrefs.Join newModel = myPreferences.editJoinModel(myItem, m);
            fireSave();

            FXUtilities.runOnFXThread(() -> myDataRows.set(getIndex(), newModel));
        }

        /** Delete this join config. */
        private void delete()
        {
            myPreferences.delete(myName);
            fireSave();

            FXUtilities.runOnFXThread(() -> myDataRows.remove(myItem));
        }
    }
}
