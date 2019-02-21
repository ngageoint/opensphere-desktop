package io.opensphere.core.viewer.impl;

import java.util.Arrays;

import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenPosition;

/**
 * A simple static viewer used for geometries that are displayed using
 * {@link ScreenPosition}s.
 */
public class ScreenViewer extends AbstractViewer
{
    /** A 4x4 identity matrix. */
    private static final float[] IDENTITY_MATRIX = { 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f };

    /** The current projection matrix. */
    private float[] myProjectionMatrix;

    /**
     * Constructor.
     * @param displayedViewer True if this user is used to display to the monitor, false if it is used
     * to render somewhere else such as frame buffers/textures.
     */
    public ScreenViewer(boolean displayedViewer)
    {
        super(displayedViewer);
    }

    @Override
    public float[] getAdjustedModelViewMatrix(Matrix4d adjustment)
    {
        throw new UnsupportedOperationException("Adjustments for a screen viewer's model-view matrix is not allowed.");
    }

    @Override
    public Vector3d getModelIntersection()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] getModelViewMatrix()
    {
        return Arrays.copyOf(IDENTITY_MATRIX, IDENTITY_MATRIX.length);
    }

    @Override
    public double getPixelWidth(Ellipsoid ellipsoid)
    {
        return 0;
    }

    @Override
    public ViewerPosition getPosition()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized float[] getProjectionMatrix()
    {
        if (myProjectionMatrix == null)
        {
            throw new IllegalStateException("Cannot get projection matrix before reshape is called.");
        }
        return Arrays.copyOf(myProjectionMatrix, myProjectionMatrix.length);
    }

    @Override
    public double getViewLength(Vector3d ptA, Vector3d ptB)
    {
        return ptA.subtract(ptB).getLength();
    }

    @Override
    public double getViewVolumeWidthAt(Vector3d modelPosition)
    {
        return 0;
    }

    @Override
    public double getViewVolumeWidthAtIntersection()
    {
        return 0;
    }

    @Override
    public boolean isInView(Ellipsoid ellipsoid, double cullCosine)
    {
        return true;
    }

    @Override
    public boolean isInView(Vector3d point, double radius)
    {
        return true;
    }

    @Override
    public boolean supportsAdjustedModelView()
    {
        return false;
    }

    @Override
    protected void doReshape(int width, int height)
    {
        resetProjectionMatrix(width, height);
    }

    @Override
    protected void resetInverseModelViewMatrix()
    {
    }

    /**
     * Reset the projection matrix to account for the viewport.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     */
    private synchronized void resetProjectionMatrix(int width, int height)
    {
        myProjectionMatrix = new float[16];
        myProjectionMatrix[0] = (float)(2. / width);
        myProjectionMatrix[5] = (float)(2. / height);
        myProjectionMatrix[10] = 1f;
        myProjectionMatrix[12] = -1f;
        myProjectionMatrix[13] = -1f;
        myProjectionMatrix[15] = 1f;
    }
}
