package io.opensphere.core.viewer.impl;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.MutableVector3d;
import io.opensphere.core.math.RectangularCylinder;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.projection.AbstractGeographicProjection;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Viewer that only allows movement in the x-y plane and uses scaling for zoom.
 */
@SuppressFBWarnings(value = "UG_SYNC_SET_UNSYNC_GET", justification = "The gets are against volatile fields, so they do not need to be synchronized.")
@SuppressWarnings("PMD.GodClass")
public class Viewer2D extends AbstractDynamicViewer
{
    /** A preference key for the viewer position. */
    public static final String POSITION_PREF_KEY = "Viewer2D.position";

    /**
     * Fraction of the screen to use when determining if a geometry is on
     * screen.
     */
    private static final double EDGE_BUFFER = 1.2;

    /**
     * The cached borders of the screen used for determining if a geometry is on
     * screen. This border is smaller than the actual border so that objects
     * just off-screen will be ready to be drawn.
     */
    private final double[] myEdges = new double[4];

    /**
     * This provider will provide terrain specific to the projection and should
     * match the rendered terrain even when more accurate values are available.
     */
    private MapContext<DynamicViewer> myMapContext;

    /** The upper zoom limit. */
    private volatile double myMaxZoom;

    /** The lower zoom limit. */
    private volatile double myMinZoom;

    /** The height of the model. */
    private volatile double myModelHeight;

    /** The width of the model. */
    private volatile double myModelWidth;

    /** Position of the viewer. */
    private final ViewerPosition2D myPosition = new ViewerPosition2D();

