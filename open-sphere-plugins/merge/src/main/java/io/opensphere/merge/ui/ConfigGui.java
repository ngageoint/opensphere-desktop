package io.opensphere.merge.ui;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.merge.layout.GenericLayout;
import io.opensphere.merge.layout.LayMode;
import io.opensphere.merge.layout.LinearLayout;
import io.opensphere.merge.model.JoinModel;
import io.opensphere.merge.model.MergePrefs;
import javafx.scene.control.Label;

/**
 * Facilitates management of existing join configurations.
 */
public class ConfigGui
{
    /** System Toolbox. */
    private Toolbox tools;

    /** The DataGroupController. */
    private DataGroupController groupCtrl;

    /** Used to get the element counts. */
    private DataElementCache dataCache;

    /** This GUI's host dialog. */
    private JFXDialog dialog;

    /** Manager for layers formed by joining. */
    private JoinManager joinMan;

    /** Top layout component. */
    private final LinearLayout vLay = LinearLayout.col();

    /** GUI root. */
    private final GenericLayout mainPane = new GenericLayout(vLay);
    {
        mainPane.getRoot().setBorder(GuiUtil.emptyBorder(5.0));
    }

    /** Persistent data model. */
    private MergePrefs prefs;

    /** Rows; each one represents a join configuration. */
    private final List<CfgRow> rows = new LinkedList<>();

    /** Callback for saving (this class does not handle persistence). */
    private Runnable saveEar;

    /** Show the dialog, creating it if necessary. */
    public void show()
    {
        if (dialog != null && dialog.isVisible())
        {
            dialog.setVisible(true);
            return;
        }
        dialog = GuiUtil.okDialog(tools, "Joins/Merges");
        dialog.setFxNode(GuiUtil.vScroll(mainPane.getRoot()));
        dialog.setSize(new Dimension(450, 450));
        dialog.setVisible(true);
    }

    /**
     * Receive a reference to the system Toolbox.
     *
     * @param tb the Toolbox
     */
    public void setTools(Toolbox tb)
    {
        tools = tb;
        MantleToolbox mtb = tools.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        groupCtrl = mtb.getDataGroupController();
        dataCache = mtb.getDataElementCache();
    }

    /**
     * Receive a reference to the JoinManager.
     *
     * @param jm the JoinManager
     */
    public void setJoinMan(JoinManager jm)
    {
        joinMan = jm;
    }

    /**
     * Specify a callback for saving.
     *
     * @param ear callback
     */
    public void setSaveEar(Runnable ear)
    {
        saveEar = ear;
    }

    /** Inform the interested party, if any, of a change to persist. */
    private void fireSave()
    {
        if (saveEar != null)
        {
            saveEar.run();
        }
    }

    /**
     * Introduce the persistent data model.
     *
     * @param p data model
     */
    public void setData(MergePrefs p)
    {
        prefs = p;
        rows.clear();
        for (MergePrefs.Join j : prefs.getJoins())
        {
            rows.add(new CfgRow(j));
        }
        layoutGui();
    }

    /** Like, lay out the GUI, ya know? */
    private void layoutGui()
    {
        vLay.clear();
        boolean first = true;
        for (CfgRow r : rows)
        {
            if (!first)
            {
                vLay.addSpace(5.0);
            }
            first = false;
            vLay.addAcross(r.row);
        }
    }

    /** Aggregation of stuff related to a single join config. */
    private class CfgRow
    {
        /** Name String. */
        public String name;

        /** Reference to the preference object. */
        public MergePrefs.Join mpj;

        /** For displaying the name. */
        private final Label lbl = new Label();

        /** Layout mechanism for this row. */
        public LinearLayout row = LinearLayout.row();

        /**
         * Create.
         *
         * @param j join config
         */
        public CfgRow(MergePrefs.Join j)
        {
            mpj = j;
            name = j.name;
            lbl.setText(name);
            layout();
        }

        /** Place subcomponents within the layout. */
        public void layout()
        {
            row.clear();
            row.add(lbl, LayMode.STRETCH, 50.0, 1.0);
            row.addSpace(15);
            row.add(GuiUtil.button("Update", () -> ThreadUtilities.runCpu(() -> update())));
            row.addSpace(5.0);
            row.add(GuiUtil.button("Edit", () -> SwingUtilities.invokeLater(() -> edit())));
            row.addSpace(5.0);
            row.add(GuiUtil.button("Delete", () -> delete()));
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
            for (MergePrefs.LayerParam lp : mpj.params)
            {
                DataTypeInfo t = groupCtrl.findMemberById(lp.typeKey);
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
                if (dataCache.getElementCountForType(t) <= 0)
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
            joinMan.handleJoin(mpj);
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
            JFXDialog edDialog = GuiUtil.okCancelDialog(dialog, "Edit Join");
            edDialog.setFxNode(GuiUtil.vScroll(gui.getMainPane()));
            edDialog.setAcceptEar(() -> acceptEdits(gui.getModel()));
            edDialog.setSize(new Dimension(450, 450));
            gui.setup(tools, edDialog);
            gui.setData(mpj);
            edDialog.setVisible(true);
        }

        /**
         * Incorporate edits into the persistent model.
         *
         * @param m editor model
         */
        private void acceptEdits(JoinModel m)
        {
            mpj = prefs.editJoinModel(mpj, m);
            name = m.getJoinName();
            lbl.setText(name);

            // persist the change
            fireSave();
        }

        /** Delete this join config. */
        private void delete()
        {
            rows.remove(this);
            layoutGui();
            prefs.delete(name);

            // persist the change
            fireSave();
        }
    }
}
