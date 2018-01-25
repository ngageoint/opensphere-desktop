package io.opensphere.imagery;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.Toolbox;
import io.opensphere.imagery.gdal.GDALImageLayerData;
import io.opensphere.imagery.gdal.GDALTools;
import io.opensphere.imagery.gdal.HistoInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * The Class ImageryDataTypeInfo.
 */
public class ImageryDataTypeInfo extends DefaultDataTypeInfo
{
    /**
     * My Enabled flag. Determines whether a layer is configured to appear
     * anywhere outside the Imagery Plugin. This is different from
     * DefaultDataTypeInfo:isVisible which just turns a layer's tiles on/off
     * visually on the map.
     */
    private boolean myEnabled;

    /** The File. */
    private File myFile;

    /** The GDAL tools. */
    private GDALTools myGDALTools;

    /** The GDAL tools initialized. */
    private boolean myGDALToolsInitialized;

    /** The GDAL toools initialize lock. */
    private final ReentrantLock myGDALTooolsInitializeLock;

    /** The Histo info. */
    private HistoInfo myHistoInfo;

    /** The Image layer data. */
    private GDALImageLayerData myImageLayerData;

    /** The Imagery file source. */
    private final ImageryFileSource myImageryFileSource;

    /** The Imagery source group. */
    private final ImagerySourceGroup myImagerySourceGroup;

    /**
     * Instantiates a new imagery data type info.
     *
     * @param tb the tb
     * @param source the source
     * @param group the group
     */
    public ImageryDataTypeInfo(Toolbox tb, ImageryFileSource source, ImagerySourceGroup group)
    {
        super(tb, ImageryFileSource.IMAGE, source.generateTypeKey(), ImageryFileSource.IMAGE, source.getName(), false);
        myImageryFileSource = source;
        myImagerySourceGroup = group;
        myGDALTooolsInitializeLock = new ReentrantLock();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultDataTypeInfo other = (DefaultDataTypeInfo)obj;
        return Objects.equals(getTypeKey(), other.getTypeKey());
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public final File getFile()
    {
        return myFile;
    }

    /**
     * Gets the gDAL tools.
     *
     * @return the gDAL tools
     */
    public final GDALTools getGDALTools()
    {
        return myGDALTools;
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName()
    {
        return myImagerySourceGroup != null ? myImagerySourceGroup.getName() : "";
    }

    /**
     * Gets the histo info.
     *
     * @return the histo info
     */
    public final HistoInfo getHistoInfo()
    {
        return myHistoInfo;
    }

    /**
     * Gets the image layer data.
     *
     * @return the image layer data
     */
    public final GDALImageLayerData getImageLayerData()
    {
        return myImageLayerData;
    }

    /**
     * Gets the imagery file source.
     *
     * @return the imagery file source
     */
    public ImageryFileSource getImageryFileSource()
    {
        return myImageryFileSource;
    }

    /**
     * Gets the imagery source group.
     *
     * @return the imagery source group
     */
    public ImagerySourceGroup getImagerySourceGroup()
    {
        return myImagerySourceGroup;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (getTypeKey() == null ? 0 : getTypeKey().hashCode());
        return result;
    }

    /**
     * Retrieve gdal meta data.
     *
     * @return true, if successful
     */
    public boolean initializeGDALTools()
    {
        if (!myGDALToolsInitialized)
        {
            myGDALTooolsInitializeLock.lock();
            try
            {
                if (!myGDALToolsInitialized)
                {
                    myFile = new File(myImageryFileSource.getFilePath());
                    myGDALTools = new GDALTools(myFile, myImageryFileSource.getBoundingBox());
                    myImageLayerData = myGDALTools.retrieveGDALImageLayerDataOriginal(myFile, 512);
                    myHistoInfo = GDALTools.predictImageHistogram(myFile);
                    myGDALToolsInitialized = true;
                }
            }
            finally
            {
                myGDALTooolsInitializeLock.unlock();
            }
        }
        return myGDALToolsInitialized;
    }

    /**
     * Checks if this layer is enabled.
     *
     * @return true, if is enabled
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Sets the enabled flag.
     *
     * @param enabled the new enabled flag
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }
}
