package io.opensphere.featureactions.editor.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Set;

import io.opensphere.controlpanels.layers.importdata.ImportDataController;
import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.ImportType;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.editor.controller.FeatureActionEditController;
import io.opensphere.featureactions.editor.model.FeatureActionImporter;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.mantle.data.DataTypeInfo;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TitledPane;

/**
 * Keeps the {@link SimpleFeatureActionEditor} synchronized with the
 * {@link SimpleFeatureActions}.
 */
public class SimpleFeatureActionEditorBinder
{
    /** Listener invoked when groups have been added or removed. */
    private final ListChangeListener<SimpleFeatureActionGroup> groupsEar = this::groupsChanged;

    /** The current layer. */
    private final DataTypeInfo layer;

    /** The controller used to save changes to the system. */
    private final FeatureActionEditController myController;

    /**
     * Allows the user to drag and drop actions.
     */
    private DragDropHandler myDnDHandler;

    /** The interface to the main editor ui. */
    private final SimpleFeatureActionEditorUI myEditor;

    /** The main model. */
    private SimpleFeatureActions myModel;

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /** The parent dialog for the detail editor. */
    private final Component myParentDialog;

    /**
     * Constructs a new binder.
     *
     * @param toolbox The system toolbox.
     * @param editor The interface to the main editor ui.
     * @param actionRegistry The registry that stores all feature actions.
     * @param type The layer whose feature actions we are editing.
     * @param dialog the dialog that contains this editor
     */
    public SimpleFeatureActionEditorBinder(Toolbox toolbox, SimpleFeatureActionEditorUI editor,
            FeatureActionsRegistry actionRegistry, DataTypeInfo type, Component dialog)
    {
        myToolbox = toolbox;
        myEditor = editor;
        layer = type;
        myParentDialog = dialog;
        myController = new FeatureActionEditController(actionRegistry, layer.getTypeKey());
        bindUI();
    }

    /** Stops keeping the UI and model synchronized. */
    public void close()
    {
        myModel.getFeatureGroups().removeListener(groupsEar);
        myEditor.getAddButton().setOnAction(null);
    }

    /**
     * Adds a new {@link TitledPane} to the accordion.
     *
     * @param group The group that has just been added.
     * @return The newly create {@link TitledPane}.
     */
    private TitledPane addGroup(SimpleFeatureActionGroup group)
    {
        SimpleFeatureActionPane pane = new SimpleFeatureActionPane(myToolbox, myModel, group, layer, myParentDialog, myDnDHandler);
        FeatureActionTitledPane titled = new FeatureActionTitledPane(myEditor.getAccordion(), myModel, group, pane,
                myToolbox.getUIRegistry());
        myDnDHandler.handlePane(titled, group);
        int index = myModel.getFeatureGroups().indexOf(group);
        myEditor.getAccordion().getPanes().add(index, titled);
        myEditor.getAccordion().setExpandedPane(titled);

        return titled;
    }

    /** Binds the ui to the model. */
    private void bindUI()
    {
        myModel = myController.getModel();
        myDnDHandler = new DragDropHandler(myModel);
        if (myModel.getFeatureGroups().isEmpty())
        {
            myModel.getFeatureGroups().add(new SimpleFeatureActionGroup());
        }
        for (SimpleFeatureActionGroup group : myModel.getFeatureGroups())
        {
            addGroup(group);
        }
        myModel.getFeatureGroups().addListener(groupsEar);
        myEditor.getAddButton().setOnAction(e -> handleAdd());
        myEditor.setSaveListener(() -> myController.applyChanges());
        myEditor.getExportButton().setOnAction(e ->
        {
            Quantify.collectMetric("mist3d.feature-actions.export");
            handleExport();
        });
        myEditor.getImportButton().setOnAction(e ->
        {
            Quantify.collectMetric("mist3d.feature-actions.import");
            handleImport();
        });
    }

    /**
     * Respond when the list of groups has changed.
     *
     * @param c the change summary
     */
    private void groupsChanged(ListChangeListener.Change<? extends SimpleFeatureActionGroup> c)
    {
        while (c.next())
        {
            for (SimpleFeatureActionGroup added : c.getAddedSubList())
            {
                addGroup(added);
            }

            for (SimpleFeatureActionGroup removed : c.getRemoved())
            {
                removeGroup(removed);
            }
        }
    }

    /** Responds when the add button is pressed. */
    private void handleAdd()
    {
        Set<String> groupNames = New.set();
        for (SimpleFeatureActionGroup aGroup : myModel.getFeatureGroups())
        {
            groupNames.add(aGroup.getGroupName());
        }

        int newNumber = 1;
        String prefix = "Feature Actions ";
        String newGroupName = prefix + newNumber;
        while (groupNames.contains(newGroupName))
        {
            newNumber++;
            newGroupName = prefix + newNumber;
        }

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(newGroupName);
        FeatureAction act = new FeatureAction();
        act.setName("Action 1");
        act.getActions().add(new StyleAction());
        group.getActions().add(new SimpleFeatureAction(act));

        myModel.getFeatureGroups().add(group);
    }

    /**
     * Removes the titled pane representing the specified group.
     *
     * @param group The group that has been removed.
     */
    private void removeGroup(SimpleFeatureActionGroup group)
    {
        int indexOf = 0;
        for (TitledPane titled : myEditor.getAccordion().getPanes())
        {
            if (((FeatureActionTitledPane)titled).getTitle().getText().equals(group.getGroupName()))
            {
                break;
            }
            indexOf++;
        }

        if (indexOf < myEditor.getAccordion().getPanes().size())
        {
            myEditor.getAccordion().getPanes().remove(indexOf);
        }
    }

    /**
     * Responds when the export button is pressed.
     */
    private void handleExport()
    {
        FeatureActionExporter exporter = new FeatureActionExporter(myToolbox.getPreferencesRegistry(),
                layer.getDisplayName(), myModel.getFeatureGroups());
        exporter.launch(myParentDialog);
    }

    /**
     * Responds when the import button is pressed.
     */
    private void handleImport()
    {
        FeatureActionImporter importer = new FeatureActionImporter(myModel);
        EventQueue.invokeLater(() -> ImportDataController.getInstance(myToolbox).importSpecific(importer, ImportType.FILE));
    }
}
