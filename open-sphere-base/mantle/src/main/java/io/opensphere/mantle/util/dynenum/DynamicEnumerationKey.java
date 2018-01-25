package io.opensphere.mantle.util.dynenum;

import java.io.Serializable;

/**
 * The Interface DynamicEnumerationKey.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public interface DynamicEnumerationKey extends Serializable
{
    /**
     * Gets the meta data key id.
     *
     * @return the meta data key id
     */
    short getMetaDataKeyId();

    /**
     * Gets the enumeration type code.
     *
     * @return the enumeration type code
     */
    short getTypeId();

    /**
     * Gets the enumeration value code.
     *
     * @return the enumeration value code
     */
    short getValueId();
}
