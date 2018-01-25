package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/**
 * Blending configuration for specifying how to mix colors when rendering a
 * geometry with previously rendered geometries or the frame buffer background
 * color. The source color is the color being blended, the Destination color is
 * the color already in the frame buffer.
 */
public class BlendingConfigGL
{
    /**
     * The default lighting. This may be used so that when lighting is the same
     * for a set of geometries, they can easily share an instance.
     */
    private static final BlendingConfigGL ourDefaultBlending = new BlendingConfigGL(BlendFactor.SRC_ALPHA,
            BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFunction.FUNC_ADD, null);

    /**
     * Blending function to use when blending the alpha component of the color.
     */
    private final BlendFunction myAlphaBlendFuction;

    /**
     * Blending factor to use when blending the alpha component of the
     * destination color.
     */
    private final BlendFactor myAlphaDstBlendFactor;

    /**
     * Blending factor to use when blending the alpha component of the source
     * color.
     */
    private final BlendFactor myAlphaSrcBlendFactor;

    /** The alpha blending color component for constant blending. */
    private final float myBlendColorAlpha;

    /** The blue blending color component for constant blending. */
    private final float myBlendColorBlue;

    /** The green blending color component for constant blending. */
    private final float myBlendColorGreen;

    /** The red blending color component for constant blending. */
    private final float myBlendColorRed;

    /**
     * Blending function to use when blending the rgb components of the color.
     */
    private final BlendFunction myColorBlendFuction;

    /**
     * Blending factor to use when blending the rgb components of the
     * destination color.
     */
    private final BlendFactor myColorDstBlendFactor;

    /**
     * Blending factor to use when blending the rgb components of the source
     * color.
     */
    private final BlendFactor myColorSrcBlendFactor;

    /**
     * Get the default blend settings.
     *
     * @return The default blend settings.
     */
    public static BlendingConfigGL getDefaultBlending()
    {
        return ourDefaultBlending;
    }

    /**
     * Constructor for separate rgb and alpha blending.
     *
     * @param colorSrcFactor The source blending factor for the rgb components.
     * @param colorDstFactor The destination blending factor for the rgb
     *            components.
     * @param colorFunction The blending function for the rgb components.
     * @param alphaSrcFactor The source blending factor for the alpha
     *            components.
     * @param alphaDstFactor The destination blending factor for the alpha
     *            components.
     * @param alphaFunction The blending function for the alpha components.
     * @param blendColor The blend color (only used for constant color
     *            blending).
     */
    public BlendingConfigGL(BlendFactor colorSrcFactor, BlendFactor colorDstFactor, BlendFunction colorFunction,
            BlendFactor alphaSrcFactor, BlendFactor alphaDstFactor, BlendFunction alphaFunction, Color blendColor)
    {
        myColorSrcBlendFactor = colorSrcFactor;
        myAlphaSrcBlendFactor = alphaSrcFactor;

        myColorDstBlendFactor = colorDstFactor;
        myAlphaDstBlendFactor = alphaDstFactor;

        myColorBlendFuction = colorFunction;
        myAlphaBlendFuction = alphaFunction;

        if (blendColor == null)
        {
            myBlendColorRed = 0;
            myBlendColorGreen = 0;
            myBlendColorBlue = 0;
            myBlendColorAlpha = 0;
        }
        else
        {
            final float colorScale = 255f;
            myBlendColorRed = blendColor.getRed() / colorScale;
            myBlendColorGreen = blendColor.getGreen() / colorScale;
            myBlendColorBlue = blendColor.getBlue() / colorScale;
            myBlendColorAlpha = blendColor.getAlpha() / colorScale;
        }
    }

    /**
     * Constructor.
     *
     * @param srcFactor The source blending factor.
     * @param dstFactor The destination blending factor.
     * @param function The blending function.
     * @param blendColor The blend color (only used for constant color
     *            blending).
     */
    public BlendingConfigGL(BlendFactor srcFactor, BlendFactor dstFactor, BlendFunction function, Color blendColor)
    {
        myColorSrcBlendFactor = srcFactor;
        myAlphaSrcBlendFactor = srcFactor;

        myColorDstBlendFactor = dstFactor;
        myAlphaDstBlendFactor = dstFactor;

        myColorBlendFuction = function;
        myAlphaBlendFuction = function;

        if (blendColor == null)
        {
            myBlendColorRed = 0;
            myBlendColorGreen = 0;
            myBlendColorBlue = 0;
            myBlendColorAlpha = 0;
        }
        else
        {
            final float colorScale = 255f;
            myBlendColorRed = blendColor.getRed() / colorScale;
            myBlendColorGreen = blendColor.getGreen() / colorScale;
            myBlendColorBlue = blendColor.getBlue() / colorScale;
            myBlendColorAlpha = blendColor.getAlpha() / colorScale;
        }
    }

