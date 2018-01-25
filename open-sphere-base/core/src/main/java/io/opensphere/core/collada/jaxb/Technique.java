package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA technique.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Technique
{
    /** The constant technique. */
    @XmlElement(name = "constant")
    private TechniqueBase myConstant;

    /** The lambert technique. */
    @XmlElement(name = "lambert")
    private TechniqueBase myLambert;

    /** The phong technique. */
    @XmlElement(name = "phong")
    private TechniqueBase myPhong;

    /**
     * Gets the constant.
     *
     * @return the constant
     */
    public TechniqueBase getConstant()
    {
        return myConstant;
    }

    /**
     * Gets the lambert.
     *
     * @return the lambert
     */
    public TechniqueBase getLambert()
    {
        return myLambert;
    }

    /**
     * Gets the phong.
     *
     * @return the phong
     */
    public TechniqueBase getPhong()
    {
        return myPhong;
    }

    /**
     * Helper method to get the color.
     *
     * @return the color, or null
     */
    public String getColor()
    {
        String color = null;
        if (myLambert != null)
        {
            color = getColor(myLambert);
        }
        else if (myPhong != null)
        {
            color = getColor(myPhong);
        }
        else if (myConstant != null)
        {
            color = getColor(myConstant);
        }
        return color;
    }

    /**
     * Gets the color from the technique.
     *
     * @param technique the technique
     * @return the color, or null
     */
    private static String getColor(TechniqueBase technique)
    {
        String color = null;
        if (technique.getDiffuse() != null && technique.getDiffuse().getColor() != null)
        {
            color = technique.getDiffuse().getColor();
        }
        else if (technique.getSpecular() != null && technique.getSpecular().getColor() != null)
        {
            color = technique.getSpecular().getColor();
        }
        else if (technique.getTransparent() != null && technique.getTransparent().getColor() != null)
        {
            color = technique.getTransparent().getColor();
        }
        return color;
    }
}
