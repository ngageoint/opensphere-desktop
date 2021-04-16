package com.bitsys.common.http.proxy;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsys.common.http.proxy.ProxyHostConfig.ProxyType;
import com.sun.deploy.net.proxy.AutoProxyScript;

/**
 * This class runs web proxy scripts.
 */
public class ProxyScriptRunner
{
    /** The <code>Logger</code> instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyScriptRunner.class);

    /** Indicates if deploy.jar and AutoProxyScript have been loaded. */
    private static AtomicBoolean deployJarLoaded = new AtomicBoolean();

    /** The system scripts loaded from AutoProxyScript. */
    private static final Collection<String> systemScripts = new ArrayList<>();

    /** The script engine. */
    private final ScriptEngine scriptEngine;

    /**
     * Indicates if the system scripts have been loaded into the script engine.
     */
    private final AtomicBoolean systemScriptsLoaded = new AtomicBoolean();

    /**
     * Constructor.
     * <p>
     * May throw a runtime {@link ScriptException} if unable to initialize the
     * scripting engine.
     */
    public ProxyScriptRunner()
    {
        final ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByExtension("js");
    }

    /**
     * Loads deploy.jar and fetches the system scripts from
     * <code>AutoProxyScript</code>.
     */
    protected synchronized static void initialize()
    {
        if (!deployJarLoaded.get())
        {
            deployJarLoaded.set(true);
            try
            {
                retrieveSystemScripts();
            }
            catch (final ClassNotFoundException e)
            {
                throw new IllegalStateException("Ensure that deploy.jar is in your MIST/version/ or MIST/version/plugins/ folder", e);
            }
        }
    }

