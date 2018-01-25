package io.opensphere.controlpanels.layers.layerpopout.model.v1;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.util.collections.New;

/**
 * Used to translate a Map to and from JAXB.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PopoutModelMap", propOrder = { "myModels" })
@XmlRootElement(name = "PopoutModelMap")
public class PopoutModelMap
{
    /**
     * The list of models.
     */
    @XmlElement(name = "models")
    private final List<PopoutModel> myModels = New.list();

    /**
     * Gets the list of models.
     *
     * @return The list of models.
     */
    public List<PopoutModel> getModels()
    {
        return myModels;
    }
}
