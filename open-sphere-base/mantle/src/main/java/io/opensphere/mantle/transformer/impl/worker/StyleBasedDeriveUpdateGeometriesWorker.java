package io.opensphere.mantle.transformer.impl.worker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometryGroup;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * The Class StyleBasedDeriveUpdateGeometriesWorker.
 */
public class StyleBasedDeriveUpdateGeometriesWorker extends AbstractDeriveUpdateGeometriesWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StyleBasedDeriveUpdateGeometriesWorker.class);

    /** The Changed parameter key to parameter map. */
    private final Map<String, VisualizationStyleParameter> myChangedParameterKeyToParameterMap;

    /** The Default vs state. */
    private final VisualizationState myDefaultVSState = new VisualizationState(true);

    /** The Requires meta data. */
    private boolean myRequiresMetaData;

    /** The Retriever. */
    private ElementTransfomerMappedDataRetriever myRetriever;

    /** The Style data element transformer worker data provider. */
    private final StyleDataElementTransformerWorkerDataProvider myStyleDataElementTransformerWorkerDataProvider;

    /**
     * Instantiates a new style based derive update geometries worker.
     *
     * @param provider the provider
     * @param idSet the id set
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param requiresMetaData the requires meta data
     */
    public StyleBasedDeriveUpdateGeometriesWorker(StyleDataElementTransformerWorkerDataProvider provider, List<Long> idSet,
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, boolean requiresMetaData)
    {
        super(provider, idSet);
        myStyleDataElementTransformerWorkerDataProvider = provider;
        myChangedParameterKeyToParameterMap = changedParameterKeyToParameterMap;
    }

    @Override
    public void cleanup()
    {
        myRetriever.clear();
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(Long id, BaseRenderProperties orig)
    {
        // NOT USED.
        return null;
    }

    @Override
    public Set<Geometry> processGeometrySet(RenderPropertyPool rpp, Collection<Geometry> geomSet)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Process Geometry Set: Size: " + geomSet.size());
        }
        Set<Geometry> newGeomSet = New.set(geomSet.size());
        ElementData ed = null;
        FeatureVisualizationStyle style = null;
        for (Geometry geom : geomSet)
        {
            if (geom instanceof AbstractRenderableGeometry)
            {
                AbstractRenderableGeometry aGeom = (AbstractRenderableGeometry)geom;
                long dmId = myStyleDataElementTransformerWorkerDataProvider.getDataModelIdFromGeometryId(geom.getDataModelId());
                long mgsTypeId = myStyleDataElementTransformerWorkerDataProvider
                        .getMGSTypeIdFromGeometryId(geom.getDataModelId());
                ed = myRetriever.getData(dmId);
                style = myStyleDataElementTransformerWorkerDataProvider.getStyle(mgsTypeId, dmId);
                AbstractRenderableGeometry derived = style.deriveGeometryFromRenderPropertyChange(
                        myChangedParameterKeyToParameterMap, rpp, aGeom, getProvider().getDataType(),
                        ed == null ? null : ed.getVisualizationState(), myDefaultVSState,
                        ed == null ? null : ed.getMetaDataProvider());
                newGeomSet.add(derived);
            }
            else if (geom instanceof AbstractGeometryGroup)
            {
                newGeomSet.addAll(processGeometrySet(rpp, ((AbstractGeometryGroup)geom).getGeometryRegistry().getGeometries()));
            }
        }
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Process Geometry Set: Result Size: " + newGeomSet.size());
        }
        return newGeomSet;
    }

    @Override
    public void retrieveNecessaryData()
    {
        myRetriever = new ElementTransfomerMappedDataRetriever(getProvider().getToolbox(), getProvider().getDataType(),
                getIdsOfInterest(), true, true, myRequiresMetaData, true);
        myRetriever.retrieveData();
    }

    /**
     * Gets the style data element transformer worker data provider.
     *
     * @return the style data element transformer worker data provider
     */
    protected StyleDataElementTransformerWorkerDataProvider getStyleDataElementTransformerWorkerDataProvider()
    {
        return myStyleDataElementTransformerWorkerDataProvider;
    }
}
