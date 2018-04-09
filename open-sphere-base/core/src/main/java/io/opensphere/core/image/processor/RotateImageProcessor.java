package io.opensphere.core.image.processor;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import com.google.common.base.Objects;

import io.opensphere.core.MapManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Image processor that rotates an image.
 */
public class RotateImageProcessor extends AbstractChainedImageProcessor
{
    /** The rotation in clockwise degrees from straight up. */
    private final double myRotation;

    /**
     * Whether to crop the rotated image so it appears the same size as the
     * normal image.
     */
    private final boolean myCropImage;

    /** The map manager. */
    private final MapManager myMapManager;

    /**
     * The interpolation type.
     *
     * @see java.awt.image.AffineTransformOp
     */
    private int myInterpolationType = AffineTransformOp.TYPE_BILINEAR;

    /**
     * Constructor.
     *
     * @param rotation The rotation in clockwise degrees from straight up
     */
    public RotateImageProcessor(double rotation)
    {
        this(rotation, true, null);
    }

    /**
     * Constructor.
     *
     * @param rotation The rotation in clockwise degrees from straight up
     * @param cropImage Whether to crop the rotated image so it appears the same
     *            size as the normal image
     * @param mapManager The map manager. Supplying this will rotate the image
     *            relative to the viewer heading.
     */
    public RotateImageProcessor(double rotation, boolean cropImage, MapManager mapManager)
    {
        myRotation = rotation;
        myCropImage = cropImage;
        myMapManager = mapManager;
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
        RotateImageProcessor other = (RotateImageProcessor)obj;
        return myInterpolationType == other.myInterpolationType && Utilities.equalsOrBothNaN(myRotation, other.myRotation)
                && Objects.equal(myMapManager, other.myMapManager);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + HashCodeHelper.getHashCode(myInterpolationType);
        result = prime * result + HashCodeHelper.getHashCode(myRotation);
        result = prime * result + HashCodeHelper.getHashCode(myMapManager);
        return result;
    }

    @Override
    public BufferedImage processInternal(BufferedImage image)
    {
        if (myRotation != 0 || myMapManager != null)
        {
            double rotation = myRotation;
            if (myMapManager != null)
            {
                double heading = Math.toDegrees(myMapManager.getStandardViewer().getHeading());
                rotation -= heading;
            }

            double radians = Math.toRadians(rotation);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double newWidth = Math.abs(image.getWidth() * cos) + Math.abs(image.getHeight() * sin);
            double newHeight = Math.abs(image.getHeight() * cos) + Math.abs(image.getWidth() * sin);

            AffineTransform transform = new AffineTransform();
            transform.translate(newWidth / 2., newHeight / 2.);
            transform.rotate(radians);
            transform.translate(-image.getWidth() / 2., -image.getHeight() / 2.);

            AffineTransformOp op = new AffineTransformOp(transform, myInterpolationType);
            BufferedImage processedImage = op.filter(image, null);

            // The rotated image is larger than the original, so crop it so that
            // it appears as the right size
            if (myCropImage)
            {
                processedImage = processedImage.getSubimage((processedImage.getWidth() - image.getWidth()) / 2,
                        (processedImage.getHeight() - image.getHeight()) / 2, image.getWidth(), image.getHeight());
                BufferedImage copyOfImage = new BufferedImage(processedImage.getWidth(), processedImage.getHeight(),
                        BufferedImage.TYPE_4BYTE_ABGR);
                copyOfImage.createGraphics().drawImage(processedImage, 0, 0, null);
                processedImage = copyOfImage;
            }

            return processedImage;
        }
        else
        {
            return image;
        }
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
