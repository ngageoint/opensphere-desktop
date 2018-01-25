package io.opensphere.core.util.security;

import java.security.cert.X509Certificate;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * Model for the KeyUsage extension (OID= 2.6.29.15) of an X.509 certificate.
 *
 * @see X509Certificate#getKeyUsage()
 */
public class KeyUsage
{
    /** Digital signature key usage. */
    public static final KeyUsage DIGITAL_SIGNATURE;

    /** Data encipherment key usage. */
    public static final KeyUsage KEY_AND_DATA_ENCIPHERMENT;

    /** The KeyUsages as strings. */
    public static final List<String> USAGES = New.unmodifiableList("digitalSignature", "nonRepudiation", "keyEncipherment",
            "dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly");

    /** Array of all true. */
    private static final boolean[] ALL = new boolean[] { true, true, true, true, true, true, true, true, true, };

    /** The KeyUsage array. */
    private final boolean[] myArray;

    static
    {
        KEY_AND_DATA_ENCIPHERMENT = new KeyUsage(new boolean[] { false, false, true, true, false, false, false, false, false, });
        DIGITAL_SIGNATURE = new KeyUsage(new boolean[] { true, false, false, false, false, false, false, false, false, });
    }

    /**
     * Construct the object.
     *
     * @param usage The usage array.
     * @see X509Certificate#getKeyUsage()
     */
    public KeyUsage(boolean[] usage)
    {
        myArray = usage == null ? ALL : usage.clone();
    }

    /**
     * Get if this key usage allows digital signing.
     *
     * @return {@code true} if digital signing is allowed.
     */
    public boolean allowsDigitalSignature()
    {
        return myArray[0];
    }

    /**
     * Get if this key usage allows data encipherment.
     *
     * @return {@code true} if data encipherment is allowed.
     */

    public boolean allowsKeyOrDataEncipherment()
    {
        return myArray[2] || myArray[3];
    }

    /**
     * Determine if any bits in this key usage are allowed by another key usage.
     *
     * @param usage The other key usage.
     * @return {@code true} if this usage is allowed by the other usage.
     */
    public boolean anyAllowedBy(KeyUsage usage)
    {
        for (int index = 0; index < myArray.length && index < usage.myArray.length; ++index)
        {
            if (myArray[index] && usage.myArray[index])
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the usage array.
     *
     * @return The usage array.
     * @see X509Certificate#getKeyUsage()
     */
    public boolean[] getArray()
    {
        return myArray.clone();
    }

    /**
     * Determine if this key usage is allowed by another key usage.
     *
     * @param usage The other key usage.
     * @return {@code true} if this usage is allowed by the other usage.
     */
    public boolean isAllowedBy(KeyUsage usage)
    {
        for (int index = 0; index < myArray.length; ++index)
        {
            if (index >= usage.myArray.length || myArray[index] && !usage.myArray[index])
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < USAGES.size(); ++index)
        {
            if (myArray[index])
            {
                sb.append(USAGES.get(index)).append(", ");
            }
        }
        if (sb.length() > 1)
        {
            sb.setLength(sb.length() - 2);
        }
        else
        {
            sb.append("none");
        }

        return sb.toString();
    }
}
