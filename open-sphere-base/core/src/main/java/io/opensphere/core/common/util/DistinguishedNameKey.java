package io.opensphere.core.common.util;

/**
 * An enumeration over the set of common distinguished name keys (also known as
 * attribute types, according to RFC-2253).
 */
public enum DistinguishedNameKey
{
    /**
     * The enum value for a country distinguished name Attribute Type.
     */
    COUNTRY("C", "Country"),

    /**
     * The enum value for a state distinguished name Attribute Type.
     */
    STATE("ST", "State"),

    /**
     * The enum value for a organizational unit distinguished name Attribute
     * Type.
     */
    ORGANIZATIONAL_UNIT("OU", "Organizational Unit"),

    /**
     * The enum value for a organization distinguished name Attribute Type.
     */
    ORGANIZATION("O", "Organization"),

    /**
     * The enum value for an email address distinguished name Attribute Type.
     */
    EMAIL_ADDRESS("E", "Email Address"),

    /**
     * The enum value for an email address distinguished name Attribute Type.
     */
    ALTERNATE_EMAIL_ADDRESS("EMAILADDRESS", "Email Address"),

    /**
     * The enum value for a common name distinguished name Attribute Type.
     */
    COMMON_NAME("CN", "Common Name"),

    /**
     * The enum value for a locality distinguished name Attribute Type.
     */
    LOCALITY("L", "Locality"),

    /**
     * The enum value for a street address distinguished name Attribute Type.
     */
    STREET_ADDRESS("STREET", "Street Address"),

    /**
     * The enum value for a domain component distinguished name Attribute Type.
     */
    DOMAIN_COMPONENT("DC", "Domain Component"),

    /**
     * The enum value for a user ID distinguished name Attribute Type.
     */
    USER_ID("UID", "User ID");

    /**
     * The textual name of the enum type.
     */
    private String attributeType;

    /**
     * The textual display name of the enum type.
     */
    private String displayName;

    /**
     * Creates a new {@link DistinguishedNameKey}, using the supplied parameter
     * as the attribute type.
     *
     * @param pAttributeType the value to use for the attribute type of the
     *            enum.
     * @param pDisplayName the value to display for the key.
     */
    private DistinguishedNameKey(String pAttributeType, String pDisplayName)
    {
        attributeType = pAttributeType;
        displayName = pDisplayName;
    }

    /**
     * Retrieves the value of the attributeType field.
     *
     * @return the attributeType
     */
    public String getAttributeType()
    {
        return attributeType;
    }

    /**
     * Retrieves the value of the displayName field.
     *
     * @return the displayName
     */
    public String getDisplayName()
    {
        return displayName;
    }
}
