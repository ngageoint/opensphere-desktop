package io.opensphere.mantle.data.geom.style.impl;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Utility class used by ellipsoid test classes.
 */
public final class EllipsoidTestUtils
{
    /**
     * The semi major axis size for the expected positions.
     */
    public static final double AXIS_A = 2;

    /**
     * The horizontal semi minor axis size for the expected positions.
     */
    public static final double AXIS_B = 1;

    /**
     * The vertical semi minor axis size for the expected positions.
     */
    public static final double AXIS_C = 1;

    /**
     * The quality for the expected positions.
     */
    public static final int QUALITY = 10;

    /**
     * Calculates the expected normals.
     *
     * @param expected The expected vertex positions.
     * @return The expected normals.
     */
    public static List<Vector3d> calculateExpectedNormals(List<List<Position>> expected)
    {
        List<Vector3d> expectedNormals = New.list();

        for (List<Position> strip : expected)
        {
            for (Position first : strip)
            {
                expectedNormals.add(first.asVector3d().getNormalized());
            }
        }

        return expectedNormals;
    }

    /**
     * Creates the easy mock map manager.
     *
     * @param support Used to create the mock.
     * @return The map manager.
     */
    public static MapManager createMapManager(EasyMockSupport support)
    {
        Projection projection = support.createMock(Projection.class);
        EasyMock.expect(projection.convertToModel(EasyMock.isA(GeographicPosition.class), EasyMock.eq(Vector3d.ORIGIN)))
                .andAnswer(EllipsoidTestUtils::convertToModelAnswer);

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getProjection()).andReturn(projection);

        return mapManager;
    }

    /**
     * Gets some canned positions that we expect to be generated given the
     * parameters above.
     *
     * @return The list of vertices organized by their individual strips.
     */
    public static List<List<Position>> getExpectedPositions()
    {
        List<List<Position>> expectedPositions = New.list();
        try (Scanner scanner = new Scanner(EllipsoidTestUtils.class.getResourceAsStream("/ellipsoidtest.txt"),
                StringUtilities.DEFAULT_CHARSET.name()))
        {
            List<Position> strip = New.list();
            expectedPositions.add(strip);
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                if (StringUtils.isEmpty(line))
                {
                    strip = New.list();
                    expectedPositions.add(strip);
                }
                else
                {
                    String[] split = line.split(" ");
                    double x = Double.parseDouble(split[0]);
                    double y = Double.parseDouble(split[1]);
                    double z = Double.parseDouble(split[2]);

                    strip.add(new ModelPosition(x, y, z));
                }
            }
        }

        return expectedPositions;
    }

    /**
     * The answer for the convertToModel mock.
     *
     * @return The geographic position as a 3d vector.
     */
    private static Vector3d convertToModelAnswer()
    {
        return ((GeographicPosition)EasyMock.getCurrentArguments()[0]).asVector3d();
    }

    /**
     * Not constructible.
     */
    private EllipsoidTestUtils()
    {
    }
}