    /**
     * Get the alphaBlendFuction.
     *
     * @return the alphaBlendFuction
     */
    public BlendFunction getAlphaBlendFuction()
    {
        return myAlphaBlendFuction;
    }

    /**
     * Get the alphaDstBlendFactor.
     *
     * @return the alphaDstBlendFactor
     */
    public BlendFactor getAlphaDstBlendFactor()
    {
        return myAlphaDstBlendFactor;
    }

    /**
     * Get the alphaSrcBlendFactor.
     *
     * @return the alphaSrcBlendFactor
     */
    public BlendFactor getAlphaSrcBlendFactor()
    {
        return myAlphaSrcBlendFactor;
    }

    /**
     * Get the blendColorAlpha.
     *
     * @return the blendColorAlpha
     */
    public float getBlendColorAlpha()
    {
        return myBlendColorAlpha;
    }

    /**
     * Get the blendColorBlue.
     *
     * @return the blendColorBlue
     */
    public float getBlendColorBlue()
    {
        return myBlendColorBlue;
    }

    /**
     * Get the blendColorGreen.
     *
     * @return the blendColorGreen
     */
    public float getBlendColorGreen()
    {
        return myBlendColorGreen;
    }

    /**
     * Get the blendColorRed.
     *
     * @return the blendColorRed
     */
    public float getBlendColorRed()
    {
        return myBlendColorRed;
    }

    /**
     * Get the colorBlendFuction.
     *
     * @return the colorBlendFuction
     */
    public BlendFunction getColorBlendFuction()
    {
        return myColorBlendFuction;
    }

    /**
     * Get the colorDstBlendFactor.
     *
     * @return the colorDstBlendFactor
     */
    public BlendFactor getColorDstBlendFactor()
    {
        return myColorDstBlendFactor;
    }

    /**
     * Get the colorSrcBlendFactor.
     *
     * @return the colorSrcBlendFactor
     */
    public BlendFactor getColorSrcBlendFactor()
    {
        return myColorSrcBlendFactor;
    }

    /**
     * Blending factors. The notation below uses the subscripts "c" for the
     * constant color, "s" for the source color and "d" for the destination
     * color. For example "Gs" would be the green component of the source color.
     */
    public enum BlendFactor
    {
        /** Color (Ac, Ac, Ac) :: Alpha (Ac). */
        CONSTANT_ALPHA,

        /** Color (Rc, Gc, Bc) :: Alpha (Ac). */
        CONSTANT_COLOR,

        /** Color (Ad, Ad, Ad) :: Alpha (Ad). */
        DST_ALPHA,

        /** Color (Rd, Gd, Bd) :: Alpha (Ad). */
        DST_COLOR,

        /** Color (1, 1, 1) :: Alpha (1). */
        ONE,

        /** Color (1, 1, 1) - (Ac, Ac, Ac) :: Alpha (1 - Ac). */
        ONE_MINUS_CONSTANT_ALPHA,

        /** Color (1, 1, 1) - (Rc, Gc, Bc) :: Alpha (1 - Ac). */
        ONE_MINUS_CONSTANT_COLOR,

        /** Color (1, 1, 1) - (Ad, Ad, Ad) :: Alpha (1 - Ad). */
        ONE_MINUS_DST_ALPHA,

        /** Color (1, 1, 1) - (Rd, Gd, Bd) :: Alpha (1 - Ad). */
        ONE_MINUS_DST_COLOR,

        /** Color (1, 1, 1) - (As, As, As) :: Alpha (1 - As). */
        ONE_MINUS_SRC_ALPHA,

        /** Color (1, 1, 1) - (Rs, Gs, Bs) :: Alpha (1 - As). */
        ONE_MINUS_SRC_COLOR,

        /** Color (As, As, As) :: Alpha (As). */
        SRC_ALPHA,

        /** Color (f, f, f) :: Alpha (1). Where f is min(As, 1 - Ad). */
        SRC_ALPHA_SATURATE,

        /** Color (Rs, Gs, Bs) :: Alpha (As). */
        SRC_COLOR,

        /** Color (0, 0, 0) :: Alpha (0). */
        ZERO,

        ;
    }

    /**
     * Blending functions. Cs is the source color, Cd is the destination color,
     * S is the source blending factor and D is the destination blending factor.
     */
    public enum BlendFunction
    {
        /** Blend function Final Color = (Cs * S) + (Cd * D). */
        FUNC_ADD,

        /** Blend function Final Color = (Cd * D) - (Cs * S). */
        FUNC_REVERSE_SUBTRACT,

        /** Blend function Final Color = (Cs * S) - (Cd * D). */
        FUNC_SUBTRACT,

        /** Blend function Final Color = max(Cs, Cd). */
        MAX,

        /** Blend function Final Color = min(Cs, Cd). */
        MIN,

        ;
    }
}
