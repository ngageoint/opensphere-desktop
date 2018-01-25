package io.opensphere.core.viewer;

import java.util.Collection;

import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.RectangularCylinder;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenPosition;

/**
 * A {@code Viewer} is responsible for managing the model-view and projection
 * matrices. It provides facilities for retrieving the matrices, transforming
 * the matrices in response to movement commands, and transforming vectors
 * between the window coordinate system, the clip coordinate system, and the
 * model coordinate system.
 */
public interface Viewer
{
    /**
     * Add an observer.
     *
     * @param obs The observer.
     */
    void addObserver(Observer obs);

    /**
     * Convert from clipping coordinates to window coordinates.
     *
     * @param clipCoords clipping coordinates
     * @return window coordinates.
     */
    Vector3d clipToWindowCoords(Vector3d clipCoords);

    /**
     * Convert from eye coordinates to clipping coordinates.
     *
     * @param eyeCoords Eye coordinates
     * @return clipping coordinates.
     */
    Vector3d eyeToClipCoords(Vector3d eyeCoords);

    /**
     * Get the model-view matrix. Multiplying this matrix by a 4-d model vector
     * will yield a vector in eye coordinates adjusted by the given adjustment
     * matrix.
     *
     * @param adjustment The adjustment matrix (multiplied from the right).
     * @return The model-view matrix in column-major order.
     */
    float[] getAdjustedModelViewMatrix(Matrix4d adjustment);

    /**
     * Get the Model position closest to the viewer.
     *
     * @return Closest model position.
     */
    Vector3d getClosestModelPosition();

    /**
     * Get the intersection of the viewer direction with the model.
     *
     * @return The model intersection (in model coordinates).
     */
    Vector3d getModelIntersection();

    /**
     * Get the model-view matrix. Multiplying this matrix by a 4-d model vector
     * will yield a vector in eye coordinates.
     *
     * @return The model-view matrix in column-major order.
     */
    float[] getModelViewMatrix();

    /**
     * Get the width in pixels of the ellipsoid within the view. This will give
     * the width as if the viewer were looking down the z-axis and assumes that
     * the ellipsoid is circular on the xy-plane.
     *
     * @param ellipsoid The ellipsoid for which the width is desired.
     * @return The width in pixels.
     */
    double getPixelWidth(Ellipsoid ellipsoid);

    /**
     * Get the position of the viewer.
     *
     * @return The viewer position in model coordinates.
     */
    ViewerPosition getPosition();

    /**
     * Get the projection matrix. Multiplying this matrix by a 4-d eye vector
     * will yield a vector in clip coordinates.
     *
     * @return The projection matrix in column-major order.
     */
    float[] getProjectionMatrix();

    /**
     * Get the projection matrix with adjusted near and far clipping planes to
     * help with depth buffer accuracy.
     *
     * @param clipFarToCenter When true the far clipping plane will be moved so
     *            that it passes through the origin. Otherwise the far clipping
     *            plane will be at twice the distance from the viewer to the
     *            origin.
     * @return The projection matrix in column-major order.
     */
    float[] getProjectionMatrixClipped(boolean clipFarToCenter);

    /**
     * Create a rectangular cylinder which is centered on the model positions,
     * is oriented to match the centered viewer position at that position and
     * bounds the model points.
     *
     * @param modelPoints The model points which the rectangular cylinder will
     *            bound.
     * @return The correctly oriented rectangular cylinder.
     */
    RectangularCylinder getRectifyBounds(Collection<? extends Vector3d> modelPoints);

    /**
     * Get the trajectory generator for the desired trajectory type.
     *
     * @param type The trajectory type.
     * @return the trajectory generator.
     */
    TrajectoryGenerator getTrajectoryGenerator(TrajectoryGeneratorType type);

    /**
     * Get the distance between two points in model coordinates.
     *
     * @param ptA The first point in model coordinates.
     * @param ptB The second point in model coordinates.
     * @return The distance in window coordinates.
     */
    double getViewLength(Vector3d ptA, Vector3d ptB);

    /**
     * When using a viewer which is set inside the space of another viewer, this
     * is the offset into the parent viewer's view port.
     *
     * @return the viewOffset
     */
    ScreenPosition getViewOffset();

