package io.opensphere.core.viewer.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Generate trajectories which arc from the start points to the end points. The
 * view is assumed to point at the center of the model, so each position on the
 * trajectory will be adjusted to point at the center of the model.
 */
public class ArcTrajectoryGenerator3D implements TrajectoryGenerator
{
    /** The maximum height to zoom the viewer out when forming the arc. */
    private static final double MAX_ARC_HEIGHT = 6. * WGS84EarthConstants.RADIUS_EQUATORIAL_M;

    /** The viewer for which the trajectory is being created. */
    private final Viewer3D myViewer;

    /**
     * Constructor.
     *
     * @param viewer The viewer for which the trajectory is being created.
     */
    public ArcTrajectoryGenerator3D(Viewer3D viewer)
    {
        myViewer = viewer;
    }

    @Override
    public List<ViewerPosition> generateTrajectory(List<TrajectorySegment> trajectorySegments)
    {
        Projection snapshot = myViewer.getMapContext().getProjection();
        List<ViewerPosition> positions = new ArrayList<>();

        for (TrajectorySegment trajectorySegment : trajectorySegments)
        {
            generateTrajectory(trajectorySegment, snapshot, positions);
        }

        return positions;
    }

    /**
     * Generate the next position.
     *
     * @param projection The projection used for converting positions.
     * @param interpStart The start of the great circle arc position.
     * @param interpEnd The end of the great circle arc position.
     * @param percent The percentage along the arc.
     * @param height The height of the new position.
     * @param up The up vector of the new position.
     * @return The newly generated viewer position.
     */
    private ViewerPosition3D generateArcPosition(Projection projection, LatLonAlt interpStart, LatLonAlt interpEnd,
            double percent, double height, Vector3d up)
    {
        LatLonAlt geo = GeographicBody3D.greatCircleInterpolate(interpStart, interpEnd, percent);
        Vector3d locVec = projection.convertToModel(new GeographicPosition(geo), Vector3d.ORIGIN).getNormalized();
        Vector3d location = locVec.multiply(height);
        Vector3d dir = locVec.multiply(-1);
        return new ViewerPosition3D(location, dir, up);
    }

    /**
     * Generate the trajectory for a trajectory segment.
     *
     * @param trajectorySegment The trajectory segment.
     * @param projection The projection to use.
     * @param positions The result list to which to add the viewer positions.
     */
    private void generateTrajectory(TrajectorySegment trajectorySegment, Projection projection, List<ViewerPosition> positions)
    {
        ViewerPosition3D start = (ViewerPosition3D)trajectorySegment.getStartPosition();
        ViewerPosition3D end = (ViewerPosition3D)trajectorySegment.getEndPosition();

        ViewerPosition3D rightedStart = myViewer.getRightedView(start);
        positions.addAll(RotationTrajectoryGenerator3D.generateRotationalPositions(start, rightedStart, -1));

        GeographicPosition startGeo = projection.convertToPosition(start.getLocation(), ReferenceLevel.ELLIPSOID);
        GeographicPosition endGeo = projection.convertToPosition(end.getLocation(), ReferenceLevel.ELLIPSOID);
        double startHeight = start.getLocation().getLength();
        double endHeight = end.getLocation().getLength();

        double angularDist = GeographicBody3D.greatCircleDistanceR(startGeo.getLatLonAlt(), endGeo.getLatLonAlt());

        // The maximum rotation should be 180 degrees, so that should give
        // us
        // the highest zoom out. For closer points we should zoom out less.
        double arcPosition = MathUtil.clamp(angularDist / Math.PI, 0., 1.);

        double minHeight = Math.min(startHeight, endHeight);

        double middleHeight = MathUtil.lerp(arcPosition, minHeight, MAX_ARC_HEIGHT);

        double steps = trajectorySegment.getSteps();
        if (steps == -1)
        {
            steps = angularDist * MathUtil.RAD_TO_DEG * 3;
        }

        // If the start, middle, and end heights are the same, make the set
        // the percentages to .5
        double firstDist = Math.abs(middleHeight - startHeight);
        double secondDist = Math.abs(middleHeight - endHeight);
        double firstPct = .5;
        double secondPct = .5;
        if (!MathUtil.isZero(firstDist))
        {
            firstPct = firstDist / (firstDist + secondDist);
            secondPct = secondDist / (firstDist + secondDist);
        }

        LatLonAlt midGeo = GeographicBody3D.greatCircleInterpolate(startGeo.getLatLonAlt(), endGeo.getLatLonAlt(), firstPct);
        Vector3d midLocVec = projection.convertToModel(new GeographicPosition(midGeo), Vector3d.ORIGIN).getNormalized();
        Vector3d midLocation = midLocVec.multiply(middleHeight);
        ViewerPosition3D midPosition = myViewer.getRightedView(midLocation);

        // For the half of the curve which has a higher end point, use less
        // steps and peak more quickly.
        double firstSteps = steps * firstPct;
        for (int i = 1; i < firstSteps; ++i)
        {
            double percent = i / firstSteps;
            double height = interpolateHeight(1. - percent, startHeight, middleHeight);
            Vector3d up = start.getUp().interpolate(midPosition.getUp(), percent);
            ViewerPosition3D nextPosition = generateArcPosition(projection, startGeo.getLatLonAlt(), midGeo, percent, height, up);
            positions.add(nextPosition);
        }

        double secondSteps = steps * secondPct;
        for (int i = 0; i < secondSteps; ++i)
        {
            double percent = i / secondSteps;
            double height = interpolateHeight(percent, endHeight, middleHeight);
            Vector3d up = midPosition.getUp().interpolate(end.getUp(), percent);
            ViewerPosition3D nextPosition = generateArcPosition(projection, midGeo, endGeo.getLatLonAlt(), percent, height, up);
            positions.add(nextPosition);
        }

        ViewerPosition3D rightedEnd = myViewer.getRightedView(end);
        positions.addAll(RotationTrajectoryGenerator3D.generateRotationalPositions(rightedEnd, end, -1));
    }

    /**
     * Parabolic interpolation for the viewer height.
     *
     * @param acrPos A value in the range [-1, 1] which gives the relative
     *            position along the parabola.
     * @param start start height.
     * @param end end height
     * @return adjusted interpolation value.
     */
    private double interpolateHeight(double acrPos, double start, double end)
    {
        double paraLoc = -acrPos * acrPos + 1;
        return start + (end - start) * paraLoc;
    }
}
