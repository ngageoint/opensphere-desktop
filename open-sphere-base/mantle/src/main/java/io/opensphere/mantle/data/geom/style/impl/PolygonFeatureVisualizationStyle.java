package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.math.Line2d;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class PolygonFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public class PolygonFeatureVisualizationStyle extends AbstractPathVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    @SuppressWarnings("hiding")
    public static final String ourPropertyKeyPrefix = "PolygonFeatureVisualizationStyle";

    /** The Constant ourFilledPropertyKey. */
    public static final String ourFilledPropertyKey = ourPropertyKeyPrefix + ".Filled";

    /** The Constant ourFillOpacityPropertyKey. */
    public static final String ourFillOpacityPropertyKey = ourPropertyKeyPrefix + ".FillOpacity";

    /** The Constant ourFilledParameter. */
    public static final VisualizationStyleParameter ourFilledParameter = new VisualizationStyleParameter(ourFilledPropertyKey,
            "Fill", Boolean.FALSE, Boolean.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(false, false));

    /** The Constant ourFillOpacityParameter. */
    public static final VisualizationStyleParameter ourFillOpacityParameter = new VisualizationStyleParameter(
            ourFillOpacityPropertyKey, "Fill Opacity", Float.valueOf(0.3f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /**
     * Instantiates a new polygon feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public PolygonFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new polygon feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public PolygonFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public PolygonFeatureVisualizationStyle clone()
    {
        return (PolygonFeatureVisualizationStyle)super.clone();
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        PolygonGeometry polygonGeom = null;
        if (bd.getMGS() instanceof MapPolygonGeometrySupport)
        {
            MapPolygonGeometrySupport mpgs = (MapPolygonGeometrySupport)bd.getMGS();
            MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
            BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();
            PolygonGeometry.Builder<GeographicPosition> polygonBuilder = new PolygonGeometry.Builder<>();
            PolygonRenderProperties props = getRenderProperties(mapVisInfo, basicVisInfo, bd.getVS(), renderPropertyPool);
            polygonBuilder.setDataModelId(bd.getGeomId());
            polygonBuilder.setLineType(mpgs.getLineType() == null ? LineType.STRAIGHT_LINE : mpgs.getLineType());

            // Add a time constraint if in time line mode.
            Constraints constraints = StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo, mapVisInfo, bd.getMGS(),
                    StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType()));

            // Convert list of LatLonAlt to list of GeographicPositions
            List<GeographicPosition> geoPos = mpgs.getLocations().stream()
                    .map(lla -> createGeographicPosition(lla, mapVisInfo, bd.getVS(), mpgs)).collect(Collectors.toList());

            if (isShowNodes())
            {
                createLocationNodes(setToAddTo, bd, renderPropertyPool);
            }
            polygonBuilder.setVertices(geoPos);

            for (List<? extends LatLonAlt> hole : mpgs.getHoles())
            {
                polygonBuilder.addHole(hole.stream().map(lla -> createGeographicPosition(lla, mapVisInfo, bd.getVS(), mpgs))
                        .collect(Collectors.toList()));
            }

            polygonGeom = new PolygonGeometry(polygonBuilder, props, constraints);

            // TODO this creates the label even when it is not displayed.
            Projection projection = getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot();
            GeographicPosition labelLoc = generateLabelLocation(geoPos, bd.getMGS().getBoundingBox(projection).getCenter());
            createLabelGeometry(setToAddTo, bd, labelLoc, polygonGeom.getConstraints(), renderPropertyPool);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot create geometries from type " + (bd.getMGS() == null ? "NULL" : bd.getMGS().getClass().getName()));
        }
        setToAddTo.add(polygonGeom);
    }

    @Override
    public PolygonFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        PolygonFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS,
                mdp, orig);

        // TODO: All the rest of the render property changes.

        // Here check selection and make sure line width property is sufficient.
        // Or in AbstractPath etc.

        return alteredRP;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapPolygonGeometrySupport.class;
    }

    /**
     * Gets the opacity.
     *
     * @return the opacity
     */
    public float getFillOpacity()
    {
        Float v = (Float)getStyleParameterValue(ourFillOpacityPropertyKey);
        return v == null ? 0f : v.floatValue() > 1.0f ? 1.0f : v.floatValue() < 0.0f ? 0.0f : v.floatValue();
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.PATH_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for polygons.";
    }

    @Override
    public String getStyleName()
    {
        return "Polygons";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourFilledPropertyKey);
        paramList
                .add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourFilledPropertyKey, true));

        param = style.getStyleParameter(ourFillOpacityPropertyKey);
        AbstractStyleParameterEditorPanel fillOpacityPanel = new FloatSliderStyleParameterEditorPanel(
                PanelBuilder.get(param.getName()), style, ourFillOpacityPropertyKey, true, false, 0f, 1f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null));
        paramList.add(fillOpacityPanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, fillOpacityPanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourFilledPropertyKey, true, Boolean.TRUE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Polygon Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourFilledParameter);
        setParameter(ourFillOpacityParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        paramSet.stream().filter(p -> p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
                .forEach(this::setParameter);
    }

    /**
     * Checks to see if the polygon is filled.
     *
     * @return true, if is filled
     */
    public boolean isFilled()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourFilledPropertyKey);
        return val != null && val.booleanValue();
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new PolygonFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets filled.
     *
     * @param filled the fill
     * @param source the source
     */
    public void setFilled(boolean filled, Object source)
    {
        setParameter(ourFilledPropertyKey, Boolean.valueOf(filled), source);
    }

    /**
     * Sets the opacity.
     *
     * @param opacity the opacity ( 0.0 to 1.0 )
     * @param source the source making the change
     */
    public void setFillOpacity(float opacity, Object source)
    {
        float adj = opacity < 0.0f ? 0.0f : opacity > 1.0f ? 1.0f : opacity;
        setParameter(ourFillOpacityPropertyKey, Float.valueOf(adj), source);
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    /**
     * Find the points on the line which intersect the polygon.
     *
     * @param line The line which may intersect the polygon.
     * @param polygon The polygon for which intersections are desired.
     * @return The points on the line which intersect the polygon.
     */
    private List<Vector2d> findIntersctions(Line2d line, List<Vector2d> polygon)
    {
        // Use a hash set in order to garauntee a unique set of intersection
        // points. It is possible to get duplicate intersection points when the
        // intersection happens on the endpoints.
        Set<Vector2d> intersections = new HashSet<>();
        Iterator<Vector2d> iter = polygon.iterator();
        Vector2d first = iter.next();
        Vector2d previous = first;
        boolean done = false;
        do
        {
            Vector2d next;
            if (iter.hasNext())
            {
                next = iter.next();
            }
            else
            {
                next = first;
                done = true;
            }

            if (!next.equals(previous))
            {
                intersections.addAll(line.getSegmentIntersection(previous, next));
            }
            previous = next;
        }
        while (!done);

        return New.list(intersections);
    }

    /**
     * Generate a location for a text label inside of the polygon. This method
     * attempts to find as large an open area as possible with doing an
     * exhaustive search. The returned position is guaranteed to be contained in
     * the polygon.
     *
     * @param polygon The polygon in which a label will be placed.
     * @param polyCenter The center of the polygon's bounding box.
     * @return The location where the label should be centered.
     */
    private GeographicPosition generateLabelLocation(List<GeographicPosition> polygon, GeographicPosition polyCenter)
    {
        Vector2d center = polyCenter.getLatLonAlt().asVec2d();
        List<Vector2d> convertedPolygon = polygon.stream().map(p -> p.getLatLonAlt().asVec2d()).collect(Collectors.toList());

        // The first object of this pair will be line segments on the interior
        // of the polygon which are co-linear with the polyCenter. The second
        // object of this pair will be the line segment interior to the polygon
        // which is perpendicular to the first segment and crosses the center of
        // the first segment.
        List<Pair<LineSegment2d, LineSegment2d>> lineSegments = New.list();
        final int numLines = 16;
        double angleStep = Math.PI / numLines;
        for (int i = 0; i < numLines; i++)
        {
            double theta = i * angleStep;
            double sin = Math.sin(theta);
            double cos = Math.cos(theta);
            Vector2d normal = new Vector2d(cos, -sin);
            Line2d line = new Line2d(center, normal);
            Line2d cross = new Line2d(center, normal.getPerpendicular());
            List<Vector2d> intersections = findIntersctions(line, convertedPolygon);
            if (!intersections.isEmpty() && intersections.size() % 2 == 0)
            {
                Collections.sort(intersections, new Line2d.DistanceComparator(cross));
                for (int j = 0; j < intersections.size(); j += 2)
                {
                    Vector2d ptA = intersections.get(j);
                    Vector2d ptB = intersections.get(j + 1);
                    // In cases where the line is tangential to the polygon the
                    // points may be so close to each other that they are
                    // unusable.
                    if (!ptA.equals(ptB))
                    {
                        LineSegment2d segment = new LineSegment2d(ptA, ptB);
                        // The segment is inside the polygon, so the crossing
                        // segment must exist.
                        LineSegment2d crossSegment = getCrossingSegment(segment, convertedPolygon);
                        if (crossSegment != null)
                        {
                            lineSegments.add(new Pair<>(segment, crossSegment));
                        }
                    }
                }
            }
        }

        if (lineSegments.isEmpty())
        {
            return new GeographicPosition(polyCenter.getLatLonAlt());
        }

        // Find the pair of segments whose summed length is longest.
        Pair<LineSegment2d, LineSegment2d> longestPair = null;
        double longestSum = 0;
        for (Pair<LineSegment2d, LineSegment2d> segment : lineSegments)
        {
            double segmentSum = Math.min(segment.getFirstObject().getLength(), segment.getSecondObject().getLength());
            if (longestPair == null || segmentSum > longestSum)
            {
                longestPair = segment;
                longestSum = segmentSum;
            }
        }

        @SuppressWarnings("null")
        Vector2d center1 = longestPair.getFirstObject().getCenter();
        Vector2d center2 = longestPair.getSecondObject().getCenter();
        // Since the second segment is co-linear with the first segment's
        // center interpolating between the two centers will give a position on
        // the second segment and will therefore be contained within the
        // polygon.
        Vector2d labelPos = center1.interpolate(center2, 0.5);
        LatLonAlt lla = LatLonAlt.createFromDegrees(labelPos.getY(), labelPos.getX(), ReferenceLevel.TERRAIN);
        return new GeographicPosition(lla);
    }

    /**
     * Get the line segment, within the polygon, perpendicular to the given
     * segment which passes through the given segment's center.
     *
     * @param segment The segment for which the crossing segment is desired.
     * @param polygon The polygon in which the segments fall.
     * @return The crossing perpendicular line segment.
     */
    private LineSegment2d getCrossingSegment(LineSegment2d segment, List<Vector2d> polygon)
    {
        Vector2d center = segment.getCenter();
        Line2d line = new Line2d(center, segment.getNormal().getPerpendicular());
        Line2d cross = new Line2d(center, segment.getNormal());
        List<Vector2d> intersections = findIntersctions(line, polygon);

        if (!intersections.isEmpty() && intersections.size() % 2 == 0)
        {
            if (intersections.size() == 2)
            {
                return new LineSegment2d(intersections.get(0), intersections.get(1));
            }
            Collections.sort(intersections, new Line2d.DistanceComparator(cross));
            for (int j = 0; j < intersections.size(); j += 2)
            {
                LineSegment2d crossSegment = new LineSegment2d(intersections.get(j), intersections.get(j + 1));
                if (segment.intersects(crossSegment))
                {
                    return crossSegment;
                }
            }
        }

        return null;
    }

    /**
     * Get the render properties for a polygon.
     *
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param visState The visualization state.
     * @param renderPropertyPool The render property pool.
     * @return The polygon render properties.
     */
    private PolygonRenderProperties getRenderProperties(MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        Color color = visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? getColor() : visState.getColor();
        PolygonRenderProperties props;
        if (isFilled())
        {
            float[] colorComp = color.getColorComponents(null);
            Color fillColor = new Color(colorComp[0], colorComp[1], colorComp[2], getFillOpacity());
            ColorRenderProperties fillColorProps = new DefaultColorRenderProperties(zOrder, true, pickable, false);
            fillColorProps.setColor(fillColor);
            props = new DefaultPolygonRenderProperties(zOrder, true, pickable, fillColorProps);
        }
        else
        {
            props = new DefaultPolygonRenderProperties(zOrder, true, pickable);
        }
        props.setColor(color);
        props.setWidth(visState.isSelected() ? getLineWidth() + MantleConstants.SELECT_WIDTH_ADDITION : getLineWidth());
        props = renderPropertyPool.getPoolInstance(props);
        return props;
    }
}
