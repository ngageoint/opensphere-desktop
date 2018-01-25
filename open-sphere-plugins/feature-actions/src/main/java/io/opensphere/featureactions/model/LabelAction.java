package io.opensphere.featureactions.model;

import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.controlpanels.styles.model.LabelOptions;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * The action that modifies a features labels.
 */
@XmlRootElement
public class LabelAction extends Action
{
    /**
     * The label options.
     */
    private LabelOptions myLabelOptions = new LabelOptions();

    @Override
    public LabelAction clone()
    {
        try
        {
            return (LabelAction)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
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
        LabelAction other = (LabelAction)obj;
        if (myLabelOptions == null)
        {
            if (other.myLabelOptions != null)
            {
                return false;
            }
        }
        else if (!myLabelOptions.equals(other.myLabelOptions))
        {
            return false;
        }
        return true;
    }

    /**
     * Gets the label options.
     *
     * @return the labelOptions The label options.
     */
    public LabelOptions getLabelOptions()
    {
        return myLabelOptions;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myLabelOptions == null ? 0 : myLabelOptions.hashCode());
        return result;
    }

    /**
     * Sets the label options.
     *
     * @param options The options.
     */
    public void setLabelOptions(LabelOptions options)
    {
        myLabelOptions = options;
    }

    @Override
    protected String getName()
    {
        return "Set Label";
    }
}
