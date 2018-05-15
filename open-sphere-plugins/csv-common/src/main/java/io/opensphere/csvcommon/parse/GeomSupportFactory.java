package io.opensphere.csvcommon.parse;

import java.awt.Color;

import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;

/** Factory for creating {@link MapLocationGeometrySupport} instances. */
@FunctionalInterface
public interface GeomSupportFactory
{
    /**
     * Construct the MapLocationGeometrySupport by checking the
     * MapVisualizationType and then constructing the appropriate artifact.
     *
     * @param mapVisInfo MapVisualizationInfo
     * @param ptData PointExtract
     * @param dotColor Color
     * @return MapLocationGeometrySupport
     */
    MapLocationGeometrySupport createGeometrySupport(MapVisualizationInfo mapVisInfo, PointExtract ptData, Color dotColor);
}
