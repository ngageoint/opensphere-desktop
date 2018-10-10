package io.opensphere.mantle.data.geom.style.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class PolylineFeatureVisualizationStyle.
 */
public class PolylineFeatureVisualizationStyle extends AbstractPathVisualizationStyle
{
    /**
     * Instantiates a new polyline feature visualization style.
     *
     * @param tb the tb
     */
    public PolylineFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new polyline feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public PolylineFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public PolylineFeatureVisualizationStyle clone()
    {
        return (PolylineFeatureVisualizationStyle)super.clone();
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool) throws IllegalArgumentException
    {
        PolylineGeometry polylineGeom = null;
        if (bd.getMGS() instanceof MapPolylineGeometrySupport)
        {
            MapPolylineGeometrySupport mpgs = (MapPolylineGeometrySupport)bd.getMGS();
            MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
            BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();
            PolylineRenderProperties props = createPolylineRenderProperties(mapVisInfo, basicVisInfo, bd, renderPropertyPool);
            PolylineGeometry.Builder<GeographicPosition> polylineBuilder = createPolylineBuilder(bd, mapVisInfo, basicVisInfo);
            polylineBuilder.setLineType(mpgs.getLineType() == null ? LineType.STRAIGHT_LINE : mpgs.getLineType());

            // Convert list of LatLonAlt to list of GeographicPositions
            List<GeographicPosition> geoPos = new ArrayList<>();
            for (LatLonAlt lla : mpgs.getLocations())
            {
                geoPos.add(createGeographicPosition(lla, mapVisInfo, bd.getVS(), mpgs));
            }
            polylineBuilder.setVertices(geoPos);

            if (isShowNodes())
            {
                createLocationNodes(setToAddTo, bd, renderPropertyPool);
            }

            // Add a time constraint if in time line mode.
            Constraints constraints = StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo, mapVisInfo, bd.getMGS(),
                    StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType()));

            polylineGeom = new PolylineGeometry(polylineBuilder, props, constraints);

            if (!geoPos.isEmpty())
            {
                int index = geoPos.size() == 1 ? 0 : (int)Math.floor(geoPos.size() / 2.0);
                createLabelGeometry(setToAddTo, bd, geoPos.get(index), polylineGeom.getConstraints(), renderPropertyPool);
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot create geometries from type " + (bd.getMGS() == null ? "NULL" : bd.getMGS().getClass().getName()));
        }
        setToAddTo.add(polylineGeom);
    }

    @Override
    public PolylineFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        PolylineFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapPolylineGeometrySupport.class;
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.PATH_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for polylines.";
    }

    @Override
    public String getStyleName()
    {
        return "Polylines";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new PolylineFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    /**
     * Creates the polyline builder.
     *
     * @param bd the bd
     * @param mapVisInfo the map vis info
     * @param basicVisInfo the basic vis info
     * @return the polyline geometry. builder
     */
    private PolylineGeometry.Builder<GeographicPosition> createPolylineBuilder(FeatureIndividualGeometryBuilderData bd,
            MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo)
    {
        PolylineGeometry.Builder<GeographicPosition> polylineBuilder = new PolylineGeometry.Builder<>();
        polylineBuilder.setDataModelId(bd.getGeomId());
        return polylineBuilder;
    }

    /**
     * Creates the polyline render properties.
     *
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param bd the bd
     * @param renderPropertyPool the render property pool
     * @return the polyline render properties
     */
    private PolylineRenderProperties createPolylineRenderProperties(MapVisualizationInfo mapVisInfo,
            BasicVisualizationInfo basicVisInfo, FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool)
    {
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();

        PolylineRenderProperties props = new DefaultPolylineRenderProperties(zOrder, true, pickable);
        props.setColor(bd.getVS().isSelected() ? MantleConstants.SELECT_COLOR
                : bd.getVS().isDefaultColor() ? getColor() : bd.getVS().getColor());
        props.setWidth(bd.getVS().isSelected() ? getLineWidth() + MantleConstants.SELECT_WIDTH_ADDITION : getLineWidth());
        props = renderPropertyPool.getPoolInstance(props);
        return props;
    }
}
