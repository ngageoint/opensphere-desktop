package io.opensphere.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.model.Position;

/**
 * Unit test for the {@link EllipsoidVertexGenerator} class.
 */
public class EllipsoidVertexGeneratorTest
{
    /**
     * Tests an ellipsoid vertex generation.
     */
    @Test
    public void test()
    {
        List<List<Position>> expectedPositions = EllipsoidTestUtils.getExpectedPositions();

        EllipsoidVertexGenerator generator = new EllipsoidVertexGenerator();
        List<List<Position>> actualPositions = generator.generateVertices(EllipsoidTestUtils.QUALITY, EllipsoidTestUtils.AXIS_A,
                EllipsoidTestUtils.AXIS_B, EllipsoidTestUtils.AXIS_C);

        assertFalse(actualPositions.isEmpty());
        assertEquals(expectedPositions.size(), actualPositions.size());

        for (int i = 0; i < expectedPositions.size(); i++)
        {
            List<Position> expectedStrip = expectedPositions.get(i);
            List<Position> actualStrip = actualPositions.get(i);
            assertFalse(actualStrip.isEmpty());
            assertEquals(expectedStrip.size(), actualStrip.size());
            int index = 0;
            for (Position expected : expectedStrip)
            {
                Position actual = actualStrip.get(index);
                assertEquals(expected.asVector3d(), actual.asVector3d());
                index++;
            }
        }
    }
}
