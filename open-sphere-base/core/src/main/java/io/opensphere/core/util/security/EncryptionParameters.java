package io.opensphere.core.util.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Parameters used for encryption.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EncryptionParameters implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The algorithm for the cipher algorithm parameters. */
    @XmlElement(name = "AlgorithmParametersAlgorithm")
    private String myAlgorithmParametersAlgorithm;

    /** The cipher transformation. */
    @XmlElement(name = "CipherTransformation")
    private String myCipherTransformation;

    /** The algorithm parameters. */
    @XmlElement(name = "AlgorithmParameters")
    private byte[] myEncodedAlgorithmParameters;

    /**
     * Constructor that takes an initialized cipher. The encryption parameters
     * will be extracted from the cipher.
     *
     * @param cipher The cipher.
     * @throws IOException If the cipher parameters cannot be encoded.
     */
    public EncryptionParameters(Cipher cipher) throws IOException
    {
        myCipherTransformation = cipher.getAlgorithm();
        AlgorithmParameters parameters = cipher.getParameters();
        if (parameters != null)
        {
            myAlgorithmParametersAlgorithm = parameters.getAlgorithm();
            myEncodedAlgorithmParameters = parameters.getEncoded();
        }
    }

    /**
     * Default constructor for JAXB.
     */
    protected EncryptionParameters()
    {
    }

    @Override
    public EncryptionParameters clone()
    {
        try
        {
            return (EncryptionParameters)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        EncryptionParameters other = (EncryptionParameters)obj;
        if (!EqualsHelper.equals(myAlgorithmParametersAlgorithm, other.myAlgorithmParametersAlgorithm, myCipherTransformation,
                other.myCipherTransformation))
        {
            return false;
        }
        return Arrays.equals(myEncodedAlgorithmParameters, other.myEncodedAlgorithmParameters);
    }

    /**
     * Get the algorithm parameters algorithm, which will be {@code null} if
     * there are no parameters.
     *
     * @return The algorithm.
     */
    public String getAlgorithmParametersAlgorithm()
    {
        return myAlgorithmParametersAlgorithm;
    }

    /**
     * Get the cipher transformation.
     *
     * @return The cipher transformation.
     */
    public String getCipherTransformation()
    {
        return myCipherTransformation;
    }

    /**
     * Create a cipher and initialize it in decryption mode.
     *
     * @param cipherFactory The cipher factory.
     * @return The cipher.
     * @throws NoSuchAlgorithmException If the algorithm parameters cannot be
     *             found.
     * @throws IOException If there is a problem decoding the algorithm
     *             parameters.
     * @throws CipherException If the cipher cannot be initialized.
     */
    public Cipher getDecryptCipher(CipherFactory cipherFactory) throws CipherException, NoSuchAlgorithmException, IOException
    {
        AlgorithmParameters params = getAlgorithmParameters();
        return cipherFactory.initCipher(Cipher.DECRYPT_MODE, myCipherTransformation, params);
    }

    /**
     * Get the algorithm parameters, which may be {@code null}.
     *
     * @return The encoded algorithm parameters.
     */
    public byte[] getEncodedAlgorithmParameters()
    {
        return myEncodedAlgorithmParameters == null ? null : myEncodedAlgorithmParameters.clone();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myAlgorithmParametersAlgorithm == null ? 0 : myAlgorithmParametersAlgorithm.hashCode());
        result = prime * result + (myCipherTransformation == null ? 0 : myCipherTransformation.hashCode());
        result = prime * result + Arrays.hashCode(myEncodedAlgorithmParameters);
        return result;
    }

    /**
     * Get the parameters for the decryption cipher.
     *
     * @return The algorithm parameters.
     * @throws NoSuchAlgorithmException If the algorithm is not available.
     * @throws IOException If the parameters cannot be decoded.
     */
    private AlgorithmParameters getAlgorithmParameters() throws NoSuchAlgorithmException, IOException
    {
        AlgorithmParameters params;
        if (myAlgorithmParametersAlgorithm != null && myEncodedAlgorithmParameters != null)
        {
            params = AlgorithmParameters.getInstance(myAlgorithmParametersAlgorithm);
            params.init(myEncodedAlgorithmParameters);
        }
        else
        {
            params = null;
        }
        return params;
    }
}
