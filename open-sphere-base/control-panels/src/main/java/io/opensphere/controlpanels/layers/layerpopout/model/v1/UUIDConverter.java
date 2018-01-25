package io.opensphere.controlpanels.layers.layerpopout.model.v1;

import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * A JAXB converter that converts UUID object to and from strings.
 */
public class UUIDConverter extends XmlAdapter<String, UUID>
{
    @Override
    public String marshal(UUID v)
    {
        return v.toString();
    }

    @Override
    public UUID unmarshal(String v)
    {
        return UUID.fromString(v);
    }
}
