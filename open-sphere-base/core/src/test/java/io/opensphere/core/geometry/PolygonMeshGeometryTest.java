package io.opensphere.core.geometry;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;
import org.junit.Assert;

/** Test for {@link LabelGeometry}. */
public class PolygonMeshGeometryTest
{
    /**
     * Test for {@link PolygonMeshGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        PolygonMeshGeometry.Builder<GeographicPosition> builder = new PolygonMeshGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);

        int[] indArray = { 1, 0, 3, 2 };
        PetrifyableTIntList indices = new PetrifyableTIntArrayList(indArray);
        builder.setIndices(indices);
        List<Vector3d> normals = Arrays.asList(new Vector3d(0., 0., 0.), new Vector3d(1., 1., 1.), new Vector3d(2., 1., 1.),
                new Vector3d(1., 0., 0.));
        builder.setNormals(normals);
        builder.setPolygonVertexCount(4);
        List<GeographicPosition> positions = Arrays.asList(new GeographicPosition(LatLonAlt.createFromDegrees(10., 10.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(20., 10.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(20., 20.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10., 20.)));
        builder.setPositions(positions);

        PolygonMeshRenderProperties renderProperties1 = new DefaultPolygonMeshRenderProperties(65763, false, true, false);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        ImageManager imageManager = new ImageManager(this, null);
        List<Vector2d> textureCoords = New.list(new Vector2d(0, 0), new Vector2d(.5, .7));

        builder.setImageManager(imageManager);
        builder.setTextureCoords(textureCoords);

        PolygonMeshGeometry geom = new PolygonMeshGeometry(builder, renderProperties1, constraints1);

        PolygonMeshGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertEquals(geom.getIndices(), clone.getIndices());
        Assert.assertEquals(geom.getNormals(), clone.getNormals());
        Assert.assertEquals(geom.getPositions(), clone.getPositions());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(textureCoords, geom.getTextureCoords());
        Assert.assertEquals(geom.getTextureCoords(), clone.getTextureCoords());
        Assert.assertEquals(imageManager, geom.getImageManager());
        Assert.assertEquals(geom.getImageManager(), clone.getImageManager());
    }

    /**
     * Test for
     * {@link PolygonMeshGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        PolygonMeshGeometry.Builder<GeographicPosition> builder = new PolygonMeshGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);

        int[] indArray = { 1, 0, 3, 2 };
        PetrifyableTIntList indices = new PetrifyableTIntArrayList(indArray);
        builder.setIndices(indices);
        List<Vector3d> normals = Arrays.asList(new Vector3d(0., 0., 0.), new Vector3d(1., 1., 1.), new Vector3d(2., 1., 1.),
                new Vector3d(1., 0., 0.));
        builder.setNormals(normals);
        builder.setPolygonVertexCount(4);
        List<GeographicPosition> positions = Arrays.asList(new GeographicPosition(LatLonAlt.createFromDegrees(10., 10.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(20., 10.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(20., 20.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10., 20.)));
        builder.setPositions(positions);

        PolygonMeshRenderProperties renderProperties1 = new DefaultPolygonMeshRenderProperties(65763, false, true, false);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        ImageManager imageManager = new ImageManager(this, null);
        List<Vector2d> textureCoords = New.list(new Vector2d(0, 0), new Vector2d(.5, .7));

        builder.setImageManager(imageManager);
        builder.setTextureCoords(textureCoords);

        PolygonMeshGeometry geom = new PolygonMeshGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        ColorRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertEquals(geom.getIndices(), ((PolygonMeshGeometry)derived).getIndices());
        Assert.assertEquals(geom.getNormals(), ((PolygonMeshGeometry)derived).getNormals());
        Assert.assertEquals(geom.getPositions(), ((PolygonMeshGeometry)derived).getPositions());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
        Assert.assertEquals(textureCoords, geom.getTextureCoords());
        Assert.assertEquals(geom.getTextureCoords(), ((PolygonMeshGeometry)derived).getTextureCoords());
        Assert.assertEquals(imageManager, geom.getImageManager());
        Assert.assertEquals(geom.getImageManager(), ((PolygonMeshGeometry)derived).getImageManager());
    }
}
