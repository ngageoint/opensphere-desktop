package io.opensphere.develop.util.launch;

/** Represents operating system info. */
public enum OsInfo
{
    /** Describes the windows launcher configuration. */
    WINDOWS("Windows", "win32"),

    /** Describes the linux launcher configuration. */
    LINUX("Linux", "linux"),

    /** Describes the macos launcher configuration. */
    MACOS("Macos", "macosx");

    /** The extension applied to the generated launcher. */
    private final String myExtension;

    /** The directory in which the launcher will run. */
    private final String myDirectory;

    /**
     * Constructor.
     *
     * @param extension The extension
     * @param directory The directory
     */
    private OsInfo(String extension, String directory)
    {
        myExtension = extension;
        myDirectory = directory;
    }

    /**
     * Gets the extension.
     *
     * @return the extension
     */
    public String getExtension()
    {
        return myExtension;
    }

    /**
     * Gets the directory.
     *
     * @return the directory
     */
    public String getDirectory()
    {
        return myDirectory;
    }
}
