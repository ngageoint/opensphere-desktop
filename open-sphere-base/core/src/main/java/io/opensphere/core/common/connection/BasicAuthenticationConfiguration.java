package io.opensphere.core.common.connection;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * This class encapsulates a single basic authentication configuraiton.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicAuthenticationConfiguration implements Cloneable
{

    /**
     * Indicates if the basic authentication configuration should be used.
     */
    @XmlAttribute(name = "useAuthentication", required = true)
    private boolean useAuthentication = false;

    /**
     * Indicates whether authentication is basic or form-based
     *
     * Form-based authentication preserved for backwards-compatibility
     */
    @XmlAttribute(name = "useForms")
    private boolean useForms = false;

    /**
     * The user name for authentication.
     */
    @XmlElement(name = "Username")
    private String username;

    /**
     * The encrypted password.
     */
    @XmlElement(name = "Password")
    private ProtectedPassword password;

    /**
     * Optional username/password credentials object that may be used to provide
     * username/password on demand.
     */
    @XmlTransient
    private UsernamePasswordCredentials usernamePasswordCredentials;

    /**
     * Default CTOR
     */
    public BasicAuthenticationConfiguration()
    {
    }

    /**
     * Copy CTOR
     *
     * @param other
     */
    public BasicAuthenticationConfiguration(BasicAuthenticationConfiguration other)
    {
        setEqualTo(other);
    }

    /**
     * Sets this {@link BasicAuthenticationConfiguration} equal to another
     *
     * @param other
     */
    public void setEqualTo(BasicAuthenticationConfiguration other)
    {
        useAuthentication = other.useAuthentication;
        username = other.username;
        if (other.password != null)
        {
            password = new ProtectedPassword();
            password.setPassword(other.getPassword().getPassword());
            password.setEncryptedPassword(other.getPassword().getEncryptedPassword());
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        boolean isEqual = false;
        if (other != null && other instanceof BasicAuthenticationConfiguration)
        {
            BasicAuthenticationConfiguration that = (BasicAuthenticationConfiguration)other;
            if (useAuthentication == that.useAuthentication && username.equals(that.username))
            {
                if (password != null && that.password != null)
                {
                    if (password.getEncryptedPassword() != null && that.password.getEncryptedPassword() != null)
                    {
                        boolean pwdEqual = Arrays.equals(password.getPassword(), that.password.getPassword());
                        if (pwdEqual && password.getEncryptedPassword().equals(that.password.getEncryptedPassword()))
                        {
                            isEqual = true;
                        }
                    }
                    else
                    {
                        isEqual = true;
                    }
                }
                else if (password == null && that.password == null)
                {
                    isEqual = true;
                }
            }

        }
        return isEqual;
    }

    /**
     * Returns true if the authentication is to be used
     *
     * @return true if to be used, false if not
     */
    public boolean isUseAuthentication()
    {
        return useAuthentication;
    }

    /**
     * Sets if authentication should be used, false if not
     *
     * @param useAuthentication
     */
    public void setUseAuthentication(boolean useAuthentication)
    {
        this.useAuthentication = useAuthentication;
    }

    /**
     * Returns true if the authentication should be form-based
     *
     * @return true if form-based, false if basic
     */
    public boolean isUseForms()
    {
        return useForms;
    }

    /**
     * Sets if authentication should be form-based, false if not
     *
     * @param useForms
     */
    public void setUseForms(boolean useForms)
    {
        this.useForms = useForms;
    }

    /**
     * Returns the user name for authentication.
     *
     * @return the user name for authentication.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the user name for authentication.
     *
     * @param username the user name for authentication.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Returns the protected password.
     *
     * @return the protected password.
     */
    public ProtectedPassword getPassword()
    {
        return password;
    }

    /**
     * Sets the protected password.
     *
     * @param password the protected password.
     */
    public void setPassword(ProtectedPassword password)
    {
        this.password = password;
    }

    /**
     * Return username/password from this
     * {@link BasicAuthenticationConfiguration} as a {@link ProxyConfiguration}
     *
     * @return the proxy configuration
     */
    public ProxyConfiguration getAsProxyConfiguration()
    {
        ProxyConfiguration pc = new ProxyConfiguration();
        pc.setUsername(getUsername());

        ProtectedPassword pw = new ProtectedPassword();
        pw.setEncryptedPassword(password.getEncryptedPassword());
        pw.setPassword(password.getPassword());
        pc.setPassword(pw);

        return pc;
    }

    @Override
    public String toString()
    {
        return "BASIC AUTH CONFIGURATION\nUsername: " + username + "\nPassword: " + password + "\nUse Form: " + useForms;
    }

    @Override
    public BasicAuthenticationConfiguration clone() throws CloneNotSupportedException
    {
        BasicAuthenticationConfiguration clone = (BasicAuthenticationConfiguration)super.clone();
        if (password != null)
        {
            clone.password = password.clone();
        }
        return clone;
    }

    /**
     * Set the username/password credentials object to be used instead of
     * creating one from my username and password.
     *
     * @param creds The credentials.
     */
    public void setUsernamePasswordCredentials(UsernamePasswordCredentials creds)
    {
        usernamePasswordCredentials = creds;
    }

    /**
     * Get the username/password credentials object to be used instead of
     * creating one from my username and password.
     *
     * @return The credentials.
     */
    public UsernamePasswordCredentials getUsernamePasswordCredentials()
    {
        UsernamePasswordCredentials creds = usernamePasswordCredentials;
        if (creds == null)
        {
            char[] pw = password.getPassword();
            String pwString = new String(pw);
            Arrays.fill(pw, '\0');
            creds = new UsernamePasswordCredentials(username, pwString);
        }
        return creds;
    }
}
