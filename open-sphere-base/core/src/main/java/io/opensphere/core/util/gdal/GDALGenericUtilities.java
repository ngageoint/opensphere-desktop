package io.opensphere.core.util.gdal;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.gdal.gdal.gdal;

/**
 * Utility class for keeping track of whether the GDAL environment has been
 * initialized and initializing it if necessary.
 */
public final class GDALGenericUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GDALGenericUtilities.class);

    /** True when GDAL has successfully loaded. */
    private static boolean ourGDALAvailable;

    /** True when the attempt to load GDAL has been done. */
    private static boolean ourLoadGDALAttempted;

    /** FQCN for Logger calls. */
    private static final String FQCN = GDALGenericUtilities.class.getName();

    /**
     * Attempt to load the GDAL environment.
     *
     * @return true when load is successful or when GDAL is already available.
     */
    public static synchronized boolean loadGDAL()
    {
        if (!ourLoadGDALAttempted)
        {
            ourLoadGDALAttempted = true;

            // The library name is different on different operating systems.
            // Just try them both.
            UnsatisfiedLinkError ex = null;
            for (String lib : new String[] { "gdal19", "gdal" })
            {
                try
                {
                    System.loadLibrary(lib);

                    try
                    {
                        System.loadLibrary("gdaljni");
                        System.loadLibrary("gdalconstjni");
                        System.loadLibrary("osrjni");
                        System.loadLibrary("ogrjni");
                        System.loadLibrary("proj");
                    }
                    catch (UnsatisfiedLinkError e)
                    {
                        ex = e;
                        break;
                    }

                    ex = null;
                    gdal.AllRegister();
                    LOGGER.info("GDAL Native Library loaded (version: " + gdal.VersionInfo("RELEASE_NAME") + ")");
                    ourGDALAvailable = true;

                    break;
                }
                catch (UnsatisfiedLinkError e)
                {
                    ex = e;
                }
            }
            if (ex != null)
            {
                LOGGER.warn("Failed to load the GDAL native libs: " + ex, ex);
                ourGDALAvailable = false;
            }
        }

        return ourGDALAvailable;
    }

    /**
     * Print the information for the last GDAL error to occur.
     *
     * @param logger The logger to use.
     * @param priority The priority of the log messages generated.
     */
    public static void logLastError(Logger logger, Priority priority)
    {
        logger.log(FQCN, priority, "Last GDAL error: " + gdal.GetLastErrorMsg(), null);
        logger.log(FQCN, priority, "Last GDAL error no: " + gdal.GetLastErrorNo(), null);
        logger.log(FQCN, priority, "Last GDAL error type: " + gdal.GetLastErrorType(), null);
    }

    /** Disallow instantiation. */
    private GDALGenericUtilities()
    {
    }
}
