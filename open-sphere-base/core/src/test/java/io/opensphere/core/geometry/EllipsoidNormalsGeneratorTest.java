package io.opensphere.core.geometry;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link EllipsoidNormalsGenerator}.
 */
public class EllipsoidNormalsGeneratorTest
{
    /**
     * Tests calculating the normals.
     */
    @Test
    public void test()
    {
        List<List<Position>> positions = New.list();

        List<Position> strip1 = New.list(new ModelPosition(0, 0, 0), new ModelPosition(1, 0, 0), new ModelPosition(1, 1, 0));
        List<Position> strip2 = New.list(new ModelPosition(0, 0, 1), new ModelPosition(1, 0, 1), new ModelPosition(1, 1, 1));
        positions.add(strip1);
        positions.add(strip2);

        List<Vector3d> expectedNormals1 = New.list(strip1.get(0).asVector3d().getNormalized(),
                strip1.get(1).asVector3d().getNormalized(), strip1.get(2).asVector3d().getNormalized());
        List<Vector3d> expectedNormals2 = New.list(strip2.get(0).asVector3d().getNormalized(),
                strip2.get(1).asVector3d().getNormalized(), strip2.get(2).asVector3d().getNormalized());

        EllipsoidNormalsGenerator normalsGenerator = new EllipsoidNormalsGenerator();
        List<List<Vector3d>> normals = normalsGenerator.calculateNormals(positions);

        assertEquals(2, normals.size());
        assertEquals(expectedNormals1, normals.get(0));
        assertEquals(expectedNormals2, normals.get(1));
    }
}
