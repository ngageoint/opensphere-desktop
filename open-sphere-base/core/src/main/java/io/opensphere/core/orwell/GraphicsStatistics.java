package io.opensphere.core.orwell;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A container in which the statistics describing the graphics system are
 * stored.
 */
public class GraphicsStatistics
{
    /**
     * The type of the underlying subsystem, i.e.: NativeWindowFactory.TYPE_KD,
     * NativeWindowFactory.TYPE_X11, etc.
     */
    private String myGraphicsDeviceType;

    /**
     * The semantic GraphicsDevice connection. On platforms supporting remote
     * devices, e.g.: via TCP/IP network, the implementation is a unique name
     * for each remote address. On X11 for example, the connection string should
     * be as the following example.
     * <ul>
     * <li><code>:0.0</code> for a local connection</li>
     * <li><code>remote.host.net:0.0</code> for a remote connection</li>
     * </ul>
     */
    private String myConnection;

    /**
     * The graphics device <code>unit ID</code>. The <code>unit ID</code>
     * support multiple graphics device configurations on a local machine.
     */
    private int myUnitID;

    /**
     * The name of the environment.
     */
    private String myEnvironmentIdentifier;

    /**
     * The version of the render context implementation.
     */
    private float myContextVersion;

    /**
     * The size of the GPU memory, in bytes.
     */
    private long myGpuMemorySizeBytes;

    /**
     * The swap interval of the GL Context.
     */
    private int mySwapInterval;

    /**
     * The name of the GL implementation's base class.
     */
    private String myGlImplBaseClassName;

    /**
     * The name of the class in which the OpenGL specification is implemented.
     */
    private String myImplName;

    /**
     * The human readable name of the OpenGL implementation.
     */
    private String myName;

    /**
     * True if rasterization is supported in hardware.
     */
    private boolean myHardwareRasterizer;

    /**
     * The name of the renderer.
     */
    private String myGlRenderer;

    /**
     * the name of the vendor that implemented the GL library.
     */
    private String myGlVendor;

    /**
     * the version of the GL specification implemented by the vendor.
     */
    private String myGlVersion;

    /**
     * The extensions string provided by the GL Implementation.
     */
    private String myGlExtensionsString;

    /**
     * The platform-specific extensions string provided by the GL
     * Implementation.
     */
    private String myPlatformExtensionsString;

    /**
     * Indicates if the GL implementation supports GLSL.
     */
    private boolean myHasGLSL;

    /**
     * Indicates if the GL Implementation supports the glCompileShader function.
     */
    private boolean myGlCompileShaderAvailable;

    /**
     * Gets the value of the {@link #myGraphicsDeviceType} field.
     *
     * @return the value stored in the {@link #myGraphicsDeviceType} field.
     */
    public String getGraphicsDeviceType()
    {
        return myGraphicsDeviceType;
    }

    /**
     * Sets the value of the {@link #myGraphicsDeviceType} field.
     *
     * @param pGraphicsDeviceType the value to store in the
     *            {@link #myGraphicsDeviceType} field.
     */
    public void setGraphicsDeviceType(String pGraphicsDeviceType)
    {
        myGraphicsDeviceType = pGraphicsDeviceType;
    }

    /**
     * Gets the value of the {@link #myConnection} field.
     *
     * @return the value stored in the {@link #myConnection} field.
     */
    public String getConnection()
    {
        return myConnection;
    }

    /**
     * Sets the value of the {@link #myConnection} field.
     *
     * @param pConnection the value to store in the {@link #myConnection} field.
     */
    public void setConnection(String pConnection)
    {
        myConnection = pConnection;
    }

    /**
     * Gets the value of the {@link #myUnitID} field.
     *
     * @return the value stored in the {@link #myUnitID} field.
     */
    public int getUnitID()
    {
        return myUnitID;
    }

    /**
     * Sets the value of the {@link #myUnitID} field.
     *
     * @param pUnitID the value to store in the {@link #myUnitID} field.
     */
    public void setUnitID(int pUnitID)
    {
        myUnitID = pUnitID;
    }

