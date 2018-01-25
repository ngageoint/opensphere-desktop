package io.opensphere.core.appl.versions.view;

import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.appl.versions.AutoUpdateToolboxUtils;
import io.opensphere.core.appl.versions.controller.AutoUpdateController;
import io.opensphere.core.appl.versions.model.AutoUpdatePreferences;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;

/**
 * Options provider for configuring automatic update settings.
 */
public class AutoUpdateOptionsProvider extends AbstractOptionsProvider
{
    /** The topic. */
    public static final String TOPIC = "Application Updates";

    /** The preferences where data is stored. */
    private final AutoUpdatePreferences myPreferences;

    /** The input panel where configuration changes are made. */
    private JPanel myPanel;

    /** The toolbox through which application state is accessed. */
    private Toolbox myToolbox;

    /** The controller used to manage user interactions. */
    private AutoUpdateController myController;

    /**
     * Creates a new options provider, bound to the supplied controller.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param controller the controller used to maintain version state.
     */
    public AutoUpdateOptionsProvider(Toolbox toolbox, AutoUpdateController controller)
    {
        super(TOPIC);
        myToolbox = toolbox;
        myController = controller;
        myPreferences = AutoUpdateToolboxUtils.getAutoUpdateToolboxToolbox(toolbox).getPreferences();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#getOptionsPanel()
     */
    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new OptionsPanel(new AutoUpdateOptionsPanel(myToolbox, myController));
        }
        return myPanel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractOptionsProvider#usesApply()
     */
    @Override
    public boolean usesApply()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#applyChanges()
     */
    @Override
    public void applyChanges()
    {
        // Apply is not used in this provider
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#restoreDefaults()
     */
    @Override
    public void restoreDefaults()
    {
        myPreferences.restoreDefaults();
    }
}
