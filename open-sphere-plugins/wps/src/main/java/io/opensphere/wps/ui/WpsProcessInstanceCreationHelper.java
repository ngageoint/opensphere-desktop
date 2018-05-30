package io.opensphere.wps.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.fx.LabeledObservableButtonBar;
import io.opensphere.mantle.data.DataGroupEvent;
import io.opensphere.wps.config.v2.ProcessConfig;
import io.opensphere.wps.config.v2.ProcessSetting;
import io.opensphere.wps.config.v2.WpsProcessSettings;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.layer.WpsDataTypeInfoBuilder;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.ui.detail.WpsInputDialog;
import io.opensphere.wps.ui.detail.WpsProcessEditorFactory;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import net.opengis.wps._100.ProcessBriefType;

/**
 * A helper class used to generate new WPS Process forms.
 */
public class WpsProcessInstanceCreationHelper
{
    /**
     * A template for button styling in which common button styles are defined. The syntax of this template expects two arguments,
     * formatted according to the Java {@link String#format(String, Object...)} method specification.
     */
    private static final String BUTTON_STYLE_TEMPLATE = "-fx-font-size: 15px; -fx-text-fill: linear-gradient(to bottom right, %1$s, derive(%1$s, %2$s))";

    /**
     * The toolbox through which application interaction occurs.
     */
    private final Toolbox myToolbox;

    /** The plugin preferences. */
    private final Preferences myPreferences;

    /**
     * The editor factory used to create new forms.
     */
    private final WpsProcessEditorFactory myFactory;

    /**
     * Creates a new process instance creation helper.
     *
     * @param pToolbox The toolbox through which application interaction occurs.
     * @param pWpsServerId the unique identifier for which the helper is configured.
     * @param preferences The plugin preferences
     */
    public WpsProcessInstanceCreationHelper(Toolbox pToolbox, String pWpsServerId, Preferences preferences)
    {
        myToolbox = pToolbox;
        myPreferences = preferences;
        myFactory = new WpsProcessEditorFactory(myToolbox);
    }

    /**
     * Creates a new WPS Process Instantiation form, based on the supplied group event. The group event's source should contain
     * the swing component that triggered the event's firing as the source, as well as the data group.
     *
     * @param pEvent the event that was triggered when creation of the form was requested.
     * @param pRootWpsUrl the URL of the server to which the process belongs.
     * @param processType the process type.
     * @param pDataTypeBuilder the builder used to construct the datatype.
     * @param config The process configuration
     * @param pResultConsumer the consumer which will accept results from the new instance.
     */
    public void createNewWpsInstance(DataGroupEvent pEvent, String pRootWpsUrl, ProcessBriefType processType,
            WpsDataTypeInfoBuilder pDataTypeBuilder, ProcessConfig config,
            BiConsumer<WpsProcessConfiguration, WpsDataTypeInfo> pResultConsumer)
    {
        if (pEvent.getSource() instanceof Component)
        {
            Component source = (Component)pEvent.getSource();
            Window window = SwingUtilities.windowForComponent(source);

            String processId = processType.getIdentifier().getValue();

            WpsProcessSettings processSettings = myPreferences.getJAXBObject(WpsProcessSettings.class, "processSettings", null);
            ProcessSetting processSetting = processSettings != null ? processSettings.getProcessSetting(processId) : null;

            WpsEditorCreationService createEditorFormService = new WpsEditorCreationService(myToolbox, myFactory, pRootWpsUrl,
                    processId, config, processSetting);

            String title = "Configure new " + processType.getTitle().getValue() + " Instance";
            WpsInputDialog dialog = new WpsInputDialog(window, title, createEditorFormService, pDataTypeBuilder,
                    this::createButtonBar, pResultConsumer, myPreferences);

            dialog.setMinimumSize(new Dimension(700, 300));
            dialog.setSize(740, 550);
            dialog.setLocationRelativeTo(window);
            dialog.setVisible(true);
        }
    }

    /**
     * Creates the button bar for the popup dialog.
     *
     * @return the button bar for the popup dialog.
     */
    protected LabeledObservableButtonBar createButtonBar()
    {
        LabeledObservableButtonBar returnValue = new LabeledObservableButtonBar("AOC");

        Label runOnceIcon = FxIcons.createIconLabel(AwesomeIconSolid.PLAY, 16);
        runOnceIcon.setStyle(String.format(BUTTON_STYLE_TEMPLATE, "#00D800", "-55%"));

        Label saveAndRunIcon = FxIcons.createIconLabel(AwesomeIconSolid.SAVE, 16);
        saveAndRunIcon.setStyle(String.format(BUTTON_STYLE_TEMPLATE, "#E2E31A", "-25%"));

        Label cancelIcon = FxIcons.createIconLabel(AwesomeIconSolid.BAN, 16);
        cancelIcon.setStyle(String.format(BUTTON_STYLE_TEMPLATE, "#FF0000", "-55%"));

        returnValue.addButton("Run Once", runOnceIcon, ButtonData.APPLY)
                .addButton("Save and Run", saveAndRunIcon, ButtonData.OK_DONE)
                .addButton("Cancel", cancelIcon, ButtonData.CANCEL_CLOSE);

        return returnValue;
    }
}
