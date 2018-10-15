package io.opensphere.mantle.data.geom.style.config.v1;

import java.util.Collection;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * The Class StyleParameterSetConfig.
 */
@XmlRootElement(name = "StyleParameterSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class StyleParameterSetConfig
{
    /** The Parameter set. */
    @XmlElement(name = "Parameter")
    private Set<StyleParameterConfig> myParameterSet;

    /** The Selected style class name. */
    @XmlAttribute(name = "StyleClass")
    private String myStyleClassName;

    /**
     * Instantiates a new style parameter set config.
     */
    public StyleParameterSetConfig()
    {
        myParameterSet = New.set();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myParameterSet == null ? 0 : myParameterSet.hashCode());
        result = prime * result + (myStyleClassName == null ? 0 : myStyleClassName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        StyleParameterSetConfig other = (StyleParameterSetConfig)obj;
        if (myParameterSet == null)
        {
            if (other.myParameterSet != null)
            {
                return false;
            }
        }
        else if (!myParameterSet.equals(other.myParameterSet))
        {
            return false;
        }
        if (myStyleClassName == null)
        {
            if (other.myStyleClassName != null)
            {
                return false;
            }
        }
        else if (!myStyleClassName.equals(other.myStyleClassName))
        {
            return false;
        }
        return true;
    }

    /**
     * Instantiates a new style parameter set config.
     *
     * @param styleClass the style class
     * @param paramCollection the param collection
     */
    public StyleParameterSetConfig(String styleClass, Collection<VisualizationStyleParameter> paramCollection)
    {
        this();
        myStyleClassName = styleClass;
        if (paramCollection != null && !paramCollection.isEmpty())
        {
            paramCollection.stream().filter(v -> v.isSaved()).map(StyleParameterConfig::new).forEach(myParameterSet::add);
        }
    }

    /**
     * Copy constructor.
     *
     * @param other the StyleParameterSetConfig to copy.
     */
    public StyleParameterSetConfig(StyleParameterSetConfig other)
    {
        Utilities.checkNull(other, "other");
        myStyleClassName = other.myStyleClassName;
        myParameterSet = New.set();
        if (other.myParameterSet != null)
        {
            other.myParameterSet.stream().map(StyleParameterConfig::new).forEach(myParameterSet::add);
        }
    }

    /**
     * Instantiates a new style parameter set config.
     *
     * @param aStyle the a style
     */
    public StyleParameterSetConfig(VisualizationStyle aStyle)
    {
        this(aStyle.getClass().getName(), aStyle.getStyleParameterSet());
    }

    /**
     * Gets the parameter set.
     *
     * @return the parameter set
     */
    public Set<StyleParameterConfig> getParameterSet()
    {
        return myParameterSet;
    }

    /**
     * Gets the style class name.
     *
     * @return the style class name
     */
    public String getStyleClassName()
    {
        return myStyleClassName;
    }

    /**
     * Sets the parameter set.
     *
     * @param parameterSet the new parameter set
     */
    public void setParameterSet(Set<StyleParameterConfig> parameterSet)
    {
        myParameterSet = parameterSet;
    }

    /**
     * Sets the style class name.
     *
     * @param styleClassName the new style class name
     */
    public void setStyleClassName(String styleClassName)
    {
        myStyleClassName = styleClassName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(this.getClass().getSimpleName()).append(" StyleClass[").append(myStyleClassName).append("] Parameters[")
                .append(myParameterSet.size()).append(']');
        if (!myParameterSet.isEmpty())
        {
            sb.append('\n');
            for (StyleParameterConfig spc : myParameterSet)
            {
                sb.append("   ");
                sb.append(spc.toString()).append('\n');
            }
        }
        return sb.toString();
    }
}
