package io.opensphere.csv.ui.summary;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.NamedSeparator;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.csv.ui.CSVWizardPanel;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.ColumnFormat;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.common.Utilities;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.ui.CsvLineTableModel;
import io.opensphere.csvcommon.ui.CsvUiUtilities;
import io.opensphere.importer.config.LayerSettings;
import io.opensphere.importer.config.SpecialColumn;

/**
 * The summary panel.
 */
public class SummaryPanel extends CSVWizardPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The UI model for this panel. */
    private final transient SummaryPanelModel myModel;

    /** The model that indicates if the preview should be formatted. */
    private final BooleanModel myPreviewFormatted = new BooleanModel();

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /** The Format settings panel. */
    private GridBagPanel myFormatSettingsPanel;

    /** The Top panel. */
    private JPanel myTopPanel;

    /** The Csv line table model. */
    private CsvLineTableModel myCsvLineTableModel;

    /** The Special column map. */
    private TIntObjectHashMap<SpecialColumn> mySpecialColumnMap;

    /** The Selected params. */
    private transient CSVParseParameters mySelectedParams;

    /**
     * Helper to format text.
     *
     * @param text the text
     * @return the formatted text
     */
    private static String formatText(String text)
    {
        String formatted;
        if (text == null)
        {
            formatted = "None";
        }
        else if (" ".equals(text))
        {
            formatted = "Space(s)";
        }
        else if ("\t".equals(text))
        {
            formatted = "Tab";
        }
        else
        {
            formatted = text;
        }
        return formatted;
    }

    /**
     * Helper to create components.
     *
     * @param models The models
     * @return The label and component
     */
    private static Component[] getComponents(ViewModel<?>... models)
    {
        Component[] components = new Component[models.length * 2];
        int i = 0;
        for (ViewModel<?> model : models)
        {
            JComponent component = ControllerFactory.createComponent(model);
            JLabel label = ControllerFactory.createLabel(model, component);
            components[i++] = label;
            components[i++] = component;
        }
        return components;
    }

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     * @param selectedParameters The parameters for parsing the file.
     * @param layerSettings The layer settings.
     * @param sampler the line sampler
     * @param namesInUse the names in use
     */
    public SummaryPanel(Toolbox toolbox, CSVParseParameters selectedParameters, LayerSettings layerSettings, LineSampler sampler,
            Set<String> namesInUse)
    {
//        (new BorderLayout(0, 4));
        BorderLayout layout = new BorderLayout(0, 4);
        setLayout(layout);

        myModel = new SummaryPanelModel(layerSettings, namesInUse);
        mySelectedParams = selectedParameters;

        // Build the UI
        buildTopPanel();
        add(myTopPanel, BorderLayout.NORTH);
        add(buildTablePanel(sampler, toolbox.getPreferencesRegistry()), BorderLayout.CENTER);

        // Wire in validation
        myValidatorSupport.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
        myModel.addListener(new ChangeListener<LayerSettings>()
        {
            @Override
            public void changed(ObservableValue<? extends LayerSettings> observable, LayerSettings oldValue,
                    LayerSettings newValue)
            {
                myValidatorSupport.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
            }
        });
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Builds the format settings panel.
     *
     * @param selectedParameters The parameters for parsing the file.
     * @return the format settings panel
     */
    private JPanel buildFormatSettingsPanel(CSVParseParameters selectedParameters)
    {
        myFormatSettingsPanel = new GridBagPanel();

        // Get the values to display
        ColumnFormat columnFormat = selectedParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat
                ? ColumnFormat.FIXED_WIDTH : ColumnFormat.DELIMITED;
        String columnDelimiter = null;
        String quoteCharacter = null;
        if (selectedParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            columnDelimiter = ((CSVDelimitedColumnFormat)selectedParameters.getColumnFormat()).getTokenDelimiter();
            quoteCharacter = ((CSVDelimitedColumnFormat)selectedParameters.getColumnFormat()).getTextDelimiter();
        }
        columnDelimiter = formatText(columnDelimiter);
        quoteCharacter = formatText(quoteCharacter);
        String commentCharacter = formatText(selectedParameters.getCommentIndicator());

        // Define styles
        final String header = "header";
        final String label = "label";
        final String value = "value";
        final String valueFill = "valueFill";
        myFormatSettingsPanel.style(header).setInsets(0, 0, 4, 0).fillHorizontal().setGridwidth(4);
        myFormatSettingsPanel.style(label).setInsets(0, 12, 4, 4).anchorWest();
        myFormatSettingsPanel.style(value).setInsets(0, 0, 4, 12).anchorWest();
        myFormatSettingsPanel.style(valueFill).setInsets(0, 0, 4, 0).fillHorizontal();

        // Layout everything
        myFormatSettingsPanel.init0();
        myFormatSettingsPanel.style(header).addRow(new NamedSeparator("Format Settings"));
        myFormatSettingsPanel.style(label, value, label, valueFill).addRow(new JLabel("Column Format:"),
                new JLabel(columnFormat.toString()), new JLabel("Comment Character:"), new JLabel(commentCharacter));
        if (columnFormat == ColumnFormat.DELIMITED)
        {
            myFormatSettingsPanel.style(label, value, label, valueFill).addRow(new JLabel("Column Delimiter:"),
                    new JLabel(columnDelimiter), new JLabel("Text Delimiter:"), new JLabel(quoteCharacter));
        }

        return myFormatSettingsPanel;
    }

    /**
     * Builds the table scroll pane.
     *
     * @param sampler the line sampler
     * @param preferencesRegistry The preferences registry.
     * @return the table scroll pane
     */
    private Component buildFormattedPreviewTable(LineSampler sampler, PreferencesRegistry preferencesRegistry)
    {
        myCsvLineTableModel = new CsvLineTableModel(sampler, preferencesRegistry);
        myCsvLineTableModel.setSelectedParameters(mySelectedParams, false);

        mySpecialColumnMap = Utilities.createSpecialColumnMap(mySelectedParams.getSpecialColumns());

        final JXTable previewTable = new JXTable(myCsvLineTableModel);
        previewTable.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);

        final TableCellRenderer defaultRenderer = previewTable.getTableHeader().getDefaultRenderer();
        previewTable.getTableHeader().setDefaultRenderer(new TableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column)
            {
                Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (!mySpecialColumnMap.isEmpty() && column > -1)
                {
                    String columnName = table.getColumnName(column);

                    int parameterColumn = mySelectedParams.getColumnNames().indexOf(columnName);

                    String columnType = null;
                    SpecialColumn specialColumn = mySpecialColumnMap.get(parameterColumn);
                    if (specialColumn != null && specialColumn.getColumnType() != null)
                    {
                        columnType = specialColumn.getColumnType().toString();
                    }

                    StringBuilder text = new StringBuilder(64);
                    text.append("<html>" + "<center>").append(columnName).append("</center>");
                    if (columnType != null)
                    {
                        text.append("<center><b>(").append(columnType).append(")</b></center>");
                    }
                    else
                    {
                        text.append("&nbsp;");
                    }
                    text.append("</html>");

                    ((JLabel)component).setText(text.toString());
                }
                return component;
            }
        });
        previewTable.packAll();

        myCsvLineTableModel.addTableModelListener(new TableModelListener()
        {
            @Override
            public void tableChanged(TableModelEvent e)
            {
                if (e.getFirstRow() == 0)
                {
                    previewTable.packAll();
                }
            }
        });

        return previewTable;
    }

    /**
     * Builds the layer settings panel.
     *
     * @return the layer settings panel
     */
    private JPanel buildLayerSettingsPanel()
    {
        GridBagPanel panel = new GridBagPanel();

        // Define styles
        final String header = "header";
        final String label = "label";
        final String inputFill = "inputFill";
        final String inputNoFill = "inputNoFill";
        panel.style(header).setInsets(0, 0, 4, 0).fillHorizontal().setGridwidth(4);
        panel.style(label).setInsets(0, 12, 4, 4).anchorWest();
        panel.style(inputFill).setInsets(0, 0, 4, 0).fillHorizontal().setGridwidth(3);
        panel.style(inputNoFill).setInsets(0, 0, 4, 0).anchorWest();

        // Layout everything
        panel.init0();
        panel.style(header).addRow(new NamedSeparator("Layer Settings"));
        panel.style(label, inputFill).addRow(getComponents(myModel.getLayerName()));
        panel.style(label, inputNoFill).addRow(getComponents(myModel.getLayerColor()));

        return panel;
    }

    /**
     * Builds the table panel.
     *
     * @param sampler the line sampler
     * @param preferencesRegistry The preferences registry.
     * @return the table panel
     */
    private JPanel buildTablePanel(final LineSampler sampler, PreferencesRegistry preferencesRegistry)
    {
        myPreviewFormatted.setNameAndDescription("Show Formatted", "Show the data formatted");
        myPreviewFormatted.set(Boolean.TRUE);
        JComponent component = ControllerFactory.createComponent(myPreviewFormatted);

        GridBagPanel controlPanel = new GridBagPanel();
        controlPanel.fillHorizontal().addRow(new NamedSeparator("Data View"));
        controlPanel.fillNone().anchorWest().addRow(component);

        final Component formattedPreview = buildFormattedPreviewTable(sampler, preferencesRegistry);
        formattedPreview.setFocusable(false);

        final JScrollPane scrollPane = new JScrollPane(formattedPreview);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        myPreviewFormatted.addListener(new ChangeListener<Boolean>()
        {
            /** The unformatted preview. */
            private JTextArea myTextArea;

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (myPreviewFormatted.get().booleanValue())
                {
                    scrollPane.setViewportView(formattedPreview);
                }
                else
                {
                    if (myTextArea == null)
                    {
                        myTextArea = new JTextArea(CsvUiUtilities.combineText(sampler.getBeginningSampleLines()));
                        myTextArea.setEditable(false);
                        myTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                    }
                    scrollPane.setViewportView(myTextArea);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the top panel.
     */
    private void buildTopPanel()
    {
        myTopPanel = new JPanel(new BorderLayout(0, 4));
        myTopPanel.add(buildLayerSettingsPanel(), BorderLayout.NORTH);
        myTopPanel.add(buildFormatSettingsPanel(mySelectedParams), BorderLayout.CENTER);
    }

    @Override
    public void updateModel(CSVParseParameters parse, LayerSettings layerSettings, DetectedParameters detected,
            CellSampler cellSampler)
    {
        myTopPanel.remove(myFormatSettingsPanel);
        buildFormatSettingsPanel(parse);
        myTopPanel.add(myFormatSettingsPanel, BorderLayout.CENTER);
        myTopPanel.revalidate();
        myCsvLineTableModel.clear();
        myCsvLineTableModel.setSampler(cellSampler);
        myCsvLineTableModel.setSelectedParameters(parse, false);
        mySelectedParams = parse;
        mySpecialColumnMap = Utilities.createSpecialColumnMap(mySelectedParams.getSpecialColumns());
        myModel.set(layerSettings);
    }
}
