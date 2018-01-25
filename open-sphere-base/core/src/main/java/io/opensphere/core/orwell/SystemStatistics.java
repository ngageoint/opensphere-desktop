package io.opensphere.core.orwell;

/**
 * A container in which the statistics describing the operating system are stored.
 */
public class SystemStatistics
{
    /**
     * The processor architecture of the system.
     */
    private String myArchitecture;

    /**
     * The number of available processors reported by the system.
     */
    private long myAvailableProcessors;

    /**
     * The name of the operating system.
     */
    private String myOperatingSystemName;

    /**
     * The version of the operating system.
     */
    private String myOperatingSystemVersion;

    /**
     * The average load on the system captured at the time of metrics gathering.
     */
    private double mySystemLoadAverage;

    /**
    * The version of the Java Management Specification implemented by the underlying virtual machine.
    */
    private String myManagementSpecVersion;

    /**
    * The name of the Java specification implemented by the underlying virtual machine.
    */
    private String mySpecName;

    /**
    * The vendor providing the implementation of the Java specification in the underlying virtual machine.
    */
    private String mySpecVendor;

    /**
    *  The version of the Java specification implemented by the underlying virtual machine.
    */
    private String mySpecVersion;

    /**
     * The name of the underlying virtual machine.
     */
    private String myJavaVmName;

    /**
     * The vendor that provided the implementation of the underlying virtual machine.
     */
    private String myJavaVmVendor;

    /**
     * the version of the underlying virtual machine.
     */
    private String myJavaVmVersion;

    /**
     * Gets the value of the {@link #myArchitecture} field.
     *
     * @return the value stored in the {@link #myArchitecture} field.
     */
    public String getArchitecture()
    {
        return myArchitecture;
    }

    /**
     * Sets the value of the {@link #myArchitecture} field.
     *
     * @param pArchitecture the value to store in the {@link #myArchitecture} field.
     */
    public void setArchitecture(String pArchitecture)
    {
        myArchitecture = pArchitecture;
    }

    /**
     * Gets the value of the {@link #myAvailableProcessors} field.
     *
     * @return the value stored in the {@link #myAvailableProcessors} field.
     */
    public long getAvailableProcessors()
    {
        return myAvailableProcessors;
    }

    /**
     * Sets the value of the {@link #myAvailableProcessors} field.
     *
     * @param pAvailableProcessors the value to store in the {@link #myAvailableProcessors} field.
     */
    public void setAvailableProcessors(long pAvailableProcessors)
    {
        myAvailableProcessors = pAvailableProcessors;
    }

    /**
     * Gets the value of the {@link #myOperatingSystemName} field.
     *
     * @return the value stored in the {@link #myOperatingSystemName} field.
     */
    public String getOperatingSystemName()
    {
        return myOperatingSystemName;
    }

    /**
     * Sets the value of the {@link #myOperatingSystemName} field.
     *
     * @param pOperatingSystemName the value to store in the {@link #myOperatingSystemName} field.
     */
    public void setOperatingSystemName(String pOperatingSystemName)
    {
        myOperatingSystemName = pOperatingSystemName;
    }

    /**
     * Gets the value of the {@link #myOperatingSystemVersion} field.
     *
     * @return the value stored in the {@link #myOperatingSystemVersion} field.
     */
    public String getOperatingSystemVersion()
    {
        return myOperatingSystemVersion;
    }

    /**
     * Sets the value of the {@link #myOperatingSystemVersion} field.
     *
     * @param pOperatingSystemVersion the value to store in the {@link #myOperatingSystemVersion} field.
     */
    public void setOperatingSystemVersion(String pOperatingSystemVersion)
    {
        myOperatingSystemVersion = pOperatingSystemVersion;
    }

    /**
     * Gets the value of the {@link #mySystemLoadAverage} field.
     *
     * @return the value stored in the {@link #mySystemLoadAverage} field.
     */
    public double getSystemLoadAverage()
    {
        return mySystemLoadAverage;
    }

