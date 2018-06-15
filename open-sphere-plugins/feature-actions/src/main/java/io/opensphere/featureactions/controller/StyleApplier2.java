package io.opensphere.featureactions.controller;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.style.FeatureActionVisualizationStyle;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.transformer.MapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleMapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleTransformerGeometryProcessor;
import io.opensphere.mantle.util.MantleToolboxUtils;

public class StyleApplier2 implements ActionApplier
{
    private final Toolbox myToolbox;

    private final MantleToolbox myMantleToolbox;

    public StyleApplier2(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
    }

    @Override
    public void applyActions(Collection<? extends Action> actions, List<? extends MapDataElement> elements, DataTypeInfo dataType)
    {
        // TODO Auto-generated method stub
        VisualizationStyle style = null;
        myMantleToolbox.getVisualizationStyleController().updateStyle(style, null, dataType.getParent(), dataType, this);
    }

    @Override
    public void clearActions(Collection<Long> elementIds, DataTypeInfo dataType)
    {
        VisualizationStyleRegistry registry = myMantleToolbox.getVisualizationStyleRegistry();
        for (VisualizationStyle style : registry.getStyles(dataType.getTypeKey()))
        {
            if (style instanceof FeatureActionVisualizationStyle)
            {
                registry.setStyle(style.getConvertedClassType(), dataType.getTypeKey(),
                        ((FeatureActionVisualizationStyle)style).getPrevious(), this);
            }
        }
    }

    @Override
    public void removeElements(Collection<Long> elementIds, DataTypeInfo dataType)
    {
        // TODO Auto-generated method stub

    }

    /**
     * Gets the geometry processor for the data type.
     *
     * @param dataType the data type
     * @return the geometry processor, or null
     */
    private StyleTransformerGeometryProcessor getGeometryProcessor(DataTypeInfo dataType)
    {
        StyleTransformerGeometryProcessor processor = null;
        MapDataElementTransformer transformer = myMantleToolbox.getDataTypeController()
                .getTransformerForType(dataType.getTypeKey());
        if (transformer instanceof StyleMapDataElementTransformer)
        {
            processor = ((StyleMapDataElementTransformer)transformer).getGeometryProcessor();
        }
        return processor;
    }
}
