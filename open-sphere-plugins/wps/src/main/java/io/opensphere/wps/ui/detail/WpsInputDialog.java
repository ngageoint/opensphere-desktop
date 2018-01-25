package io.opensphere.wps.ui.detail;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;

import javax.swing.JDialog;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.LabeledObservableButtonBar;
import io.opensphere.wps.config.v2.ProcessSetting;
import io.opensphere.wps.config.v2.WpsProcessSettings;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.layer.WpsDataTypeInfoBuilder;
import io.opensphere.wps.request.WpsExecutionMode;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.WpsUtilities;
import net.opengis.wps._100.InputDescriptionType;
import net.opengis.wps._100.ProcessDescriptionType.DataInputs;

/**
 * A Swing / JavaFX dialog in which a form container is rendered.
 */
public class WpsInputDialog extends JDialog
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = 6150560690243670315L;

    /**
     * The container in which the dialog's contents are rendered.
     */
    private final AsynchronousFormContainer myFormContainer;

    /**
     * The consumer to call when the dialog is dismissed.
     */
    private final BiConsumer<WpsProcessConfiguration, WpsDataTypeInfo> myResultConsumer;

    /** The plugin preferences. */
    private final Preferences myPreferences;

    /**
     * A builder instance used to construct a new WPS data type for the
     * configured process (and with which results will be associated).
     */
    private final WpsDataTypeInfoBuilder myDataTypeBuilder;

    /**
     * Creates a new JFX Dialog, with a custom supplied button bar.
     *
     * @param pOwner The component which owns the dialog, may be null.
     * @param pTitle The textual title to display within the dialog.
     * @param pFxSupplier The supplier of the JavaFX component.
     * @param pDataTypeBuilder A builder instance used to construct a new WPS
     *            data type for the configured process (and with which results
     *            will be associated).
     * @param pButtonBarSupplier the supplier method used to add the button bar.
     * @param pResultConsumer the consumer to call when the dialog is dismissed.
     * @param preferences The plugin preferences
     */
    public WpsInputDialog(Window pOwner, String pTitle, Service<WpsProcessForm> pFxSupplier,
            WpsDataTypeInfoBuilder pDataTypeBuilder,
            Supplier<LabeledObservableButtonBar> pButtonBarSupplier,
            BiConsumer<WpsProcessConfiguration, WpsDataTypeInfo> pResultConsumer, Preferences preferences)
    {
        super(pOwner, pTitle, ModalityType.MODELESS);
        myDataTypeBuilder = pDataTypeBuilder;
        myResultConsumer = pResultConsumer;
        myPreferences = preferences;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        assert EventQueue.isDispatchThread();

        final JFXPanel fxPanel = new JFXPanel();
        add(fxPanel);

        myFormContainer = new AsynchronousFormContainer(pFxSupplier, pButtonBarSupplier, this::handleButtonClick);
        fxPanel.setScene(FXUtilities.addDesktopStyle(new Scene(myFormContainer)));
    }

    /**
     * Handles a button click.
     *
     * @param pButtonData the button clicked
     */
    protected void handleButtonClick(ButtonData pButtonData)
    {
        assert Platform.isFxApplicationThread();

        if (pButtonData == ButtonData.APPLY || pButtonData == ButtonData.OK_DONE)
        {
            WpsProcessForm form = myFormContainer.getForm();

            WpsProcessConfigurationVisitor visitor = new WpsProcessConfigurationVisitor(form.getServerId(),
                    form.getProcessDescription());
            form.visit(visitor);
            WpsProcessConfiguration processConfiguration = visitor.getResult();

            myDataTypeBuilder.setDisplayName(processConfiguration.getProcessTitle());
            WpsDataTypeInfo dataType = myDataTypeBuilder.build(processConfiguration.getInstanceId());
            processConfiguration.setResultType(dataType);
            dataType.setProcessConfiguration(processConfiguration);

            processConfiguration.setProcessIdentifier(form.getProcessDescription().getIdentifier().getValue());
            if (pButtonData == ButtonData.APPLY)
            {
                processConfiguration.setRunMode(WpsExecutionMode.RUN_ONCE);
            }
            else
            {
                processConfiguration.setRunMode(WpsExecutionMode.SAVE_AND_RUN);
            }

            saveSettings(processConfiguration);

            myResultConsumer.accept(processConfiguration, dataType);
        }

        // OK to call off the swing thread
        dispose();
    }

    /**
     * Saves the configuration to the preferences.
     *
     * @param processConfiguration the configuration
     */
    protected void saveSettings(WpsProcessConfiguration processConfiguration)
    {
        WpsProcessSettings processSettings = myPreferences.getJAXBObject(WpsProcessSettings.class, "processSettings",
                new WpsProcessSettings());
        ProcessSetting process = processSettings.getOrCreateProcessSetting(processConfiguration.getProcessIdentifier());

        process.getLastUsedValues().putAll(processConfiguration.getInputs());

        // Save layer=>column mappings
        DataInputs dataInputs = processConfiguration.getProcessDescription().getDataInputs();
        if (dataInputs != null && dataInputs.getInput() != null)
        {
            String layerValue = processConfiguration.getInputs().get("TYPENAME");
            List<InputDescriptionType> columnFields = dataInputs.getInput().stream().filter(WpsUtilities::isColumnField)
                    .collect(Collectors.toList());
            for (InputDescriptionType columnField : columnFields)
            {
                String columnValue = processConfiguration.getInputs().get(columnField.getIdentifier().getValue());
                process.getLastUsedColumns().put(layerValue, columnValue);
            }
        }

        myPreferences.putJAXBObject("processSettings", processSettings, false, this);
    }
}
