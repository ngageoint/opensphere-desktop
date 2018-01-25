package io.opensphere.mantle.data.geom.style.dialog;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;

/**
 * The Class StyleNodeUserObject.
 */
public class StyleNodeUserObject
{
    /** The Base mgs class. */
    private final Class<? extends VisualizationSupport> myBaseMGSClass;

    /** The Default style instance. */
    private VisualizationStyle myDefaultStyleInstance;

    /** The Style class. */
    private final Class<? extends VisualizationStyle> myStyleClass;

    /**
     * Instantiates a new style node user object.
     *
     * @param styleClass the style class
     * @param defaultInstance the default instance
     * @param baseMGSClass the base mgs class
     */
    public StyleNodeUserObject(Class<? extends VisualizationStyle> styleClass, VisualizationStyle defaultInstance,
            Class<? extends VisualizationSupport> baseMGSClass)
    {
        myBaseMGSClass = baseMGSClass;
        myStyleClass = styleClass;
        myDefaultStyleInstance = defaultInstance;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        StyleNodeUserObject other = (StyleNodeUserObject)obj;
        if (myBaseMGSClass == null)
        {
            if (other.myBaseMGSClass != null)
            {
                return false;
            }
        }
        else if (other.myBaseMGSClass == null || !myBaseMGSClass.getName().equals(other.myBaseMGSClass.getName()))
        {
            return false;
        }
        if (myStyleClass == null)
        {
            if (other.myStyleClass != null)
            {
                return false;
            }
        }
        else if (other.myStyleClass == null || !myStyleClass.getName().equals(other.myStyleClass.getName()))
        {
            return false;
        }
        return EqualsHelper.equals(myDefaultStyleInstance, other.myDefaultStyleInstance);
    }

    /**
     * Gets the base mgs class.
     *
     * @return the base mgs class
     */
    public Class<? extends VisualizationSupport> getBaseMGSClass()
    {
        return myBaseMGSClass;
    }

    /**
     * Gets the default style instance.
     *
     * @return the default style instance
     */
    public VisualizationStyle getDefaultStyleInstance()
    {
        return myDefaultStyleInstance;
    }

    /**
     * Gets the style class.
     *
     * @return the style class
     */
    public Class<? extends VisualizationStyle> getStyleClass()
    {
        return myStyleClass;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myBaseMGSClass == null ? 0 : myBaseMGSClass.getName().hashCode());
        result = prime * result + (myDefaultStyleInstance == null ? 0 : myDefaultStyleInstance.hashCode());
        result = prime * result + (myStyleClass == null ? 0 : myStyleClass.getName().hashCode());
        return result;
    }

    /**
     * Sets the default style instance.
     *
     * @param defaultInstance the new default style instance
     */
    public void setDefaultStyleInstance(VisualizationStyle defaultInstance)
    {
        myDefaultStyleInstance = defaultInstance;
    }

    @Override
    public String toString()
    {
        return myDefaultStyleInstance == null ? "NULL(" + myStyleClass.getSimpleName() + ")"
                : myDefaultStyleInstance.getStyleName();
    }
}
