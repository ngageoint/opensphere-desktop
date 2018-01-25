package io.opensphere.overlay.compass;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.TorusMeshGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.math.Matrix3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;

/** The renderable component of the Compass to put in the 3d frame. */
public class CompassRenderable extends Renderable
{
    /** Geometry which can be used to move the compass. */
    private TileGeometry myDragGeometry;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public CompassRenderable(Component parent)
    {
        super(parent);
        setBaseColor(new Color(166, 166, 200, 255));
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void init()
    {
        myDragGeometry = getPickTile(getDrawBounds(), getBaseZOrder());
        getGeometries().add(myDragGeometry);
        myMouseSupport.setActionGeometry(myDragGeometry);
        addTorus();
        addInnerTorus();
        addNorthPointer();
    }

    @Override
    public void mouseDragged(Geometry geom, Point dragStart, MouseEvent evt)
    {
        if (Utilities.sameInstance(geom, myDragGeometry))
        {
            Point end = evt.getPoint();
            moveWindow(new ScreenPosition((int)(end.getX() - dragStart.getX()), (int)(end.getY() - dragStart.getY())));
        }
    }

    /** Create a torus. */
    private void addInnerTorus()
    {
        TorusMeshGeometry.Builder<ModelPosition> builder = new TorusMeshGeometry.Builder<ModelPosition>();
        builder.setCenter(new ModelPosition(Vector3d.ORIGIN));
        builder.setCircularPoints(8);
        builder.setTubeSections(24);
        builder.setRadius(1000);
        builder.setTubeRadius(400);
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(0, true, false, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());
        Matrix3d trans = new Matrix3d();
        trans.fromAngleAxis(MathUtil.HALF_PI, Vector3d.UNIT_Y);
        builder.setPositionTransform(trans);

        TorusMeshGeometry torus = new TorusMeshGeometry(builder, props, null);
        getGeometries().add(torus);
    }

    /** Stupid north pointer. */
    private void addNorthPointer()
    {
        List<ModelPosition> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();

        Vector3d tip = new Vector3d(0, 0, 8000);
        double side = 500;
        double height = 1400;
        Vector3d northWest = new Vector3d(-side, side, height);
        Vector3d southWest = new Vector3d(-side, -side, height);
        Vector3d southEast = new Vector3d(side, -side, height);
        Vector3d northEast = new Vector3d(side, side, height);

        Vector3d normal = northEast.subtract(northWest).cross(northWest.subtract(tip)).getNormalized();
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
        positions.add(new ModelPosition(northEast));
        positions.add(new ModelPosition(northWest));
        positions.add(new ModelPosition(tip));

        normal = southEast.subtract(northEast).cross(northEast.subtract(tip)).getNormalized();
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
        positions.add(new ModelPosition(southEast));
        positions.add(new ModelPosition(northEast));
        positions.add(new ModelPosition(tip));

        normal = southWest.subtract(southEast).cross(southEast.subtract(tip)).getNormalized();
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
        positions.add(new ModelPosition(southWest));
        positions.add(new ModelPosition(southEast));
        positions.add(new ModelPosition(tip));

        normal = northWest.subtract(southWest).cross(southWest.subtract(tip)).getNormalized();
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
        positions.add(new ModelPosition(northWest));
        positions.add(new ModelPosition(southWest));
        positions.add(new ModelPosition(tip));

        PolygonMeshGeometry.Builder<ModelPosition> polyBuilder = new PolygonMeshGeometry.Builder<ModelPosition>();
        polyBuilder.setPolygonVertexCount(3);

        polyBuilder.setPositions(positions);
        polyBuilder.setNormals(normals);
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(getBaseZOrder() + 1, true, false, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        PolygonMeshGeometry mesh = new PolygonMeshGeometry(polyBuilder, props, null);
        getGeometries().add(mesh);
    }

    /** Create a torus. */
    private void addTorus()
    {
        TorusMeshGeometry.Builder<ModelPosition> builder = new TorusMeshGeometry.Builder<ModelPosition>();
        builder.setCenter(new ModelPosition(Vector3d.ORIGIN));
        builder.setCircularPoints(8);
        builder.setTubeSections(24);
        builder.setRadius(6000);
        builder.setTubeRadius(600);
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(0, true, false, true);
        props.setColor(getBaseColor());
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        TorusMeshGeometry torus = new TorusMeshGeometry(builder, props, null);
        getGeometries().add(torus);
    }
}
