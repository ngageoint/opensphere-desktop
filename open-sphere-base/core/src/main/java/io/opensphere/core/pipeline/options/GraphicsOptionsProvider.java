package io.opensphere.core.pipeline.options;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.quantify.QuantifyToolboxUtils;

/**
 * Options provider for the graphics pipeline.
 */
public class GraphicsOptionsProvider extends AbstractOptionsProvider
{
    /** Tooltip for the display lists checkbox when it's disabled. */
    private static final String DISPLAY_LISTS_DISABLED_TOOLTIP =
            "Fast text rendering cannot be enabled when safe mode is active.";

    /** Tooltip for the display lists checkbox. */
    private static final String DISPLAY_LISTS_TOOLTIP =
            "Disabling fast text rendering will reduce graphics performance, but may improve stability.";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GraphicsOptionsProvider.class);

    /** The preferences key for using display lists. */
    private final String myDisplayListsPrefsKey;

    /** The pipeline preferences. */
    private final Preferences myPreferences;

    /** The preferences key for using safe mode. */
    private final String mySafeModePrefsKey;

    private final Toolbox myToolbox;

    /**
     * Default constructor.
     *
     * @param pipelinePrefs The preferences registry.
     * @param displayListsPrefsKey The preferences key for using display lists.
     * @param safeModePrefsKey The preferences key for using safe mode.
     */
    public GraphicsOptionsProvider(Toolbox toolbox, Preferences pipelinePrefs, String displayListsPrefsKey,
            String safeModePrefsKey)
    {
        super("Graphics Performance");
        myToolbox = toolbox;
        myPreferences = pipelinePrefs;
        myDisplayListsPrefsKey = displayListsPrefsKey;
        mySafeModePrefsKey = safeModePrefsKey;
    }

    @Override
    public void applyChanges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public JPanel getOptionsPanel()
    {
        final JCheckBox safeModeCb =
                new JCheckBox("Use safe mode for next launch", myPreferences.getBoolean(mySafeModePrefsKey, false));
        safeModeCb.setToolTipText("Using safe mode will result in reduced graphics performance, but may improve stability.");

        final JCheckBox displayListsCb = new JCheckBox("Fast text rendering for next launch (NVIDIA recommended)",
                myPreferences.getBoolean(myDisplayListsPrefsKey, false));

        if (safeModeCb.isSelected())
        {
            displayListsCb.setSelected(false);
            displayListsCb.setEnabled(false);
            displayListsCb.setToolTipText(DISPLAY_LISTS_DISABLED_TOOLTIP);
        }
        else
        {
            displayListsCb.setToolTipText(DISPLAY_LISTS_TOOLTIP);
        }

        safeModeCb.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.graphics-performance.use-safe-mode-checkbox");
            boolean safeMode = safeModeCb.isSelected();
            myPreferences.putBoolean(mySafeModePrefsKey, safeMode, GraphicsOptionsProvider.this);
            LOGGER.info("Safe mode is " + (safeMode ? "enabled" : "disabled") + " for next launch.");
            if (safeMode)
            {
                displayListsCb.setSelected(false);
                displayListsCb.setEnabled(false);
                displayListsCb.setToolTipText(DISPLAY_LISTS_DISABLED_TOOLTIP);
            }
            else
            {
                displayListsCb.setEnabled(true);
                displayListsCb.setSelected(myPreferences.getBoolean(myDisplayListsPrefsKey, true));
                displayListsCb.setToolTipText(DISPLAY_LISTS_TOOLTIP);
            }
        });

        displayListsCb.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.graphics-performance.fast-text-rendering-checkbox");
            boolean displayLists = displayListsCb.isSelected();
            myPreferences.putBoolean(myDisplayListsPrefsKey, displayLists, GraphicsOptionsProvider.this);
            LOGGER.info("Display lists are " + (displayLists ? "enabled" : "disabled") + " for next launch.");
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(safeModeCb);
        panel.add(displayListsCb);
        return panel;
    }

    @Override
    public void restoreDefaults()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }
}