    /**
     * Gets the value of the {@link #myEnvironmentIdentifier} field.
     *
     * @return the value stored in the {@link #myEnvironmentIdentifier} field.
     */
    public String getEnvironmentIdentifier()
    {
        return myEnvironmentIdentifier;
    }

    /**
     * Sets the value of the {@link #myEnvironmentIdentifier} field.
     *
     * @param pEnvironmentIdentifier the value to store in the
     *            {@link #myEnvironmentIdentifier} field.
     */
    public void setEnvironmentIdentifier(String pEnvironmentIdentifier)
    {
        myEnvironmentIdentifier = pEnvironmentIdentifier;
    }

    /**
     * Gets the value of the {@link #myContextVersion} field.
     *
     * @return the value stored in the {@link #myContextVersion} field.
     */
    public float getContextVersion()
    {
        return myContextVersion;
    }

    /**
     * Sets the value of the {@link #myContextVersion} field.
     *
     * @param pContextVersion the value to store in the
     *            {@link #myContextVersion} field.
     */
    public void setContextVersion(float pContextVersion)
    {
        myContextVersion = pContextVersion;
    }

    /**
     * Gets the value of the {@link #myGpuMemorySizeBytes} field.
     *
     * @return the value stored in the {@link #myGpuMemorySizeBytes} field.
     */
    public long getGpuMemorySizeBytes()
    {
        return myGpuMemorySizeBytes;
    }

    /**
     * Sets the value of the {@link #myGpuMemorySizeBytes} field.
     *
     * @param pGpuMemorySizeBytes the value to store in the
     *            {@link #myGpuMemorySizeBytes} field.
     */
    public void setGpuMemorySizeBytes(long pGpuMemorySizeBytes)
    {
        myGpuMemorySizeBytes = pGpuMemorySizeBytes;
    }

    /**
     * Gets the value of the {@link #mySwapInterval} field.
     *
     * @return the value stored in the {@link #mySwapInterval} field.
     */
    public int getSwapInterval()
    {
        return mySwapInterval;
    }

    /**
     * Sets the value of the {@link #mySwapInterval} field.
     *
     * @param pSwapInterval the value to store in the {@link #mySwapInterval}
     *            field.
     */
    public void setSwapInterval(int pSwapInterval)
    {
        mySwapInterval = pSwapInterval;
    }

    /**
     * Gets the value of the {@link #myGlImplBaseClassName} field.
     *
     * @return the value stored in the {@link #myGlImplBaseClassName} field.
     */
    public String getGlImplBaseClassName()
    {
        return myGlImplBaseClassName;
    }

    /**
     * Sets the value of the {@link #myGlImplBaseClassName} field.
     *
     * @param pGlImplBaseClassName the value to store in the
     *            {@link #myGlImplBaseClassName} field.
     */
    public void setGlImplBaseClassName(String pGlImplBaseClassName)
    {
        myGlImplBaseClassName = pGlImplBaseClassName;
    }

    /**
     * Gets the value of the {@link #myImplName} field.
     *
     * @return the value stored in the {@link #myImplName} field.
     */
    public String getImplName()
    {
        return myImplName;
    }

    /**
     * Sets the value of the {@link #myImplName} field.
     *
     * @param pImplName the value to store in the {@link #myImplName} field.
     */
    public void setImplName(String pImplName)
    {
        myImplName = pImplName;
    }

    /**
     * Gets the value of the {@link #myName} field.
     *
     * @return the value stored in the {@link #myName} field.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets the value of the {@link #myName} field.
     *
     * @param pName the value to store in the {@link #myName} field.
     */
    public void setName(String pName)
    {
        myName = pName;
    }

    /**
     * Gets the value of the {@link #myHardwareRasterizer} field.
     *
     * @return the value stored in the {@link #myHardwareRasterizer} field.
     */
    public boolean isHardwareRasterizer()
    {
        return myHardwareRasterizer;
    }

