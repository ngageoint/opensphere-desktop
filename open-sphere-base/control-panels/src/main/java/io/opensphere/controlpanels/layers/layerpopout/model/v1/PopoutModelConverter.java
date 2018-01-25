package io.opensphere.controlpanels.layers.layerpopout.model.v1;

import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import io.opensphere.core.util.collections.New;

/**
 * A JAXB converter that converts a map of PopoutModels to and from lists of
 * PopoutModels.
 */
public class PopoutModelConverter extends XmlAdapter<PopoutModelMap, Map<UUID, PopoutModel>>
{
    @Override
    public PopoutModelMap marshal(Map<UUID, PopoutModel> v)
    {
        PopoutModelMap map = new PopoutModelMap();

        map.getModels().addAll(v.values());

        return map;
    }

    @Override
    public Map<UUID, PopoutModel> unmarshal(PopoutModelMap v)
    {
        Map<UUID, PopoutModel> map = New.map();

        for (PopoutModel model : v.getModels())
        {
            map.put(model.getId(), model);
        }

        return map;
    }
}
