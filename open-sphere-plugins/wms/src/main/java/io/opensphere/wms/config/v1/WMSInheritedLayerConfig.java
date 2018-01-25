package io.opensphere.wms.config.v1;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * These values affect configuration for the layer, but may not be specific to
 * the layer. They may be inherited from either parent layers or from the server
 * configuration.
 */
public class WMSInheritedLayerConfig implements Cloneable
{
    /** The bounding region for the layer. */
    private GeographicBoundingBox myBoundingBox;

    /** List of exception formats. */
    private Collection<String> myExceptionFormats;

    /** List of getMap image formats. */
    private Collection<String> myGetMapFormats;

    /** The list of Spatial Reference Systems supported by this layer. */
    private Set<String> mySrsOptions = new TreeSet<>();

    /** The available styles for the layer. */
    private Set<String> myStyles = new TreeSet<>();

    /** The list (path) of directories to this layer. */
    private List<String> myDirectoryPath = new LinkedList<>();

    /**
     * Adds a level to the end of the directory path.
     *
     * @param name the name of the level to add
     */
    public void addLevelToPath(String name)
    {
        if (StringUtils.isNotEmpty(name))
        {
            myDirectoryPath.add(name);
        }
    }

    /**
     * Adds a Spatial Reference System to the local list.
     *
     * @param srs the SRS to add
     */
    public void addSrsOption(String srs)
    {
        mySrsOptions.add(srs);
    }

    @Override
    public WMSInheritedLayerConfig clone() throws CloneNotSupportedException
    {
        WMSInheritedLayerConfig config = (WMSInheritedLayerConfig)super.clone();

        // Create copies of SRSs and Styles so that layers can have their own
        // unique sets. Exception and GetMap Formats will be the same for all
        // layers, so let Object.clone() create those.
        config.setSRSOptions(mySrsOptions);
        config.setStyles(myStyles);

        config.setDirectoryPath(myDirectoryPath);

        return config;
    }

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     */
    public GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Gets the directory path as an ordered list.
     *
     * @return the directory path list
     */
    public List<String> getDirectoryPath()
    {
        return myDirectoryPath;
    }

    /**
     * Gets the exception formats.
     *
     * @return the exception formats
     */
    public Collection<String> getExceptionFormats()
    {
        return myExceptionFormats;
    }

    /**
     * Gets the getMap image formats.
     *
     * @return the getMap formats
     */
    public Collection<String> getGetMapFormats()
    {
        return myGetMapFormats;
    }

    /**
     * Gets the SRS list.
     *
     * @return the SRS list
     */
    public Collection<String> getSRSOptions()
    {
        return mySrsOptions;
    }

    /**
     * Get the styles.
     *
     * @return the styles
     */
    public Collection<String> getStyles()
    {
        return myStyles;
    }

    /**
     * Sets the bounding box.
     *
     * @param bbox the new bounding box
     */
    public void setBoundingBox(GeographicBoundingBox bbox)
    {
        myBoundingBox = bbox;
    }

    /**
     * Sets the hierarchy of directories that make up this layer's path.
     *
     * @param path the new path of directories to this layer.
     */
    public void setDirectoryPath(List<String> path)
    {
        myDirectoryPath = new LinkedList<>(path);
    }

    /**
     * Sets the exception formats.
     *
     * @param exceptionFormats the new exception formats
     */
    public void setExceptionFormats(Collection<String> exceptionFormats)
    {
        myExceptionFormats = exceptionFormats;
    }

    /**
     * Sets the getMap formats.
     *
     * @param getMapFormats the new getMap formats
     */
    public void setGetMapFormats(Collection<String> getMapFormats)
    {
        myGetMapFormats = getMapFormats;
    }

    /**
     * Sets the Spatial Reference System (SRS) options.
     *
     * @param srs the new SRS options
     */
    public void setSRSOptions(Collection<String> srs)
    {
        mySrsOptions = new TreeSet<>(srs);
    }

    /**
     * Set the styles.
     *
     * @param styles the styles to set
     */
    public void setStyles(Collection<String> styles)
    {
        myStyles = new TreeSet<>(styles);
    }
}
