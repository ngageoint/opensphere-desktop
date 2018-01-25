package io.opensphere.core.viewer.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Generate trajectories which maintain the starting height. The view is assumed
 * to point at the center of the model, so each position on the trajectory will
 * be adjusted to point at the center of the model.
 */
public class FlatTrajectoryGenerator3D implements TrajectoryGenerator
{
    /** The viewer for which the trajectory is being created. */
    private final Viewer3D myViewer;

    /**
     * Constructor.
     *
     * @param viewer The viewer for which the trajectory is being created.
     */
    public FlatTrajectoryGenerator3D(Viewer3D viewer)
    {
        myViewer = viewer;
    }

    @Override
    public List<ViewerPosition> generateTrajectory(List<TrajectorySegment> trajectorySegments)
    {
        List<ViewerPosition> positions = new ArrayList<>();

        for (TrajectorySegment trajectorySegment : trajectorySegments)
        {
            positions.add(trajectorySegment.getStartPosition());

            ViewerPosition3D start = (ViewerPosition3D)trajectorySegment.getStartPosition();
            GeographicPosition startGeo = myViewer.getMapContext().getProjection().convertToPosition(start.getLocation(),
                    ReferenceLevel.ELLIPSOID);
            ViewerPosition3D end = (ViewerPosition3D)trajectorySegment.getEndPosition();
            GeographicPosition endGeo = myViewer.getMapContext().getProjection().convertToPosition(end.getLocation(),
                    ReferenceLevel.ELLIPSOID);
            double height = start.getLocation().getLength();

            double angularDist = GeographicBody3D.greatCircleDistanceR(startGeo.getLatLonAlt(), endGeo.getLatLonAlt());

            double steps = trajectorySegment.getSteps();
            if (steps == -1)
            {
                steps = angularDist * MathUtil.RAD_TO_DEG * 3;
            }

            for (int i = 1; i < steps; ++i)
            {
                double percent = i / steps;
                LatLonAlt geo = GeographicBody3D.greatCircleInterpolate(startGeo.getLatLonAlt(), endGeo.getLatLonAlt(), percent);
                Vector3d locVec = myViewer.getMapContext().getProjection()
                        .convertToModel(new GeographicPosition(geo), Vector3d.ORIGIN).getNormalized();
                Vector3d location = locVec.multiply(height);
                Vector3d dir = locVec.multiply(-1);
                Vector3d up = start.getUp().interpolate(end.getUp(), percent);
                positions.add(new ViewerPosition3D(location, dir, up));
            }

            positions.add(trajectorySegment.getEndPosition());
        }

        return positions;
    }
}
