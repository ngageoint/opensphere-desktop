package io.opensphere.featureactions.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * Contains all features actions for a given layer.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FeatureActions
{
    /**
     * The feature actions for that layer.
     */
    @XmlElement(name = "actions")
    private final List<FeatureAction> myActions = New.list();

    /**
     * Gets the actions for that layer.
     *
     * @return The actions for that layer.
     */
    public List<FeatureAction> getActions()
    {
        return myActions;
    }
}
