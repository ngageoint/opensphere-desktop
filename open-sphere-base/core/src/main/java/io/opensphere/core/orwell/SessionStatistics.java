package io.opensphere.core.orwell;

import java.util.List;
import java.util.Map;

import io.opensphere.core.util.collections.New;

/**
 * A statistics container describing the user's current session.
 */
public class SessionStatistics
{
    /**
     * The date / time at which the session began.
     */
    private long myStartDate;

    /**
     * The classpath which which the application was launched.
     */
    private String myClasspath;

    /**
     * The boot classpath with which the application was launched.
     */
    private String myBootClasspath;

    /**
     * The library path with which the application was launched.
     */
    private String myLibraryPath;

    /**
     * The runtime name of the application.
     */
    private String myRuntimeName;

    /**
     * The set of arguments provided as input to the application when it was
     * invoked.
     */
    private final List<String> myInputArguments = New.list();

    /**
     * The system properties present in the current application invocation.
     */
    private final Map<String, String> mySystemProperties = New.map();

    /**
     * Gets the value of the {@link #myStartDate} field.
     *
     * @return the value stored in the {@link #myStartDate} field.
     */
    public long getStartDate()
    {
        return myStartDate;
    }

    /**
     * Sets the value of the {@link #myStartDate} field.
     *
     * @param pStartDate the value to store in the {@link #myStartDate} field.
     */
    public void setStartDate(long pStartDate)
    {
        myStartDate = pStartDate;
    }

    /**
     * Gets the value of the {@link #myClasspath} field.
     *
     * @return the value stored in the {@link #myClasspath} field.
     */
    public String getClasspath()
    {
        return myClasspath;
    }

    /**
     * Sets the value of the {@link #myClasspath} field.
     *
     * @param pClasspath the value to store in the {@link #myClasspath} field.
     */
    public void setClasspath(String pClasspath)
    {
        myClasspath = pClasspath;
    }

    /**
     * Gets the value of the {@link #myBootClasspath} field.
     *
     * @return the value stored in the {@link #myBootClasspath} field.
     */
    public String getBootClasspath()
    {
        return myBootClasspath;
    }

    /**
     * Sets the value of the {@link #myBootClasspath} field.
     *
     * @param pBootClasspath the value to store in the {@link #myBootClasspath}
     *            field.
     */
    public void setBootClasspath(String pBootClasspath)
    {
        myBootClasspath = pBootClasspath;
    }

    /**
     * Gets the value of the {@link #myLibraryPath} field.
     *
     * @return the value stored in the {@link #myLibraryPath} field.
     */
    public String getLibraryPath()
    {
        return myLibraryPath;
    }

    /**
     * Sets the value of the {@link #myLibraryPath} field.
     *
     * @param pLibraryPath the value to store in the {@link #myLibraryPath}
     *            field.
     */
    public void setLibraryPath(String pLibraryPath)
    {
        myLibraryPath = pLibraryPath;
    }

    /**
     * Gets the value of the {@link #myRuntimeName} field.
     *
     * @return the value stored in the {@link #myRuntimeName} field.
     */
    public String getRuntimeName()
    {
        return myRuntimeName;
    }

    /**
     * Sets the value of the {@link #myRuntimeName} field.
     *
     * @param pRuntimeName the value to store in the {@link #myRuntimeName}
     *            field.
     */
    public void setRuntimeName(String pRuntimeName)
    {
        myRuntimeName = pRuntimeName;
    }

    /**
     * Gets the value of the {@link #myInputArguments} field.
     *
     * @return the value stored in the {@link #myInputArguments} field.
     */
    public List<String> getInputArguments()
    {
        return myInputArguments;
    }

    /**
     * Gets the value of the {@link #mySystemProperties} field.
     *
     * @return the value stored in the {@link #mySystemProperties} field.
     */
    public Map<String, String> getSystemProperties()
    {
        return mySystemProperties;
    }
}
