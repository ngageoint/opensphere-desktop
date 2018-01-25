package io.opensphere.core.pipeline.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Color;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * Unit tests the {@link PolygonMeshData}.
 */
public class PolygonMeshDataTest
{
    /**
     * Tests the {@link PolygonMeshData}.
     */
    @Test
    public void test()
    {
        List<? extends Vector3d> modelCoords = New.list(new Vector3d(1, 2, 3), new Vector3d(4, 5, 6), new Vector3d(7, 8, 9));
        List<? extends Vector3d> normals = New.list(new Vector3d(3, 4, 5));
        List<? extends Color> colors = New.list(Color.red, Color.blue, Color.green);
        List<? extends Vector2d> textCoords = New.list(new Vector2d(0, 0), new Vector2d(.3, .6), new Vector2d(.7, .4));
        int[] indArray = { 1, 0, 3, 2 };
        PetrifyableTIntList indices = new PetrifyableTIntArrayList(indArray);

        PolygonMeshData meshData = new PolygonMeshData(modelCoords, normals, indices, colors, textCoords, 3, false);

        assertEquals(modelCoords, meshData.getVertexData().getModelCoords());
        assertEquals(normals, meshData.getVertexData().getNormals());
        assertEquals(indices, meshData.getModelIndices());
        assertEquals(colors, meshData.getVertexData().getColors());
        assertEquals(textCoords, meshData.getVertexData().getTextureCoords());
        assertEquals(3, meshData.getTesseraVertexCount());
        assertFalse(meshData.isPetrified());
    }
}