    /**
     * Sets the value of the {@link #mySystemLoadAverage} field.
     *
     * @param pSystemLoadAverage the value to store in the {@link #mySystemLoadAverage} field.
     */
    public void setSystemLoadAverage(double pSystemLoadAverage)
    {
        mySystemLoadAverage = pSystemLoadAverage;
    }

    /**
     * Gets the value of the {@link #myManagementSpecVersion} field.
     *
     * @return the value stored in the {@link #myManagementSpecVersion} field.
     */
    public String getManagementSpecVersion()
    {
        return myManagementSpecVersion;
    }

    /**
     * Sets the value of the {@link #myManagementSpecVersion} field.
     *
     * @param pManagementSpecVersion
     *            the value to store in the {@link #myManagementSpecVersion} field.
     */
    public void setManagementSpecVersion(String pManagementSpecVersion)
    {
        myManagementSpecVersion = pManagementSpecVersion;
    }

    /**
     * Gets the value of the {@link #mySpecName} field.
     *
     * @return the value stored in the {@link #mySpecName} field.
     */
    public String getSpecName()
    {
        return mySpecName;
    }

    /**
     * Sets the value of the {@link #mySpecName} field.
     *
     * @param pSpecName
     *            the value to store in the {@link #mySpecName} field.
     */
    public void setSpecName(String pSpecName)
    {
        mySpecName = pSpecName;
    }

    /**
     * Gets the value of the {@link #mySpecVendor} field.
     *
     * @return the value stored in the {@link #mySpecVendor} field.
     */
    public String getSpecVendor()
    {
        return mySpecVendor;
    }

    /**
     * Sets the value of the {@link #mySpecVendor} field.
     *
     * @param pSpecVendor
     *            the value to store in the {@link #mySpecVendor} field.
     */
    public void setSpecVendor(String pSpecVendor)
    {
        mySpecVendor = pSpecVendor;
    }

    /**
     * Gets the value of the {@link #mySpecVersion} field.
     *
     * @return the value stored in the {@link #mySpecVersion} field.
     */
    public String getSpecVersion()
    {
        return mySpecVersion;
    }

    /**
     * Sets the value of the {@link #mySpecVersion} field.
     *
     * @param pSpecVersion
     *            the value to store in the {@link #mySpecVersion} field.
     */
    public void setSpecVersion(String pSpecVersion)
    {
        mySpecVersion = pSpecVersion;
    }

    /**
     * Gets the value of the {@link #myJavaVmName} field.
     *
     * @return the value stored in the {@link #myJavaVmName} field.
     */
    public String getJavaVmName()
    {
        return myJavaVmName;
    }

    /**
     * Sets the value of the {@link #myJavaVmName} field.
     *
     * @param pJavaVmName
     *            the value to store in the {@link #myJavaVmName} field.
     */
    public void setJavaVmName(String pJavaVmName)
    {
        myJavaVmName = pJavaVmName;
    }

    /**
     * Gets the value of the {@link #myJavaVmVendor} field.
     *
     * @return the value stored in the {@link #myJavaVmVendor} field.
     */
    public String getJavaVmVendor()
    {
        return myJavaVmVendor;
    }

    /**
     * Sets the value of the {@link #myJavaVmVendor} field.
     *
     * @param pJavaVmVendor
     *            the value to store in the {@link #myJavaVmVendor} field.
     */
    public void setJavaVmVendor(String pJavaVmVendor)
    {
        myJavaVmVendor = pJavaVmVendor;
    }

    /**
     * Gets the value of the {@link #myJavaVmVersion} field.
     *
     * @return the value stored in the {@link #myJavaVmVersion} field.
     */
    public String getJavaVmVersion()
    {
        return myJavaVmVersion;
    }

    /**
     * Sets the value of the {@link #myJavaVmVersion} field.
     *
     * @param pJavaVmVersion
     *            the value to store in the {@link #myJavaVmVersion} field.
     */
    public void setJavaVmVersion(String pJavaVmVersion)
    {
        myJavaVmVersion = pJavaVmVersion;
    }
}
