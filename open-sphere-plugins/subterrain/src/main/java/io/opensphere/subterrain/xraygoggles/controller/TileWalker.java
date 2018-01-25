package io.opensphere.subterrain.xraygoggles.controller;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Collects all the leaf tile geometries that are contained within the xray
 * window.
 */
public class TileWalker
{
    /**
     * Contains all of the geometries being displayed on the globe.
     */
    private final GeometryRegistry myGeometryRegistry;

    /**
     * Used to convert the tile's coordinates into screen coordinates.
     */
    private final MapManager myMapManager;

    /**
     * Contains information on the xray window and where it is on the globe.
     */
    private final XrayGogglesModel myModel;

    /**
     * Constructs a new xray tile walker.
     *
     * @param mapManager Used to convert the tile's coordinates into screen
     *            coordinates.
     * @param geometryRegistry Contains all of the geometries being displayed on
     *            the globe.
     * @param model Contains information on the xray window and where it is on
     *            the globe.
     */
    public TileWalker(MapManager mapManager, GeometryRegistry geometryRegistry, XrayGogglesModel model)
    {
        myMapManager = mapManager;
        myGeometryRegistry = geometryRegistry;
        myModel = model;
    }

    /**
     * Collects all of the leaf tiles that are contained within the xray window.
     *
     * @return The tiles contained within the xray window or empty if none are
     *         contained or the window doesn't fully project on the globe.
     */
    public List<TileGeometry> collectTiles()
    {
        List<TileGeometry> theGeometries = New.list();
        XrayGogglesModel model = myModel.clone();
        boolean hasGeo = model.getLowerLeftGeo() != null && model.getLowerRightGeo() != null && model.getUpperLeftGeo() != null
                && model.getUpperRightGeo() != null && model.getCenterGeo() != null;
        boolean hasScreen = model.getLowerLeft() != null && model.getLowerRight() != null && model.getUpperLeft() != null
                && model.getUpperRight() != null;
        if (hasGeo && hasScreen)
        {
            Collection<Geometry> geometries = myGeometryRegistry.getGeometries();
            for (Geometry geometry : geometries)
            {
                if (geometry instanceof TileGeometry)
                {
                    TileGeometry tile = (TileGeometry)geometry;
                    if (tile.getBounds() instanceof GeographicBoundingBox
                            && overlaps((GeographicBoundingBox)tile.getBounds(), model))
                    {
                        collectTiles(tile, theGeometries, model);
                    }
                }
            }
        }

        return theGeometries;
    }

    /**
     * Checks to see if the tile is within the triangle parts of the trapezoid.
     *
     * @param tilesScreenPositions The tile's screen positions.
     * @param model Contains information about the xray window.
     * @return True if it is contained in the left or right triangle of the
     *         trapezoid.
     */
    private boolean checkIfInTriangles(List<Vector2i> tilesScreenPositions, XrayGogglesModel model)
    {
        boolean inTriangles = false;
        ScreenPosition leftTriangleV1 = model.getUpperLeft();
        ScreenPosition leftTriangleV2 = new ScreenPosition(model.getUpperLeft().getY(), model.getLowerLeft().getX());
        ScreenPosition leftTriangleV3 = model.getLowerLeft();

        ScreenPosition rightTriangleV1 = model.getUpperRight();
        ScreenPosition rightTriangleV2 = new ScreenPosition(model.getUpperRight().getY(), model.getLowerRight().getX());
        ScreenPosition rightTriangleV3 = model.getLowerRight();

        for (Vector2i pt : tilesScreenPositions)
        {
            if (pt.getX() < model.getLowerLeft().getX() && pt.getX() > model.getUpperLeft().getX()
                    && pt.getY() > model.getUpperLeft().getY() && pt.getY() < model.getLowerLeft().getY())
            {
                inTriangles = pointInTriangle(pt, leftTriangleV1, leftTriangleV2, leftTriangleV3);
                if (inTriangles)
                {
                    break;
                }
            }
            else if (pt.getX() < model.getUpperRight().getX() && pt.getX() > model.getLowerLeft().getX()
                    && pt.getY() > model.getUpperRight().getY() && pt.getY() < model.getLowerRight().getY())
            {
                inTriangles = pointInTriangle(pt, rightTriangleV1, rightTriangleV2, rightTriangleV3);
                if (inTriangles)
                {
                    break;
                }
            }
        }

        return inTriangles;
    }

