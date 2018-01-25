package io.opensphere.mantle.data.columns.gui;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.core.util.fx.FXUtilities;

/** Column mapping OptionsProvider for global settings. */
public class ColumnMappingOptionsProvider extends AbstractOptionsProvider
{
    /** The topic. */
    public static final String TOPIC = Constants.COLUMN_MAPPING.pluralTitleCase();

    /** The column mapping resources. */
    private final ColumnMappingResources myResources;

    /** The panel. */
    private JPanel myPanel;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param resources the resources
     */
    public ColumnMappingOptionsProvider(Toolbox toolbox, ColumnMappingResources resources)
    {
        super(TOPIC);
        myToolbox = toolbox;
        myResources = resources;
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            final JFXPanel fxPanel = new JFXPanel();
            PlatformImpl.runAndWait(() -> fxPanel.setScene(createScene()));
            myPanel = new OptionsPanel(fxPanel, true);
        }
        return myPanel;
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public void applyChanges()
    {
        // Apply is not used in this provider
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }

    @Override
    public void restoreDefaults()
    {
        // Restore is not used in this provider
    }

    /**
     * Creates the scene for the FX panel.
     *
     * @return the scene
     */
    private Scene createScene()
    {
        return FXUtilities.addDesktopStyle(new Scene(new ColumnMappingPane(myToolbox, myResources), 300, 200));
    }
}
