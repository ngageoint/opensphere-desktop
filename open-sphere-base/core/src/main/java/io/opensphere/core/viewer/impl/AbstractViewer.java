package io.opensphere.core.viewer.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Quaternion;
import io.opensphere.core.math.RectangularCylinder;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.NoScale;
import io.opensphere.core.pipeline.ScaleDetector;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WeakHashSet;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Abstract viewer implementation that provides viewport and observer support.
 */
public abstract class AbstractViewer implements Viewer
{
    /** The inverse of the model view matrix. */
    private Matrix4d myInverseModelViewMatrix;

    /** Derived model to window transform for fast calculations. */
    private Matrix4d myModelToWindowTransform;

    /** The observers of this viewer. */
    private final Set<Observer> myObservers = Collections.synchronizedSet(new WeakHashSet<Observer>());

    /** Generators for viewer trajectories. */
    private final Map<TrajectoryGeneratorType, TrajectoryGenerator> myTrajectoryGenerators = new HashMap<>(
            3);

    /**
     * When using a viewer which is set inside the space of another viewer, give
     * the offset into the parent viewer's view port.
     */
    private ScreenPosition myViewOffset = new ScreenPosition(0, 0);

    /** The viewport height in window coordinates. */
    private int myViewportHeight;

    /** The viewport width in window coordinates. */
    private int myViewportWidth;

    /** Derived window to model transform for fast calculations. */
    private Matrix4d myWindowToModelTransform;

    /**
     * Used to calculate the dpi scaling on the current monitor.
     */
    private final ScaleDetector myDPIScale;

    /**
     * Constructor.
     * @param displayedViewer True if this user is used to display to the monitor, false if it is used
     * to render somewhere else such as frame buffers/textures.
     */
    public AbstractViewer(boolean displayedViewer)
    {
        if(displayedViewer)
        {
            myDPIScale = new ScaleDetector();
        }
        else
        {
            myDPIScale = new NoScale();
        }
    }

    @Override
    public void addObserver(Observer obs)
    {
        myObservers.add(obs);
    }

    /**
     * Add a trajectory generator to my available generators.
     *
     * @param type The type of the generator.
     * @param generator The generator to add.
     */
    public void addTrajectoryGenerator(TrajectoryGeneratorType type, TrajectoryGenerator generator)
    {
        myTrajectoryGenerators.put(type, generator);
    }

    @Override
    public Vector3d clipToWindowCoords(Vector3d clipCoords)
    {
        return new Vector3d((1. + clipCoords.getX()) * getViewportWidth() / 2.,
                (1. - clipCoords.getY()) * getViewportHeight() / 2., 0.);
    }

    @Override
    public Vector3d eyeToClipCoords(Vector3d eyeCoords)
    {
        Matrix4d mat = new Matrix4d(getProjectionMatrix());
        Quaternion quat = new Quaternion(eyeCoords.getX(), eyeCoords.getY(), eyeCoords.getZ(), 1.);
        quat = mat.mult(quat);

        return new Vector3d(quat.getX() / quat.getW(), quat.getY() / quat.getW(), quat.getZ() / quat.getW());
    }

    @Override
    public Vector3d getClosestModelPosition()
    {
        return getModelIntersection();
    }

    @Override
    public float[] getProjectionMatrixClipped(boolean clipFarToCenter)
    {
        return getProjectionMatrix();
    }

    @Override
    public RectangularCylinder getRectifyBounds(Collection<? extends Vector3d> modelPoints)
    {
        return new RectangularCylinder(modelPoints);
    }

    @Override
    public TrajectoryGenerator getTrajectoryGenerator(TrajectoryGeneratorType type)
    {
        return getTrajectoryGenerators().get(type);
    }

    /**
     * Get the trajectoryGenerators.
     *
     * @return the trajectoryGenerators
     */
    public Map<TrajectoryGeneratorType, TrajectoryGenerator> getTrajectoryGenerators()
    {
        return myTrajectoryGenerators;
    }

    @Override
    public synchronized ScreenPosition getViewOffset()
    {
        return myViewOffset;
    }

    @Override
    public synchronized int getViewportHeight()
    {
        return myViewportHeight;
    }

    @Override
    public synchronized int getViewportWidth()
    {
        return myViewportWidth;
    }

    @Override
    public Vector3d modelToEyeCoords(Vector3d modelCoords)
    {
        Matrix4d mat = new Matrix4d(getModelViewMatrix());
        return mat.mult(modelCoords);
    }

    @Override
    public synchronized Vector3d modelToWindowCoords(Vector3d modelCoords)
    {
        Quaternion quat = new Quaternion(modelCoords.getX(), modelCoords.getY(), modelCoords.getZ(), 1.);
        Quaternion mult = getModelToWindowTransform().mult(quat);
        return new Vector3d(mult.getX() / mult.getW(), mult.getY() / mult.getW(), mult.getZ() / mult.getW());
    }

    @Override
    public void removeObserver(Observer obs)
    {
        myObservers.remove(obs);
    }

    @Override
    public synchronized void reshape(int width, int height)
    {
        if (width != myViewportWidth || height != myViewportHeight)
        {
            myViewportWidth = (int)(width / myDPIScale.getScale());
            myViewportHeight = (int)(height / myDPIScale.getScale());
            doReshape(myViewportWidth, myViewportHeight);
            clearModelToWindowTransform();
            notifyViewChanged(ViewChangeSupport.ViewChangeType.WINDOW_RESIZE);
        }
    }

    /**
     * Set the inverseModelViewMatrix.
     *
     * @param inverseModelViewMatrix the inverseModelViewMatrix to set
     */
    public synchronized void setInverseModelViewMatrix(Matrix4d inverseModelViewMatrix)
    {
        myInverseModelViewMatrix = inverseModelViewMatrix;
    }

