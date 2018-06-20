package io.opensphere.controlpanels.layers.layersets;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import io.opensphere.controlpanels.layers.event.ShowLayerSetManagerEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.ActiveDataGroupSavedSetsChangedEvent;
import io.opensphere.mantle.data.ActiveGroupEntry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The controller for layer sets.
 */
public class LayerSetController
{
    /** The default layer set name. */
    public static final String DEFAULT_LAYER_SET_NAME = "Default Set";

    /** The change executor. */
    private final ProcrastinatingExecutor myChangeExecutor = new ProcrastinatingExecutor("LayerSetController::Dispatch", 300,
            500);

    /** The change support. */
    private final WeakChangeSupport<LayerSetControllerListener> myChangeSupport;

    /** The DataGroupController. */
    private final DataGroupController myDGC;

    /** The active group saved sets changed listener. */
    private final EventListener<ActiveDataGroupSavedSetsChangedEvent> mySavedSetsChangedListener = new EventListener<ActiveDataGroupSavedSetsChangedEvent>()
    {
        @Override
        public void notify(ActiveDataGroupSavedSetsChangedEvent event)
        {
            fireLayerSetsChanged();
        }
    };

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new layer set panel.
     *
     * @param tb the {@link Toolbox}
     */
    public LayerSetController(Toolbox tb)
    {
        myToolbox = tb;
        myDGC = MantleToolboxUtils.getMantleToolbox(tb).getDataGroupController();
        myChangeSupport = new WeakChangeSupport<>();
        myToolbox.getEventManager().subscribe(ActiveDataGroupSavedSetsChangedEvent.class, mySavedSetsChangedListener);
    }

    /**
     * Activate layer set.
     *
     * @param layerSetName the layer set name
     * @param exclusive the exclusive
     */
    public void activateLayerSet(String layerSetName, boolean exclusive)
    {
        ThreadUtilities.runBackground(() -> Boolean.valueOf(myDGC.loadActiveSet(layerSetName, exclusive)));
    }