    /** Listener for preference changes. */
    private final PreferenceChangeListener myPreferenceChangeListener = new PreferenceChangeListener()
    {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            if (evt.getSource() != Viewer2D.this)
            {
                setPosition(getPreferences().getJAXBObject(ViewerPosition3D.class, POSITION_PREF_KEY, new ViewerPosition3D()));
            }
        }
    };

    /**
     * The current stretch factor. This is the ratio of the model aspect ratio
     * to the viewport aspect ratio.
     */
    private volatile double myStretchFactor;

    /**
     * Construct a viewer.
     *
     * @param builder An object that describes how the viewer should be created.
     */
    public Viewer2D(AbstractDynamicViewer.Builder builder)
    {
        super(builder);
        addTrajectoryGenerator(TrajectoryGeneratorType.ARC, new FlatTrajectoryGenerator2D(this));
        addTrajectoryGenerator(TrajectoryGeneratorType.FLAT, new FlatTrajectoryGenerator2D(this));
        reset(builder);
        if (getPreferences() != null)
        {
            getPreferences().addPreferenceChangeListener(POSITION_PREF_KEY, myPreferenceChangeListener);
        }
    }

    @Override
    public float[] getAdjustedModelViewMatrix(Matrix4d adjustment)
    {
        throw new UnsupportedOperationException("Adjustments for a 2D viewer's model-view matrix is not allowed.");
    }

    @Override
    public double getAltitude()
    {
        Projection proj = getMapContext().getProjection(Viewer2D.class);
        // This calculation only applies when the projection is geographic
        if (proj instanceof AbstractGeographicProjection)
        {
            double widthStretch = myStretchFactor > 1. ? myStretchFactor : 1.;
            double metersPerModel = WGS84EarthConstants.CIRCUMFERENCE_MEAN_M / proj.getModelWidth();
            double viewWidthMeters = 0.5 * (metersPerModel * proj.getModelWidth() / getScale() / widthStretch);
            return viewWidthMeters / Math.tan(0.5 * MathUtil.QUARTER_PI);
        }
        return 0.;
    }

    @Override
    public KMLCompatibleCamera getCamera(ViewerPosition position)
    {
        if (!(position instanceof ViewerPosition2D))
        {
            throw new UnsupportedOperationException("2D viewer cannot create a camera for a non-2D position.");
        }
        Projection proj = getMapContext().getProjection(Viewer2D.class);
        if (!(proj instanceof AbstractGeographicProjection))
        {
            throw new UnsupportedOperationException(
                    "Cannot create viewer postion from geographic position for non-geographic projection.");
        }
        ViewerPosition2D position2D = (ViewerPosition2D)position;
        double alt = getAltitude();
        GeographicPosition location = proj.convertToPosition(position2D.getLocation(), ReferenceLevel.ELLIPSOID);
        LatLonAlt altAdjustLocation = LatLonAlt.createFromDegreesMeters(location.getLatLonAlt().getLatD(),
                location.getLatLonAlt().getLonD(), alt, ReferenceLevel.ELLIPSOID);
        return new KMLCompatibleCamera(altAdjustLocation, 0., 0., 0.);
    }

    @Override
    public ViewerPosition2D getCenteredView(Vector3d position)
    {
        double xSpan = 2. * (myModelWidth * .5 - Math.abs(position.getX()));
        double xScale = myModelWidth / xSpan;
        double ySpan = 2. * (myModelHeight * .5 - Math.abs(position.getY()));
        double yScale = myModelHeight / ySpan;
        double scale = Math.max(getScale(), Math.min(xScale, yScale));
        return new ViewerPosition2D(position, scale);
    }

    @Override
    public ViewerPosition getCenteredView(Vector3d position, Vector3d centroidHit)
    {
        return getCenteredView(position);
    }

    @Override
    public double getHeading()
    {
        return 0;
    }

    @Override
    public MapContext<DynamicViewer> getMapContext()
    {
        return myMapContext;
    }

    /**
     * Get the maxZoom.
     *
     * @return the maxZoom
     */
    public double getMaxZoom()
    {
        return myMaxZoom;
    }

    /**
     * Get the minZoom.
     *
     * @return the minZoom
     */
    public double getMinZoom()
    {
        return myMinZoom;
    }

    /**
     * Get the modelHeight.
     *
     * @return the modelHeight
     */
    public double getModelHeight()
    {
        return myModelHeight;
    }

    @Override
    public Vector3d getModelIntersection()
    {
        return getPosition().getLocation();
    }

    @Override
    public synchronized float[] getModelViewMatrix()
    {
        float[] matrix = new float[16];
        matrix[0] = (float)getScale();
        matrix[5] = (float)getScale();
        matrix[10] = (float)getScale();
        matrix[12] = -(float)myPosition.getLocation().getX() * (float)getScale();
        matrix[13] = -(float)myPosition.getLocation().getY() * (float)getScale();
        matrix[15] = 1f;
        return matrix;
    }

    /**
     * Get the modelWidth.
     *
     * @return the modelWidth
     */
    public double getModelWidth()
    {
        return myModelWidth;
    }

    @Override
    public double getPitch()
    {
        return 0;
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
        double pctOfModel = modelSize / myModelWidth;
        double pctOfView = pctOfModel * myPosition.getScale();
        return pctOfView * getViewportWidth();
    }

    @Override
    public ViewerPosition2D getPosition()
    {
        return myPosition.clone();
    }

    @Override
    public float[] getProjectionMatrix()
    {
        float[] orthoMatrix = new float[16];
        orthoMatrix[0] = (float)(2. / myModelWidth * (myStretchFactor > 1. ? myStretchFactor : 1.));
        orthoMatrix[5] = (float)(2. / myModelHeight / (myStretchFactor < 1. ? myStretchFactor : 1.));
        orthoMatrix[10] = 1f;
        orthoMatrix[15] = 1f;
        return orthoMatrix;
    }

    @Override
    public ViewerPosition2D getRightedView(Vector3d location)
    {
        ViewerPosition2D pos = new ViewerPosition2D();
        pos.setLocation(location);
        pos.setScale(getScale());
        return pos;
    }

    @Override
    public ViewerPosition2D getRightedView(ViewerPosition position)
    {
        if (position instanceof ViewerPosition2D)
        {
            ViewerPosition2D pos = new ViewerPosition2D();
            pos.setLocation(position.getLocation());
            pos.setScale(((ViewerPosition2D)position).getScale());
            return pos;
        }
        return null;
    }

    @Override
    public ViewerPosition2D getRightedViewAtIntersection(ViewerPosition location)
    {
        ViewerPosition2D pos = new ViewerPosition2D();
        pos.setPosition((ViewerPosition2D)location);
        return pos;
    }

    /**
     * Get the scale. This is the magnification factor of the map, with 1:1
     * being when the whole map is on-screen in its short dimension.
     *
     * @return the scale
     */
    public double getScale()
    {
        return myPosition.getScale();
    }

    /**
     * Get the stretchFactor.
     *
     * @return the stretchFactor
     */
    public double getStretchFactor()
    {
        return myStretchFactor;
    }

    @Override
    public Vector3d getTerrainIntersection()
    {
        return windowToModelCoords(new Vector2i(getViewportWidth() / 2, getViewportHeight() / 2));
    }

    @Override
    public Vector3d getTerrainIntersection(Vector2i screenLoc)
    {
        Vector2i correctedLoc = new Vector2i(screenLoc.getX(), getViewportHeight() - screenLoc.getY());
        return windowToModelCoords(correctedLoc);
    }

    @Override
    public ViewerPosition2D getViewerPosition(KMLCompatibleCamera camera)
    {
        Projection proj = getMapContext().getProjection(Viewer2D.class);
        if (!(proj instanceof AbstractGeographicProjection))
        {
            throw new UnsupportedOperationException(
                    "Cannot create viewer postion from geographic position for non-geographic projection.");
        }
        LatLonAlt location = camera.getLocation();
        double widthStretch = myStretchFactor > 1. ? myStretchFactor : 1.;
        double viewWidthMeters = location.getAltM() * Math.tan(0.5 * MathUtil.QUARTER_PI);
        double metersPerModel = WGS84EarthConstants.CIRCUMFERENCE_MEAN_M / proj.getModelWidth();
        double scale = 0.5 * (metersPerModel * proj.getModelWidth() / viewWidthMeters / widthStretch);
        Vector3d modelLocation = proj.convertToModel(new GeographicPosition(location), Vector3d.ORIGIN);
        return new ViewerPosition2D(modelLocation, scale);
    }

    @Override
    public double getViewLength(Vector3d ptA, Vector3d ptB)
    {
        double dx = (ptA.getX() - ptB.getX()) * getScale() / myModelWidth * (myStretchFactor > 1. ? myStretchFactor : 1.)
                * getViewportWidth();
        double dy = (ptA.getY() - ptB.getY()) * getScale() / myModelHeight * (myStretchFactor < 1. ? myStretchFactor : 1.)
                * getViewportHeight();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public double getViewVolumeWidthAt(Vector3d modelPosition)
    {
//        // TODO This is incorrect. Need to somehow
//        // determine the relationship of scale to what
//        // the scaleFactor should be.
//        final double lowerLimit = 1.;
//        double scale = getScale() >= lowerLimit ? getScale() : lowerLimit;
//        double scaleFactor = 20000 * 1 / Math.abs(scale);
//
//        return scaleFactor;
        return myModelWidth / myPosition.getScale();
    }

    @Override
    public double getViewVolumeWidthAtIntersection()
    {
        return getViewVolumeWidthAt(null);
    }

    @Override
    public ViewerPosition2D getZoomToFitView(RectangularCylinder bounds)
    {
        double xSpan = bounds.getSpan(new Vector3d(1., 0., 0));
        double xScale = myModelWidth / xSpan;
        double ySpan = bounds.getSpan(new Vector3d(0., 1., 0));
        double yScale = myModelHeight / ySpan;
        double scale = Math.min(xScale, yScale);
        return new ViewerPosition2D(bounds.getCenter(), scale);
    }

    @Override
    public ViewerPosition getZoomToFitView(RectangularCylinder bounds, Vector3d centroidHit)
    {
        return getZoomToFitView(bounds);
    }

    @Override
    public boolean isInView(Ellipsoid ellipsoid, double cullCosine)
    {
        if (ellipsoid instanceof Sphere)
        {
            return isInView(ellipsoid.getCenter(), ((Sphere)ellipsoid).getRadius());
        }
        return false;
    }

    @Override
    public boolean isInView(Vector3d point, double radius)
    {
        double x = point.getX();
        double y = point.getY();
        return x + radius > myEdges[0] && x - radius < myEdges[1] && y + radius > myEdges[2] && y - radius < myEdges[3];
    }

    @Override
    public final void reset(Builder builder)
    {
        super.reset(builder);
        myMaxZoom = builder.getMaxZoom();
        myMinZoom = builder.getMinZoom();
        myModelWidth = builder.getModelWidth();
        myModelHeight = builder.getModelHeight();
    }

    @Override
    public void resetView()
    {
        // No applicable action for this viewer type.
    }

    @Override
    public void setMapContext(MapContext<DynamicViewer> context)
    {
        myMapContext = context;
    }

    @Override
    public synchronized void setPosition(ViewerPosition viewerPosition)
    {
        if (viewerPosition instanceof ViewerPosition2D)
        {
            myPosition.setPosition((ViewerPosition2D)viewerPosition);
            double newScale = Math.min(Math.max(((ViewerPosition2D)viewerPosition).getScale(), myMinZoom), myMaxZoom);
            myPosition.setScale(newScale);
            checkCenterPoint();
            clearModelToWindowTransform();
            viewChanged();
        }
        else
        {
            throw new IllegalArgumentException(
                    "Non compatible position type for Viewer2D : " + viewerPosition.getClass().getName());
        }
    }

    @Override
    public void startAnimationToPreferredPosition()
    {
        final ViewerPosition2D pos = getPreferences().getJAXBObject(ViewerPosition2D.class, POSITION_PREF_KEY, null);
        if (pos != null)
        {
            setPosition(pos);
        }
    }

    @Override
    public boolean supportsAdjustedModelView()
    {
        return false;
    }

    @Override
    protected void doReshape(int width, int height)
    {
        determineStretchFactor(width, height);
        checkCenterPoint();
    }

    @Override
    protected void resetInverseModelViewMatrix()
    {
        setInverseModelViewMatrix(new Matrix4d(getModelViewMatrix()).invert());
    }

    /**
     * Set the scale. This verifies that the scale is in range and checks the
     * center point and notifies observers that the view has changed.
     *
     * @param in The new scale.
     */
    protected synchronized void setScale(double in)
    {
        double newScale = Math.min(Math.max(in, myMinZoom), myMaxZoom);
        if (Math.abs(newScale - getScale()) > MathUtil.DBL_EPSILON)
        {
            myPosition.setScale(newScale);
            checkCenterPoint();
            clearModelToWindowTransform();
            if (getPreferences() != null)
            {
                getPreferences().putJAXBObject(POSITION_PREF_KEY, getPosition().clone(), true, this);
            }
            viewChanged();
        }
    }

    /** Handle a view change. */
    protected final void viewChanged()
    {
        synchronized (this)
        {
            notifyViewChanged(ViewChangeSupport.ViewChangeType.VIEW_CHANGE);
            if (getPreferences() != null)
            {
                getPreferences().putJAXBObject(POSITION_PREF_KEY, getPosition().clone(), true, this);
            }
        }
    }

    /**
     * Ensure that the center point is in the valid range. If not, adjust it.
     */
    private synchronized void checkCenterPoint()
    {
        Vector2d dim = getScreenDimensionsInModelCoords();
        double minX = (dim.getX() - myModelWidth) * 0.5;
        double minY = (dim.getY() - myModelHeight) * 0.5;
        double maxX = (myModelWidth - dim.getX()) * 0.5;
        double maxY = (myModelHeight - dim.getY()) * 0.5;
        Vector3d loc = Vector3d.clamp(myPosition.getLocation(), minX, maxX, minY, maxY, 0., 0.);
        myPosition.setLocation(loc);
        getEdges();
        resetInverseModelViewMatrix();
    }

    /**
     * Determine the stretch factor, which is the ratio of the model aspect
     * ratio to the viewport aspect ratio.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     */
    private void determineStretchFactor(int width, int height)
    {
        double viewportAspect = (double)width / height;
        double modelAspect = myModelWidth / myModelHeight;
        myStretchFactor = modelAspect / viewportAspect;
    }

    /**
     * Get the screen borders to be used to determine if an object is in view.
     */
    private synchronized void getEdges()
    {
        Vector2d dim = getScreenDimensionsInModelCoords().multiply(EDGE_BUFFER * 0.5);
        double minX = myPosition.getLocation().getX() - dim.getX();
        double maxX = myPosition.getLocation().getX() + dim.getX();
        double minY = myPosition.getLocation().getY() - dim.getY();
        double maxY = myPosition.getLocation().getY() + dim.getY();
        myEdges[0] = minX;
        myEdges[1] = maxX;
        myEdges[2] = minY;
        myEdges[3] = maxY;
    }

    /**
     * Get the screen dimensions in model coordinates.
     *
     * @return The screen dimensions.
     */
    private Vector2d getScreenDimensionsInModelCoords()
    {
        double xFactor = myStretchFactor > 1. ? myStretchFactor : 1.;
        double yFactor = myStretchFactor < 1. ? myStretchFactor : 1.;
        double screenWidthInModelCoords = myModelWidth / xFactor / getScale();
        double screenHeightInModelCoords = myModelHeight * yFactor / getScale();
        return new Vector2d(screenWidthInModelCoords, screenHeightInModelCoords);
    }

    /** Position and orientation information for the 2D viewer. */
    @XmlRootElement
    public static class ViewerPosition2D implements Cloneable, ViewerPosition
    {
        /** The center of the window in model coordinates. */
        private volatile Vector3d myLocation = Vector3d.ORIGIN;

        /** Zoom scale. */
        private volatile double myScale = 1.;

        /** Construct a default position. */
        public ViewerPosition2D()
        {
        }

        /**
         * Constructor.
         *
         * @param location Location of the viewer.
         * @param scale View scale.
         */
        public ViewerPosition2D(Vector3d location, double scale)
        {
            myScale = scale;
            myLocation = location;
        }

        @Override
        public ViewerPosition2D clone()
        {
            try
            {
                return (ViewerPosition2D)super.clone();
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
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            ViewerPosition2D other = (ViewerPosition2D)obj;
            return EqualsHelper.equals(myLocation, other.getLocation()) && MathUtil.isZero(myScale - other.getScale());
        }

        @Override
        @XmlJavaTypeAdapter(MutableVector3d.Vector3dAdapter.class)
        public Vector3d getLocation()
        {
            return myLocation;
        }

        /**
         * Get the scale. This is the magnification factor of the map, with 1:1
         * being when the whole map is on-screen in its short dimension.
         *
         * @return the scale
         */
        public double getScale()
        {
            return myScale;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myLocation == null ? 0 : myLocation.hashCode());
            result = prime * result + Float.floatToIntBits((float)myScale);
            return result;
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
         * Set this position to match the given position.
         *
         * @param viewerPosition The position to which to set.
         */
        public void setPosition(ViewerPosition2D viewerPosition)
        {
            myScale = viewerPosition.getScale();
            setLocation(viewerPosition.getLocation());
        }

        /**
         * Set the scale.
         *
         * @param scale the scale to set
         */
        public void setScale(double scale)
        {
            myScale = scale;
        }
    }
}
