package io.opensphere.featureactions.style;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.model.Action;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractChainedFeatureVisualizationStyle;

/** */
public class FeatureActionVisualizationStyle extends AbstractChainedFeatureVisualizationStyle
{
    /** List of feature actions. */
    private final Map<Action, List<? extends MapDataElement>> myFeatureActions = New.map();

    /**
     *
     * @param tb
     */
    public FeatureActionVisualizationStyle(Toolbox tb)
    {
        super(tb);
        // TODO Auto-generated constructor stub
    }

    /**
     *
     * @param tb
     * @param dtiKey
     */
    public FeatureActionVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
        // TODO Auto-generated constructor stub
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends VisualizationSupport> getConvertedClassType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStyleDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStyleName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VisualizationStyleParameter getStyleParameter(String paramKey)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AbstractRenderableGeometry modifyGeometry(Set<Geometry> singletonGeo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *
     * @param featureAction
     * @param elements
     */
    public void setFeatureActions(Action featureAction, List<MapDataElement> elements)
    {
        myFeatureActions.put(featureAction, elements);
    }
}