    /**
     * Attempts to add <code>deploy.jar</code> to the system class loader.
     * On Java 9+ the system class loader is no longer a <code>URLClassLoader</code>, so this no longer functions.
     *
     * @return <code>true</code> if the jar was successfully loaded.
     */
    protected synchronized static boolean addDeployJar()
    {
        boolean jarAdded = false;
        final ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader)
        {
            final URLClassLoader ucl = (URLClassLoader)cl;

            // Attempt to load deploy.jar even if it isn't already available.
            final String javaHome = System.getProperty("java.home");
            final String deployJarPath = javaHome + File.separator + "lib" + File.separator + "deploy.jar";
            final File deployJar = new File(deployJarPath);
            if (deployJar.isFile() && deployJar.canRead())
            {
                try
                {
                    final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    addUrl.setAccessible(true);
                    final URI uri = deployJar.toURI();
                    addUrl.invoke(ucl, uri.toURL());
                    jarAdded = true;
                }
                catch (final Exception e)
                {
                    LOGGER.error("Failed to load " + deployJarPath, e);
                }
            }
            else
            {
                LOGGER.warn(deployJarPath + " is not a file or is not readable.  Please ensure you are using Oracle Java.");
            }
        }
        else
        {
            LOGGER.warn("The System Class Loader is not a URLClassLoader.  Ensure that "
                    + "JAVA_HOME/lib/deploy.jar is in your CLASSPATH for proxying to work.");
        }
        return jarAdded;
    }

    /**
     * Retrieves the system scripts from <code>AutoProxyScript</code>.
     *
     * @throws ClassNotFoundException if unable to load
     *             <code>AutoProxyScript</code>.
     */
    protected synchronized static void retrieveSystemScripts() throws ClassNotFoundException
    {
        final Field[] fields = AutoProxyScript.class.getFields();
        String preMyIp = null;
        String postMyIp = null;
        for (final Field field : fields)
        {
            try
            {
                if (field.getName().startsWith("js") && !field.getName().endsWith("ForIE"))
                {
                    final String fieldValue = (String)field.get(null);
                    if (field.getName().endsWith("0"))
                    {
                        preMyIp = fieldValue;
                    }
                    else if (field.getName().endsWith("1"))
                    {
                        postMyIp = fieldValue;
                    }
                    else
                    {
                        LOGGER.debug(field.getName() + "=" + fieldValue);
                        systemScripts.add(fieldValue);
                    }
                }
            }
            catch (final IllegalArgumentException e)
            {
                throw new IllegalStateException("Failed to retrieve the value for AutoProxyScript." + field.getName(), e);
            }
            catch (final IllegalAccessException e)
            {
                throw new IllegalStateException("Failed to retrieve the value for AutoProxyScript." + field.getName(), e);
            }
        }

        if (preMyIp != null && postMyIp != null)
        {
            final InetAddress localHost;
            try
            {
                localHost = InetAddress.getLocalHost();
            }
            catch (final UnknownHostException e)
            {
                throw new IllegalStateException("Failed to get the localhost instance", e);
            }
            LOGGER.debug(preMyIp + localHost.getHostAddress() + postMyIp);
            systemScripts.add(preMyIp + localHost.getHostAddress() + postMyIp);
        }
    }

    /**
     * Loads the system scripts into the script engine.
     *
     * @throws ScriptException if a script fails to evaluate.
     */
    protected synchronized void loadSystemScripts() throws ScriptException
    {
        ProxyScriptRunner.initialize();
        if (!systemScriptsLoaded.get())
        {
            for (final String script : systemScripts)
            {
                scriptEngine.eval(script);
            }
            systemScriptsLoaded.set(true);
        }
    }

    /**
     * Adds another script to the engine.
     *
     * @param reader the script to add.
     * @throws ScriptException if an error occurs while processing the new
     *             script.
     */
    public void addScript(final Reader reader) throws ScriptException
    {
        // Ensure that the system scripts are loaded.
        if (!systemScriptsLoaded.get())
        {
            loadSystemScripts();
        }
        scriptEngine.eval(reader);
    }
    
    /**
     * Get the system scripts. This is used for testing.
     * @return The list of loaded scripts.
     */
    public synchronized static Collection<String> getSystemScripts()
    {
    	return systemScripts;
    }

    /**
     * Invokes the <code>FindProxyForURL</code> proxy function to determine the
     * appropriate proxy server configuration for the given URL/host
     * combination.
     *
     * @param url the destination URL.
     * @param hostName the destination host name.
     * @return the list of <code>ProxyHostConfig</code>s. They should be
     *         attempted in order until one succeeds.
     * @throws ScriptException if an error occurs while invoking
     *             <code>FindProxyForURL</code>.
     * @throws NoSuchMethodException if the <code>FindProxyForURL</code> method
     *             does not exist. Pass the script to {@link #addScript(Reader)}
     *             that defines <code>FindProxyForURL</code>.
     */
    public List<ProxyHostConfig> findProxyForUrl(final URL url, final String hostName)
        throws ScriptException, NoSuchMethodException
    {
        // Ensure that the system scripts are loaded.
        if (!systemScriptsLoaded.get())
        {
            loadSystemScripts();
        }
        final List<ProxyHostConfig> configs = new ArrayList<>();
        final Object retValue = ((Invocable)scriptEngine).invokeFunction("FindProxyForURL", url.toExternalForm(), hostName);

        // If a result was returned, process it.
        if (retValue != null)
        {
            final String[] results = retValue.toString().split(";");
            for (int index = 0; index < results.length; index++)
            {
                final String result = results[index].trim();

                // Ignore "null" result values.
                if (result.equalsIgnoreCase("null") || StringUtils.isEmpty(result))
                {
                    LOGGER.debug("Ignoring a 'null' or empty result for configuration " + index + ": " + results[index]);
                    continue;
                }

                final String[] parts = result.split("(\\s|:)");
                ProxyType proxyType;
                try
                {
                    proxyType = ProxyType.valueOf(parts[0].toUpperCase());
                }
                catch (final IllegalArgumentException e)
                {
                    LOGGER.debug("Unexpected auto-proxy type for configuration " + index + ": " + parts[0], e);
                    continue;
                }

                // Ensure that the proxy type is known.
                if (proxyType == null)
                {
                    LOGGER.debug("Ignoring unknown auto-proxy type for configuration " + index + ": " + parts[0]);
                    continue;
                }

                String host = null;
                int port = -1;
                if (proxyType == ProxyType.DIRECT && parts.length > 1)
                {
                    LOGGER.debug("DIRECT proxy type contains too much information for configuration " + index + ": " + result);
                }
                else if (parts.length > 1)
                {
                    host = parts[1];

                    if (parts.length > 2)
                    {
                        try
                        {
                            port = Integer.parseInt(parts[2]);
                        }
                        catch (final NumberFormatException e)
                        {
                            LOGGER.warn("Failed to parse the port for configuration " + index + ": " + parts[2]);
                        }
                        if (parts.length > 3)
                        {
                            LOGGER.debug("Unexpected information after port found for configuration " + index + ": " + result);
                        }
                    }
                }

                configs.add(new ProxyHostConfig(proxyType, host, port));
            }
        }

        // If no configurations were found, use the default "DIRECT" method.
        if (configs.isEmpty())
        {
            configs.add(new ProxyHostConfig());
        }

        return configs;
    }
}
