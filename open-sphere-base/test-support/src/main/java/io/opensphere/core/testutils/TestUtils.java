package io.opensphere.core.testutils;

/** Utilities for unit and functional tests. */
public final class TestUtils
{
    /**
     * Converts a data file path to the appropriate path for the runtime OS.
     *
     * @param path the path
     * @return the converted path
     */
    public static String convertDataPath(String path)
    {
        String modifiedPath;
        boolean isLinux = "Linux".equals(System.getProperty("os.name"));
        if (isLinux)
        {
            modifiedPath = path.replace("\\", "/");
            modifiedPath = modifiedPath.replace("X:", "/data");
        }
        else
        {
            modifiedPath = path.replace("/data", "X:");
            modifiedPath = modifiedPath.replace("/", "\\");
        }
        return modifiedPath;
    }

    /** Private constructor. */
    private TestUtils()
    {
    }
}
