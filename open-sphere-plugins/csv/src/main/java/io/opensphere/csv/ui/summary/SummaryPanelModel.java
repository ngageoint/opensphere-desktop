package io.opensphere.csv.ui.summary;

import java.util.Set;

import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.input.model.ColorModel;
import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.importer.config.LayerSettings;
import io.opensphere.mantle.data.util.LayerUtils;

/**
 * The UI model for the SummaryPanel.
 */
class SummaryPanelModel extends WrappedModel<LayerSettings>
{
    /** The layer name. */
    private final NameModel myLayerName = new NameModel(new LayerNamePredicate());

    /** The layer color. */
    private final ColorModel myLayerColor = new ColorModel();

    /**
     * Constructor.
     *
     * @param layerSettings The layer settings.
     * @param namesInUse the names in use
     */
    public SummaryPanelModel(LayerSettings layerSettings, Set<String> namesInUse)
    {
        myLayerName.setDisallowedNames(namesInUse);
        myLayerName.setNameAndDescription("Layer Name", "The name of the layer");
        myLayerColor.setNameAndDescription("Layer Color", "The color of the layer");

        addModel(myLayerName);
        addModel(myLayerColor);

        set(layerSettings);

        // Auto apply changes
        addListener((observable, oldValue, newValue) -> applyChanges());
    }

    /**
     * Gets the layerName.
     *
     * @return the layerName
     */
    public NameModel getLayerName()
    {
        return myLayerName;
    }

    /**
     * Gets the layerColor.
     *
     * @return the layerColor
     */
    public ColorModel getLayerColor()
    {
        return myLayerColor;
    }

    @Override
    protected void updateDomainModel(LayerSettings domainModel)
    {
        domainModel.setName(myLayerName.get());
        domainModel.setColor(myLayerColor.get());
    }

    @Override
    protected void updateViewModel(LayerSettings domainModel)
    {
        myLayerName.set(domainModel.getName());
        myLayerColor.set(domainModel.getColor());
    }

    /**
     * Additional predicate to verify disallowed layer name characters.
     */
    private static class LayerNamePredicate implements PredicateWithMessage<String>
    {
        @Override
        public boolean test(String layerName)
        {
            for (char ch : LayerUtils.getDisallowedLayerNameChars())
            {
                if (layerName.indexOf(ch) != -1)
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getMessage()
        {
            StringBuilder characters = new StringBuilder();
            for (char ch : LayerUtils.getDisallowedLayerNameChars())
            {
                characters.append(ch).append(' ');
            }
            return StringUtilities.concat("Layer name may not contain any of these characters:  ", characters.toString());
        }
    }
}
