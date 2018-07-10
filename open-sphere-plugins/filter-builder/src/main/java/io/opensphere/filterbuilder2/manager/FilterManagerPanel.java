package io.opensphere.filterbuilder2.manager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.bind.JAXBException;

import io.opensphere.controlpanels.layers.SearchAvailableLayersPanel;
import io.opensphere.controlpanels.layers.activedata.controller.AvailableDataDataLayerController;
import io.opensphere.controlpanels.layers.activedata.controller.PredicatedAvailableDataDataLayerController;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder.controller.FilterSet;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.FilterEditorDialog;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.filter.HasPredicatedMemberPredicate;

/**
 * The main filter manager panel.
 */
public class FilterManagerPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The filter builder toolbox. */
    private final FilterBuilderToolbox myFbToolbox;

    /** The data type info or null if there are multiple types. */
    private final DataTypeInfo myDataType;

    /** The operator for combining filters. */
    private ChoiceModel<Logical> myOperator;

    /** The export button. */
    private JButton myExportButton;

    /** The import button. */
    private JButton myImportButton;

    /**
     * The main panel in which filters are managed.
     */
    private FilterManagerFiltersPanel myMainPanel;

    /**
     * The filter set managed in the panel.
     */
    private FilterSet myFilterSet;

    /**
     * A flag used to track the state of the persistence mode, defaults to true.
     */
    private boolean myPersistMode = true;

    /**
     * Constructor.
     *
     * @param fbToolbox the filter builder toolbox
     * @param dataType the data type info
     */
    public FilterManagerPanel(FilterBuilderToolbox fbToolbox, DataTypeInfo dataType)
    {
        myFbToolbox = fbToolbox;
        myDataType = dataType;
        if (myDataType != null)
        {
            myOperator = myFbToolbox.getController().getCombinationOperator(myDataType.getTypeKey());
        }

        fillHorizontal();
        addRow(buildTopPanel());

        fillBoth();
        anchorWest();
        addRow(buildCenterPanel());

        createExportButton();
        createImportButton();
    }

    /**
     * Creates a new panel, configured with the supplied toolbox and filter set.
     *
     * @param tools the toolbox with which to configure the panel.
     * @param fs the filter set with which to configure the panel.
     */
    public FilterManagerPanel(FilterBuilderToolbox tools, FilterSet fs)
    {
        myPersistMode = false;
        myFbToolbox = tools;
        myFilterSet = fs;
        myDataType = getType(myFilterSet.getTypeKey());
        myOperator = new ChoiceModel<>();
        myOperator.setOptions(new Logical[] { Logical.AND, Logical.OR });
        myOperator.set(myFilterSet.getLogicOp());
        myOperator.addListener((o, v0, v1) -> myFilterSet.setFilterOp(v1));

        fillHorizontal();
        addRow(buildTopPanel());

        fillBoth();
        anchorWest();
        addRow(buildCenterPanel2(myFilterSet));
    }

    /**
     * Gets the {@link DataTypeInfo} corresponding to the supplied type key.
     *
     * @param typeKey the type key for which to search.
     * @return the {@link DataTypeInfo} corresponding to the supplied type key, or null if none could be found.
     */
    private DataTypeInfo getType(String typeKey)
    {
        return myFbToolbox.getMantleToolBox().getDataGroupController().findMemberById(typeKey);

        // GCD: doesn't work!
        // return myFbToolbox.getMantleToolBox().getDataTypeController().
        // getDataTypeInfoForType(typeKey);
    }

    /**
     * Gets the export button.
     *
     * @return the export button
     */
    public JButton getExportButton()
    {
        return myExportButton;
    }

    /**
     * Gets the import button.
     *
     * @return the import button
     */
    public JButton getImportButton()
    {
        return myImportButton;
    }

    /**
     * Builds the center panel.
     *
     * @return the center panel
     */
    private JComponent buildCenterPanel()
    {
        String typeKey = myDataType == null ? null : myDataType.getTypeKey();
        myMainPanel = new FilterManagerFiltersPanel(myFbToolbox, typeKey);
        return scroll(myMainPanel);
    }

    /**
     * Creates a panel in which the supplied filter set is displayed. This is generated to allow editing a temporary filter set.
     *
     * @param fs the filter set with which the panel will be configured.
     * @return a new {@link JComponent} in which a filter set editor is displayed.
     */
    private JComponent buildCenterPanel2(FilterSet fs)
    {
        myMainPanel = new FilterManagerFiltersPanel(myFbToolbox, fs);
        return scroll(myMainPanel);
    }

    /**
     * Creates a new scroll pane wrapped around the supplied component.
     *
     * @param c the component to wrap.
     * @return a new {@link JScrollPane} in which the supplied component is wrapped.
     */
    private static JScrollPane scroll(Component c)
    {
        JScrollPane scrollPane = new JScrollPane(c);
        int width = Math.max(c.getPreferredSize().width + 24, 300);
        int height = 400;
        scrollPane.setPreferredSize(new Dimension(width, height));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    /**
     * Builds the top panel.
     *
     * @return the top panel
     */
    private JPanel buildTopPanel()
    {
        JButton createButton = new JButton("New Filter");
        IconUtil.setIcons(createButton, IconType.PLUS, Color.GREEN);
        createButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        createButton.setToolTipText("Add a new filter");
        createButton.addActionListener(e -> editNewFilter());

        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.setInsets(0, 0, 6, 0);
        panel.add(createButton);
        panel.fillHorizontalSpace().fillNone();
        if (myOperator != null)
        {
            panel.add(Box.createHorizontalStrut(Constants.INSET * 3));
            panel.add(new JLabel("Match: "));
            panel.add(ControllerFactory.createComponent(myOperator));
        }
        return panel;
    }

    /**
     * Creates the export button.
     *
     * @return the export button
     */
    private JButton createExportButton()
    {
        myExportButton = new JButton("Export");
        IconUtil.setIcons(myExportButton, IconType.EXPORT);
        myExportButton.setMargin(ButtonPanel.INSETS_JOPTIONPANE);
        myExportButton.setToolTipText("Export filters to a file");
        myExportButton.addActionListener(e -> exportFilter());
        return myExportButton;
    }

    /**
     * Creates the import button.
     *
     * @return the import button
     */
    private JButton createImportButton()
    {
        myImportButton = new JButton("Import");
        IconUtil.setIcons(myImportButton, IconType.IMPORT);
        myImportButton.setMargin(ButtonPanel.INSETS_JOPTIONPANE);
        myImportButton.setToolTipText("Import filters from a file");
        myImportButton.addActionListener(e -> importFilter());
        return myImportButton;
    }

    /**
     * Shows the edit new filter dialog.
     */
    private void editNewFilter()
    {
        QuantifyToolboxUtils.collectMetric(myFbToolbox.getMainToolBox(), "mist3d.filter-manager.new");
        DataTypeInfo editDataType = myDataType;

        // If no data type, ask the user for it
        if (editDataType == null)
        {
            AvailableDataDataLayerController controller = new PredicatedAvailableDataDataLayerController(
                    myFbToolbox.getMainToolBox(), new HasPredicatedMemberPredicate(DataTypeInfo.HAS_METADATA_PREDICATE));
            SearchAvailableLayersPanel layerPanel = new SearchAvailableLayersPanel(myFbToolbox.getMainToolBox(), controller,
                    TreeSelectionModel.SINGLE_TREE_SELECTION, DataTypeInfo.HAS_METADATA_PREDICATE);
            layerPanel.initGuiElements();
            OptionDialog dialog = new OptionDialog(SwingUtilities.getWindowAncestor(this), layerPanel, "Select Layer");
            dialog.buildAndShow();
            if (dialog.getSelection() == JOptionPane.OK_OPTION)
            {
                editDataType = CollectionUtilities.getItemOrNull(layerPanel.getSelectedDataTypes(), 0);
            }
        }

        // Edit the new filter
        if (editDataType != null)
        {
            List<String> columns = myFbToolbox.getColumnsForDataType(editDataType);
            if (columns.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Unable to create filter for layer without meta data.",
                        "Unable to Create Filter", JOptionPane.WARNING_MESSAGE);
            }
            else
            {
                Filter filter = new Filter("", editDataType);
                Window parent = SwingUtilities.getWindowAncestor(this);
                FilterEditorDialog editorDialog = new FilterEditorDialog(parent, myFbToolbox, filter, editDataType, true);
                editorDialog.setPersistMode(myPersistMode);
                if (!myPersistMode)
                {
                    editorDialog.setNewFilterEar(() ->
                    {
                        myFilterSet.getFilters().add(filter);
                        myMainPanel.regenFilterSet();
                    });
                }
                editorDialog.buildAndShow();
            }
        }
    }

    /**
     * Exports a filter.
     */
    private void exportFilter()
    {
        QuantifyToolboxUtils.collectMetric(myFbToolbox.getMainToolBox(), "mist3d.filter-manager.export");
        // Show the export dialog
        ExportFilterPanel exportPanel = new ExportFilterPanel(myFbToolbox);
        String title = StringUtilities.concat("Export ", myDataType != null ? myDataType.getDisplayName() : "All", " Filters");
        OptionDialog dialog = new OptionDialog(SwingUtilities.getWindowAncestor(this), exportPanel, title);
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            // Have the controller export the filter
            String dataTypeKey = myDataType != null ? myDataType.getTypeKey() : null;
            try
            {
                myFbToolbox.getController().exportFilters(exportPanel.getFile(), dataTypeKey, exportPanel.isActiveOnly());
            }
            catch (JAXBException e)
            {
                String message = "Failed to export file: " + e.getMessage();
                JOptionPane.showMessageDialog(this, message, "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Imports a filter.
     */
    private void importFilter()
    {
        QuantifyToolboxUtils.collectMetric(myFbToolbox.getMainToolBox(), "mist3d.filter-manager.import");
        // Show the import dialog
        MnemonicFileChooser chooser = new MnemonicFileChooser(myFbToolbox.getMainToolBox().getPreferencesRegistry(),
                ExportFilterPanel.class.getName());
        chooser.setDialogTitle("Choose File");
        chooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
        if (chooser.showDialog(SwingUtilities.getWindowAncestor(this), "OK") == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null)
            {
                // Have the controller import the filter
                myFbToolbox.getController().importFilters(selectedFile);
                validate();
            }
        }
    }
}