    /**
     * Get the viewport height in pixels.
     *
     * @return The viewport height.
     */
    int getViewportHeight();

    /**
     * Get the viewport width in pixels.
     *
     * @return The viewport width.
     */
    int getViewportWidth();

    /**
     * Get the size of the view width in the model's native units at the
     * position.
     *
     * @param modelPosition the model coordinates where the view volume width is
     *            desired.
     * @return view volume width.
     */
    double getViewVolumeWidthAt(Vector3d modelPosition);

    /**
     * Get the size of the view width in the model's native units at the
     * position in the view volume where the viewer intersects the model.
     *
     * @return view volume width.
     */
    double getViewVolumeWidthAtIntersection();

    /**
     * Determine if any part of a ellipsoid is currently in view.
     *
     * @param ellipsoid The center of the sphere.
     * @param cullCosine The maximum cosine of the angle between the ellipsoid's
     *            Z axis and the view direction before the ellipsoid is culled
     *            from the view. (When the ellipsoid faces directly at the
     *            viewer, the cosine is -1. When the ellipsoid faces exactly
     *            opposite the viewer, the cosine is 1.)
     * @return If the ellipsoid is in view.
     */
    boolean isInView(Ellipsoid ellipsoid, double cullCosine);

    /**
     * Determine if any part of a sphere is currently in view.
     *
     * @param point The center of the sphere.
     * @param radius The radius of the sphere.
     * @return If the sphere is in view.
     */
    boolean isInView(Vector3d point, double radius);

    /**
     * Convert from model coordinates to eye coordinates.
     *
     * @param modelCoords model coordinates
     * @return eye coordinates.
     */
    Vector3d modelToEyeCoords(Vector3d modelCoords);

    /**
     * Convert from model coordinates to window coordinates.
     *
     * @param modelCoords model coordinates
     * @return window coordinates.
     */
    Vector3d modelToWindowCoords(Vector3d modelCoords);

    /**
     * Remove an observer from this viewer.
     *
     * @param obs The observer.
     */
    void removeObserver(Observer obs);

    /**
     * Reshape the viewport.
     *
     * @param width The width of the viewport in pixels.
     * @param height The height of the viewport in pixels.
     */
    void reshape(int width, int height);

    /**
     * When using a viewer which is set inside the space of another viewer, this
     * is the offset into the parent viewer's view port.
     *
     * @param viewOffset the viewOffset to set
     */
    void setViewOffset(ScreenPosition viewOffset);

    /**
     * Tell whether adjusting the model-view matrix is allowed.
     *
     * @return true when adjusting the model-view matrix is allowed.
     */
    boolean supportsAdjustedModelView();

    /**
     * Transform from window to clip coordinates. This inverts the viewport
     * transformation.
     *
     * @param windowCoords The input window coordinates.
     * @param allowNegative Whether or not to allow negative x,y values
     * @return The clip coordinates.
     */
    Vector3d windowToClipCoords(Vector3d windowCoords, boolean allowNegative);

    /**
     * Transform from window to model coordinates. This inverts the viewport,
     * projection, and model-view transformations.
     *
     * @param windowCoords The input window coordinates.
     * @return The model coordinates.
     */
    Vector3d windowToModelCoords(Vector2i windowCoords);

    /**
     * Interface for an object that will be notified when the view changes.
     */
    @FunctionalInterface
    public interface Observer
    {
        /**
         * Notify this observer that the view has changed.
         *
         * @param type The type of view update.
         */
        void notifyViewChanged(ViewChangeSupport.ViewChangeType type);
    }

    /** The available types of trajectories which can be generated. */
    enum TrajectoryGeneratorType
    {
        /** Trajectory which forms an arc outward from the model surface. */
        ARC,

        /**
         * Trajectory which goes straight from one position to another
         * preserving the viewer zoom.
         */
        FLAT,

        /** Trajectory which rotates without movement. */
        ROTATION,

        ;
    }

    /** Position and orientation information for the viewer. */
    @FunctionalInterface
    public interface ViewerPosition
    {
        /**
         * Get the location.
         *
         * @return the location
         */
        Vector3d getLocation();
    }
}