    /**
     * Adds the {@link LayerSetControllerListener}.
     *
     * @param listener the {@link LayerSetControllerListener}
     */
    public void addListener(LayerSetControllerListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Delete layers from set.
     *
     * @param setName the set name
     * @param layersToDelete the layers to delete
     */
    public void deleteLayersFromSet(String setName, Set<ActiveGroupEntry> layersToDelete)
    {
        DataGroupInfoActiveSet set = myDGC.getActiveSet(setName);
        if (set != null)
        {
            Set<ActiveGroupEntry> entries = New.set(set.getGroupEntries());
            entries.removeAll(layersToDelete);
            myDGC.saveActiveSet(setName, entries);
        }
    }

    /**
     * Delete set.
     *
     * @param setName the set name
     */
    public void deleteSet(String setName)
    {
        myDGC.removeActiveSet(setName);
    }

    /**
     * Gets the available group ids.
     *
     * @return the available group ids
     */
    public Set<String> getAvailableGroupIds()
    {
        Set<DataGroupInfo> dgiSet = New.set();
        final Set<String> ids = New.set();
        myDGC.findDataGroupInfo(new Predicate<DataGroupInfo>()
        {
            @Override
            public boolean test(DataGroupInfo value)
            {
                ids.add(value.getId());
                return true;
            }
        }, dgiSet, false);
        return ids;
    }

    /**
     * Gets the saved set layers.
     *
     * @param setName the set name
     * @return the saved set layers
     */
    public List<ActiveGroupEntry> getSavedSetLayers(String setName)
    {
        List<ActiveGroupEntry> result = New.list();
        DataGroupInfoActiveSet set = myDGC.getActiveSet(setName);
        if (set != null)
        {
            result.addAll(set.getGroupEntries());
            Collections.sort(result, ActiveGroupEntry.ComparatorByName);
        }
        return result;
    }

    /**
     * Gets the saved set names.
     *
     * @return the saved set names
     */
    public List<String> getSavedSetNames()
    {
        List<String> names = New.list(myDGC.getActiveSetNames());
        Collections.sort(names);
        return names;
    }

    /**
     * Removes the {@link LayerSetControllerListener}.
     *
     * @param listener the {@link LayerSetControllerListener}
     */
    public void removeListener(LayerSetControllerListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Rename set.
     *
     * @param oldName the old name
     */
    public void renameSet(String oldName)
    {
        DataGroupInfoActiveSet set = myDGC.getActiveSet(oldName);
        Frame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
        if (set != null)
        {
            String bmName = oldName;
            boolean saveIt = false;
            boolean done = false;
            Set<String> currentSetNames = New.set(myDGC.getActiveSetNames());
            currentSetNames.remove(oldName);
            while (!done)
            {
                bmName = JOptionPane.showInputDialog(parent, "Please enter a new name for the layer set:", bmName);
                if (bmName == null)
                {
                    done = true;
                    saveIt = false;
                }
                else if (bmName.isEmpty())
                {
                    JOptionPane.showMessageDialog(parent, "The name for the layer set can not be blank.",
                            "Invalid Layer Set Name", JOptionPane.ERROR_MESSAGE);
                }
                else if (DEFAULT_LAYER_SET_NAME.equals(bmName))
                {
                    JOptionPane.showMessageDialog(parent, "The layer set name \"" + bmName + "\" cannot be used.",
                            "Invalid Layer Set Name", JOptionPane.ERROR_MESSAGE);
                }
                else if (currentSetNames.contains(bmName) || DEFAULT_LAYER_SET_NAME.equals(bmName))
                {
                    int result = JOptionPane.showConfirmDialog(parent,
                            "The layer set name \"" + bmName
                                    + "\" is already in use.\nDo you want to replace the existing layer set?",
                            "Replace Layer Set Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result == JOptionPane.CANCEL_OPTION)
                    {
                        done = true;
                        saveIt = false;
                    }
                    else if (result == JOptionPane.YES_OPTION)
                    {
                        done = true;
                        saveIt = true;
                    }
                }
                else
                {
                    done = true;
                    saveIt = true;
                }
            }
            if (saveIt)
            {
                myDGC.removeActiveSet(oldName);
                myDGC.saveActiveSet(bmName, set.getGroupEntries());
            }
        }
        else
        {
            JOptionPane.showMessageDialog(parent, "There is no set named \"" + oldName + "\" to rename.",
                    "Invalid Layer Set Name", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Rename set.
     *
     * @param oldName the old name
     * @param newName the new name
     */
    public void renameSet(String oldName, String newName)
    {
        DataGroupInfoActiveSet set = myDGC.getActiveSet(oldName);
        if (set != null)
        {
            myDGC.removeActiveSet(oldName);
            myDGC.saveActiveSet(newName, set.getGroupEntries());
        }
    }

    /**
     * Restore the default active layer set.
     */
    public void restoreDefaultActiveSet()
    {
        myDGC.restoreDefaultActiveSet(true);
    }

    /**
     * Save current set.
     */
    public void saveCurrentSet()
    {
        boolean save = false;
        boolean done = false;
        Set<String> currentSetNames = New.set(myDGC.getActiveSetNames());
        String name = getUniqueName("New Layer Set", currentSetNames);

        List<String> names = New.list();
        names.addAll(myDGC.getActiveSetNames());
        Collections.sort(names);

        names.add(0, name);
        JComboBox<String> comboBox = new JComboBox<>(New.array(names, String.class));
        comboBox.setEditable(true);
        comboBox.requestFocus();

        JPanel jp = createSaveDialogEntryPanel(comboBox);

        Frame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
        while (!done)
        {
            int option = JOptionPane.showConfirmDialog(parent, jp, "Create/Update Layer Set", JOptionPane.OK_CANCEL_OPTION);
            name = option == JOptionPane.CANCEL_OPTION ? null : comboBox.getSelectedItem().toString();
            if (name == null)
            {
                done = true;
                save = false;
            }
            else if (name.isEmpty())
            {
                JOptionPane.showMessageDialog(parent, "The name for the layer set cannot be blank.", "Invalid Layer Set Name",
                        JOptionPane.ERROR_MESSAGE);
            }
            else if (currentSetNames.contains(name))
            {
                int result = JOptionPane.showConfirmDialog(parent,
                        "The layer set name \"" + name + "\" is already in use.\nDo you want to replace the existing layer set?",
                        "Replace Layer Set Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.CANCEL_OPTION)
                {
                    done = true;
                    save = false;
                }
                else if (result == JOptionPane.YES_OPTION)
                {
                    done = true;
                    save = true;
                }
            }
            else if (DEFAULT_LAYER_SET_NAME.equals(name))
            {
                JOptionPane.showMessageDialog(parent, "The layer set name \"" + name + "\" cannot be used.",
                        "Invalid Layer Set Name", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                done = true;
                save = true;
            }
        }
        if (save)
        {
            myDGC.saveActiveSet(name);
        }
    }

    /**
     * Show layer set manager.
     */
    public void showLayerSetManager()
    {
        myToolbox.getEventManager().publishEvent(new ShowLayerSetManagerEvent());
    }

    /**
     * Creates the save dialog entry panel.
     *
     * @param comboBox the combo box
     * @return the j panel
     */
    private JPanel createSaveDialogEntryPanel(JComboBox<String> comboBox)
    {
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        JTextArea jta = new JTextArea(
                "Please enter a new name for the layer set or select an existing name to update the layer set:");
        jta.setFont(jta.getFont().deriveFont(jta.getFont().getSize() + 2.0f));
        jta.setEditable(false);
        jta.setWrapStyleWord(true);
        jta.setLineWrap(true);
        jta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jta.setBackground(jp.getBackground());
        jp.add(Box.createVerticalGlue());
        jp.add(jta);
        jp.add(Box.createVerticalStrut(5));
        jp.add(comboBox);
        jp.add(Box.createVerticalStrut(20));
        jp.add(Box.createVerticalGlue());
        Dimension d = new Dimension(500, 100);
        jp.setMinimumSize(d);
        jp.setPreferredSize(d);

        return jp;
    }

    /**
     * Fire layer sets changed.
     */
    private void fireLayerSetsChanged()
    {
        myChangeSupport.notifyListeners(new Callback<LayerSetController.LayerSetControllerListener>()
        {
            @Override
            public void notify(LayerSetControllerListener listener)
            {
                listener.layerSetsChanged();
            }
        }, myChangeExecutor);
    }

    /**
     * Gets the unique name.
     *
     * @param baseName the base name
     * @param currentSetNames the current set names
     * @return the unique name
     */
    private String getUniqueName(String baseName, Set<String> currentSetNames)
    {
        String resultName = baseName;

        if (currentSetNames != null && !currentSetNames.isEmpty())
        {
            int count = 0;
            while (currentSetNames.contains(resultName))
            {
                count++;
                resultName = baseName + " (" + count + ")";
            }
        }
        return resultName;
    }

    /**
     * Listener for changes to layer sets.
     */
    @FunctionalInterface
    public interface LayerSetControllerListener
    {
        /**
         * Layer sets changed.
         */
        void layerSetsChanged();
    }
}
