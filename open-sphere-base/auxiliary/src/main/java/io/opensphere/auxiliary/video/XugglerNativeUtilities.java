package io.opensphere.auxiliary.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import io.opensphere.core.util.io.StreamReader;

/**
 * Utility class for extracting the Xuggler native libraries.
 */
public final class XugglerNativeUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(XugglerNativeUtilities.class);

    /** True when extracting the native libraries has been attempted. */
    private static boolean ourExplodeAttempted;

    /**
     * Extract the native libraries(s) from the jar and put them in a place
     * where Xuggler can use them.
     */
    public static synchronized void explodeXugglerNatives()
    {
        if (!ourExplodeAttempted)
        {
            ourExplodeAttempted = true;

            // Create the directory to store the libs if necessary.
            String outPath = System.getProperty("java.io.tmpdir") + File.separator + "XugglerNative";
            File outputDir = new File(outPath);
            if (!outputDir.exists() && !outputDir.mkdir())
            {
                return;
            }

            // Add the location to the java.library.path so that Xuggler can
            // find the libs.
            String pathVar = System.getProperty("java.library.path", "");
            StringBuilder pathBuilder = new StringBuilder(pathVar);
            if (!pathVar.isEmpty())
            {
                pathBuilder.append(System.getProperty("path.separator"));
            }
            pathBuilder.append(outPath);
            System.setProperty("java.library.path", pathBuilder.toString());

            // extract the libs for the correct OS
            String osName = System.getProperty("os.name");
            if (osName.contains("Windows"))
            {
                boolean amd64 = "amd64".equals(System.getProperty("os.arch"));
                String prefix = amd64 ? "win32/x86_64/" : "win32/x86/";
                String libgcc = amd64 ? "libgcc_s_seh-1.dll" : "libgcc_s_sjlj-1.dll";

                explodeLib(prefix, outputDir, libgcc);
                System.load(outPath + File.separator + libgcc);
                explodeLib(prefix, outputDir, "libstdc++-6.dll");
                System.load(outPath + File.separator + "libstdc++-6.dll");
                // We do not need to load this, Xuggler will do it for us.
                explodeLib(prefix, outputDir, "libxuggle-5.dll");
            }
            else
            {
                explodeLib("linux/x86_64/", outputDir, "libxuggle.so");
            }
        }
    }

    /**
     * Extract the library from the path location and write it to a file in the
     * given location.
     *
     * @param libResourcePrefix The resource path where the library currently
     *            resides.
     * @param outputDir The directory where the library should be written.
     * @param libName The name of the library in the output location.
     */
    private static void explodeLib(String libResourcePrefix, File outputDir, String libName)
    {
        try (InputStream libStream = XugglerNativeUtilities.class.getClassLoader()
                .getResourceAsStream(libResourcePrefix + libName))
        {
            // Always write the file in case a new version is deployed
            File libFile = new File(outputDir.getAbsolutePath() + File.separator + libName);
            try (FileOutputStream libOutput = new FileOutputStream(libFile))
            {
                StreamReader reader = new StreamReader(libStream);
                reader.readStreamToOutputStream(libOutput);
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to extract xuggler native library." + e, e);
        }
    }

    /** Disallow instantiation. */
    private XugglerNativeUtilities()
    {
    }
}