    /**
     * Collects all the tiles we need to make transparent.
     *
     * @param parent The parent tile.
     * @param theGeometries The list to add the tiles to. projected on the
     *            globe.
     * @param model Contains information on the xray window and where it is on
     *            the globe.
     */
    private void collectTiles(TileGeometry parent, List<TileGeometry> theGeometries, XrayGogglesModel model)
    {
        if (parent.hasChildren())
        {
            Collection<TileGeometry> children = parent.getChildren(false);
            for (TileGeometry child : children)
            {
                if (child.getBounds() instanceof GeographicBoundingBox
                        && overlaps((GeographicBoundingBox)child.getBounds(), model))
                {
                    collectTiles(child, theGeometries, model);
                }
            }
        }
        else
        {
            theGeometries.add(parent);
        }
    }

    /**
     * Checks to see if the xray bounds contains the some or all of the tile
     * bounds.
     *
     * @param tileBounds The bounds of the tile.
     * @param model Contains information on the xray window and where it is on
     *            the globe.
     * @return True if tile bounds is within the xray window.
     */
    private boolean overlaps(GeographicBoundingBox tileBounds, XrayGogglesModel model)
    {
        boolean isEqual = tileBounds.getUpperLeft().getLatLonAlt().equals(model.getUpperLeftGeo().getLatLonAlt())
                && tileBounds.getUpperRight().getLatLonAlt().equals(model.getUpperRightGeo().getLatLonAlt())
                && tileBounds.getLowerLeft().getLatLonAlt().equals(model.getLowerLeftGeo().getLatLonAlt())
                && tileBounds.getLowerRight().getLatLonAlt().equals(model.getLowerRightGeo().getLatLonAlt());
        boolean isContainedInWindow = false;
        boolean isContainedInTile = false;

        if (!isEqual)
        {
            List<Vector2d> xrayPositions = New.list(model.getUpperLeftGeo().asVector2d(), model.getUpperRightGeo().asVector2d(),
                    model.getLowerLeftGeo().asVector2d(), model.getLowerRightGeo().asVector2d(),
                    model.getCenterGeo().asVector2d());
            Vector2d tileUpperRight = tileBounds.getUpperRight().asVector2d();
            Vector2d tileLowerLeft = tileBounds.getLowerLeft().asVector2d();
            for (Vector2d xrayPos : xrayPositions)
            {
                if (xrayPos.getX() > tileLowerLeft.getX() && xrayPos.getX() < tileUpperRight.getX()
                        && xrayPos.getY() > tileLowerLeft.getY() && xrayPos.getY() < tileUpperRight.getY())
                {
                    isContainedInTile = true;
                    break;
                }
            }

            if (!isContainedInTile)
            {
                List<Vector2i> tilesScreenPositions = New.list();
                for (GeographicPosition position : tileBounds.getVertices())
                {
                    Vector2i screenPos = myMapManager.convertToPoint(position);
                    tilesScreenPositions.add(screenPos);
                }
                tilesScreenPositions.add(myMapManager.convertToPoint(tileBounds.getCenter()));

                for (Vector2i tilePos : tilesScreenPositions)
                {
                    if (tilePos != null && tilePos.getX() > model.getLowerLeft().getX() && tilePos.getX() < model.getLowerRight().getX()
                            && tilePos.getY() > model.getUpperLeft().getY() && tilePos.getY() < model.getLowerLeft().getY())
                    {
                        isContainedInWindow = true;
                        break;
                    }
                }

                if (!isContainedInWindow)
                {
                    isContainedInWindow = checkIfInTriangles(tilesScreenPositions, model);
                }
            }
        }

        return isContainedInWindow || isContainedInTile || isEqual;
    }

    /**
     * Checks to see if the given point is within the triangle.
     *
     * @param pt The point to check for containment.
     * @param v1 The first point in the triangle.
     * @param v2 The second point in the triangle.
     * @param v3 The third point in the triangle.
     * @return True if the point is within the triangle, false otherwise.
     */
    private boolean pointInTriangle(Vector2i pt, ScreenPosition v1, ScreenPosition v2, ScreenPosition v3)
    {
        boolean b1;
        boolean b2;
        boolean b3;

        b1 = triArea(pt, v1, v2) < 0.0f;
        b2 = triArea(pt, v2, v3) < 0.0f;
        b3 = triArea(pt, v3, v1) < 0.0f;

        return b1 == b2 && b2 == b3;
    }

    /**
     * Calculates the area of the triangle.
     *
     * @param p1 The first point in the triangle.
     * @param p2 The second point in the triangle.
     * @param p3 The third point in the triangle.
     * @return The area of the triangle.
     */
    private double triArea(Vector2i p1, ScreenPosition p2, ScreenPosition p3)
    {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }
}
