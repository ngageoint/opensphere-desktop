package io.opensphere.csv.ui.format;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jdesktop.swingx.JXTable;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.swing.NamedSeparator;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent.Property;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.csv.ui.CSVWizardPanel;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.ColumnFormat;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.ui.CsvUiUtilities;
import io.opensphere.csvcommon.ui.format.ColumnWidthChooserPanel;
import io.opensphere.csvcommon.ui.format.FormatPanelModel;
import io.opensphere.importer.config.LayerSettings;

/**
 * The format panel for choosing things like header/data rows, columns, and
 * quote/comment characters.
 */
public class FormatPanel extends CSVWizardPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The UI model for this panel. */
    private final transient FormatPanelModel myModel;

    /** The model that indicates if the preview should be formatted. */
    private final BooleanModel myPreviewFormatted = new BooleanModel();

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Helper to create components.
     *
     * @param model The model
     * @param preferredComponent The preferred component type
     * @param glueCount The number of clue components to add at the end
     * @return The label and component
     */
    private static Component[] getComponents(ViewModel<?> model, Class<? extends JComponent> preferredComponent, int glueCount)
    {
        JComponent component = ControllerFactory.createComponent(model, preferredComponent);
        JLabel label = ControllerFactory.createLabel(model, component);
        Component[] components = new Component[2 + glueCount];
        components[0] = label;
        components[1] = component;
        for (int i = 0; i < glueCount; i++)
        {
            components[i + 2] = Box.createHorizontalGlue();
        }
        return components;
    }

    /**
     * Helper to create components.
     *
     * @param model The model
     * @param preferredComponent The preferred component type
     * @param model2 The second model
     * @return The label and component
     */
    private static Component[] getComponents(ViewModel<?> model, Class<? extends JComponent> preferredComponent,
            ViewModel<?> model2)
    {
        JComponent component1 = ControllerFactory.createComponent(model, preferredComponent);
        JLabel label = ControllerFactory.createLabel(model, component1);
        JComponent component2 = ControllerFactory.createComponent(model2);
        return new Component[] { label, component1, component2, Box.createHorizontalGlue() };
    }

    /**
     * Constructor.
     *
     * @param file The file which is being loaded.
     * @param selectedParameters The parameters for parsing the file.
     * @param detectedParameters The detected parameters.
     * @param sampler the line sampler
     */
    public FormatPanel(File file, CSVParseParameters selectedParameters, DetectedParameters detectedParameters,
            LineSampler sampler)
    {
        super();
        myModel = new FormatPanelModel(file, selectedParameters, detectedParameters, sampler);

        myPreviewFormatted.setNameAndDescription("Show Formatted", "Show the data formatted");
        myPreviewFormatted.set(Boolean.TRUE);

        buildUI();

        // Wire in validation
        myValidatorSupport.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
        myModel.addListener(new ChangeListener<CSVParseParameters>()
        {
            @Override
            public void changed(ObservableValue<? extends CSVParseParameters> observable, CSVParseParameters oldValue,
                    CSVParseParameters newValue)
            {
                myValidatorSupport.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
            }
        });

        myModel.getCustomDelimiter().addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void stateChanged(PropertyChangeEvent e)
            {
                if (e.getProperty() == Property.VISIBLE)
                {
                    revalidate();
                }
            }
        });
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
        myModel.updateModels(parse, layerSettings, detected, cellSampler);
    }

    /**
     * Builds the UI.
     */
    private void buildUI()
    {
        // Create the preview table
        final JXTable previewTable = new JXTable(myModel.getTableModel());
        previewTable.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
        previewTable.setFocusable(false);
        previewTable.packAll();
        myModel.getTableModel().addTableModelListener(new TableModelListener()
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

        // Create the widths panel
        String sampleText = CsvUiUtilities.combineText(myModel.getSampler().getBeginningSampleLines());
        final ColumnWidthChooserPanel widthsPanel = new ColumnWidthChooserPanel(sampleText, null);
        int[] columnWidths = myModel.getColumnDivisions();
        if (columnWidths != null && columnWidths.length > 0)
        {
            widthsPanel.setColumnBreaks(columnWidths);
        }
        widthsPanel.setActionListener(e -> myModel.setColumnDivisions(widthsPanel.getColumnBreaks()));

        // Create the text area
        final JTextArea textArea = new JTextArea(sampleText);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        final NamedSeparator bottomSeparator = new NamedSeparator(getBottomLabelText());

        final JScrollPane scrollPane = new JScrollPane(
                myModel.getColumnFormat().get() == ColumnFormat.DELIMITED ? previewTable : widthsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // Add listeners
        myModel.getColumnFormat().addListener((obs, o, n) -> bottomSeparator.getLabel().setText(getBottomLabelText()));
        ChangeListener<Object> viewportListener = (obs, o, n) -> setViewportView(previewTable, widthsPanel, textArea, scrollPane);
        myModel.getColumnFormat().addListener(viewportListener);
        myPreviewFormatted.addListener(viewportListener);

        // Define styles
        final String header = "header";
        final String label = "label";
        final String input = "input";
        final String panel = "panel";
        final String fill = "fill";
        style(header).setInsets(0, 0, 4, 0).fillHorizontal().setGridwidth(4);
        style(label).setInsets(4, 12, 4, 4).anchorWest();
        style(input).setInsets(0, 0, 4, 4).setFill(GridBagConstraints.HORIZONTAL);
        style(panel).setInsets(0, 0, 4, 0).anchorWest().setGridwidth(3);
        style(fill).fillHorizontal();

        // Layout everything
        init0();

        style(header).addRow(new NamedSeparator("Row Settings"));
        style(label, input, input, fill).addRow(getComponents(myModel.getHeaderRow(), JSpinner.class, myModel.getHasHeader()));
        style(label, input, input, fill).addRow(getComponents(myModel.getFirstDataRow(), JSpinner.class, 2));
        style(label, input, input, fill).addRow(getComponents(myModel.getCommentCharacter(), null, 2));

        style(header).setInsets(8, 0, 4, 0);
        style(header).addRow(new NamedSeparator("Column Settings"));
        style(label, panel, fill).addRow(getComponents(myModel.getColumnFormat(), RadioButtonPanel.class, 1));
        style(label, input, input, fill).addRow(getComponents(myModel.getColumnDelimiter(), null, myModel.getCustomDelimiter()));
        style(label, input, input, fill).addRow(getComponents(myModel.getQuoteCharacter(), null, 2));

        style(header).addRow(bottomSeparator);
        addRow(ControllerFactory.createComponent(myPreviewFormatted));
        setInsets(0, 0, 0, 0).fillBoth().setGridwidth(4).addRow(scrollPane);
    }

    /**
     * Gets the bottom label text based on the current state.
     *
     * @return the bottom label text
     */
    private String getBottomLabelText()
    {
        return myModel.getColumnFormat().get() == ColumnFormat.DELIMITED ? "Data View" : "Choose Column Breaks";
    }

    /**
     * Sets the viewport view.
     *
     * @param previewTable the preview table
     * @param widthsPanel the widths panel
     * @param textArea the text area
     * @param scrollPane the scroll pane
     */
    private void setViewportView(JXTable previewTable, ColumnWidthChooserPanel widthsPanel, JTextArea textArea,
            JScrollPane scrollPane)
    {
        Component view;
        if (!myPreviewFormatted.get().booleanValue())
        {
            view = textArea;
        }
        else if (myModel.getColumnFormat().get() == ColumnFormat.DELIMITED)
        {
            view = previewTable;
        }
        else
        {
            view = widthsPanel;
        }
        scrollPane.setViewportView(view);
    }
}
