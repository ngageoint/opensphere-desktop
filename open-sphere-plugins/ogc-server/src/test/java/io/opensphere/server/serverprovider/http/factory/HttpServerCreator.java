package io.opensphere.server.serverprovider.http.factory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;

import com.bitsys.common.http.auth.Credentials;
import com.bitsys.common.http.auth.UsernamePasswordCredentials;
import com.bitsys.common.http.client.DefaultHttpClient;
import com.bitsys.common.http.client.HttpClient;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.video.VideoTestUtil;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;
import io.opensphere.server.serverprovider.http.HttpServerImpl;
import io.opensphere.server.serverprovider.http.header.HeaderValuesImpl;
import io.opensphere.server.serverprovider.http.requestors.RequestorProviderImpl;

/**
 * Creates an HttpServer for use in functional tests.
 */
public final class HttpServerCreator
{
    /**
     * The api certificates key token.
     */
    private static String ourToken = "";

    /**
     * The api certificates username.
     */
    private static String ourUserName = "";

    /**
     * The api certificates password.
     */
    private static String ourPassword = "";

    /**
     * The trust store.
     */
    private static String ourTrustStore = "";

    /**
     * The api certificate.
     */
    private static String ourCertificate = "";

    /**
     * The parameter to not use a certificate.
     */
    private static String ourNoCert = "false";

    /**
     * The key store type.
     */
    private static String ourKeyStoreType = "pkcs12";

    /**
     * The trust store type.
     */
    private static String ourTrustStoreType = KeyStore.getDefaultType();

    /**
     * The delimiter character used to separate cert/passwords.
     */
    private static final String ourDelim = ",";

    /**
     * Gets the api certificates key token.
     *
     * @return the key token.
     */
    public static String getKeyToken()
    {
        return ourToken;
    }

    /**
     * Creates a HttpServer connection.
     *
     * @param url The URL
     * @return The newly create HttpServer.
     * @throws IOException If anything bad happens
     */
    public static HttpServer createServer(URL url) throws IOException
    {
        String host = url.getHost();
        int port = url.getPort();
        String protocol = url.getProtocol();

        HttpClient httpClient = createHttpClient(host, port);
        RequestorProviderImpl provider = new RequestorProviderImpl(httpClient, new HeaderValuesImpl("test"), null, null);
        return new HttpServerImpl(host, protocol, provider);
    }

    /**
     * Creates an HttpClient.
     *
     * @param host the host
     * @param port the port
     * @return the HttpClient
     * @throws IOException If anything bad happens
     */
    private static HttpClient createHttpClient(String host, int port) throws IOException
    {
        HttpClient httpClient = new DefaultHttpClient();

        ConfigurerParameters parameters = new ConfigurerParameters();
        parameters.setClient(httpClient);
        parameters.setHost(host);
        parameters.setPort(port);
        parameters.setServerKey(StringUtilities.concat(host, ":", String.valueOf(port)));
        try
        {
            parameters.setProvider(createSecurityProvider());
        }
        catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
        {
            throw new IOException(e);
        }

        new CertificateConfigurer().configure(parameters);
        new ConnectionPoolConfigurer().configure(httpClient);

        return httpClient;
    }

    /**
     * Creates the SecurityComponentsProvider.
     *
     * @return the SecurityComponentsProvider
     * @throws KeyStoreException invalid key store
     * @throws NoSuchAlgorithmException invalid encryption algorithm
     * @throws IOException could not get cert file
     * @throws CertificateException invalid certificate
     * @throws UnrecoverableKeyException unable to create a keyManager
     */
    public static SecurityComponentsProvider createSecurityProvider()
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
    {
        // create mock object
        EasyMockSupport support = new EasyMockSupport();
        SecurityComponentsProvider securityProvider = support.createMock(SecurityComponentsProvider.class);

        ourTrustStoreType = loadProperty("trustStoreType", ourTrustStoreType);
        ourToken = loadProperty("token", ourToken);

        if (loadProperty("noCert", ourNoCert).equals("false"))
        {
            // get cert info from system properties
            ourTrustStore = loadProperty("trustStore", ourTrustStore);
            String[] trustStoreAndPass = ourTrustStore.split(ourDelim);

            ourCertificate = loadProperty("certificate", ourCertificate);
            String[] certAndPass = ourCertificate.split(ourDelim);

            ourUserName = loadProperty("userName", ourUserName);
            ourPassword = loadProperty("password", ourPassword);

            ourKeyStoreType = loadProperty("keyStoreType", ourKeyStoreType);

            // create trust manager
            KeyStore trustStore = KeyStore.getInstance(ourTrustStoreType);
            String trustStoreFileName = VideoTestUtil.getFile(trustStoreAndPass[0]).getPath();
            FileInputStream trustStoreStream = new FileInputStream(trustStoreFileName);
            trustStore.load(trustStoreStream, trustStoreAndPass[1].toCharArray());
            trustStoreStream.close();

            TrustManager trustAllCerts = new X509TrustManager()
            {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }
            };

            // create keymanager
            KeyStore keyStore = KeyStore.getInstance(ourKeyStoreType);
            String certFileName = VideoTestUtil.getFile(certAndPass[0]).getPath();
            FileInputStream keyStoreStream = new FileInputStream(certFileName);
            keyStore.load(keyStoreStream, certAndPass[1].toCharArray());
            keyStoreStream.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, certAndPass[1].toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // set up credentials
            Credentials cred = new UsernamePasswordCredentials(ourUserName, ourPassword.toCharArray());

            // set up mock object
            EasyMock.expect(securityProvider.getUserCredentials(EasyMock.isA(String.class), EasyMock.isA(String.class),
                    EasyMock.isNull(), EasyMock.isNull())).andReturn(cred).anyTimes();
            EasyMock.expect(securityProvider.getKeyManager(EasyMock.isA(String.class), EasyMock.isA(String.class),
                    EasyMock.isNull(), EasyMock.isNull(), EasyMock.isNull())).andReturn(keyManagers[0]).anyTimes();
            EasyMock.expect(securityProvider.getTrustManager(EasyMock.isA(String.class), EasyMock.isA(String.class),
                    EasyMock.isNull(), EasyMock.isNull())).andReturn(trustAllCerts).anyTimes();
        }
        else
        {
            TrustManager trustAllCerts = new X509TrustManager()
            {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }
            };

            EasyMock.expect(securityProvider.getKeyManager(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject(),
                    EasyMock.anyObject(), EasyMock.anyObject())).andReturn(support.createMock(KeyManager.class));
            EasyMock.expect(securityProvider.getTrustManager(EasyMock.isA(String.class), EasyMock.isA(String.class),
                    EasyMock.isNull(), EasyMock.isNull())).andReturn(trustAllCerts).anyTimes();
        }
        support.replayAll();
        return securityProvider;
    }

    /**
     * Tries to get property from system property, then environmental variable.
     * Otherwise, uses defaultValue.
     *
     * @param propertyName the property name.
     * @param defaultValue the default property name if none is found.
     * @return the property value.
     */
    private static String loadProperty(String propertyName, String defaultValue)
    {
        String property = System.getProperty(propertyName);
        if (property == null)
        {
            property = System.getenv(propertyName);
        }
        return property == null ? defaultValue : property;
    }

    /** Private constructor. */
    private HttpServerCreator()
    {
    }

    /**
     * Gets the certificate credentials username.
     *
     * @return the username.
     */
    public static String getUsername()
    {
        return ourUserName;
    }

    /**
     * Gets the certificate credentials password.
     *
     * @return the password.
     */
    public static String getPassword()
    {
        return ourPassword;
    }
}
