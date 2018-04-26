package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;

import io.opensphere.core.math.Matrix3d;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Translate raw key events into viewer movements. The key events are only used
 * to determine what action to take, so all of the methods in this class ignore
 * the event and do a predefined action for each method.
 */
@SuppressWarnings("PMD.GodClass")
public class Viewer3DControlTranslator extends AbstractViewerControlTranslator
{
    /** The maximum angle of rotation for the model. */
    private static final double MAX_MODEL_ROTATION_ANGLE = 5. * MathUtil.DEG_TO_RAD;

    /** The factor to scale movements by when doing micro-movements. */
    // TODO this should be configurable from the options panel (like the zoom
    // rate).
    private static final double MICRO_MOVEMENT_FACTOR = 0.05;

    /** Multiplier to use for pan input. */
    private static final double PAN_FACTOR = 3.;

    /** The angle to rotate relative to the viewer. */
    private static final double VIEW_ROTATION_ANGLE = 2.5 * MathUtil.DEG_TO_RAD;

    /** True when the controls are enabled and false when they are not. */
    private boolean myControlEnabled = true;

    /** The viewer current yaw axis. */
    private volatile Vector3d myYawAxis = new Vector3d(0, 0, 1);

    /**
     * Construct a View3DControlTranslator.
     *
     * @param viewer The viewer to manipulate.
     */
    public Viewer3DControlTranslator(VolatileReference<DynamicViewer> viewer)
    {
        super(viewer);
    }

