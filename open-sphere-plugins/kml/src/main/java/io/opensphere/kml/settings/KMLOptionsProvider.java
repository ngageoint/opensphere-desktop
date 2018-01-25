package io.opensphere.kml.settings;

import javax.swing.JPanel;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.kml.common.util.KMLToolbox;

/** KML options provider. */
public class KMLOptionsProvider extends AbstractOptionsProvider
{
    /** The KML toolbox. */
    private final KMLToolbox myKmlToolbox;

    /** The panel. */
    private JPanel myPanel;

    /** The settings view model. */
    private KMLSettingsViewModel myViewModel;

    /**
     * Constructor.
     *
     * @param kmlToolbox the KML toolbox
     */
    public KMLOptionsProvider(KMLToolbox kmlToolbox)
    {
        super("KML");
        myKmlToolbox = kmlToolbox;
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public void applyChanges()
    {
        myViewModel.populateDomainModel(myKmlToolbox.getSettings());
        myKmlToolbox.saveSettings();
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myViewModel = new KMLSettingsViewModel();
            myViewModel.populateFromDomainModel(myKmlToolbox.getSettings());
            if (!usesApply())
            {
                myViewModel.getChanged().addObserver((o, arg) -> applyChanges());
            }
            myPanel = new OptionsPanel(new KMLSettingsPanel(myViewModel));
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
        myKmlToolbox.getSettings().reset();
        myViewModel.populateFromDomainModel(myKmlToolbox.getSettings());
        myKmlToolbox.saveSettings();
    }
}
