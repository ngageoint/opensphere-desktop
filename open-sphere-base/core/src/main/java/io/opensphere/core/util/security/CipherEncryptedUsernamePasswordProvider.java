package io.opensphere.core.util.security;

import io.opensphere.core.util.Utilities;

/**
 * A username/password provider that encrypts the password internally.
 */
public class CipherEncryptedUsernamePasswordProvider implements EncryptedUsernamePasswordProvider
{
    /** The cipher factory. */
    private final CipherFactory myCipherFactory;

    /** The username/password. */
    private final EncryptedUsernamePassword myEncryptedUsernamePassword;

    /** If the username/password may be persisted. */
    private final boolean myPersistable;

    /**
     * Constructor.
     *
     * @param encryptedUsernamePassword The encrypted username/password pair.
     * @param cipherFactory Factory for creating a decryption cipher.
     * @param persistable If the username/password may be persisted.
     */
    public CipherEncryptedUsernamePasswordProvider(EncryptedUsernamePassword encryptedUsernamePassword,
            CipherFactory cipherFactory, boolean persistable)
    {
        myEncryptedUsernamePassword = Utilities.checkNull(encryptedUsernamePassword, "encryptedUsernamePassword");
        myCipherFactory = Utilities.checkNull(cipherFactory, "cipherFactory");
        myPersistable = persistable;
    }

    @Override
    public EncryptedUsernamePassword getEncryptedUsernamePassword()
    {
        return myEncryptedUsernamePassword;
    }

    @Override
    public char[] getPassword() throws DecryptionException
    {
        try
        {
            return myEncryptedUsernamePassword.getPassword(myCipherFactory);
        }
        catch (CipherException e)
        {
            throw new DecryptionException("Failed to decrypt password: " + e, e);
        }
    }

    @Override
    public String getPurpose()
    {
        return myEncryptedUsernamePassword.getPurpose();
    }

    @Override
    public String getUsername()
    {
        return myEncryptedUsernamePassword.getUsername();
    }

    @Override
    public boolean isPersistable()
    {
        return myPersistable;
    }
}
