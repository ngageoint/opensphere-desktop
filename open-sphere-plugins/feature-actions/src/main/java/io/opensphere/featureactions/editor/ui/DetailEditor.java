package io.opensphere.featureactions.editor.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.controlpanels.styles.model.LabelOptions;
import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.ui.LabelOptionsPanel;
import io.opensphere.controlpanels.styles.ui.StyleOptionsPanel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.featureactions.editor.controller.FilterActionAdapter;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.LabelAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.filterbuilder2.editor.FilterEditorPanel;
import io.opensphere.mantle.data.DataTypeInfo;

/** Shows the detail editor for feature actions. */
@SuppressWarnings("PMD.GodClass")
public class DetailEditor
{
    /** Title applied to the style action editor. */
    private static final String STYLE_TITLE = "Style Options";

    /** Title applied to the label action editor. */
    private static final String LABEL_TITLE = "Label Options";

    /** Option to create style settings. */
    private static final String STYLE_CREATE = "Style";

    /** Option to create label settings. */
    private static final String LABEL_CREATE = "Label";

    /** The model to be edited. */
    private SimpleFeatureAction model;

    /** The feature action to be edited. */
    private FeatureAction currentAction;

    /** The layer to which the feature action applies. */
    private DataTypeInfo layer;

    /** The editor for the filter part of the feature action. */
    private FilterEditorPanel filterEditor;

    /** The root of the options editors. */
    private JFXPanel actionEditor;

    /** Drop-down for creating a new action. */
    private ComboBox<String> createAction;

    /** Container for the action editors. */
    private Accordion actionPanes;

    /** The part of the feature action that affects style, if any. */
    private StyleAction styleAct;

    /** A copy of the style options used for editing. */
    private StyleOptions styleEdit;

    /** The part of the feature action that affects labels, if any. */
    private LabelAction labelAct;

    /** A copy of the label options used for editing. */
    private LabelOptions labelEdit;

    /** Callback for when edits have been accepted by the user. */
    private Runnable editEar;

    /**
     * Attach a listener to be informed when editing has completed.  The
     * listener is invoked on the JFX thread after all changes are applied.
     *
     * @param ear the listener
     */
    public void setEditListener(Runnable ear)
    {
        editEar = ear;
    }

    /**
     * Prepare this editor for launch by linking to the required resources.
     *
     * @param tools the system toolbox
     * @param toEdit the model containing the feature action to be edited
     * @param type the layer to which the feature action applies
     */
    public void setup(Toolbox tools, SimpleFeatureAction toEdit, DataTypeInfo type)
    {
        model = toEdit;
        currentAction = model.getFeatureAction();
        layer = type;

        filterEditor = FilterEditorPanel.noNameEditor(tools, currentAction.getFilter(), layer);

        ObservableList<Action> acts = currentAction.getActions();
        styleAct = getTypeElement(acts, StyleAction.class);
        if (styleAct != null)
        {
            styleEdit = XMLUtilities.jaxbClone(styleAct.getStyleOptions(), StyleOptions.class);
        }

        labelAct = getTypeElement(acts, LabelAction.class);
        if (labelAct != null)
        {
            labelEdit = XMLUtilities.jaxbClone(labelAct.getLabelOptions(), LabelOptions.class);
        }

        createAction = new ComboBox<String>();
        Platform.runLater(() -> refreshCreateOptions());

        GridPane actionRoot = new GridPane();
        actionRoot.setHgap(5);
        actionRoot.add(new Label("Add Action:"), 0, 0);
        actionRoot.add(createAction, 1, 0);

        actionPanes = new Accordion();
        if (styleEdit != null)
        {
            actionPanes.getPanes().add(styleOptionsPane(styleEdit));
        }
        if (labelEdit != null)
        {
            actionPanes.getPanes().add(labelOptionsPane(labelEdit));
        }
        if (!actionPanes.getPanes().isEmpty())
        {
            actionPanes.setExpandedPane(actionPanes.getPanes().get(0));
        }
        actionRoot.add(actionPanes, 0, 1, 3, 1);

        actionEditor = new JFXPanel();

        Platform.runLater(() ->  actionEditor.setScene(FXUtilities.addDesktopStyle(new Scene(actionRoot))));
        actionEditor.setPreferredSize(new Dimension(500, 300));
    }

