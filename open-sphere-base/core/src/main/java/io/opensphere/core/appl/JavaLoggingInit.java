package io.opensphere.core.appl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;

/**
 * Responsible for initializing the Java logging framework.
 */
public class JavaLoggingInit
{
    /** Constructor that performs the initialization. */
    public JavaLoggingInit()
    {
        // Only load the java logging properties file if another configuration
        // has not been set.
        InputStream is = null;
        String configFile = System.getProperty("java.util.logging.config.file");
        if (configFile != null)
        {
            try
            {
                InputStream in = new FileInputStream(configFile);
                is = new BufferedInputStream(in);
            }
            catch (FileNotFoundException e)
            {
                Logger.getLogger(JavaLoggingInit.class).error("Failed to read java.util.logging.config.file: " + e, e);
            }
        }
        if (is == null)
        {
            is = JavaLoggingInit.class.getClassLoader().getResourceAsStream("java-logging.properties");
        }
        try
        {
            LogManager.getLogManager().readConfiguration(is);
        }
        catch (SecurityException | IOException e)
        {
            Logger.getLogger(JavaLoggingInit.class).error("Failed to read java logging configuration: " + e, e);
        }
        catch (RuntimeException | Error e)
        {
            Logger.getLogger(JavaLoggingInit.class).error("Failed to initialize java logging: " + e, e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                Logger.getLogger(JavaLoggingInit.class).error("Could not close logging config stream: " + e, e);
            }
        }
    }
}
