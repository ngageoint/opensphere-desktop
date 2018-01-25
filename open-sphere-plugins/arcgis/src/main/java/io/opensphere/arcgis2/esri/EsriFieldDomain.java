package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * The Class EsriFieldDomain.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = EsriFieldRangeDomain.class, name = "range"),
    @Type(value = EsriFieldCodedValueDomain.class, name = "codedValue"),
    @Type(value = EsriFieldInheritedDomain.class, name = "inherited") })
public abstract class EsriFieldDomain implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
}