    /**
     * Sets the value of the {@link #myHardwareRasterizer} field.
     *
     * @param pHardwareRasterizer the value to store in the
     *            {@link #myHardwareRasterizer} field.
     */
    public void setHardwareRasterizer(boolean pHardwareRasterizer)
    {
        myHardwareRasterizer = pHardwareRasterizer;
    }

    /**
     * Gets the value of the {@link #myGlRenderer} field.
     *
     * @return the value stored in the {@link #myGlRenderer} field.
     */
    public String getGlRenderer()
    {
        return myGlRenderer;
    }

    /**
     * Sets the value of the {@link #myGlRenderer} field.
     *
     * @param pGlRenderer the value to store in the {@link #myGlRenderer} field.
     */
    public void setGlRenderer(String pGlRenderer)
    {
        myGlRenderer = pGlRenderer;
    }

    /**
     * Gets the value of the {@link #myGlVendor} field.
     *
     * @return the value stored in the {@link #myGlVendor} field.
     */
    public String getGlVendor()
    {
        return myGlVendor;
    }

    /**
     * Sets the value of the {@link #myGlVendor} field.
     *
     * @param pGlVendor the value to store in the {@link #myGlVendor} field.
     */
    public void setGlVendor(String pGlVendor)
    {
        myGlVendor = pGlVendor;
    }

    /**
     * Gets the value of the {@link #myGlVersion} field.
     *
     * @return the value stored in the {@link #myGlVersion} field.
     */
    public String getGlVersion()
    {
        return myGlVersion;
    }

    /**
     * Sets the value of the {@link #myGlVersion} field.
     *
     * @param pGlVersion the value to store in the {@link #myGlVersion} field.
     */
    public void setGlVersion(String pGlVersion)
    {
        myGlVersion = pGlVersion;
    }

    /**
     * Gets the value of the {@link #myGlExtensionsString} field.
     *
     * @return the value stored in the {@link #myGlExtensionsString} field.
     */
    public String getGlExtensionsString()
    {
        return myGlExtensionsString;
    }

    /**
     * Sets the value of the {@link #myGlExtensionsString} field.
     *
     * @param pGlExtensionsString the value to store in the
     *            {@link #myGlExtensionsString} field.
     */
    public void setGlExtensionsString(String pGlExtensionsString)
    {
        myGlExtensionsString = pGlExtensionsString;
    }

    /**
     * Gets the value of the {@link #myPlatformExtensionsString} field.
     *
     * @return the value stored in the {@link #myPlatformExtensionsString}
     *         field.
     */
    public String getPlatformExtensionsString()
    {
        return myPlatformExtensionsString;
    }

    /**
     * Sets the value of the {@link #myPlatformExtensionsString} field.
     *
     * @param pPlatformExtensionsString the value to store in the
     *            {@link #myPlatformExtensionsString} field.
     */
    public void setPlatformExtensionsString(String pPlatformExtensionsString)
    {
        myPlatformExtensionsString = pPlatformExtensionsString;
    }

    /**
     * Gets the value of the {@link #myHasGLSL} field.
     *
     * @return the value stored in the {@link #myHasGLSL} field.
     */
    public boolean isHasGLSL()
    {
        return myHasGLSL;
    }

    /**
     * Sets the value of the {@link #myHasGLSL} field.
     *
     * @param pHasGLSL the value to store in the {@link #myHasGLSL} field.
     */
    public void setHasGLSL(boolean pHasGLSL)
    {
        myHasGLSL = pHasGLSL;
    }

    /**
     * Gets the value of the {@link #myGlCompileShaderAvailable} field.
     *
     * @return the value stored in the {@link #myGlCompileShaderAvailable}
     *         field.
     */
    public boolean isGlCompileShaderAvailable()
    {
        return myGlCompileShaderAvailable;
    }

    /**
     * Sets the value of the {@link #myGlCompileShaderAvailable} field.
     *
     * @param pGlCompileShaderAvailable the value to store in the
     *            {@link #myGlCompileShaderAvailable} field.
     */
    public void setGlCompileShaderAvailable(boolean pGlCompileShaderAvailable)
    {
        myGlCompileShaderAvailable = pGlCompileShaderAvailable;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
