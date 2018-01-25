package com.bitsys.common.http.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a {@link ProxyResolver} specializing in automatic proxy
 * configuration scripts.
 */
public class AutomaticProxyResolver implements ProxyResolver
{
   /** The <code>Logger</code> instance. */
   private static final Logger LOGGER = LoggerFactory
      .getLogger(AutomaticProxyResolver.class);

   /** The automatic proxy script URL. */
   private final URL scriptUrl;

   /** Runs the web proxy scripts. */
   private final ProxyScriptRunner proxyScriptRunner;

   /**
    * Constructs a new {@linkplain AutomaticProxyResolver} with the given script
    * URL.
    *
    * @param scriptUrl
    *           the script URL.
    */
   public AutomaticProxyResolver(final URL scriptUrl)
   {
      if (scriptUrl == null)
      {
         throw new IllegalArgumentException("The script URL is null");
      }
      this.scriptUrl = scriptUrl;
      try {
         proxyScriptRunner = new ProxyScriptRunner();
      }
      catch (final Exception e) {
         throw new IllegalStateException("Failed to initialize the proxy script runner", e);
      }
   }

   @Override
   public List<ProxyHostConfig> getProxyServer(final URL destination)
      throws IOException
   {
      final List<ProxyHostConfig> configs = new ArrayList<>();
      final InputStream inputStream = scriptUrl.openStream();
      final String response = null;
      try
      {
         proxyScriptRunner.addScript(new InputStreamReader(inputStream));
         configs.addAll(proxyScriptRunner.findProxyForUrl(destination, destination.getHost()));
         LOGGER.debug("Proxying " + destination + " through " + response);
      }
      catch (final ScriptException e) {
         throw new IllegalStateException(e);
      }
      catch (final NoSuchMethodException e) {
         throw new IllegalStateException(e);
      }
      finally
      {
         IOUtils.closeQuietly(inputStream);
      }

      return configs;
   }

   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("AutomaticProxyResolver [Script URL=");
      builder.append(scriptUrl);
      builder.append("]");
      return builder.toString();
   }
}
