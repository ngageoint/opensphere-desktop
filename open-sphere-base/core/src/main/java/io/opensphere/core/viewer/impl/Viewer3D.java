package io.opensphere.core.viewer.impl;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import io.opensphere.core.math.DefaultEllipsoid;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Matrix3d;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.MutableVector3d;
import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Quaternion;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.RectangularCylinder;
import io.opensphere.core.math.Shape;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.projection.AbstractGeographicProjection;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangeSupport.ProjectionChangeListener;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.TerrainUtil;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * A viewer which views a scene in three dimensions.
 */
@SuppressWarnings("PMD.GodClass")
public class Viewer3D extends AbstractDynamicViewer
{
    /** A preference key for the viewer position. */
    public static final String POSITION_PREF_KEY = "Viewer3D.position";

    /** Tolerance for the viewer's position with respect to the terrain. */
    public static final double TERRAIN_SAFETY_TOLERANCE = -0.00001;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Viewer3D.class);

    /** The maximum distance from the origin constant. */
    private static final double ourMaxOriginDistance = 2000000000.;

    /**
     * The number of radians to add to the top clipping planes rotation
     * calculation.
     */
    private static final double ourTopClipIncreaseAngle = .2;

    /**
     * The maximum amount of degrees we allow the camera to move when zoomed in.
     */
    private static final double ourZoomedInDegreeTolerance = .03;

    /** The aspect ration. */
    private volatile double myAspectRatio = 1.;

    /** Bottom clipping plane. */
    private final Plane myBottomClip = new Plane();

    /** The half view angle x component. */
    private volatile double myHalfFOVx = MathUtil.QUARTER_PI * 0.5;

    /** The half view angle y component. */
    private volatile double myHalfFOVy = myHalfFOVx;

    /** The left clipping plane. */
    private final Plane myLeftClip = new Plane();

    /** The map context. */
    private MapContext<DynamicViewer> myMapContext;

    /** The model which I view. */
    private Shape myModel = new DefaultEllipsoid(WGS84EarthConstants.RADIUS_EQUATORIAL_M, WGS84EarthConstants.RADIUS_EQUATORIAL_M,
            WGS84EarthConstants.RADIUS_POLAR_M);

    /** The model view matrix. */
    private float[] myModelViewMatrix;

    /** The position and orientation of the viewer. */
    private final ViewerPosition3D myPosition = new ViewerPosition3D();

    /** Listener for preference changes. */
    private final PreferenceChangeListener myPreferenceChangeListener = new PreferenceChangeListener()
    {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            if (evt.getSource() != Viewer3D.this)
            {
                setPosition(getPreferences().getJAXBObject(ViewerPosition3D.class, POSITION_PREF_KEY, new ViewerPosition3D()));
            }
        }
    };

    /** Listener for projection changes. */
    private final ProjectionChangeListener myProjectionChangeListener = new ProjectionChangeListener()
    {
        @Override
        public void projectionChanged(ProjectionChangedEvent evt)
        {
            resetProjectionMatrixClipped();
        }
    };

    /** The projection matrix. */
    private float[] myProjectionMatrix;

    /**
     * The clipped projection matrix that maximizes depth buffer precision. This
     * one is clipped at twice the distance from the viewer position to the
     * origin.
     */
    private float[] myProjectionMatrixClipped;

    /**
     * The clipped projection matrix that maximizes depth buffer precision. This
     * one is clipped at the origin.
     */
    private float[] myProjectionMatrixClippedFarToCenter;

    /** The right clipping plane. */
    private final Plane myRightClip = new Plane();

    /** The max angle by which to spin. */
    private final double mySpinAngleLimit = Math.toRadians(7);

    /** The top clipping plane. */
    private final Plane myTopClip = new Plane();

    /**
     * Construct me.
     *
     * @param builder builder with construction parameters.
     */
    public Viewer3D(Builder builder)
    {
        super(builder);
        addTrajectoryGenerator(TrajectoryGeneratorType.ARC, new ArcTrajectoryGenerator3D(this));
        addTrajectoryGenerator(TrajectoryGeneratorType.ROTATION, new RotationTrajectoryGenerator3D());
        addTrajectoryGenerator(TrajectoryGeneratorType.FLAT, new FlatTrajectoryGenerator3D(this));
        resetClipPlanes();
        if (getPreferences() != null)
        {
            getPreferences().addPreferenceChangeListener(POSITION_PREF_KEY, myPreferenceChangeListener);
        }
    }

    @Override
    public synchronized float[] getAdjustedModelViewMatrix(Matrix4d adjustment)
    {
        if (myModelViewMatrix == null)
        {
            resetModelViewMatrix();
        }
        return new Matrix4d(getModelViewMatrix()).mult(adjustment).toFloatArray();
    }

    @Override
    public double getAltitude()
    {
        // Using closest model position to account for pitching of earth.
        return myPosition.getLocation().subtract(getClosestModelPosition()).getLength();
    }

    /**
     * Get the bottomClip.
     *
     * @return the bottomClip
     */
    public Plane getBottomClip()
    {
        return myBottomClip;
    }

    @Override
    public KMLCompatibleCamera getCamera(ViewerPosition position)
    {
        if (!(position instanceof ViewerPosition3D))
        {
            throw new UnsupportedOperationException("3D viewer cannot create a camera for a non-3D position.");
        }
        Projection proj = getMapContext().getProjection(Viewer3D.class);
        if (!(proj instanceof AbstractGeographicProjection))
        {
            throw new UnsupportedOperationException(
                    "Cannot create viewer postion from geographic position for non-geographic projection.");
        }
        ViewerPosition3D position3D = (ViewerPosition3D)position;
        GeographicPosition location = proj.convertToPosition(position3D.getLocation(), ReferenceLevel.ELLIPSOID);
        Vector3d posToOrigin = position3D.getLocation().multiply(-1.).getNormalized();
        double roll;
        double tilt;
        ViewerPosition3D untilted;
        /* If there is no tilt (the viewer direction is directly at the model
         * origin), then there can only be heading. If this viewer was created
         * from a KML camera which specified either roll or a combination of
         * roll and heading this will result in a KML definition which does not
         * contain the same values, but is equivalent. */
        if (MathUtil.isZero(1. - Math.abs(position3D.getDir().dot(posToOrigin))))
        {
            roll = 0.;
            tilt = 0.;
            untilted = position3D;
        }
        else
        {
            /* Get the roll. This operation rotates around the viewer's z-axis
             * my moving the viewer's y-axis so that the model's origin is
             * coplanar to the viewer's y-z plane. */
            ViewerPosition3D unrolled = new ViewerPosition3D(position3D.getLocation(), position3D.getDir(),
                    position3D.getLocation());
            roll = unrolled.getUp().getAngleUnit(position3D.getUp(), position3D.getDir()) * MathUtil.RAD_TO_DEG;
            /* Get the tilt. This operation points the viewer's direction at the
             * model's origin. When the tilt is more than 90 or less than -90 we
             * need to reverse the unrolled view position's y-axis. */
            if (0. > Math.signum(unrolled.getDir().dot(posToOrigin)))
            {
                untilted = new ViewerPosition3D(unrolled.getLocation(), posToOrigin, unrolled.getUp().multiply(-1));
            }
            else
            {
                untilted = new ViewerPosition3D(unrolled.getLocation(), posToOrigin, unrolled.getUp());
            }
            tilt = untilted.getDir().getAngleUnit(unrolled.getDir(), unrolled.getRight()) * MathUtil.RAD_TO_DEG;
        }
        /* Get the heading. This operation puts view righted? */
        ViewerPosition3D righted = new ViewerPosition3D(untilted.getLocation(), untilted.getDir(), new Vector3d(0., 0., 1.));
        double heading = righted.getUp().getAngleUnit(untilted.getUp(), untilted.getDir()) * MathUtil.RAD_TO_DEG;
        return new KMLCompatibleCamera(location.getLatLonAlt(), heading, tilt, roll);
    }

    @Override
    public ViewerPosition3D getCenteredView(Vector3d position)
    {
        ViewerPosition3D righted = getRightedView(position);
        Vector3d altAdjustedLoc = righted.getLocation().getNormalized().multiply(myPosition.getLocation().getLength());
        return new ViewerPosition3D(altAdjustedLoc, righted.getDir(), righted.getUp());
    }

    @Override
    public ViewerPosition getCenteredView(Vector3d position, Vector3d centroidHint)
    {
        if (centroidHint == null || position.dot(centroidHint) > 0.)
        {
            return getCenteredView(position);
        }
        else
        {
            return getCenteredView(position.multiply(-1));
        }
    }

    @Override
    public Vector3d getClosestModelPosition()
    {
        Vector3d loc = myPosition.getLocation();
        return myModel.getIntersection(new Ray3d(loc, loc.multiply(-1.).getNormalized()));
    }

    @Override
    public double getHeading()
    {
        return myModel.getMapOrientationHeading(myPosition.getDir(), myPosition.getUp());
    }

    /**
     * Get the horizontal field-of-view in radians.
     *
     * @return The field-of-view.
     */
    public double getHorizontalFOV()
    {
        return myHalfFOVx * 2.;
    }

    /**
     * Get the leftClip.
     *
     * @return the leftClip
     */
    public Plane getLeftClip()
    {
        return myLeftClip;
    }

    @Override
    public MapContext<DynamicViewer> getMapContext()
    {
        return myMapContext;
    }

    /**
     * Get the model.
     *
     * @return the model
     */
    public Shape getModel()
    {
        return myModel;
    }

    @Override
    public Vector3d getModelIntersection()
    {
        return myModel.getIntersection(new Ray3d(myPosition.getLocation(), myPosition.getDir()));
    }

    /**
     * Get the point on the model which is in view at the given screen location.
     *
     * @param screenLoc Screen location.
     * @return model position.
     */
    public Vector3d getModelIntersection(Vector2i screenLoc)
    {
        ScreenPosition ul = getViewOffset();
        int viewportHeight = getViewportHeight();
        int correctedX = (int)(screenLoc.getX() - ul.getX());
        int correctedY = (int)(viewportHeight - screenLoc.getY() - ul.getY());
        Vector2i correctedScreenLoc = new Vector2i(correctedX, correctedY);
        return windowToModelCoords(correctedScreenLoc);
    }

    @Override
    public synchronized float[] getModelViewMatrix()
    {
        if (myModelViewMatrix == null)
        {
            resetModelViewMatrix();
        }
        return myModelViewMatrix.clone();
    }

    @Override
    public double getPitch()
    {
        // Find pitch by finding angle between model intersection and plane
        // normal to direction.
        // Pitch is then 90 degrees minus this angle value.
        Vector3d modelIntersect = getModelIntersection();
        if (modelIntersect == null)
        {
            return 0;
        }
        modelIntersect = modelIntersect.getNormalized();
        Vector3d projectedIntersect = Plane.unitProjection(myPosition.getDir().multiply(-1), modelIntersect).getNormalized();
        // Now calculate angle between vectors (and keep sign)
        Vector3d orthogonal = myPosition.getRight().multiply(-1);
        Vector3d cross = modelIntersect.cross(projectedIntersect);
        double dot = modelIntersect.dot(projectedIntersect);
        double resultAngle = Math.atan2(orthogonal.dot(cross), dot);
        double ninetyDegrees = Math.toRadians(90);
        return Math.signum(resultAngle) < 0 ? -1 * (ninetyDegrees - Math.abs(resultAngle)) : ninetyDegrees - resultAngle;
    }

    @Override
    public double getPixelWidth(Ellipsoid ellipsoid)
    {
        double modelSize;
        if (ellipsoid instanceof Sphere)
        {
            modelSize = ((Sphere)ellipsoid).getRadius() * 2.;
        }
        else
        {
            modelSize = ellipsoid.getXAxis().getLength() * 2.;
        }
        double pctOfView = modelSize / getViewVolumeWidthAt(ellipsoid.getCenter());
        return pctOfView * getViewportWidth();
    }

    @Override
    public ViewerPosition3D getPosition()
    {
        return myPosition;
    }

    @Override
    public float[] getProjectionMatrix()
    {
        synchronized (this)
        {
            if (myProjectionMatrix == null)
            {
                resetProjectionMatrix();
            }
            return myProjectionMatrix.clone();
        }
    }

    @Override
    public float[] getProjectionMatrixClipped(boolean clipFarToCenter)
    {
        synchronized (this)
        {
            if (clipFarToCenter)
            {
                if (myProjectionMatrixClippedFarToCenter == null)
                {
                    myProjectionMatrixClippedFarToCenter = generateProjectionMatrixClipped(true);
                }
                return myProjectionMatrixClippedFarToCenter.clone();
            }
            else
            {
                if (myProjectionMatrixClipped == null)
                {
                    myProjectionMatrixClipped = generateProjectionMatrixClipped(false);
                }
                return myProjectionMatrixClipped.clone();
            }
        }
    }

    @Override
    public RectangularCylinder getRectifyBounds(Collection<? extends Vector3d> modelPoints)
    {
        RectangularCylinder cardinalBounds = new RectangularCylinder(modelPoints);
        ViewerPosition3D centeredView = getCenteredView(cardinalBounds.getCenter());
        Vector3d[] axes = centeredView.getAxes();
        return new RectangularCylinder(modelPoints, axes[0], axes[1], axes[2]);
    }

    /**
     * Get the rightClip.
     *
     * @return the rightClip
     */
    public Plane getRightClip()
    {
        return myRightClip;
    }

    @Override
    public ViewerPosition3D getRightedView(Vector3d location)
    {
        ViewerPosition3D pos = new ViewerPosition3D();
        Vector3d dir = location.multiply(-1.).getNormalized();
        Vector3d up = Vector3d.UNIT_Z;
        pos.setPosition(location, dir, up);
        return pos;
    }

    @Override
    public ViewerPosition3D getRightedView(ViewerPosition position)
    {
        if (position instanceof ViewerPosition3D)
        {
            return getRightedView(position.getLocation());
        }
        return null;
    }

    @Override
    public ViewerPosition3D getRightedViewAtIntersection(ViewerPosition position)
    {
        ViewerPosition3D pos = new ViewerPosition3D();
        Projection snapshot = myMapContext.getProjection();
        Vector3d intersect = snapshot
                .getTerrainIntersection(new Ray3d(position.getLocation(), ((ViewerPosition3D)position).getDir()), this);
        if (intersect == null)
        {
            intersect = snapshot.getTerrainIntersection(
                    new Ray3d(position.getLocation(), ((ViewerPosition3D)position).getLocation().multiply(-1.)), this);
        }
        if (intersect == null)
        {
            Vector3d dir = position.getLocation().multiply(-1.);
            Vector3d up = Vector3d.UNIT_Z;
            pos.setPosition(position.getLocation(), dir, up);
        }
        else
        {
            double height = intersect.getLength() + intersect.subtract(position.getLocation()).getLength();
            Vector3d intersectNormal = intersect.getNormalized();
            Vector3d location = intersectNormal.multiply(height);
            Vector3d up = Vector3d.UNIT_Z;
            Vector3d dir = intersectNormal.multiply(-1.);
            pos.setPosition(location, dir, up);
        }
        return pos;
    }

    /**
     * Create a new viewer position which is the given position spun about an
     * axis of rotation.
     *
     * @param angleRads The amount to spin the viewer position in radians.
     * @param spinAxis The axis about which to spin the viewer position.
     * @param limitAngle When true, the angle will be limited to a pre-defined
     *            maximum.
     * @param position The position to spin.
     * @return The newly created viewer position.
     */
    public ViewerPosition3D getSpinOnAxisPosition(double angleRads, Vector3d spinAxis, boolean limitAngle,
            ViewerPosition3D position)
    {
        double ang = limitAngle ? Math.min(angleRads, mySpinAngleLimit) : angleRads;
        if (Double.isNaN(ang))
        {
            return null;
        }
        Matrix3d rotMat = new Matrix3d();
        rotMat.fromAngleNormalAxis(ang, spinAxis);
        Vector3d pos = rotMat.mult(position.getLocation());
        Vector3d dir = rotMat.mult(position.getDir());
        Vector3d up = rotMat.mult(position.getUp());
        return new ViewerPosition3D(pos, dir, up);
    }

    @Override
    public Vector3d getTerrainIntersection()
    {
        return myMapContext.getProjection().getTerrainIntersection(new Ray3d(myPosition.getLocation(), myPosition.getDir()),
                this);
    }

    @Override
    public Vector3d getTerrainIntersection(Vector2i screenLoc)
    {
        ScreenPosition ul = getViewOffset();
        int viewportHeight = getViewportHeight();
        int correctedX = (int)(screenLoc.getX() - ul.getX());
        int correctedY = (int)(viewportHeight - screenLoc.getY() - ul.getY());
        Vector2i correctedScreenLoc = new Vector2i(correctedX, correctedY);
        Vector3d model = super.windowToModelCoords(correctedScreenLoc);
        Vector3d lookAt = model.subtract(myPosition.getLocation());
        return myMapContext.getProjection().getTerrainIntersection(new Ray3d(myPosition.getLocation(), lookAt), this);
    }

    /**
     * Get the topClip.
     *
     * @return the topClip
     */
    public Plane getTopClip()
    {
        return myTopClip;
    }

    /**
     * Get the vertical field-of-view in radians.
     *
     * @return The field-of-view.
     */
    public double getVerticalFOV()
    {
        return myHalfFOVy * 2.;
    }

    @Override
    public ViewerPosition3D getViewerPosition(KMLCompatibleCamera camera)
    {
        Projection proj = getMapContext().getProjection(Viewer3D.class);
        if (!(proj instanceof AbstractGeographicProjection))
        {
            throw new UnsupportedOperationException(
                    "Cannot create viewer postion from geographic position for non-geographic projection.");
        }
        Vector3d modelPosition = proj.convertToModel(new GeographicPosition(camera.getLocation()), Vector3d.ORIGIN);
        ViewerPosition3D viewPosition = getRightedView(modelPosition);
        // Heading - Z rotation
        Matrix3d rotation = new Matrix3d();
        rotation.fromAngleNormalAxis(camera.getHeading() * MathUtil.DEG_TO_RAD, viewPosition.getDir());
        Vector3d up = rotation.mult(viewPosition.getUp());
        viewPosition.setPosition(viewPosition.getLocation(), viewPosition.getDir(), up);
        // Tilt - X rotation
        rotation.fromAngleAxis(camera.getTilt() * MathUtil.DEG_TO_RAD, viewPosition.getRight());
        Vector3d dir = rotation.mult(viewPosition.getDir());
        up = rotation.mult(viewPosition.getUp());
        viewPosition.setPosition(viewPosition.getLocation(), dir, up);
        // Roll - Z rotation again
        rotation.fromAngleNormalAxis(camera.getRoll() * MathUtil.DEG_TO_RAD, viewPosition.getDir());
        up = rotation.mult(viewPosition.getUp());
        viewPosition.setPosition(viewPosition.getLocation(), viewPosition.getDir(), up);
        return viewPosition;
    }

    @Override
    public double getViewLength(Vector3d ptA, Vector3d ptB)
    {
        Vector3d vToA = ptA.subtract(myPosition.getLocation()).getNormalized();
        Vector3d vToB = ptB.subtract(myPosition.getLocation()).getNormalized();
        double dot = MathUtil.clamp(vToA.dot(vToB), -1., 1.);
        double angle = Math.abs(Math.acos(dot));
        return getViewportHeight() * angle / myHalfFOVy * 0.5;
    }

    @Override
    public double getViewVolumeWidthAt(Vector3d modelPosition)
    {
        // The tangent of the field of view angle * distance will give the
        // view width at the distance.
        double tanHalfAngle = Math.tan(myHalfFOVx * 2.);
        double distance = modelPosition.distance(myPosition.getLocation());
        return distance * tanHalfAngle;
    }

    @Override
    public double getViewVolumeWidthAtIntersection()
    {
        Vector3d modelIntersect = myModel.getIntersection(new Ray3d(myPosition.getLocation(), myPosition.getDir()));
        return getViewVolumeWidthAt(modelIntersect);
    }

    @Override
    public ViewerPosition3D getZoomToFitView(RectangularCylinder bounds)
    {
        Vector3d center = bounds.getCenter();
        ViewerPosition3D centeredView = getCenteredView(center);
        // Zoom to to fit the bounds.
        double xSpan = bounds.getSpan(centeredView.getRight());
        double ySpan = bounds.getSpan(centeredView.getUp());
        double dist1 = xSpan / Math.tan(myHalfFOVx) * .5;
        double dist2 = ySpan / Math.tan(myHalfFOVy) * .5;
        double distance = Math.max(dist1, dist2);
        Vector3d pos = center.add(centeredView.getDir().multiply(-distance));
        return new ViewerPosition3D(pos, centeredView.getDir(), centeredView.getUp());
    }

    @Override
    public ViewerPosition getZoomToFitView(RectangularCylinder bounds, Vector3d centroidHint)
    {
        if (centroidHint == null || bounds.getCenter().dot(centroidHint) > 0.)
        {
            return getZoomToFitView(bounds);
        }
        else
        {
            ViewerPosition3D centeredView = getCenteredView(centroidHint);
            double halfSpan = getModel().getBoundingRadius();
            // Do not divide by 2 since we are only using half the span.
            double distance = halfSpan / Math.tan(myHalfFOVx);
            Vector3d pos = centroidHint.add(centeredView.getDir().multiply(-distance));
            return new ViewerPosition3D(pos, centeredView.getDir(), centeredView.getUp());
        }
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public boolean isInView(Ellipsoid ellipsoid, double cullCosine)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.concat("Testing inView for ellipsoid [", ellipsoid, "] cullCosine [",
                    Double.valueOf(cullCosine), "]"));
        }
        if (isInView(ellipsoid.getCenter(), 0.))
        {
            LOGGER.trace("inView center is in view");
            return true;
        }
        else
        {
            Vector3d localTopNormal = ellipsoid.normalToLocal(myTopClip.getNormal()).getNormalized();
            Vector3d topEllipsePoint = ellipsoid.localToModel(localTopNormal);
            if (!myTopClip.isInFront(topEllipsePoint, 0.))
            {
                LOGGER.trace("not in front topClip");
                return false;
            }
            Vector3d localBottomNormal = ellipsoid.normalToLocal(myBottomClip.getNormal()).getNormalized();
            Vector3d bottomEllipsePoint = ellipsoid.localToModel(localBottomNormal);
            if (!myBottomClip.isInFront(bottomEllipsePoint, 0.))
            {
                LOGGER.trace("not in front bottomClip");
                return false;
            }
            Vector3d localLeftNormal = ellipsoid.normalToLocal(myLeftClip.getNormal()).getNormalized();
            Vector3d leftEllipsePoint = ellipsoid.localToModel(localLeftNormal);
            if (!myLeftClip.isInFront(leftEllipsePoint, 0.))
            {
                LOGGER.trace("not in front leftClip");
                return false;
            }
            Vector3d localRightNormal = ellipsoid.normalToLocal(myRightClip.getNormal()).getNormalized();
            Vector3d rightEllipsePoint = ellipsoid.localToModel(localRightNormal);
            if (!myRightClip.isInFront(rightEllipsePoint, 0.))
            {
                LOGGER.trace("not in front rightClip");
                return false;
            }
            if (myTopClip.hasIntersection(topEllipsePoint, bottomEllipsePoint)
                    || myBottomClip.hasIntersection(topEllipsePoint, bottomEllipsePoint)
                    || myRightClip.hasIntersection(leftEllipsePoint, rightEllipsePoint)
                    || myLeftClip.hasIntersection(leftEllipsePoint, rightEllipsePoint))
            {
                LOGGER.trace("inView intersects frustrum");
                return true;
            }
        }
        if (cullCosine < 1. && ellipsoid.getZAxis().getNormalized().dot(getPosition().getDir()) > cullCosine)
        {
            LOGGER.trace("culled");
            return false;
        }
        LOGGER.trace("inView");
        return true;
    }

    @Override
    public boolean isInView(Vector3d point, double radius)
    {
        // if it is in front of all of the clip planes, then it is in the view
        // volume
        if (myLeftClip.isInFront(point, radius) && myRightClip.isInFront(point, radius) && myTopClip.isInFront(point, radius)
                && myBottomClip.isInFront(point, radius))
        {
            Plane earthBisector = new Plane(Vector3d.ORIGIN, myPosition.getLocation());
            if (earthBisector.isInFront(point, radius))
            {
                return true;
            }
        }
        return false;
    }

    /** Set the field of view back to the default value. */
    public synchronized void resetFOV()
    {
        setFOV(Math.toDegrees(MathUtil.QUARTER_PI));
    }

    @Override
    public void resetView()
    {
        setPosition(getRightedViewAtIntersection(myPosition));
    }

    /**
     * Determine the vector which points from the viewer position to the given
     * screen position.
     *
     * @param pos screen position
     * @return Vector pointing at the screen position
     */
    public Vector3d screenToModelPointVector(Vector2i pos)
    {
        double adjustedX = pos.getX() - getViewOffset().getX();
        double adjustedY = pos.getY() - getViewOffset().getY();
        // get the percentage of the screen for the positions
        // then use that to get the angles off of the viewer
        // Intersect those with the earth to get the actual selection points.
        double fromXpct = adjustedX / getViewportWidth() - 0.5;
        double fromYpct = adjustedY / getViewportHeight() - 0.5;
        double fromXAngleChange = -fromXpct * myHalfFOVx * 2.;
        double fromYAngleChange = -fromYpct * myHalfFOVy * 2.;
        return rotateDir(fromXAngleChange, fromYAngleChange).getNormalized();
    }

    @Override
    public void setCenteredView(Viewer viewer)
    {
        // Set this viewer to have the same direction, up, and right, but set
        // the position so that the viewer points at the origin and maintains
        // the same distance.
        if (viewer instanceof Viewer3D)
        {
            Viewer3D v3d = (Viewer3D)viewer;
            double dist = myPosition.getLocation().getLength();
            Vector3d pos = v3d.getPosition().getDir().multiply(-dist);
            ViewerPosition3D viewerPosition = new ViewerPosition3D(pos, v3d.getPosition().getDir(), v3d.getPosition().getUp());
            setPosition(viewerPosition);
        }
    }

    /**
     * Set the field of view.
     *
     * @param fov field of view to set.
     */
    public synchronized void setFOV(double fov)
    {
        double halfx = Math.toRadians(fov) * 0.5;
        if (!MathUtil.isZero(myHalfFOVx - halfx))
        {
            myHalfFOVx = halfx;
            myHalfFOVy = Math.atan(Math.tan(myHalfFOVx) / myAspectRatio);
            resetProjectionMatrix();
        }
    }

    @Override
    public void setMapContext(MapContext<DynamicViewer> context)
    {
        myMapContext = context;
        myMapContext.getProjectionChangeSupport().addProjectionChangeListener(myProjectionChangeListener);
    }

    /**
     * Set the model.
     *
     * @param model the model to set
     */
    public void setModel(Shape model)
    {
        myModel = model;
    }

    @Override
    public void setPosition(ViewerPosition viewerPosition)
    {
        if (viewerPosition instanceof ViewerPosition3D)
        {
            ViewerPosition3D pos = (ViewerPosition3D)viewerPosition;
            myPosition.setPosition(pos.getLocation(), pos.getDir(), pos.getUp(), pos.getGeoPosition());
            viewChanged();
        }
        else
        {
            throw new IllegalArgumentException(
                    "Non compatible position type for Viewer3D : " + viewerPosition.getClass().getName());
        }
    }

    /**
     * Set the view.
     *
     * @param pos position of viewer.
     * @param dir direction of viewer.
     * @param up up of viewer.
     */
    public void setView(Vector3d pos, Vector3d dir, Vector3d up)
    {
        Vector3d scaledPos = pos;
        double originDistance = pos.getLength();
        if (originDistance > ourMaxOriginDistance)
        {
            double scaleFactor = ourMaxOriginDistance / originDistance;
            scaledPos = pos.multiply(scaleFactor);
        }
        // Make sure that the viewer still points at the model
        Vector3d intersection = getModel().getIntersection(new Ray3d(scaledPos, dir));
        if (intersection == null || intersection.dot(dir) > -100)
        {
            return;
        }
        // Make sure that the viewer is not inside the model
        MapContext<DynamicViewer> mapContext = getMapContext();
        GeographicPosition geopos = null;
        if (mapContext != null)
        {
            if (!mapContext.getProjection().isOutsideModel(scaledPos.add(scaledPos.multiply(Viewer3D.TERRAIN_SAFETY_TOLERANCE))))
            {
                return;
            }
            else
            {
                geopos = mapContext.getProjection().convertToPosition(pos, ReferenceLevel.ELLIPSOID);
                double elevation = TerrainUtil.getInstance().getElevationInMeters(mapContext, geopos);
                geopos = new GeographicPosition(LatLonAlt.createFromDegreesMeters(geopos.getLatLonAlt().getLatD(),
                        geopos.getLatLonAlt().getLonD(), geopos.getLatLonAlt().getAltM() - elevation, ReferenceLevel.TERRAIN));
                if (!validateNewPosition(geopos))
                {
                    return;
                }
//                System.out.println(geopos);
            }
        }
        ViewerPosition3D viewerPosition = new ViewerPosition3D(scaledPos, dir, up);
        viewerPosition.setGeoPosition(geopos);
        setPosition(viewerPosition);
    }

    /**
     * Spin the viewer around an arbitrary axis such that the <i>from</i> point
     * is moved to the <i>to</i> point.
     *
     * @param from The from point.
     * @param to The to point.
     * @param spinAxis The normalized axis to rotate about.
     */
    public void spinOnAxis(Vector3d from, Vector3d to, Vector3d spinAxis)
    {
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
        ViewerPosition3D first = getSpinOnAxisPosition(rotAngle, spinAxis, false, getPosition());
        // tilt the spin axis
        Vector3d oToV = new Vector3d(first.getLocation()).getNormalized();
        if (MathUtil.isZero(Math.abs(oToV.dot(spinAxis)) - 1d))
        {
            return;
        }
        Vector3d tiltPlaneNormal = oToV.cross(spinAxis).getNormalized();
        startProj = Plane.unitProjection(tiltPlaneNormal, from);
        endProj = Plane.unitProjection(tiltPlaneNormal, to);
        dot = MathUtil.clamp(startProj.dot(endProj), -1., 1.);
        rotAngle = Math.acos(dot);
        rotAxis = startProj.cross(endProj).getNormalized();
        if (rotAxis.dot(tiltPlaneNormal) > 0)
        {
            rotAngle *= -1.;
        }
        ViewerPosition3D finish = getSpinOnAxisPosition(rotAngle, tiltPlaneNormal, false, first);
        setView(finish.getLocation(), finish.getDir(), finish.getUp());
    }

    @Override
    public void startAnimationToPreferredPosition()
    {
        final ViewerPosition3D pos = getPreferences().getJAXBObject(ViewerPosition3D.class, POSITION_PREF_KEY, null);
        if (pos != null)
        {
            new ViewerAnimator(this, pos).start();
        }
    }

    @Override
    public boolean supportsAdjustedModelView()
    {
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Position : ").append(myPosition);
        sb.append(", Direction : ").append(myPosition.getDir());
        sb.append(", Up : ").append(myPosition.getUp());
        return sb.toString();
    }

    @Override
    public void validateViewerPosition()
    {
        Vector3d pos = new Vector3d(myPosition.getLocation());
        double terrainTolerance = TERRAIN_SAFETY_TOLERANCE;
        boolean changed = false;
        if (myPosition.getGeoPosition() != null)
        {
            Vector3d oldPos = pos;
            pos = myMapContext.getProjection().convertToModel(myPosition.getGeoPosition(), Vector3d.ORIGIN);
            changed = Math.abs(pos.getX() - oldPos.getX()) > 400 || Math.abs(pos.getY() - oldPos.getY()) > 400
                    || Math.abs(pos.getZ() - oldPos.getZ()) > 400;
            if (myPosition.getGeoPosition().getAlt().getReferenceLevel() != ReferenceLevel.TERRAIN)
            {
                terrainTolerance *= 10;
            }
        }
        Vector3d backup = myPosition.getDir().multiply(-100);
        while (!myMapContext.getProjection().isOutsideModel(pos.add(pos.multiply(terrainTolerance))))
        {
            changed = true;
            pos = pos.add(backup);
        }
        if (changed)
        {
            ViewerPosition3D viewerPosition = new ViewerPosition3D(pos, myPosition.getDir(), myPosition.getUp());
            viewerPosition.setGeoPosition(myPosition.getGeoPosition());
            setPosition(viewerPosition);
        }
    }

    @Override
    public Vector3d windowToModelCoords(Vector2i windowCoords)
    {
        Vector3d model = super.windowToModelCoords(windowCoords);
        Vector3d lookAt = model.subtract(myPosition.getLocation());
        return myModel.getIntersection(new Ray3d(myPosition.getLocation(), lookAt));
    }

    @Override
    protected void doReshape(int width, int height)
    {
        setAspectRatio((double)width / (double)height);
    }

    /**
     * Generate the clipped projection matrix.
     *
     * @param clipFarToCenter When true the far clipping plane will be moved so
     *            that it passes through the origin. Otherwise the far clipping
     *            plane will be at twice the distance from the viewer to the
     *            origin.
     * @return The matrix.
     */
    protected synchronized float[] generateProjectionMatrixClipped(boolean clipFarToCenter)
    {
        if (myProjectionMatrix == null)
        {
            resetProjectionMatrix();
        }
        float[] clipped = myProjectionMatrix.clone();
        Vector3d model = getClosestModelPosition();
        double elevation;
        if (myMapContext == null)
        {
            elevation = model.subtract(myPosition.getLocation()).getLength();
        }
        else
        {
            // convertToPosition() gives the geographic location with a 0
            // altitude (referenced by terrain) and convertToModel() gives
            // the location on the terrain.
            Projection snapshot = myMapContext.getProjection();
            Vector3d terrainModel = snapshot.convertToModel(snapshot.convertToPosition(model, ReferenceLevel.TERRAIN),
                    Vector3d.ORIGIN);
            elevation = terrainModel.subtract(myPosition.getLocation()).getLength();
        }
        double tanHalfFov = (float)Math.tan(myHalfFOVx);
        final double minNearClipDistance = 5.;
        double near = Math.max(elevation / (2. * Math.sqrt(2. * tanHalfFov * tanHalfFov + 1.)), minNearClipDistance);
        double radius = myModel.getBoundingRadius();
        double far = Math.sqrt(elevation * (2. * radius + elevation));
        far *= clipFarToCenter ? 1 : 2;
        double depth = far - near;
        double c = -(far + near) / depth;
        double d = -2. * far * near / depth;
        clipped[10] = (float)c;
        clipped[14] = (float)d;
        return clipped;
    }

    /** reset the clipping plane. */
    protected final void resetClipPlanes()
    {
        // set the point for each plane to the viewer position
        Vector3d loc = myPosition.getLocation();
        myLeftClip.setPoint(loc);
        myRightClip.setPoint(loc);
        myBottomClip.setPoint(loc);
        myTopClip.setPoint(loc);
        myRightClip.setNormal(myPosition.getRight().rotate(myPosition.getUp(), Math.PI - myHalfFOVx));
        myLeftClip.setNormal(myPosition.getRight().rotate(myPosition.getUp(), myHalfFOVx));
        myBottomClip.setNormal(myPosition.getUp().rotate(myPosition.getRight(), -myHalfFOVy));
        // Increase the top clip a bit to account for zoomed in with terrain.
        myTopClip.setNormal(myPosition.getUp().rotate(myPosition.getRight(), myHalfFOVy - Math.PI + ourTopClipIncreaseAngle));
    }

    @Override
    protected synchronized void resetInverseModelViewMatrix()
    {
        setInverseModelViewMatrix(new Matrix4d(getModelViewMatrix()).invert());
    }

    /** Reset the model view matrix. */
    protected synchronized void resetModelViewMatrix()
    {
        Matrix4d transform = new Matrix4d();
        // translate so that the viewer is at the origin
        Matrix4d translation = new Matrix4d();
        translation.setTranslation(myPosition.getLocation().multiply(-1.));
        // rotate to put the viewer pointing in the -z direction with the up
        // vector along the +y axis.
        Quaternion quat = Quaternion.lookAt(myPosition.getDir().multiply(-1.), myPosition.getUp());
        Matrix4d rotation = new Matrix4d();
        rotation.setRotationQuaternion(quat);
        // set the transform.
        transform.multLocal(rotation);
        transform.multLocal(translation);
        myModelViewMatrix = transform.toFloatArray();
        setInverseModelViewMatrix(transform.invert());
        clearModelToWindowTransform();
    }

    /** Reset the projection matrix. */
    protected void resetProjectionMatrix()
    {
        float tan = (float)Math.tan(myHalfFOVx);
        synchronized (this)
        {
            resetProjectionMatrixClipped();
            myProjectionMatrix = new float[16];
            myProjectionMatrix[0] = 1f / tan;
            myProjectionMatrix[5] = (float)(myAspectRatio / tan);
            myProjectionMatrix[10] = -1f;
            myProjectionMatrix[11] = -1f;
            myProjectionMatrix[14] = -2f;
            clearModelToWindowTransform();
        }
    }

    /**
     * Clear the clipped projection matrices.
     */
    protected void resetProjectionMatrixClipped()
    {
        synchronized (this)
        {
            myProjectionMatrixClipped = null;
            myProjectionMatrixClippedFarToCenter = null;
        }
    }

    /**
     * Rotate my current direction and return the resultant vector.
     *
     * @param xChange change in the x direction (radians).
     * @param yChange change in the y direction (radians).
     * @return the resultant vector after rotation.
     */
    protected Vector3d rotateDir(double xChange, double yChange)
    {
        return myPosition.getDir().rotate(myPosition.getUp(), xChange).rotate(myPosition.getRight(), yChange);
    }

    /**
     * Set the aspect ratio.
     *
     * @param aspectRatio aspect ratio to set.
     */
    protected synchronized void setAspectRatio(double aspectRatio)
    {
        myAspectRatio = aspectRatio;
        myHalfFOVy = Math.atan(Math.tan(myHalfFOVx) / myAspectRatio);
        resetProjectionMatrix();
    }

    /** Handle a view change. */
    protected final void viewChanged()
    {
        synchronized (this)
        {
            resetProjectionMatrixClipped();
            resetModelViewMatrix();
            resetClipPlanes();
            notifyViewChanged(ViewChangeSupport.ViewChangeType.VIEW_CHANGE);
            if (getPreferences() != null)
            {
                getPreferences().putJAXBObject(POSITION_PREF_KEY, getPosition().clone(), true, this);
            }
        }
    }

    /**
     * Validates the new position against the current position to make sure it
     * doesn't move drastically.
     *
     * @param newPosition The new position to validate.
     * @return True if the new position is valid, false if there was way too
     *         much movement.
     */
    private boolean validateNewPosition(GeographicPosition newPosition)
    {
        boolean isValid = true;
        if (getPosition().getGeoPosition() != null)
        {
            LatLonAlt current = getPosition().getGeoPosition().getLatLonAlt();
            if (isValid && current.getAltM() < 2000)
            {
                isValid = Math.abs(newPosition.getLatLonAlt().getLatD() - current.getLatD()) < ourZoomedInDegreeTolerance
                        && Math.abs(newPosition.getLatLonAlt().getLonD() - current.getLonD()) < ourZoomedInDegreeTolerance;
            }
            if (isValid && (newPosition.getLatLonAlt().getAltM() < 2000 || current.getAltM() < 2000))
            {
                isValid = newPosition.getLatLonAlt().getAltM() - current.getAltM() < 1500;
            }
        }
        return isValid;
    }

    /** Position and orientation information for the 3D viewer. */
    @XmlRootElement
    public static class ViewerPosition3D implements ViewerPosition, Cloneable
    {
        /** Direction the viewer faces. */
        private volatile Vector3d myDir = new Vector3d(-1., 0., 0.);

        /**
         * The geographic position of the viewer.
         */
        private GeographicPosition myGeoPosition;

        /** Viewer current position in model coordinates. */
        private volatile Vector3d myLocation = new Vector3d(25000000, 0, 0);

        /** The viewer current roll orientation. */
        private volatile Vector3d myUp = Vector3d.UNIT_Z;

        /**
         * Construct a default viewer position.
         */
        public ViewerPosition3D()
        {
        }

        /**
         * Constructor.
         *
         * @param location Location of the viewer.
         * @param dir Direction which the viewer faces.
         * @param up Direction in the view which is towards the top of the
         *            screen.
         */
        public ViewerPosition3D(Vector3d location, Vector3d dir, Vector3d up)
        {
            setPosition(location, dir, up);
        }

        @Override
        public ViewerPosition3D clone()
        {
            try
            {
                return (ViewerPosition3D)super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                throw new ExpectedCloneableException(e);
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            ViewerPosition3D other = (ViewerPosition3D)obj;
            return EqualsHelper.equals(myDir, other.myDir, myLocation, other.myLocation, myUp, other.myUp);
        }

        /**
         * Get the axes for the local coordinate system of the viewer position.
         *
         * @return The axes ordered by x-axis, y-axis, z-axis.
         */
        public Vector3d[] getAxes()
        {
            return new Vector3d[] { getRight(), getUp(), getDir() };
        }

        /**
         * Get the dir.
         *
         * @return the dir
         */
        @XmlJavaTypeAdapter(MutableVector3d.Vector3dAdapter.class)
        public Vector3d getDir()
        {
            return myDir;
        }

        /**
         * Gets the geographic position of the viewers position.
         *
         * @return The geographic position of the viewers position, or null if
         *         it hasn't been set.
         */
        public GeographicPosition getGeoPosition()
        {
            return myGeoPosition;
        }

        @Override
        @XmlJavaTypeAdapter(MutableVector3d.Vector3dAdapter.class)
        public Vector3d getLocation()
        {
            return myLocation;
        }

        /**
         * Get the normalized cross product of the viewer direction and up.
         *
         * @return The vector pointing directly right of the viewer.
         */
        public final Vector3d getRight()
        {
            return myDir.cross(myUp).getNormalized();
        }

        /**
         * Get the up.
         *
         * @return the up
         */
        @XmlJavaTypeAdapter(MutableVector3d.Vector3dAdapter.class)
        public Vector3d getUp()
        {
            return myUp;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myDir == null ? 0 : myDir.hashCode());
            result = prime * result + (myLocation == null ? 0 : myLocation.hashCode());
            result = prime * result + (myUp == null ? 0 : myUp.hashCode());
            return result;
        }

        /**
         * Set the dir.
         *
         * @param dir the dir to set
         */
        public void setDir(Vector3d dir)
        {
            myDir = dir;
        }

        /**
         * Set the location.
         *
         * @param location the location to set
         */
        public void setLocation(Vector3d location)
        {
            myLocation = location;
        }

        /**
         * Set the position and orientation.
         *
         * @param location Location of the viewer
         * @param dir Direction the viewer faces.
         * @param up The direction which is towards the top of the screen.
         */
        public final void setPosition(Vector3d location, Vector3d dir, Vector3d up)
        {
            myLocation = location;
            myDir = dir.getNormalized();
            myUp = up.square(myDir).getNormalized();
        }

        /**
         * Set the position and orientation.
         *
         * @param location Location of the viewer
         * @param dir Direction the viewer faces.
         * @param up The direction which is towards the top of the screen.
         * @param geoPos The geographic position of the viewer.
         */
        public final void setPosition(Vector3d location, Vector3d dir, Vector3d up, GeographicPosition geoPos)
        {
            setPosition(location, dir, up);
            myGeoPosition = geoPos;
        }

        /**
         * Set this position to match the given position.
         *
         * @param viewerPosition The position to which to set.
         */
        public void setPosition(ViewerPosition3D viewerPosition)
        {
            setPosition(viewerPosition.getLocation(), viewerPosition.getDir(), viewerPosition.getUp());
        }

        /**
         * Set the up.
         *
         * @param up the up to set
         */
        public void setUp(Vector3d up)
        {
            myUp = up;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(64);
            sb.append("Location : ").append(myLocation);
            sb.append(" | Direction : ").append(myDir);
            sb.append(" | Up : ").append(myUp);
            return sb.toString();
        }

        /**
         * Sets the {@link GeographicPosition} the viewer is supposed to be in.
         * This is used to keep the viewer in the correct location even after
         * terrain updates.
         *
         * @param geoPosition The {@link GeographicPosition} of the viewer.
         */
        protected void setGeoPosition(GeographicPosition geoPosition)
        {
            myGeoPosition = geoPosition;
        }
    }
}