    /** Allow the user to create options that are not already present. */
    private void refreshCreateOptions()
    {
        // stop listening for selections while changing the options
        createAction.setOnAction(null);
        createAction.getItems().clear();
        createAction.getItems().add("");
        if (styleEdit == null)
        {
            createAction.getItems().add(STYLE_CREATE);
        }
        if (labelEdit == null)
        {
            createAction.getItems().add(LABEL_CREATE);
        }
        // start listening again
        createAction.setOnAction(e -> handleCreate());
    }

    /** Create a new set of options, per user request. */
    private void handleCreate()
    {
        String selected = createAction.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty())
        {
            return;
        }

        if (selected.equals(STYLE_CREATE))
        {
            addStyleAction();
        }
        else if (selected.equals(LABEL_CREATE))
        {
            addLabelAction();
        }

        Platform.runLater(() -> refreshCreateOptions());
    }

    /**
     * Launches the editor, on the swing thread, for this feature action.
     *
     * @param parentDialog used as the parent of the dialog to be launched
     */
    public void launch(Component parentDialog)
    {
        OptionDialog dialog = new OptionDialog(parentDialog);
        JPanel root = new JPanel();
        StackLayout stack = new StackLayout();
        stack.attach(root);

        // include the filter editor
        stack.add(filterEditor);

        // include an editor for each of the actions
        stack.add(actionEditor);

        dialog.setComponent(root);
        dialog.setModal(true);
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            // do some stuff on the swing thread
            filterEditor.applyChanges();
            // do some more stuff on the JFX thread
            Platform.runLater(() -> applyChangesJfx());
        }
    }

    /** Upon user acceptance, save some changes on the JFX thread. */
    private void applyChangesJfx()
    {
        saveStyle();
        saveLabel();

        FilterActionAdapter.filterToModel(model);

        if (editEar != null)
        {
            editEar.run();
        }
    }

    /**
     * Create the editor for style options.
     *
     * @param sty the style options
     * @return the style options editor root
     */
    private TitledPane styleOptionsPane(StyleOptions sty)
    {
        Button button = deleteButton("Remove style options.", () -> deleteStyleAction());

        sty.setStyles(StyleAction.STYLES_LIST);
        StyleOptionsPanel stylesPanel = new StyleOptionsPanel(sty);
        HBox box = new HBox(5, stylesPanel, button);
        return new TitledPane(STYLE_TITLE, box);
    }

    /** Write style settings back to the original model. */
    private void saveStyle()
    {
        if (styleEdit == null)
        {
            // no style options now, if they existed before, delete them
            if (styleAct != null)
            {
                currentAction.getActions().remove(styleAct);
            }
            return;
        }
        if (styleAct == null)
        {
            // style actions are new, so create and insert them
            styleAct = new StyleAction();
            currentAction.getActions().add(styleAct);
        }
        // copy values to the original model
        styleAct.getStyleOptions().copyFrom(styleEdit);
    }

    /** Create new style options and show the editor. */
    private void addStyleAction()
    {
        styleEdit = new StyleOptions();
        addActionPane(styleOptionsPane(styleEdit));
    }

    /** Remove the style options and their editor. */
    private void deleteStyleAction()
    {
        styleEdit = null;
        actionPanes.getPanes().removeIf(p -> STYLE_TITLE.equals(p.getText()));
        Platform.runLater(() -> refreshCreateOptions());
    }

    /**
     * Create the editor for label options.
     *
     * @param lbl the label options
     * @return the label options editor root
     */
    private TitledPane labelOptionsPane(LabelOptions lbl)
    {
        Button button = deleteButton("Remove label options.", () -> deleteLabelAction());

        List<String> colunms = layer.getMetaDataInfo().getKeyNames();
        LabelOptionsPanel labelPanel = new LabelOptionsPanel(lbl, colunms);
        HBox labelBox = new HBox(5, labelPanel, button);
        return new TitledPane(LABEL_TITLE, labelBox);
    }

    /** Write label settings back to the original model. */
    private void saveLabel()
    {
        if (labelEdit == null)
        {
            // no label options now, if they existed before, delete them
            if (labelAct != null)
            {
                currentAction.getActions().remove(labelAct);
            }
            return;
        }
        if (labelAct == null)
        {
            // label actions are new, so create and insert them
            labelAct = new LabelAction();
            currentAction.getActions().add(labelAct);
        }
        // copy values to the original model
        labelAct.getLabelOptions().copyFrom(labelEdit);
    }

    /** Create new label options and show the editor. */
    private void addLabelAction()
    {
        labelEdit = new LabelOptions();
        addActionPane(labelOptionsPane(labelEdit));
    }

    /** Remove the label options and their editor. */
    private void deleteLabelAction()
    {
        labelEdit = null;
        actionPanes.getPanes().removeIf(p -> LABEL_TITLE.equals(p.getText()));
        Platform.runLater(() -> refreshCreateOptions());
    }

    /**
     * Insert an editor into the accordion and expand it.
     *
     * @param pane the new editor root
     */
    private void addActionPane(TitledPane pane)
    {
        actionPanes.getPanes().add(pane);
        actionPanes.setExpandedPane(pane);
    }

    /**
     * Create a button styled to indicate that pressing it deletes things.
     *
     * @param tip a tooltip for the button
     * @param ear a callback to be invoked when the button is pushed
     * @return the button
     */
    private static Button deleteButton(String tip, Runnable ear)
    {
        Button b = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        b.setTooltip(new Tooltip(tip));
        b.setOnAction(e -> ear.run());
        return b;
    }

    /**
     * Search the list for any member of the specified class.
     *
     * @param <T> the type of the elements in the list
     * @param things a list of things
     * @param c a class to be found
     * @return a member of <i>c</i>, if any, or null
     */
    @SuppressWarnings("unchecked")
    private static <T> T getTypeElement(List<?> things, Class<T> c)
    {
        return things.stream().filter(x -> c.isInstance(x)).map(x -> (T)x).findAny().orElse(null);
    }

    /**
     * A layout manager that stacks subcomponents vertically and stretches
     * them horizontally.
     */
    protected static class StackLayout implements LayoutManager
    {
        /** The container whose subcomponents are managed. */
        private Container host;

        /** The managed subcomponents. */
        private final List<Component> kids = new LinkedList<>();

        /** Vertical spacing. */
        private static final int vSpace = 20;

        /**
         * Attach to the container; i.e., become its layout manager and
         * attribute all managed subcomponents as its children.
         *
         * @param p the container
         */
        public void attach(Container p)
        {
            host = p;
            for (Component c :  kids)
            {
                host.add(c);
            }
            host.setLayout(this);
        }

        /**
         * Add a new subcomponent to the bottom of the stack.
         *
         * @param c the new subcomponent
         */
        public void add(Component c)
        {
            kids.add(c);
            if (host != null)
            {
                host.add(c);
            }
        }

        /** Remove all subcomponents. */
        public void clear()
        {
            if (host != null)
            {
                host.removeAll();
            }
            kids.clear();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent)
        {
            Dimension d = new Dimension();
            for (Component c :  kids)
            {
                Dimension childPref = c.getPreferredSize();
                d.width = Math.max(d.width, childPref.width);
                d.height += childPref.height;
            }
            d.height += Math.max(0, kids.size() - 1) * vSpace;
            return d;
        }

        @Override
        public void layoutContainer(Container parent)
        {
            Dimension size = host.getSize();
            int y = 0;
            for (Component c :  kids)
            {
                int dy = Math.min(c.getPreferredSize().height, size.height - y);
                c.setBounds(0, y, size.width, dy);
                y += dy + vSpace;
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container parent)
        {
            return minimumLayoutSize(parent);
        }

        @Override
        public void addLayoutComponent(String name, Component comp)
        {
        }

        @Override
        public void removeLayoutComponent(Component comp)
        {
        }
    }
}
