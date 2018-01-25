package io.opensphere.core.image.processor;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import io.opensphere.core.util.Utilities;

/**
 * Image processor that scales an image.
 */
public class ScaleImageProcessor extends AbstractChainedImageProcessor
{
    /**
     * The interpolation type.
     *
     * @see java.awt.image.AffineTransformOp
     */
    private int myInterpolationType = AffineTransformOp.TYPE_BILINEAR;

    /** The x scale. */
    private final double myScaleX;

    /** The y scale. */
    private final double myScaleY;

    /**
     * Constructor.
     *
     * @param scaleX The x scale
     * @param scaleY The y scale
     */
    public ScaleImageProcessor(double scaleX, double scaleY)
    {
        myScaleX = scaleX;
        myScaleY = scaleY;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        ScaleImageProcessor other = (ScaleImageProcessor)obj;
        if (myInterpolationType != other.myInterpolationType)
        {
            return false;
        }
        return Utilities.equalsOrBothNaN(myScaleX, other.myScaleX) && Utilities.equalsOrBothNaN(myScaleY, other.myScaleY);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myInterpolationType;
        long temp;
        temp = Double.doubleToLongBits(myScaleX);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myScaleY);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public BufferedImage processInternal(BufferedImage image)
    {
        AffineTransform transform = new AffineTransform();
        transform.scale(myScaleX, myScaleY);

        AffineTransformOp op = new AffineTransformOp(transform, myInterpolationType);
        return op.filter(image, null);
    }

    /**
     * Setter for interpolationType.
     *
     * @param interpolationType the interpolationType
     * @see java.awt.image.AffineTransformOp
     */
    public void setInterpolationType(int interpolationType)
    {
        myInterpolationType = interpolationType;
    }
}