    @Override
    public synchronized void setViewOffset(ScreenPosition viewOffset)
    {
        myViewOffset = viewOffset;
    }

    @Override
    public Vector3d windowToClipCoords(Vector3d windowCoords, boolean allowNeg)
    {
        double windowX = windowCoords.getX();
        double viewportWidth = getViewportWidth();
        if (!allowNeg && windowX < 0)
        {
            windowX += viewportWidth;
        }
        double windowY = windowCoords.getY();
        double viewportHeight = getViewportHeight();
        if (!allowNeg && windowY < 0)
        {
            windowY += viewportHeight;
        }
        return new Vector3d(windowX / viewportWidth * 2. - 1., 1. - windowY / viewportHeight * 2., 0.);
    }

    @Override
    public Vector3d windowToModelCoords(Vector2i windowCoords)
    {
        Matrix4d mat = getWindowToModelTransform();
        Quaternion quat = new Quaternion(windowCoords.getX(), windowCoords.getY(), -1., 1.);
        quat = mat.mult(quat);

        return new Vector3d(quat.getX(), quat.getY(), quat.getZ());
    }

    /**
     * Clear the cached model to window transform. This must be called whenever
     * any of the other matrices are changed.
     */
    protected synchronized void clearModelToWindowTransform()
    {
        myModelToWindowTransform = null;
        myWindowToModelTransform = null;
    }

    /**
     * Convert clip coords to viewer coords.
     *
     * @param clipCoords clip coords to convert.
     * @return viewer coords.
     */
    protected Vector3d clipToEyeCoords(Vector3d clipCoords)
    {
        return new Matrix4d(getProjectionMatrix()).invert().mult(clipCoords);
    }

    /**
     * Hook method to be implemented by concrete classes to adjust due to a
     * reshape.
     *
     * @param width The new viewport width in pixels.
     * @param height The new viewport height in pixels.
     */
    protected abstract void doReshape(int width, int height);

    /**
     * Convert viewer coords to model coords.
     *
     * @param eyeCoords viewer coords to convert.
     * @return model coords.
     */
    protected Vector3d eyeToModelCoords(Vector3d eyeCoords)
    {
        return getInverseModelViewMatrix().mult(eyeCoords);
    }

    /**
     * Get the inverseModelViewMatrix.
     *
     * @return the inverseModelViewMatrix
     */

    protected synchronized Matrix4d getInverseModelViewMatrix()
    {
        if (myInverseModelViewMatrix == null)
        {
            resetInverseModelViewMatrix();
        }
        return new Matrix4d(myInverseModelViewMatrix);
    }

    /**
     * Get the inverse of the projection matrix.
     *
     * @return The inverse of the projection matrix.
     */
    protected Matrix4d getInverseProjectionMatrix()
    {
        return new Matrix4d(getProjectionMatrix()).invert();
    }

    /**
     * Get a transformation matrix that transforms window coordinates into clip
     * coordinates.
     *
     * @return The matrix.
     */
    protected double[] getInverseViewportTransform()
    {
        double[] mat = new double[16];
        mat[0] = 2. / getViewportWidth();
        mat[5] = 2. / getViewportHeight();
        mat[10] = 1.;
        mat[12] = -1.;
        mat[13] = -1.;
        mat[15] = 1.;
        return mat;
    }

    /**
     * Get a transformation matrix that transforms model coordinates into window
     * coordinates.
     *
     * @return The matrix.
     */
    protected synchronized Matrix4d getModelToWindowTransform()
    {
        if (myModelToWindowTransform == null)
        {
            Matrix4d modelToWindowTransform = new Matrix4d(getViewportTransform());
            modelToWindowTransform.multLocal(new Matrix4d(getProjectionMatrix()));
            modelToWindowTransform.multLocal(new Matrix4d(getModelViewMatrix()));
            myModelToWindowTransform = modelToWindowTransform;
        }
        return myModelToWindowTransform;
    }

    /**
     * Get a transformation matrix that transforms clip coordinates into window
     * coordinates.
     *
     * @return The matrix.
     */
    protected double[] getViewportTransform()
    {
        double[] mat = new double[16];
        mat[0] = getViewportWidth() * .5;
        mat[5] = getViewportHeight() * .5;
        mat[10] = 1.;
        mat[12] = mat[0];
        mat[13] = mat[5];
        mat[15] = 1.;
        return mat;
    }

    /**
     * Get a transformation matrix that transforms model coordinates into window
     * coordinates.
     *
     * @return The matrix.
     */
    protected synchronized Matrix4d getWindowToModelTransform()
    {
        if (myWindowToModelTransform == null)
        {
            Matrix4d modelToWindowTransform = getInverseModelViewMatrix();
            modelToWindowTransform.multLocal(getInverseProjectionMatrix());
            modelToWindowTransform.multLocal(new Matrix4d(getInverseViewportTransform()));
            myWindowToModelTransform = modelToWindowTransform;
        }
        return myWindowToModelTransform;
    }

    /**
     * Notify my observers that the view has changed.
     *
     * @param type The enum type of view update.
     */
    protected void notifyViewChanged(ViewChangeSupport.ViewChangeType type)
    {
        Collection<Observer> observers;
        synchronized (myObservers)
        {
            observers = New.collection(myObservers);
        }
        for (Observer obs : observers)
        {
            obs.notifyViewChanged(type);
        }
    }

    /**
     * Reset the inverse model view matrix.
     */
    protected abstract void resetInverseModelViewMatrix();
}
