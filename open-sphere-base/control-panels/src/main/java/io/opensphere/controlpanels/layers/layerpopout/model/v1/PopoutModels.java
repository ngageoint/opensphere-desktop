package io.opensphere.controlpanels.layers.layerpopout.model.v1;

import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.util.collections.New;

/**
 * Contains all saved pop out models.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PopoutModels", propOrder = { "myModels" })
@XmlRootElement(name = "PopoutModels")
public class PopoutModels
{
    /**
     * All saved pop out models.
     */
    @XmlElement(name = "models", type = PopoutModelMap.class)
    @XmlJavaTypeAdapter(PopoutModelConverter.class)
    private final Map<UUID, PopoutModel> myModels = New.map();

    /**
     * Gets all of the saved pop out models.
     *
     * @return The saved pop out models.
     */
    public Map<UUID, PopoutModel> getModels()
    {
        return myModels;
    }
}
