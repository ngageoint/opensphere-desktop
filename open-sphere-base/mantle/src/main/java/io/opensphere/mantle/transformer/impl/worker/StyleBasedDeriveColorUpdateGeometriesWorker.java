package io.opensphere.mantle.transformer.impl.worker;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;

/**
 * The Class StyleBasedDeriveUpdateGeometriesWorker.
 */
public class StyleBasedDeriveColorUpdateGeometriesWorker extends StyleBasedDeriveUpdateGeometriesWorker
{
    /**
     * Generate color only parameter map.
     *
     * @param c the color
     * @return the map
     */
    private static Map<String, VisualizationStyleParameter> generateColorOnlyParameterMap(Color c)
    {
        Map<String, VisualizationStyleParameter> result = null;

        if (c != null)
        {
            VisualizationStyleParameter param = AbstractFeatureVisualizationStyle.DEFAULT_COLOR_PROPERTY.deriveWithNewValue(c);
            if (param != null)
            {
                result = New.map();
                result.put(param.getKey(), param);
            }
        }
        return result == null ? Collections.<String, VisualizationStyleParameter>emptyMap() : result;
    }

    /**
     * Instantiates a new style based derive color update geometries worker.
     *
     * @param provider the provider
     * @param idSet the id set
     * @param c the default color
     * @param requiresMetaData the requires meta data
     */
    public StyleBasedDeriveColorUpdateGeometriesWorker(StyleDataElementTransformerWorkerDataProvider provider, List<Long> idSet,
            Color c, boolean requiresMetaData)
    {
        super(provider, idSet, generateColorOnlyParameterMap(c), requiresMetaData);
    }
}
