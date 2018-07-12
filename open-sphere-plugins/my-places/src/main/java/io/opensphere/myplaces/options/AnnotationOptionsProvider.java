package io.opensphere.myplaces.options;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.editor.view.AnnotationStyleEditorPanel;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.OptionsAccessor;

/**
 * Options provider for annotation points.
 */
public class AnnotationOptionsProvider extends AbstractOptionsProvider
{
    /** The topic for default settings. */
    public static final String DEFAULTS_TOPIC = "My Places Defaults";

    /**
     * Gets and saves point options.
     */
    private final OptionsAccessor myOptions;

    /** The panel. */
    private AnnotationStyleEditorPanel myPanel;

    /** The default user placemark. */
    private Placemark myPlacemark;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public AnnotationOptionsProvider(Toolbox toolbox)
    {
        super(DEFAULTS_TOPIC);
        myOptions = new OptionsAccessor(toolbox);
        myPlacemark = myOptions.getDefaultPlacemark();
        myToolbox = toolbox;
        ExtendedDataUtils.putBoolean(myPlacemark.getExtendedData(), Constants.IS_HEADING_DISTANCE_CAPABLE, true);
        ExtendedDataUtils.putBoolean(myPlacemark.getExtendedData(), Constants.IS_LOCATION_CAPABLE, true);
    }

    @Override
    public void applyChanges()
    {
        QuantifyToolboxUtils.collectMetric("mist3d.settings.my-places-defaults.apply-button");
        getOptionsPanel().getModel().applyChanges();

        int index = 0;
        List<Data> placemarkData = myPlacemark.getExtendedData().getData();
        for (Data aData : placemarkData)
        {
            if (aData.getName().equals(Constants.IS_HEADING_DISTANCE_CAPABLE))
            {
                break;
            }

            index++;
        }

        if (index < placemarkData.size())
        {
            placemarkData.remove(index);
        }

        myOptions.saveDefaultPlacemark(myPlacemark);
    }

    @Override
    public AnnotationStyleEditorPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new AnnotationStyleEditorPanel(myToolbox, false, false, myPlacemark.getGeometry() instanceof Polygon);
            myPanel.setPlacemark(myPlacemark);
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
        // Clear out the placemark
        myPlacemark = new Placemark();
        myOptions.saveDefaultPlacemark(myPlacemark);

        // Ask for a new default to fill in necessary values and save that one
        myPlacemark = myOptions.getDefaultPlacemark();
        myOptions.saveDefaultPlacemark(myPlacemark);

        getOptionsPanel().setPlacemark(myPlacemark);
    }
}
