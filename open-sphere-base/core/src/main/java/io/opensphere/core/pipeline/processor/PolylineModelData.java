package io.opensphere.core.pipeline.processor;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.Vector3f;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.util.VectorBufferUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.collections.New;

/**
 * The data in model coordinates which is required in order to render a
 * polyline.
 */
public class PolylineModelData implements SizeProvider, AbstractRenderer.ModelData
{
    /** The vector array. */
    private final Vector3f[] myModelPositions;

    /** The optional time spans corresponding to the model positions. */
    private final TimeSpan[] myTimeSpans;

    /**
     * Constructor.
     *
     * @param modelPositions The model positions which represent the line.
     */
    public PolylineModelData(List<Vector3d> modelPositions)
    {
        this(modelPositions, (List<TimeSpan>)null);
    }

    /**
     * Constructor.
     *
     * @param modelPositions The model positions which represent the line.
     * @param timeSpans Optional time spans that the vertices are visible.
     */
    public PolylineModelData(List<Vector3d> modelPositions, List<TimeSpan> timeSpans)
    {
        if (modelPositions.isEmpty())
        {
            throw new IllegalArgumentException("No vertices provided for polyline.");
        }
        myModelPositions = new Vector3f[modelPositions.size() == 1 ? 2 : modelPositions.size()];
        int index = 0;
        for (Vector3d pos : modelPositions)
        {
            myModelPositions[index] = new Vector3f(pos);
            ++index;
        }

        // Some graphics drivers behave badly when given a line with only one
        // vertex, so add a second vertex just in case.
        if (index == 1)
        {
            myModelPositions[1] = myModelPositions[0];
        }

        if (timeSpans == null)
        {
            myTimeSpans = null;
        }
        else
        {
            if (timeSpans.size() == 1)
            {
                myTimeSpans = new TimeSpan[2];
                timeSpans.toArray(myTimeSpans);
                myTimeSpans[1] = myTimeSpans[0];
            }
            else
            {
                myTimeSpans = New.array(timeSpans, TimeSpan.class);
            }
            if (myModelPositions.length != myTimeSpans.length)
            {
                throw new IllegalArgumentException("Model positions must be the same number as timeSpans.");
            }
        }
    }

    /**
     * Constructor.
     *
     * @param modelPositions The model positions which represent the line.
     */
    public PolylineModelData(Vector3f[] modelPositions)
    {
        myModelPositions = Arrays.copyOf(modelPositions, modelPositions.length);
        myTimeSpans = null;
    }

    /**
     * Get the model positions as a float buffer.
     *
     * @return directly allocated float buffer with my array data
     */
    public FloatBuffer getBuffer()
    {
        return VectorBufferUtilities.vec3fToFloatBuffer(myModelPositions);
    }

    /**
     * Get the model coordinates vector array.
     *
     * @return The model positions which represent the line.
     */
    public Vector3f[] getModelPositions()
    {
        return myModelPositions.clone();
    }

    @Override
    public long getSizeBytes()
    {
        long sizeBytes = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES * 2,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + myModelPositions.length * Constants.REFERENCE_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES)
                + myModelPositions.length * Vector3f.SIZE_BYTES;
        if (myTimeSpans != null)
        {
            sizeBytes += MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + myTimeSpans.length * Constants.REFERENCE_SIZE_BYTES,
                    Constants.MEMORY_BLOCK_SIZE_BYTES);
            for (TimeSpan ts : myTimeSpans)
            {
                sizeBytes += ts.getSizeBytes();
            }
        }
        return sizeBytes;
    }

    /**
     * Get the array of time spans corresponding to the model positions. This
     * may be {@code null}.
     *
     * @return The array.
     */
    public TimeSpan[] getTimeSpans()
    {
        return myTimeSpans == null ? null : myTimeSpans.clone();
    }

    /**
     * Get the number of vectors in the array.
     *
     * @return The number of vectors.
     */
    public int getVectorCount()
    {
        return myModelPositions.length;
    }
}
