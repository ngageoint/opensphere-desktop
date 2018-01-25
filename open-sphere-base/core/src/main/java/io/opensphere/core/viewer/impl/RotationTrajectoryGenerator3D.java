package io.opensphere.core.viewer.impl;

import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Generate trajectories which rotate the view without moving it.
 */
public class RotationTrajectoryGenerator3D implements TrajectoryGenerator
{
    /**
     * Helper method to generate the intermediate positions for a trajectory
     * segment.
     *
     * @param start The start of the segment.
     * @param end The end of the segment.
     * @param rotSteps The number of rotations from the start to the end.
     * @return The rotational positions.
     */
    public static List<ViewerPosition> generateRotationalPositions(ViewerPosition3D start, ViewerPosition3D end, int rotSteps)
    {
        List<ViewerPosition> positions = New.list();

        positions.add(start);

        double dirAngDif = start.getDir().getAngleDifference(end.getDir());
        double upAngDif = start.getUp().getAngleDifference(end.getUp());

        double steps = rotSteps;
        if (steps == -1)
        {
            final double three = 3.;
            steps = Math.max(dirAngDif * MathUtil.RAD_TO_DEG, upAngDif * MathUtil.RAD_TO_DEG) * three;
        }

        for (int i = 1; i < steps; ++i)
        {
            double percent = i / steps;

            Vector3d dir = start.getDir().interpolate(end.getDir(), percent);
            Vector3d up = start.getUp().interpolate(end.getUp(), percent);

            positions.add(new ViewerPosition3D(start.getLocation(), dir, up));
        }

        positions.add(end);

        return positions;
    }

    @Override
    public List<ViewerPosition> generateTrajectory(List<TrajectorySegment> trajectorySegments)
    {
        List<ViewerPosition> positions = New.list();

        for (TrajectorySegment sgmt : trajectorySegments)
        {
            ViewerPosition3D start = (ViewerPosition3D)sgmt.getStartPosition();
            ViewerPosition3D end = (ViewerPosition3D)sgmt.getEndPosition();
            positions.addAll(generateRotationalPositions(start, end, sgmt.getSteps()));
        }

        return positions;
    }
}