    @Override
    public boolean canAdjustViewer(Vector2i from, Vector2i to)
    {
        Vector3d fromInter = getViewer().getModelIntersection(from);
        if (fromInter != null)
        {
            Vector3d toInter = getViewer().getModelIntersection(to);
            if (toInter != null)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void moveView(double deltaX, double deltaY)
    {
        if (!myControlEnabled)
        {
            return;
        }
        ViewerPosition3D viewPosition = getViewer().getPosition();
        ViewerPosition3D first = getViewer().getSpinOnAxisPosition(Math.toRadians(deltaX), myYawAxis, true, viewPosition);
        Vector3d oToV = new Vector3d(viewPosition.getLocation()).getNormalized();
        Vector3d tiltPlaneNormal = oToV.cross(myYawAxis);
        ViewerPosition3D finish = getViewer().getSpinOnAxisPosition(Math.toRadians(deltaY), tiltPlaneNormal, true, first);
        getViewer().setView(finish.getLocation(), finish.getDir(), finish.getUp());
    }

    @Override
    public void moveView(Vector2i from, Vector2i to)
    {
        if (!myControlEnabled)
        {
            return;
        }
        spinOnAxis(from, to, myYawAxis);
    }

    @Override
    public void moveViewAxes(double deltaX, double deltaY)
    {
        if (!myControlEnabled)
        {
            return;
        }
        ViewerPosition3D viewPosition = getViewer().getPosition();
        ViewerPosition3D first = getViewer().getSpinOnAxisPosition(Math.toRadians(deltaX), viewPosition.getUp(), true,
                viewPosition);
        ViewerPosition3D finish = getViewer().getSpinOnAxisPosition(Math.toRadians(deltaY), viewPosition.getRight(), true, first);
        getViewer().setView(finish.getLocation(), finish.getDir(), finish.getUp());
    }

    @Override
    public void moveViewAxes(Vector2i from, Vector2i to)
    {
        if (!myControlEnabled)
        {
            return;
        }
        Vector3d fromInter = getViewer().getModelIntersection(from);
        if (fromInter != null)
        {
            Vector3d toInter = getViewer().getModelIntersection(to);
            if (toInter != null)
            {
                Matrix3d rotMat = new Matrix3d();
                rotMat.fromStartEndVectors(toInter.getNormalized(), fromInter.getNormalized());

                ViewerPosition3D viewPosition = getViewer().getPosition();
                Vector3d pos = rotMat.mult(viewPosition.getLocation());
                Vector3d dir = rotMat.mult(viewPosition.getDir());
                Vector3d up = rotMat.mult(viewPosition.getUp());

                getViewer().setView(pos, dir, up);
            }
        }
    }

    @Override
    public void moveYawAxis(double deltaX, double deltaY)
    {
        if (!myControlEnabled)
        {
            return;
        }
        ViewerPosition3D viewPosition = getViewer().getPosition();
        myYawAxis = myYawAxis.rotate(viewPosition.getUp(), Math.toRadians(deltaX))
                .rotate(viewPosition.getRight(), Math.toRadians(deltaY)).getNormalized();
    }

    @Override
    public void moveYawAxis(Vector2i from, Vector2i to)
    {
        if (!myControlEnabled)
        {
            return;
        }
        Vector3d fromInter = getViewer().getModelIntersection(from);
        if (fromInter != null)
        {
            Vector3d toInter = getViewer().getModelIntersection(to);
            if (toInter != null)
            {
                Matrix3d rotMat = new Matrix3d();
                rotMat.fromStartEndVectors(toInter.getNormalized(), fromInter.getNormalized());

                myYawAxis = rotMat.mult(myYawAxis).getNormalized();
            }
        }
    }

    @Override
    public void pitchView(double angleRads)
    {
        if (!myControlEnabled || Double.isNaN(angleRads))
        {
            return;
        }
        Vector3d earthInter = getViewer().getTerrainIntersection();
        ViewerPosition3D viewPosition = getViewer().getPosition();
        Vector3d right = viewPosition.getRight();

        if (earthInter == null)
        {
            Vector3d viewLoc = viewPosition.getLocation();
            Vector3d dir = viewPosition.getDir();

            double ang = MathUtil.HALF_PI - viewLoc.getAngleDifference(dir);
            viewLoc = viewLoc.rotate(right, ang);
            earthInter = getViewer().getMapContext().getProjection()
                    .getTerrainIntersection(new Ray3d(viewLoc, viewLoc.multiply(-1.)), getViewer());
            if (earthInter == null)
            {
                resetView(null);
                return;
            }
        }

        // translate to the intersection point
        Matrix4d translation = new Matrix4d();
        translation.setTranslation(earthInter.multiply(-1));

        // rotate
        Matrix4d tilt = new Matrix4d();
        tilt.fromAngleAxis(angleRads, right);

        // translate back
        Matrix4d transBack = new Matrix4d();
        transBack.setTranslation(earthInter);

        Matrix4d transform = new Matrix4d();
        transform.multLocal(transBack);
        transform.multLocal(tilt);
        transform.multLocal(translation);

        Vector3d newPos = transform.mult(viewPosition.getLocation());
        Vector3d newDir = earthInter.subtract(newPos).getNormalized();

        // When the angle between the center of the earth to the intersection of
        // the point on the earth and the viewer direction gets close to PI,
        // prevent further pitching.
        Vector3d earthInterNormal = earthInter.getNormalized();
        double origDot = Math.abs(earthInterNormal.dot(viewPosition.getDir()));
        double newDot = Math.abs(earthInterNormal.dot(newDir));
        final double pitchTolerance = 0.1;
        if ((newDot < pitchTolerance || origDot < pitchTolerance) && newDot < origDot)
        {
            return;
        }

        getViewer().setView(newPos, newDir, tilt.mult(viewPosition.getUp()));
    }

    @Override
    public void pitchView(Vector2i from, Vector2i to)
    {
        if (!myControlEnabled || !canAdjustViewer(from, to))
        {
            return;
        }

        Vector3d fromDir = getViewer().screenToModelPointVector(from);
        Vector3d toDir = getViewer().screenToModelPointVector(to);

        // This rotation angle is chosen based of feel.
        Vector3d right = getViewer().getPosition().getRight();
        Vector3d startProj = Plane.unitProjection(right, fromDir);
        Vector3d endProj = Plane.unitProjection(right, toDir);
        final double scaleFactor = 3.5;
        double dot = MathUtil.clamp(startProj.dot(endProj), -1., 1.);
        double rotAngle = -Math.acos(dot) * scaleFactor;
        Vector3d rotAxis = startProj.cross(endProj).getNormalized();
        if (rotAxis.dot(right) < 0)
        {
            rotAngle *= -1.;
        }

        pitchView(rotAngle);
    }

    @Override
    public void pitchViewDown(InputEvent event)
    {
        pitchView(Math.toRadians(-PAN_FACTOR));
    }

    @Override
    public void pitchViewUp(InputEvent event)
    {
        pitchView(Math.toRadians(PAN_FACTOR));
    }

    @Override
    public void resetView(InputEvent event)
    {
        if (!myControlEnabled)
        {
            return;
        }
        ViewerPosition3D pos = getViewer().getRightedViewAtIntersection(getViewer().getPosition());
        getViewer().setPosition(pos);
    }

    @Override
    public void rollView(double angleRads)
    {
        if (!myControlEnabled || Double.isNaN(angleRads))
        {
            return;
        }
        ViewerPosition3D viewPosition = getViewer().getPosition();
        Matrix4d tilt = new Matrix4d();
        tilt.fromAngleNormalAxis(angleRads, viewPosition.getDir());
        Vector3d up = tilt.mult(viewPosition.getUp());

        getViewer().setView(viewPosition.getLocation(), viewPosition.getDir(), up);
    }

    @Override
    public void rollView(Vector2i from, Vector2i to)
    {
        // Not implemented for the default controls
    }

    @Override
    public void rollViewLeft(InputEvent event)
    {
        rollView(Math.toRadians(-PAN_FACTOR));
    }

    @Override
    public void rollViewRight(InputEvent event)
    {
        rollView(Math.toRadians(PAN_FACTOR));
    }

    @Override
    public void setControlEnabled(boolean enable)
    {
        myControlEnabled = enable;
    }

    @Override
    public void spinOnAxis(double angleRads)
    {
        spinOnAxis(angleRads, myYawAxis);
    }

    @Override
    public void spinOnAxis(double angleRads, Vector3d spinAxis)
    {
        if (!myControlEnabled || Double.isNaN(angleRads))
        {
            return;
        }
        Matrix3d rotMat = new Matrix3d();
        rotMat.fromAngleNormalAxis(angleRads, spinAxis);

        ViewerPosition3D viewPosition = getViewer().getPosition();
        Vector3d pos = rotMat.mult(viewPosition.getLocation());
        Vector3d dir = rotMat.mult(viewPosition.getDir());
        Vector3d up = rotMat.mult(viewPosition.getUp());

        getViewer().setView(pos, dir, up);
    }

    @Override
    public void viewDown(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        double modelRotationAngle = getModelRotaitonAngleR();
        modelRotationAngle = microMovement ? modelRotationAngle * MICRO_MOVEMENT_FACTOR : modelRotationAngle;
        spinOnAxis(modelRotationAngle, getViewer().getPosition().getRight());
    }

    @Override
    public void viewLeft(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        double modelRotationAngle = getModelRotaitonAngleR();
        modelRotationAngle = microMovement ? modelRotationAngle * MICRO_MOVEMENT_FACTOR : modelRotationAngle;

        // Reverse the angle if the viewer is upside-down.
        double sign = Math.signum(getViewer().getPosition().getUp().getZ());
        if (!Double.isNaN(sign) && !MathUtil.isZero(sign))
        {
            modelRotationAngle *= sign;
        }
        spinOnAxis(-modelRotationAngle);
    }

    @Override
    public void viewRight(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        double modelRotationAngle = getModelRotaitonAngleR();
        modelRotationAngle = microMovement ? modelRotationAngle * MICRO_MOVEMENT_FACTOR : modelRotationAngle;

        // Reverse the angle if the viewer is upside-down.
        double sign = Math.signum(getViewer().getPosition().getUp().getZ());
        if (!Double.isNaN(sign) && !MathUtil.isZero(sign))
        {
            modelRotationAngle *= sign;
        }
        spinOnAxis(modelRotationAngle);
    }

    @Override
    public void viewUp(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        double modelRotationAngle = getModelRotaitonAngleR();
        modelRotationAngle = microMovement ? modelRotationAngle * MICRO_MOVEMENT_FACTOR : modelRotationAngle;
        spinOnAxis(modelRotationAngle, getViewer().getPosition().getRight().multiply(-1.));
    }

    @Override
    public void yawView(double angleRads)
    {
        // Not implemented for the default controls
    }

    @Override
    public void yawView(Vector2i from, Vector2i to)
    {
        if (!myControlEnabled || !canAdjustViewer(from, to))
        {
            return;
        }
        Vector3d intersection = getViewer().getTerrainIntersection();
        if (intersection == null)
        {
            intersection = getViewer().getModelIntersection();
        }
        Vector3d spinAxis = intersection.multiply(-1.).getNormalized();
        spinOnAxis(from, to, spinAxis);
    }

    @Override
    public void yawViewLeft(InputEvent event)
    {
        yawView(Math.toRadians(PAN_FACTOR));
    }

    @Override
    public void yawViewRight(InputEvent event)
    {
        yawView(Math.toRadians(-PAN_FACTOR));
    }

    @Override
    public void zoomView(double delta)
    {
        if (!myControlEnabled)
        {
            return;
        }
        ViewerPosition3D viewPosition = getViewer().getPosition();
        Vector3d earthInter = getViewer().getModelIntersection();
        if (earthInter == null)
        {
            getViewer().setPosition(getViewer().getRightedView(viewPosition));
            return;
        }
        final double scalingFactor = 120.;
        double scaledAmount = delta * viewPosition.getLocation().subtract(earthInter).getLength() / scalingFactor;

        Vector3d dir = viewPosition.getDir().multiply(scaledAmount);
        Vector3d newPos = viewPosition.getLocation().add(dir);
        getViewer().setView(newPos, viewPosition.getDir(), viewPosition.getUp());
    }

    @Override
    protected Viewer3D getViewer()
    {
        return (Viewer3D)super.getViewer();
    }

    /**
     * Spin the model on a given axis.
     *
     * @param from end spin position.
     * @param to start spin position.
     * @param spinAxis axis around which to rotate.
     */
    protected void spinOnAxis(Vector2i from, Vector2i to, Vector3d spinAxis)
    {
        if (!myControlEnabled)
        {
            return;
        }
        MapContext<DynamicViewer> mapContext = getViewer().getMapContext();
        boolean contextNull = mapContext == null;
        Vector3d fromInter = contextNull ? null : getViewer().getTerrainIntersection(from);
        if (fromInter == null)
        {
            fromInter = getViewer().getModelIntersection(from);
        }
        if (fromInter != null)
        {
            Vector3d toInter = contextNull ? null : getViewer().getTerrainIntersection(to);
            if (toInter == null)
            {
                toInter = getViewer().getModelIntersection(from);
            }
            if (toInter != null)
            {
                spinOnAxis(fromInter, toInter, spinAxis);
            }
        }
    }

    /**
     * Get the amount of rotation to apply for a generic model rotation movement
     * (for example a key press) based on the viewer distance from the model.
     *
     * @return The angle by which to rotate in radians.
     */
    private double getModelRotaitonAngleR()
    {
        Vector3d viewerPos = getViewer().getPosition().getLocation();
        Vector3d modelIntersect = getViewer().getClosestModelPosition();

        double viewToModel = viewerPos.distance(modelIntersect);
        double originToModel = modelIntersect.distance(Vector3d.ORIGIN);
        double modelRotationDistance = viewToModel * Math.tan(VIEW_ROTATION_ANGLE);
        double modelRotationAngle = Math.atan(modelRotationDistance / originToModel);
        modelRotationAngle = Math.min(modelRotationAngle, MAX_MODEL_ROTATION_ANGLE);
        return modelRotationAngle;
    }

    /**
     * Spin the viewer around an arbitrary axis such that the <i>from</i> point
     * is moved to the <i>to</i> point.
     *
     * @param from The from point.
     * @param to The to point.
     * @param spinAxis The normalized axis to rotate about.
     */
    private void spinOnAxis(Vector3d from, Vector3d to, Vector3d spinAxis)
    {
        if (!myControlEnabled)
        {
            return;
        }
        // get the amount of rotation around the spin axis required to
        // move "from" to "to". To do this, project the vectors
        // onto the plane perpendicular to the spin axis, then find the
        // angle between the projections.
        Vector3d startProj = Plane.unitProjection(spinAxis, from);
        Vector3d endProj = Plane.unitProjection(spinAxis, to);
        double dot = MathUtil.clamp(startProj.dot(endProj), -1., 1.);
        double rotAngle = Math.acos(dot);

        // Determine whether the rotation is positive or negative by comparing
        // the spinAxis direction with the rotAxis direction.
        Vector3d rotAxis = startProj.cross(endProj).getNormalized();
        if (rotAxis.dot(spinAxis) > 0)
        {
            rotAngle *= -1.;
        }

        ViewerPosition3D first = getViewer().getSpinOnAxisPosition(rotAngle, spinAxis, false, getViewer().getPosition());

        Vector3d tiltPlaneNormal = getTiltPlane(from, spinAxis, first);
        startProj = Plane.unitProjection(tiltPlaneNormal, from);
        endProj = Plane.unitProjection(tiltPlaneNormal, to);

        dot = MathUtil.clamp(startProj.dot(endProj), -1., 1.);
        rotAngle = Math.acos(dot);
        rotAxis = startProj.cross(endProj).getNormalized();
        if (rotAxis.dot(tiltPlaneNormal) > 0)
        {
            rotAngle *= -1.;
        }

        ViewerPosition3D finish = getViewer().getSpinOnAxisPosition(rotAngle, tiltPlaneNormal, false, first);
        getViewer().setView(finish.getLocation(), finish.getDir(), finish.getUp());
    }

    /**
     * Calculates the tilt plane from the supplied vectors. If the spin axis is
     * tilted at all, a calculation is made to assist in generating the rotation
     * tilt plane. If not, the original viewpoint is used to determine the tilt
     * plane.
     *
     * @param pFrom the original vector describing the current view.
     * @param pSpinAxis the vector describing the spin axis.
     * @param pFirst the first viewer position.
     * @return A vector describing the plane on which the spin axis is tilted.
     */
    protected Vector3d getTiltPlane(Vector3d pFrom, Vector3d pSpinAxis, ViewerPosition3D pFirst)
    {
        // tilt the spin axis
        Vector3d oToV = new Vector3d(pFirst.getLocation()).getNormalized();
        Vector3d tiltPlaneNormal;
        if (!MathUtil.isZero(Math.abs(oToV.dot(pSpinAxis)) - 1d))
        {
            // the axis of rotation is tilted from pointing straight down.
            // Calculate the tilt plane:
            tiltPlaneNormal = oToV.cross(pSpinAxis).getNormalized();
        }
        else
        {
            // the axis of rotation is pointed straight down, calculate the tilt
            // plane from the original vector differently:
            tiltPlaneNormal = pFrom.cross(pSpinAxis).getNormalized();
        }
        return tiltPlaneNormal;
    }
}
