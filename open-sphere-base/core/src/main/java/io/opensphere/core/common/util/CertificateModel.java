package io.opensphere.core.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CertificateModel
{
    /**
     * The common name of the certificate, or the full name of a
     * basic-authentication user.
     */
    private String commonName;

    /**
     * The set of fields extracted from a certificate String.
     */
    private Map<String, String> fields;

    /**
     * Default constructor, hidden from implementation.
     */
    @SuppressWarnings("unused")
    private CertificateModel()
    {
        /* intentionally blank */
    }

    /**
     * Generates a new certificate model using the supplied Raw String. Accepts
     * comma-delimited pairs of key / value records, in which each key is
     * delimited from its corresponding value using an equal sign. For example:
     *
     * <pre>
     * <code>EMAILADDRESS=moningerj@bit-sys.com, CN=label3, OU=Denver, O=bit-sys.com, ST=Colorado, C=US</code>
     * </pre>
     *
     * @param pCertificateString the string from which to extract field data.
     */
    public CertificateModel(String pCertificateString)
    {
        boolean propertiesExtracted = false;
        fields = new HashMap<>();
        if (pCertificateString.contains(","))
        {
            String[] rawFields = pCertificateString.split(", ");

            for (String field : rawFields)
            {
                // each field may contain whitespace on either end, trim before
                // splitting again.
                String[] tokens = field.trim().split("=");
                if (tokens.length == 2)
                {
                    fields.put(tokens[0].trim(), tokens[1].trim());
                    propertiesExtracted = true;
                }
            }
        }
        else if (pCertificateString.contains("="))
        {
            String[] tokens = pCertificateString.trim().split("=");
            if (tokens.length == 2)
            {
                fields.put(tokens[0].trim(), tokens[1].trim());
                propertiesExtracted = true;
            }
        }

        if (propertiesExtracted == false)
        {
            // if we didn't populate a single property, let's assume that the
            // String is a basic authentication
            // user name, therefore, store it as the full common name.
            commonName = pCertificateString;
        }
        else if (fields.containsKey(DistinguishedNameKey.COMMON_NAME.getAttributeType()))
        {
            // a common name was extracted, store it for later use.
            commonName = fields.get(DistinguishedNameKey.COMMON_NAME.getAttributeType());
        }
    }

    /**
     * Retrieves the value of the commonName field.
     *
     * @return the commonName
     */
    public String getCommonName()
    {
        return commonName;
    }

    /**
     * Gets the set of keys extracted from the certificate string.
     *
     * @return the set of keys extracted from the certificate string.
     */
    public Set<String> getKeys()
    {
        return fields.keySet();
    }

    /**
     * Gets the value of the field associated with the named Key.
     *
     * @param pKey the key for which to get the associated value.
     * @return the value associated with the named key.
     */
    public String get(String pKey)
    {
        if (pKey == null)
        {
            throw new NullPointerException("The supplied key cannot be null.");
        }
        return fields.get(pKey);
    }

    /**
     * Gets the value of the field associated with the named Key.
     *
     * @param pKey the key for which to get the associated value.
     * @return the value associated with the named key.
     */
    public String get(DistinguishedNameKey pKey)
    {
        if (pKey == null)
        {
            throw new NullPointerException("The supplied key cannot be null.");
        }
        return fields.get(pKey.getAttributeType());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return DistinguishedNameKey.COMMON_NAME.getDisplayName() + " = "
                + fields.get(DistinguishedNameKey.COMMON_NAME.getAttributeType()) + "\n"
                + DistinguishedNameKey.EMAIL_ADDRESS.getDisplayName() + " = "
                + fields.get(DistinguishedNameKey.EMAIL_ADDRESS.getAttributeType()) + "\n"
                + DistinguishedNameKey.ORGANIZATION.getDisplayName() + " = "
                + fields.get(DistinguishedNameKey.ORGANIZATION.getAttributeType()) + "\n"
                + DistinguishedNameKey.ORGANIZATIONAL_UNIT.getDisplayName() + " = "
                + fields.get(DistinguishedNameKey.ORGANIZATIONAL_UNIT.getAttributeType()) + "\n"
                + DistinguishedNameKey.STATE.getDisplayName() + " = " + fields.get(DistinguishedNameKey.STATE.getAttributeType())
                + "\n" + DistinguishedNameKey.COUNTRY.getDisplayName() + " = "
                + fields.get(DistinguishedNameKey.COUNTRY.getAttributeType());
    }

    /**
     * Gets the number of fields extracted from the username.
     *
     * @return the total number of fields extracted from the username.
     */
    public int getFieldCount()
    {
        return fields.size();
    }
}
