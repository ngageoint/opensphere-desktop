package io.opensphere.mantle.data.geom.style.config.v1;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * The Class FeatureTypeStyleConfig.
 */
@XmlRootElement(name = "FeatureTypeStyle")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeatureTypeStyleConfig
{
    /** The Base mgs class name. */
    @XmlAttribute(name = "BaseMGSClass")
    private String myBaseMGSClassName;

    /** The Selected style class name. */
    @XmlAttribute(name = "SelectedStyleClass")
    private String mySelectedStyleClassName;

    /** The Style class name to param set map. */
    @XmlElement(name = "StyleParameterSet")
    private final List<StyleParameterSetConfig> myStyleParameterSetConfigList;

    /**
     * Instantiates a new feature type style config.
     */
    public FeatureTypeStyleConfig()
    {
        myStyleParameterSetConfigList = New.list();
    }

    /**
     * Copy constructor.
     *
     * @param other the {@link FeatureTypeStyleConfig}
     */
    public FeatureTypeStyleConfig(FeatureTypeStyleConfig other)
    {
        Utilities.checkNull(other, "other");
        myBaseMGSClassName = other.myBaseMGSClassName;
        mySelectedStyleClassName = other.mySelectedStyleClassName;
        myStyleParameterSetConfigList = New.list();
        if (other.myStyleParameterSetConfigList != null)
        {
            myStyleParameterSetConfigList.stream().map(cfg -> new StyleParameterSetConfig(cfg))
                    .forEach(myStyleParameterSetConfigList::add);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean isEqual = false;

        if (obj instanceof FeatureTypeStyleConfig)
        {
            FeatureTypeStyleConfig other = (FeatureTypeStyleConfig)obj;
            isEqual = Objects.equals(other.myBaseMGSClassName, myBaseMGSClassName)
                    && Objects.equals(other.mySelectedStyleClassName, mySelectedStyleClassName)
                    && Objects.equals(other.myStyleParameterSetConfigList, myStyleParameterSetConfigList);
        }

        return isEqual;
    }

    /**
     * Gets the base mgs class name.
     *
     * @return the base mgs class name
     */
    public String getBaseMGSClassName()
    {
        return myBaseMGSClassName;
    }

    /**
     * Gets the selected style class name.
     *
     * @return the selected style class name
     */
    public String getSelectedStyleClassName()
    {
        return mySelectedStyleClassName;
    }

    /**
     * Gets the style parameter set config for style c lass.
     *
     * @param styleClass the style class
     * @return the style parameter set config for style c lass
     */
    public StyleParameterSetConfig getStyleParameterSetConfigForStyleClass(String styleClass)
    {
        if (myStyleParameterSetConfigList == null)
        {
            return null;
        }

        return myStyleParameterSetConfigList.stream().filter(cfg -> Objects.equals(styleClass, cfg.getStyleClassName()))
                .findFirst().orElse(null);
    }

    /**
     * Gets the style parameter set config list.
     *
     * @return the style parameter set config list
     */
    public List<StyleParameterSetConfig> getStyleParameterSetConfigList()
    {
        return myStyleParameterSetConfigList;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myBaseMGSClassName == null ? 0 : myBaseMGSClassName.hashCode());
        result = prime * result + (mySelectedStyleClassName == null ? 0 : mySelectedStyleClassName.hashCode());
        result = prime * result + (myStyleParameterSetConfigList == null ? 0 : myStyleParameterSetConfigList.hashCode());
        return result;
    }

    /**
     * Sets the base mgs class name.
     *
     * @param baseMGSClassName the new base mgs class name
     */
    public void setBaseMGSClassName(String baseMGSClassName)
    {
        myBaseMGSClassName = baseMGSClassName;
    }

    /**
     * Sets the selected style class name.
     *
     * @param selectedStyleClassName the new selected style class name
     * @return true, if changed
     */
    public boolean setSelectedStyleClassName(String selectedStyleClassName)
    {
        boolean changed = Objects.equals(mySelectedStyleClassName, selectedStyleClassName);
        mySelectedStyleClassName = selectedStyleClassName;
        return changed;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName()).append("\n" + "  BaseMGSClassName      [").append(myBaseMGSClassName)
                .append("]\n" + "  SelectedStyleClassName[").append(mySelectedStyleClassName)
                .append("]\n" + "  StyleParameterSetConfigList Size[").append(myStyleParameterSetConfigList.size()).append("]\n");

        if (!myStyleParameterSetConfigList.isEmpty())
        {
            sb.append('\n');
            for (StyleParameterSetConfig spc : myStyleParameterSetConfigList)
            {
                sb.append(spc.toString()).append("\n\n");
            }
        }
        return sb.toString();
    }
}
