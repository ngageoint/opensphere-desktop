package io.opensphere.wfs.state.save;

import java.util.Set;

import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * The Interface StyleParameterSaver.
 */
public interface StyleParameterSaver
{
    /**
     * Use my existing save state parameters to populate the visualization
     * style.
     *
     * @param visStyle The style which should match my saved state.
     * @return the style parameters populated with the values set from the
     *         existing save state.
     */
    Set<VisualizationStyleParameter> populateVisualizationStyle(VisualizationStyle visStyle);

    /**
     * Save style params.
     *
     * @param visStyle the vis style
     */
    void saveStyleParams(VisualizationStyle visStyle);
}
