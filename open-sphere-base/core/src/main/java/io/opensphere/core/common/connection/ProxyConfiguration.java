package io.opensphere.core.common.connection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * This class encapsulates a single proxy connection configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProxyConfiguration implements Cloneable
{
    /**
     * Indicates if the proxy configuration should be used.
     */
    @XmlAttribute(name = "useProxy", required = true)
    private boolean useProxy;

    /**
     * The proxy server's host name or IP address.
     */
    @XmlElement(name = "Host", nillable = true, required = false)
    private String host;

    /**
     * The proxy server's port.
     */
    @XmlElement(name = "Port", nillable = true, required = false)
    private int port;

    /**
     * The automatic proxy configuration URL.
     */
    @XmlElement(name = "ProxyScriptUrl", nillable = true, required = false)
    private String proxyScriptUrl;

    /**
     * The user name for authentication.
     */
    @XmlElement(name = "Username", nillable = true, required = false)
    private String username;

    /**
     * The encrypted password.
     */
    @XmlElement(name = "Password", nillable = true, required = false)
    private ProtectedPassword password;

    /**
     * Optional username/password credentials object that may be used to provide
     * username/password on demand.
     */
    @XmlTransient
    private UsernamePasswordCredentials usernamePasswordCredentials;

    /**
     * Sets this {@link ProxyConfiguration} equal to another
     *
     * @param other
     */
    public void setEqualTo(ProxyConfiguration other)
    {
        useProxy = other.useProxy;
        host = other.host;
        port = other.port;
        proxyScriptUrl = other.proxyScriptUrl;
        username = other.username;
        if (other.password != null)
        {
            password = new ProtectedPassword();
            password.setPassword(other.getPassword().getPassword());
            password.setEncryptedPassword(other.getPassword().getEncryptedPassword());
        }
        usernamePasswordCredentials = other.usernamePasswordCredentials;
    }

    /**
     * Indicates if the proxy configuration should be used.
     *
     * @return indicates if the proxy configuration should be used.
     */
    public boolean isUseProxy()
    {
        return useProxy;
    }

    /**
     * Sets the use proxy flag indicating if the proxy configuration should be
     * used.
     *
     * @param useProxy indicates if the proxy configuration should be used.
     */
    public void setUseProxy(boolean useProxy)
    {
        this.useProxy = useProxy;
    }

    /**
     * Returns the proxy server's host name or IP address.
     *
     * @return the proxy server's host name or IP address.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the proxy server's host name or IP address.
     *
     * @param host the proxy server's host name or IP address.
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * Returns the proxy server's port.
     *
     * @return the proxy server's port.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the proxy server's port.
     *
     * @param port the proxy server's port.
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Sets the automatic proxy configuration URL.
     *
     * @param proxyScriptUrl the automatic proxy configuration URL.
     */
    public void setProxyScriptUrl(String proxyScriptUrl)
    {
        this.proxyScriptUrl = proxyScriptUrl;
    }

    /**
     * Returns the automatic proxy configuration URL.
     *
     * @return the automatic proxy configuration URL.
     */
    public String getProxyScriptUrl()
    {
        return proxyScriptUrl;
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

    @Override
    public String toString()
    {
        return "PROXY CONFIGURATION\nUsername: " + username + "\nPassword: " + password + "\nUse proxy: " + useProxy
                + "\nProxy Script: " + proxyScriptUrl + "\nProxy Host: " + host + "\nProxy Port: " + port;
    }

    @Override
    public ProxyConfiguration clone() throws CloneNotSupportedException
    {
        ProxyConfiguration clone = (ProxyConfiguration)super.clone();
        if (password != null)
        {
            clone.password = password.clone();
        }
        return clone;
    }

    /**
     * Get the usernamePasswordCredentials.
     *
     * @return The usernamePasswordCredentials.
     */
    public UsernamePasswordCredentials getUsernamePasswordCredentials()
    {
        return usernamePasswordCredentials;
    }

    /**
     * Set the usernamePasswordCredentials.
     *
     * @param usernamePasswordCredentials The usernamePasswordCredentials to
     *            set.
     */
    public void setUsernamePasswordCredentials(UsernamePasswordCredentials usernamePasswordCredentials)
    {
        this.usernamePasswordCredentials = usernamePasswordCredentials;
    }

}
