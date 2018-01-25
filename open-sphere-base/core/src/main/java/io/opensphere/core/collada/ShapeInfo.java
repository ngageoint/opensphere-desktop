package io.opensphere.core.collada;

import io.opensphere.core.collada.jaxb.Effect;
import io.opensphere.core.collada.jaxb.Image;
import io.opensphere.core.collada.jaxb.Material;

/**
 * Stores various pieces of shape information.
 *
 * @param <T> the type of the shape
 */
public class ShapeInfo<T>
{
    /** The shape. */
    private final T myShape;

    /** The material. */
    private final Material myMaterial;

    /** The effect. */
    private final Effect myEffect;

    /** The image. */
    private final Image myImage;

    /**
     * Constructor.
     *
     * @param shape the shape
     * @param material The material
     * @param effect The effect
     * @param image The image
     */
    public ShapeInfo(T shape, Material material, Effect effect, Image image)
    {
        myShape = shape;
        myMaterial = material;
        myEffect = effect;
        myImage = image;
    }

    /**
     * Gets the shape.
     *
     * @return the shape
     */
    public T getShape()
    {
        return myShape;
    }

    /**
     * Gets the material.
     *
     * @return the material
     */
    public Material getMaterial()
    {
        return myMaterial;
    }

    /**
     * Gets the effect.
     *
     * @return the effect
     */
    public Effect getEffect()
    {
        return myEffect;
    }

    /**
     * Gets the image.
     *
     * @return the image
     */
    public Image getImage()
    {
        return myImage;
    }
}
