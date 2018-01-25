package io.opensphere.filterbuilder2.manager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.scene.control.ButtonBar.ButtonData;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.columns.MutableColumnMappingController;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.ScrollableGridBagPanel;
import io.opensphere.filterbuilder.controller.FilterBuilderController;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.FilterEditorDialog;
import io.opensphere.filterbuilder2.layers.LayerAssociationPane;

/**
 * View for managing a single filter.
 */
public class FilterManagerFilterPanel extends ScrollableGridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * A constant in which the title of the panel is defined.
     */
    private static final String DEL_TITLE = "Confirm Delete!";

    /**
     * A constant in which the prefix of the delete message is defined.
     */
    private static final String DEL_MSG_PREFIX = "<html>Filter <b>";

    /**
     * A constant in which the suffix of the delete message is defined.
     */
    private static final String DEL_MSG_SUFFIX = "</b> no longer supports any\n" + "layers and will be deleted.  Is that okay?";

    /** The filter builder toolbox. */
    private final transient FilterBuilderToolbox myFbToolbox;

    /**
     * The controller through which the filter builder is managed.
     */
    private final transient FilterBuilderController myFbController;

    /**
     * The toolbox through which the panel interacts with the rest of the application.
     */
    private final transient  Toolbox myToolbox;

    /**
     * The column mapping controller in which mappings are managed.
     */
    private final transient MutableColumnMappingController myColumnMappingController;

    /** The filter. */
    private final Filter myFilter;

    /** The check box. */
    private JCheckBox myCheckBox;

    /**
     * A flag used to indicate that the panel has been launched in persistence mode. Defaults to true.
     */
    private boolean myPersistMode = true;

    /**
     * The thread on which the delete operation executes.
     */
    private transient Runnable myDeleteEar;

    /**
     * Constructor.
     *
     * @param fbToolbox the filter builder toolbox
     * @param filter the filter
     */
    public FilterManagerFilterPanel(FilterBuilderToolbox fbToolbox, Filter filter)
    {
        myFbToolbox = fbToolbox;
        myFbController = myFbToolbox.getController();
        myToolbox = myFbToolbox.getMainToolBox();
        myColumnMappingController = (MutableColumnMappingController)myToolbox.getDataFilterRegistry()
                .getColumnMappingController();
        myFilter = filter;
    }

    /**
     * Sets the value of the {@link #myPersistMode} field.
     *
     * @param pPersistMode the value to store in the {@link #myPersistMode} field.
     */
    public void setPersistMode(boolean pPersistMode)
    {
        myPersistMode = pPersistMode;
    }

    /**
     * Sets the value of the {@link #myDeleteEar} field.
     *
     * @param pDeleteEar the value to store in the {@link #myDeleteEar} field.
     */
    public void setDeleteEar(Runnable pDeleteEar)
    {
        myDeleteEar = pDeleteEar;
    }

    /**
     * Gets the value of the {@link #myFilter} field.
     *
     * @return the value stored in the {@link #myFilter} field.
     */
    public Filter getFilter()
    {
        return myFilter;
    }

    /**
     * Gets the check box.
     *
     * @return the check box
     */
    public JCheckBox getCheckBox()
    {
        return myCheckBox;
    }

    /** Builds the filter panel. */
    public void buildPanel()
    {
        myCheckBox = new JCheckBox(null, null, myFilter.isActive());
        if (myFilter.getFilterDescription() != null)
        {
            myCheckBox.setToolTipText(myFilter.getFilterDescription());
        }
        myCheckBox.addItemListener(this::toggleActivation);

        JTextArea checkBoxText = new JTextArea(myFilter.getName());
        checkBoxText.setWrapStyleWord(true);
        checkBoxText.setLineWrap(true);
        checkBoxText.setBorder(null);
        checkBoxText.setEditable(false);
        checkBoxText.setBackground(getBackground());
        checkBoxText.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent pE)
            {
                myCheckBox.setSelected(!myCheckBox.isSelected());
            }
        });

        IconButton chooseLayersButton = new IconButton(IconType.STACK);
        chooseLayersButton.setEnabled(myPersistMode);
        chooseLayersButton.setToolTipText("Choose layers for the filter");
        chooseLayersButton.addActionListener(e -> chooseLayers());

        IconButton editButton = new IconButton(IconType.EDIT);
        editButton.setToolTipText("Edit the filter");
        editButton.addActionListener(e -> editFilter());

        IconButton deleteButton = new IconButton(IconType.CLOSE, Color.RED);
        deleteButton.setToolTipText("Delete the filter (after confirmation)");
        deleteButton.addActionListener(e -> deleteFilter(deleteButton));

        fillNone().setInsets(0, 0, 0, Constants.INSET);
        add(myCheckBox);
        fillHorizontal().anchorWest();
        add(checkBoxText);
        fillNone().setInsets(Constants.INSET, 0, Constants.INSET, Constants.INSET);
        add(chooseLayersButton);
        add(editButton);
        add(deleteButton);
    }

    /**
     * Toggles filter activation.
     *
     * @param e the item event.
     */
    private void toggleActivation(ItemEvent e)
    {
        myFilter.setActive(e.getStateChange() == ItemEvent.SELECTED, this);
        if (myPersistMode)
        {
            myFbController.saveFilters();
        }
    }

    /** Choose layers for the filter. */
    private void chooseLayers()
    {
        if (!myPersistMode)
        {
            return;
        }

        LayerAssociationPane lap = new LayerAssociationPane(myColumnMappingController, myFbToolbox.getMantleToolBox(), myFilter);
        JFrame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
        JFXDialog dialog = new JFXDialog(parent, "Choose Layers for " + myFilter.getName());
        dialog.setFxNode(lap);
        dialog.setConfirmer(() -> !lap.requireConfirmation() || userConf(dialog, DEL_TITLE, getDeleteMsg()));
        dialog.setMinimumSize(new Dimension(300, 300));
        dialog.setSize(460, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        if (dialog.getResponse() == ButtonData.CANCEL_CLOSE)
        {
            return;
        }

        myFbController.updateFilter(myFilter);
        myFbController.saveFilters();
    }

    /**
     * Gets the delete message configured for the bound filter.
     *
     * @return the delete message configured for the bound filter.
     */
    private String getDeleteMsg()
    {
        return DEL_MSG_PREFIX + myFilter.getName() + DEL_MSG_SUFFIX;
    }

    /**
     * Launches the confirmation dialog with the supplied parameters, and returns the result.
     *
     * @param parent the component to use as the parent of the dialog.
     * @param title the title of the dialog.
     * @param msg the message to display in the dialog.
     * @return true if the user confirmed the message, false otherwise.
     */
    private static boolean userConf(Component parent, String title, String msg)
    {
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Deletes a filter.
     *
     * @param parent the parent component
     */
    private void deleteFilter(Component parent)
    {
        if (myPersistMode && (myFilter.getOtherSources().size() > 1 || userConf(parent, DEL_TITLE, getDeleteMsg())))
        {
            myFbController.removeFilter(myFilter);
            myFbController.saveFilters();
        }
        if (myDeleteEar != null)
        {
            myDeleteEar.run();
        }
    }

    /** Edits a filter using the new filter builder. */
    private void editFilter()
    {
        Component parent = SwingUtilities.getWindowAncestor(this);
        FilterEditorDialog editorDialog = new FilterEditorDialog(parent, myFbToolbox, myFilter, false);
        editorDialog.buildAndShow();
    }
}
