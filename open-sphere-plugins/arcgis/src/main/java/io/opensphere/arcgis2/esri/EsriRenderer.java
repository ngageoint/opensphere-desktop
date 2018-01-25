package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * The Class EsriRenderer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = EsriSimpleRenderer.class, name = "simple"),
    @Type(value = EsriUniqueValueRenderer.class, name = "uniqueValue"),
    @Type(value = EsriClassBreaksRenderer.class, name = "classBreaks") })

public abstract class EsriRenderer implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
}
