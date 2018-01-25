package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.processor.PolygonProcessor;
import io.opensphere.core.pipeline.processor.PolygonProcessor.PolygonModelData;
import io.opensphere.core.pipeline.processor.PolylineModelData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelDataRetriever;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Helper class that generates buffer objects for {@link PolygonGeometry}s.
 */
class MultiDrawBufferGenerator
{
    /**
     * Get the render key to buffer pair map.
     *
     * @param geometries The geometries.
     * @param highlight Flag indicating if the geometries should be highlighted.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param projectionSnapshot The projection snapshot.
     * @param groupTimeSpan The group time span.
     * @return The render key to buffer pair map.
     */
    public Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> getRenderKeyToBufferPairMap(
            Collection<? extends PolygonGeometry> geometries, boolean highlight, PickManager pickManager,
            ModelDataRetriever<PolygonGeometry> dataRetriever, Projection projectionSnapshot, TimeSpan groupTimeSpan)
    {
        Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> keyToBuffers = New
                .insertionOrderMap();
        Map<PolygonRenderKey, Pair<List<PolygonGeometry>, List<PolygonProcessor.PolygonModelData>>> sorted = getSorted(geometries,
                dataRetriever, projectionSnapshot);
        for (Entry<PolygonRenderKey, Pair<List<PolygonGeometry>, List<PolygonProcessor.PolygonModelData>>> entry : sorted
                .entrySet())
        {
            List<PolygonModelData> modelDataForKey = entry.getValue().getSecondObject();
            Collection<PolygonGeometry> meshGeoms = New.collection(modelDataForKey.size());
            Collection<PolygonMeshData> meshData = New.collection(modelDataForKey.size());
            Map<PolygonGeometry, Collection<? extends PolylineModelData>> lineGeomsToDataMap = New.map(modelDataForKey.size());

            List<PolygonGeometry> geomsForKey = entry.getValue().getFirstObject();
            Iterator<PolygonGeometry> geomIter = geomsForKey.iterator();
            for (PolygonModelData modelData : modelDataForKey)
            {
                PolygonGeometry geom = geomIter.next();
                if (modelData.getLineData() != null)
                {
                    lineGeomsToDataMap.put(geom, modelData.getLineData());
                }
                if (modelData.getMeshData() != null)
                {
                    meshGeoms.add(geom);
                    meshData.add(modelData.getMeshData());
                }
            }

            MultiDrawPolygonMeshDataBuffered meshBuffer = meshGeoms.isEmpty() ? null : new MultiDrawPolygonMeshDataBuffered(
                    meshGeoms, meshData, highlight, pickManager, groupTimeSpan, entry.getKey().getTesseraVertexCount());
            MultiDrawPolylineDataBuffered lineBuffer = lineGeomsToDataMap.isEmpty() ? null
                    : new MultiDrawPolylineDataBuffered(lineGeomsToDataMap, highlight, pickManager, groupTimeSpan);
            keyToBuffers.put(entry.getKey(), Pair.create(meshBuffer, lineBuffer));
        }
        return keyToBuffers;
    }

    /**
     * Sort the geometries by render parameters.
     *
     * @param input The geometries.
     * @param dataRetriever The data retriever.
     * @param projectionSnapshot The projection snapshot.
     * @return The sorted geometries.
     */
    private Map<PolygonRenderKey, Pair<List<PolygonGeometry>, List<PolygonProcessor.PolygonModelData>>> getSorted(
            Collection<? extends PolygonGeometry> input, ModelDataRetriever<PolygonGeometry> dataRetriever,
            Projection projectionSnapshot)
    {
        Map<PolygonRenderKey, Pair<List<PolygonGeometry>, List<PolygonProcessor.PolygonModelData>>> sorted = new TreeMap<>();

        for (PolygonGeometry geom : input)
        {
            PolygonRenderProperties props = geom.getRenderProperties();

            PolygonProcessor.PolygonModelData modelData = (PolygonProcessor.PolygonModelData)dataRetriever.getModelData(geom,
                    projectionSnapshot, null, TimeBudget.ZERO);

            if (modelData != null)
            {
                int tesseraVertexCount = modelData.getMeshData() == null ? 0 : modelData.getMeshData().getTesseraVertexCount();
                PolygonRenderKey key = new PolygonRenderKey(props.getRenderingOrder(), props.getWidth(), props.getLighting(),
                        props.getStipple(), props.isObscurant(), geom.isLineSmoothing(), tesseraVertexCount);

                Pair<List<PolygonGeometry>, List<PolygonModelData>> pair = sorted.get(key);
                List<PolygonProcessor.PolygonModelData> modelDataList;
                List<PolygonGeometry> geomList;
                if (pair == null)
                {
                    geomList = New.list();
                    modelDataList = New.list();
                    pair = Pair.create(geomList, modelDataList);
                    sorted.put(key, pair);
                }
                else
                {
                    geomList = pair.getFirstObject();
                    modelDataList = pair.getSecondObject();
                }
                geomList.add(geom);
                modelDataList.add(modelData);
            }
        }

        return sorted;
    }
}
