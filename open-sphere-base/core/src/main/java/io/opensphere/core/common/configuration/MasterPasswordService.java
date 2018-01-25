package io.opensphere.core.common.configuration;

import java.awt.Window;

import io.opensphere.core.common.connection.ProtectedPassword;

public interface MasterPasswordService
{
    /**
     * Returns the password's digest value.
     *
     * @return the password's digest value or <code>null</code> if no password
     *         has been set.
     */
    String getDigest();

    /**
     * This convenience method decrypts the password, if necessary, from the
     * given <code>ProtectedPassword</code> instance and returns it as a
     * <code>String</code>.
     *
     * @param protectedPassword the <code>ProtectedPassword</code> from which to
     *            extract the password.
     * @return the password as a <code>String</code> or <code>null</code> if the
     *         password could not be fetched.
     */
    String getPasswordAsString(ProtectedPassword protectedPassword);

    /**
     * Decrypts the password stored in the given <code>ProtectedPassword</code>.
     *
     * @param protectedPassword contains the password to be decrypted.
     */
    void decryptPassword(ProtectedPassword protectedPassword);

    /**
     * Adds a <code>ProtectedPassword</code> to the manager.
     *
     * @param password the password to manage.
     */
    void addPassword(ProtectedPassword password);

    /**
     * Removes the <code>ProtectedPassword</code> from the manager.
     *
     * @param password the password to remove.
     * @return <code>true</code> if the manager removed the password.
     */
    boolean removePassword(ProtectedPassword password);

    /**
     * Indicates if the specified password is managed.
     *
     * @param password the password to check.
     * @return <code>true</code> if the given password is managed.
     */
    boolean containsPassword(ProtectedPassword password);

    /**
     * Forces a prompt to the user to change the master password.
     *
     * @param window the owner of the dialog prompt.
     */
    void changePassword(Window window);

    /**
     * Returns the master password. If the password is not known, the user is
     * prompted for it. For stronger security, it is recommended that the
     * returned character array be cleared after use by setting each character
     * to zero.
     *
     * @return the master password.
     */
    char[] getMasterPassword();

}
