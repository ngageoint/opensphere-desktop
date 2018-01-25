package io.opensphere.core.viewer.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;

/**
 * Generate trajectories which maintain the starting height. The view is assumed
 * to point at the center of the model, so each position on the trajectory will
 * be adjusted to point at the center of the model.
 */
public class FlatTrajectoryGenerator2D implements TrajectoryGenerator
{
    /** The viewer for which the trajectory is being created. */
    private final Viewer2D myViewer;

    /**
     * Constructor.
     *
     * @param viewer The viewer for which the trajectory is being created.
     */
    public FlatTrajectoryGenerator2D(Viewer2D viewer)
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

            ViewerPosition2D start = (ViewerPosition2D)trajectorySegment.getStartPosition();
            GeographicPosition startGeo = myViewer.getMapContext().getProjection().convertToPosition(start.getLocation(),
                    ReferenceLevel.ELLIPSOID);
            ViewerPosition2D end = (ViewerPosition2D)trajectorySegment.getEndPosition();
            GeographicPosition endGeo = myViewer.getMapContext().getProjection().convertToPosition(end.getLocation(),
                    ReferenceLevel.ELLIPSOID);

            double angularDistDeg = Math.abs(endGeo.subtract(startGeo).getLength());

            double steps = trajectorySegment.getSteps();
            if (steps == -1)
            {
                steps = angularDistDeg * 3;
            }

            for (int i = 1; i < steps; ++i)
            {
                double percent = i / steps;
                Vector3d location = start.getLocation().interpolate(end.getLocation(), percent);
                double scale = MathUtil.lerp(percent, start.getScale(), end.getScale());
                positions.add(new ViewerPosition2D(location, scale));
            }

            positions.add(trajectorySegment.getEndPosition());
        }

        return positions;
    }
}
