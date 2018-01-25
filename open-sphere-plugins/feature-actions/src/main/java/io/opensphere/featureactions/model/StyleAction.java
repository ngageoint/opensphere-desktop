package io.opensphere.featureactions.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * The action that modifies a feature's style in some fashion.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class StyleAction extends Action
{
    /** Styles currently supported in feature actions. */
    public static final List<Styles> STYLES_LIST;
    static
    {
        List<Styles> styles = New.list(4);
        styles.add(Styles.POINT);
        styles.add(Styles.ICON);
        styles.add(Styles.ELLIPSE);
        styles.add(Styles.ELLIPSE_WITH_CENTER);
        STYLES_LIST = Collections.unmodifiableList(styles);
    }

    /** The style options. */
    @XmlElement(name = "styleOptions")
    private final StyleOptions myStyleOptions = new StyleOptions();

    @Override
    public StyleAction clone()
    {
        try
        {
            return (StyleAction)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Gets the style options.
     *
     * @return the styleOptions The style options.
     */
    public StyleOptions getStyleOptions()
    {
        return myStyleOptions;
    }

    @Override
    protected String getName()
    {
        return "Set Style";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((myStyleOptions == null) ? 0 : myStyleOptions.hashCode());
        return result;
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
        StyleAction other = (StyleAction)obj;
        return Objects.equals(myStyleOptions, other.myStyleOptions);
    }
}
