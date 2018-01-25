package io.opensphere.csv.ui.columndefinition.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.swing.ExtendedComboBox;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.table.CheckBoxHeader;
import io.opensphere.csv.ui.CSVWizardPanel;
import io.opensphere.csv.ui.columndefinition.controller.ColumnDefinitionController;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.help.DateFormatHelp;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.ui.AvailableDataTypesBinder;
import io.opensphere.csvcommon.ui.columndefinition.ui.AvailableFormatsBinder;
import io.opensphere.csvcommon.ui.columndefinition.ui.ColumnDefinitionBinder;
import io.opensphere.csvcommon.ui.columndefinition.ui.ColumnDefinitionTableBinder;
import io.opensphere.csvcommon.ui.columndefinition.ui.ComboBoxCellRenderer;
import io.opensphere.csvcommon.ui.columndefinition.ui.EditableComboBoxCellEditor;
import io.opensphere.csvcommon.ui.columndefinition.ui.EditableTextCellRenderer;
import io.opensphere.importer.config.LayerSettings;

/**
 * The main panel that allows the users to define their columns to import, names
 * of the columns, and the types of the columns.
 *
 */
public class ColumnDefinitionPanel extends CSVWizardPanel
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Keeps the model and the warning messages in sync.
     */
    private final transient ColumnDefinitionBinder myBinder;

    /**
     * The column definition controller.
     */
    private transient ColumnDefinitionController myController;

    /**
     * Binds the available data types model to the available data types ui.
     */
    private transient AvailableDataTypesBinder myDataTypeBinder;

    /**
     * Used to show format help.
     */
    private final transient DateFormatHelp myFormatHelp;

    /**
     * Binds the available formats model to the available formats ui.
     */
    private transient AvailableFormatsBinder myFormatsBinder;

    /**
     * The model used by this ui.
     */
    private final transient ColumnDefinitionModel myModel;

    /** The Prefs. */
    private final transient PreferencesRegistry myPrefs;

    /**
     * Keeps the selected column row in sync with ui and model.
     */
    private transient ColumnDefinitionTableBinder myTableBinder;

    /**
     * Flag indicating if the column checkboxes are being changed as a result of
     * the "all" checkbox being changed.
     */
    private boolean myUpdating;

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Constructs a new column definition panel that allows the user to define
     * their columns within a csv import.
     *
     * @param preferencesRegistry The system toolbox.
     * @param selectedParameters The CSV import model to read and edit.
     * @param detectedParameters The auto detected parameters used to make
     *            guesses and order choices.
     * @param sampler Used to display before and after data.
     */
    public ColumnDefinitionPanel(PreferencesRegistry preferencesRegistry, CSVParseParameters selectedParameters,
            DetectedParameters detectedParameters, CellSampler sampler)
    {
        myModel = new ColumnDefinitionModel();
        myModel.setSelectedParameters(selectedParameters);
        myModel.setDetectedParameters(detectedParameters);
        myPrefs = preferencesRegistry;
        myFormatHelp = new DateFormatHelp();
        initComponents();

        myBinder = new ColumnDefinitionBinder(myValidatorSupport, myModel);
        myController = new ColumnDefinitionController(preferencesRegistry, myModel, sampler);
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    @Override
    public void updateModel(CSVParseParameters parse, LayerSettings layerSettings, DetectedParameters detected,
            CellSampler cellSampler)
    {
        myController.close();
        myModel.setSelectedDefinition(null);
        myModel.setSelectedParameters(parse);
        myModel.setDetectedParameters(detected);
        myModel.getDefinitionTableModel().clear();
        myController = new ColumnDefinitionController(myPrefs, myModel, cellSampler);
    }

    @Override
    protected void finalize() throws Throwable
    {
        myDataTypeBinder.close();
        myTableBinder.close();
        myController.close();
        myFormatsBinder.close();
        myBinder.close();
        super.finalize();
    }

    /**
     * Creates the before after table.
     *
     * @return The before after table.
     */
    private JTable createBeforeAfterTable()
    {
        final JXTable beforeAfterTable = new JXTable(myModel.getBeforeAfterTableModel());
        Font font = beforeAfterTable.getFont();
        beforeAfterTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, font == null ? 12 : font.getSize()));
        beforeAfterTable.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
        beforeAfterTable.packAll();
        beforeAfterTable.setSortable(false);
        beforeAfterTable.setColumnMargin(10);
        beforeAfterTable.getColumn(0).setPreferredWidth(130);
        beforeAfterTable.getColumn(1).setPreferredWidth(130);

        return beforeAfterTable;
    }

    /**
     * Creates and initializes the column definition table.
     *
     * @return The column definition table.
     */
    private JTable createColumnDefinitionTable()
    {
        final JXTable columnDefinitionTable = new JXTable(myModel.getDefinitionTableModel());
        columnDefinitionTable.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);

        final CheckBoxHeader checkboxHeader = new CheckBoxHeader("Import", columnDefinitionTable.getTableHeader());
        checkboxHeader.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (!myUpdating)
                {
                    myUpdating = true;
                    myController.setAllColumnsImport(e.getStateChange() == ItemEvent.SELECTED);
                    myUpdating = false;
                }
            }
        });
        columnDefinitionTable.getColumnModel().getColumn(0).setHeaderRenderer(checkboxHeader);

        myModel.getDefinitionTableModel().addTableModelListener(new TableModelListener()
        {
            @Override
            public void tableChanged(TableModelEvent e)
            {
                if (!myUpdating)
                {
                    myUpdating = true;
                    checkboxHeader.setSelected(myModel.getDefinitionTableModel().isImportAllColumns());
                    columnDefinitionTable.getTableHeader().repaint();
                    myUpdating = false;
                }
            }
        });

        columnDefinitionTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnDefinitionTable.setSortable(false);

        columnDefinitionTable.getColumn(0).setPreferredWidth(70);
        columnDefinitionTable.getColumn(1).setPreferredWidth(110);
        columnDefinitionTable.getColumn(1).setCellRenderer(new EditableTextCellRenderer());

        DefaultCellEditor nameEditor = new DefaultCellEditor(new JTextField());
        nameEditor.setClickCountToStart(1);
        columnDefinitionTable.getColumn(1).setCellEditor(nameEditor);

        JComboBox<String> availableDataTypes = new ExtendedComboBox<>(120);
        JComboBox<String> availableFormats = new ExtendedComboBox<>(200);

        columnDefinitionTable.getColumn(2).setPreferredWidth(110);
        columnDefinitionTable.getColumn(2).setCellRenderer(new ComboBoxCellRenderer(availableDataTypes, null));
        columnDefinitionTable.getColumn(3).setPreferredWidth(150);
        columnDefinitionTable.getColumn(3).setCellRenderer(new ComboBoxCellRenderer(availableFormats, "Type new format"));

        myDataTypeBinder = new AvailableDataTypesBinder(availableDataTypes, myModel);

        TableColumn dataTypeColumn = columnDefinitionTable.getColumnModel().getColumn(2);
        dataTypeColumn.setCellEditor(new EditableComboBoxCellEditor(availableDataTypes));

        availableFormats.setEditable(true);
        myFormatsBinder = new AvailableFormatsBinder(availableFormats, myModel);

        TableColumn formatColumn = columnDefinitionTable.getColumnModel().getColumn(3);
        formatColumn.setCellEditor(new EditableComboBoxCellEditor(availableFormats));

        myTableBinder = new ColumnDefinitionTableBinder(columnDefinitionTable, myModel);

        return columnDefinitionTable;
    }

    /**
     * Creates and initializes the UI components.
     */
    private void initComponents()
    {
        JTable columnDefinitionTable = createColumnDefinitionTable();
        JTable beforeAfterTable = createBeforeAfterTable();

        JScrollPane definitionPane = new JScrollPane(columnDefinitionTable);
        definitionPane.setPreferredSize(new Dimension(415, 600));

        JScrollPane beforeAfterPane = new JScrollPane(beforeAfterTable);
        beforeAfterPane.setPreferredSize(new Dimension(325, 600));

        GridBagPanel definitionPanel = new GridBagPanel();
        definitionPanel.fillBoth().setGridwidth(1).addRow(definitionPane);
        definitionPanel.setBorder(BorderFactory.createTitledBorder("Column Definitions"));

        GridBagPanel beforeAfterPanel = new GridBagPanel();
        beforeAfterPanel.fillBoth().setGridwidth(1).addRow(beforeAfterPane);
        beforeAfterPanel.setBorder(BorderFactory.createTitledBorder("Preview"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, definitionPanel, beforeAfterPanel);
        splitPane.setBorder(null);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.5);

        IconButton helpButton = new IconButton();
        helpButton.setIcon("/images/sc_helperdialog.png");
        helpButton.setPressedIcon("/images/sc_helperdialog_pressed.png");
        helpButton.setBorder(null);
        helpButton.setSize(16, 16);
        helpButton.setFocusPainted(false);
        helpButton.setContentAreaFilled(false);
        helpButton.setBorder(null);
        helpButton.setToolTipText("Show date format help");
        helpButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myFormatHelp.showHelp(SwingUtilities.windowForComponent(ColumnDefinitionPanel.this));
            }
        });

        setInsets(0, 0, 0, 0).fillNone().setAnchor(GridBagConstraints.EAST).setGridwidth(0).addRow(helpButton);

        setInsets(0, 0, 0, 0).fillBoth().setGridwidth(1).addRow(splitPane);
    }
}
