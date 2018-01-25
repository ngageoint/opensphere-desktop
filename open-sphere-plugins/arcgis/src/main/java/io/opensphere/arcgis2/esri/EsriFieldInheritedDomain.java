package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;

/**
 * Inherited domains apply to domains on sub-types. It implies that the domain
 * for a field at the sub-type level is the same as the domain for the field at
 * the layer level.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriFieldInheritedDomain extends EsriFieldDomain
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
}
