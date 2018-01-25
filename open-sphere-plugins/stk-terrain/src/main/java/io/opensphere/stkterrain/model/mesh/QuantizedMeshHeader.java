package io.opensphere.stkterrain.model.mesh;

import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.lang.ToStringHelper;

/** Quantized Mesh Header. */
@Immutable
public class QuantizedMeshHeader implements Serializable
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

//    /**
//     * The X value of the center of the tile in Earth-centered fixed
//     * coordinates.
//     */
//    private final double myCenterX;
//
//    /**
//     * The Y value of the center of the tile in Earth-centered fixed
//     * coordinates.
//     */
//    private final double myCenterY;
//
//    /**
//     * The Z value of the center of the tile in Earth-centered fixed
//     * coordinates.
//     */
//    private final double myCenterZ;

    /** The minimum height. */
    private final float myMinHeight;

    /** The maximum height. */
    private final float myMaxHeight;

//    /** The tile's bounding sphere X coordinate. */
//    private final double myBoundingSphereCenterX;
//
//    /** The tile's bounding sphere Y coordinate. */
//    private final double myBoundingSphereCenterY;
//
//    /** The tile's bounding sphere Z coordinate. */
//    private final double myBoundingSphereCenterZ;
//
//    /** The tile's bounding sphere radius. */
//    private final double myBoundingSphereRadius;
//
//    /** The X value of the horizon occlusion point. */
//    private final double myHorizonOcclusionPointX;
//
//    /** The Y value of the horizon occlusion point. */
//    private final double myHorizonOcclusionPointY;
//
//    /** The Z value of the horizon occlusion point. */
//    private final double myHorizonOcclusionPointZ;

    /**
     * Constructor.
     *
     * @param buffer the byte buffer
     */
    public QuantizedMeshHeader(ByteBuffer buffer)
    {
        /* myCenterX = */ buffer.getDouble();
        /* myCenterY = */ buffer.getDouble();
        /* myCenterZ = */ buffer.getDouble();
        myMinHeight = buffer.getFloat();
        myMaxHeight = buffer.getFloat();
        /* myBoundingSphereCenterX = */ buffer.getDouble();
        /* myBoundingSphereCenterY = */ buffer.getDouble();
        /* myBoundingSphereCenterZ = */ buffer.getDouble();
        /* myBoundingSphereRadius = */ buffer.getDouble();
        /* myHorizonOcclusionPointX = */ buffer.getDouble();
        /* myHorizonOcclusionPointY = */ buffer.getDouble();
        /* myHorizonOcclusionPointZ = */ buffer.getDouble();
    }

//    /**
//     * Gets the centerX.
//     *
//     * @return the centerX
//     */
//    public double getCenterX()
//    {
//        return myCenterX;
//    }
//
//    /**
//     * Gets the centerY.
//     *
//     * @return the centerY
//     */
//    public double getCenterY()
//    {
//        return myCenterY;
//    }
//
//    /**
//     * Gets the centerZ.
//     *
//     * @return the centerZ
//     */
//    public double getCenterZ()
//    {
//        return myCenterZ;
//    }

    /**
     * Gets the minHeight.
     *
     * @return the minHeight
     */
    public float getMinHeight()
    {
        return myMinHeight;
    }

    /**
     * Gets the maxHeight.
     *
     * @return the maxHeight
     */
    public float getMaxHeight()
    {
        return myMaxHeight;
    }

//    /**
//     * Gets the boundingSphereCenterX.
//     *
//     * @return the boundingSphereCenterX
//     */
//    public double getBoundingSphereCenterX()
//    {
//        return myBoundingSphereCenterX;
//    }
//
//    /**
//     * Gets the boundingSphereCenterY.
//     *
//     * @return the boundingSphereCenterY
//     */
//    public double getBoundingSphereCenterY()
//    {
//        return myBoundingSphereCenterY;
//    }
//
//    /**
//     * Gets the boundingSphereCenterZ.
//     *
//     * @return the boundingSphereCenterZ
//     */
//    public double getBoundingSphereCenterZ()
//    {
//        return myBoundingSphereCenterZ;
//    }
//
//    /**
//     * Gets the boundingSphereRadius.
//     *
//     * @return the boundingSphereRadius
//     */
//    public double getBoundingSphereRadius()
//    {
//        return myBoundingSphereRadius;
//    }
//
//    /**
//     * Gets the horizonOcclusionPointX.
//     *
//     * @return the horizonOcclusionPointX
//     */
//    public double getHorizonOcclusionPointX()
//    {
//        return myHorizonOcclusionPointX;
//    }
//
//    /**
//     * Gets the horizonOcclusionPointY.
//     *
//     * @return the horizonOcclusionPointY
//     */
//    public double getHorizonOcclusionPointY()
//    {
//        return myHorizonOcclusionPointY;
//    }
//
//    /**
//     * Gets the horizonOcclusionPointZ.
//     *
//     * @return the horizonOcclusionPointZ
//     */
//    public double getHorizonOcclusionPointZ()
//    {
//        return myHorizonOcclusionPointZ;
//    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
//        helper.add("CenterX", myCenterX);
//        helper.add("CenterY", myCenterY);
//        helper.add("CenterZ", myCenterZ);
        helper.add("MinHeight", myMinHeight);
        helper.add("MaxHeight", myMaxHeight);
//        helper.add("BoundingSphereCenterX", myBoundingSphereCenterX);
//        helper.add("BoundingSphereCenterY", myBoundingSphereCenterY);
//        helper.add("BoundingSphereCenterZ", myBoundingSphereCenterZ);
//        helper.add("BoundingSphereRadius", myBoundingSphereRadius);
//        helper.add("HorizonOcclusionPointX", myHorizonOcclusionPointX);
//        helper.add("HorizonOcclusionPointY", myHorizonOcclusionPointY);
//        helper.add("HorizonOcclusionPointZ", myHorizonOcclusionPointZ);
        return helper.toStringMultiLine(1);
    }
}
