package io.opensphere.search.controller;

import java.awt.Color;
import java.util.List;

import javafx.collections.ListChangeListener;

import com.google.common.collect.BiMap;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.search.model.SearchModel;

/**
 * Draws the geometries for the results of a search.
 */
public class SearchTransformer implements ListChangeListener<SearchResult>
{
    /**
     * The published geometries mapped to their result.
     */
    private final BiMap<SearchResult, Geometry> myGeometries;

    /**
     * The published label geometries mapped to their result.
     */
    private final BiMap<SearchResult, Geometry> myLabelGeometries;

    /**
     * Used to publish the search results to the globe.
     */
    private final GeometryRegistry myGeometryRegistry;

    /**
     * The model used by the search.
     */
    private final SearchModel myModel;

    /**
     * Constructs a new search transformer.
     *
     * @param model The model used by the search.
     * @param geometryRegistry Used to publish the search results to the globe.
     * @param geometries The map to store the published geometries, mapped by
     *            their corresponding {@link SearchResult}.
     * @param labelGeometries the map to store the published label geometries,
     *            mapped by their corresponding {@link SearchResult}.
     */
    public SearchTransformer(SearchModel model, GeometryRegistry geometryRegistry, BiMap<SearchResult, Geometry> geometries,
            BiMap<SearchResult, Geometry> labelGeometries)
    {
        myGeometryRegistry = geometryRegistry;
        myModel = model;
        myGeometries = geometries;
        myLabelGeometries = labelGeometries;
        myModel.getShownResults().addListener(this);
    }

    /**
     * Stops listening for result changes.
     */
    public void close()
    {
        myModel.getShownResults().removeListener(this);
    }

    @Override
    public void onChanged(Change<? extends SearchResult> c)
    {
        while (c.next())
        {
            addGeometries(c.getAddedSubList());
            removeGeometries(c.getRemoved());
        }
    }

    /**
     * Adds the results to the map if they have a geometry component to them.
     *
     * @param adds The added search results.
     */
    private void addGeometries(List<? extends SearchResult> adds)
    {
        List<Geometry> geometries = New.list();
        for (SearchResult result : adds)
        {
            if (result.isCreateGeometry() && !myGeometries.containsKey(result))
            {
                Geometry geometry = null;
                if (result.getLocations().size() == 1)
                {
                    geometry = createPoint(result.getLocations().get(0));
                }
                else if (!result.getLocations().isEmpty())
                {
                    geometry = createPolyline(result.getLocations());
                }

                if (geometry != null)
                {
                    myGeometries.put(result, geometry);
                    geometries.add(geometry);

                    Geometry labelGeometry = createLabel((GeographicPosition)geometry.getReferencePoint(), result.getText());
                    myLabelGeometries.put(result, labelGeometry);
                    geometries.add(labelGeometry);
                }
            }
        }

        if (!geometries.isEmpty())
        {
            myGeometryRegistry.addGeometriesForSource(this, geometries);
        }
    }

    /**
     * Creates a new label geometry, tied to the supplied position, using the
     * supplied text.
     *
     * @param position the location at which the label will be created (it will
     *            be offset slightly)
     * @param label
     * @return a label geometry created using the supplied information.
     */
    private Geometry createLabel(GeographicPosition position, String label)
    {
        LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<>();

        builder.setPosition(position);
        builder.setText(label);
        builder.setHorizontalAlignment(-.11f);
        builder.setVerticalAlignment(0.35f);

        LabelGeometry geometry = new LabelGeometry(builder, createLabelProperties(), null);
        return geometry;
    }

    /**
     * Creates a point geometry.
     *
     * @param location The location of the point.
     * @return The created geometry.
     */
    private Geometry createPoint(LatLonAlt location)
    {
        PointGeometry.Builder<GeographicPosition> builder = new PointGeometry.Builder<>();
        builder.setPosition(new GeographicPosition(location));

        PointGeometry geometry = new PointGeometry(builder, createPointProperties(), null);

        return geometry;
    }

    /**
     * Creates a polyline geometry.
     *
     * @param locations The vertices of the polyline.
     * @return The created geometry.
     */
    private Geometry createPolyline(List<LatLonAlt> locations)
    {
        List<GeographicPosition> positions = New.list();
        for (LatLonAlt location : locations)
        {
            positions.add(new GeographicPosition(location));
        }
        PolylineGeometry.Builder<GeographicPosition> builder = new PolylineGeometry.Builder<>();
        builder.setVertices(positions);
        PolylineGeometry geometry = new PolylineGeometry(builder, createPolyineProperties(), null);

        return geometry;
    }

    /**
     * Creates the properties needed to render a label geometry.
     *
     * @return the properties needed to render a label geometry.
     */
    private LabelRenderProperties createLabelProperties()
    {
        LabelRenderProperties labelProperties = new DefaultLabelRenderProperties(0, true, true);
        labelProperties.setColor(Color.WHITE);
        labelProperties.setShadowColor(Color.BLACK);
        labelProperties.setShadowOffset(1.0F, -1.0F);
        labelProperties.setHighlightColor(Color.RED);

        return labelProperties;
    }

    /**
     * Creates new render properties for points.
     *
     * @return a new point render properties, initialized with default values.
     */
    private PointRenderProperties createPointProperties()
    {
        PointRenderProperties pointProperties = new DefaultPointRenderProperties(DefaultPointRenderProperties.TOP_Z, true, true,
                false);
        pointProperties.setSize(5);
        pointProperties.setHighlightSize(7);
        pointProperties.setColor(Color.CYAN);
        return pointProperties;
    }

    /**
     * Creates new render properties for polylines.
     *
     * @return a new polyline render properties, initialized with default
     *         values.
     */
    private PolylineRenderProperties createPolyineProperties()
    {
        PolylineRenderProperties polylineRenderProperties = new DefaultPolylineRenderProperties(
                DefaultPolylineRenderProperties.TOP_Z, true, true);
        polylineRenderProperties.setColor(Color.CYAN);
        polylineRenderProperties.setWidth(4);

        return polylineRenderProperties;
    }

    /**
     * Removes the geometries from the globe.
     *
     * @param removes The search results to remove from the globe.
     */
    private void removeGeometries(List<? extends SearchResult> removes)
    {
        List<Geometry> toRemove = New.list();
        for (SearchResult result : removes)
        {
            if (result.isCreateGeometry())
            {
                Geometry geometry = myGeometries.remove(result);
                if (geometry != null)
                {
                    toRemove.add(geometry);
                }
            }
            Geometry labelGeometry = myLabelGeometries.remove(result);
            if (labelGeometry != null)
            {
                toRemove.add(labelGeometry);
            }
        }

        if (!toRemove.isEmpty())
        {
            myGeometryRegistry.removeGeometriesForSource(this, toRemove);
        }
    }
}
