package io.opensphere.core.pipeline.processor;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link VertexData}.
 */
public class VertexDataTest
{
    /**
     * Tests {@link VertexData}.
     */
    @Test
    public void test()
    {
        List<? extends Vector3d> modelCoords = New.list(new Vector3d(1, 2, 3), new Vector3d(4, 5, 6), new Vector3d(7, 8, 9));
        List<? extends Vector3d> normals = New.list(new Vector3d(3, 4, 5));
        List<? extends Color> colors = New.list(Color.red, Color.blue, Color.green);
        List<? extends Vector2d> textCoords = New.list(new Vector2d(0, 0), new Vector2d(.3, .6), new Vector2d(.7, .4));

        VertexData vertexData = new VertexData(modelCoords, normals, colors, textCoords);

        assertEquals(modelCoords, vertexData.getModelCoords());
        assertEquals(normals, vertexData.getNormals());
        assertEquals(colors, vertexData.getColors());
        assertEquals(textCoords, vertexData.getTextureCoords());
    }
}
