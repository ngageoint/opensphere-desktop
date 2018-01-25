package io.opensphere.filterbuilder.config;

import java.io.File;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * The Class FilterBuilderConfiguration.
 */
@XmlRootElement(name = "FilterBuilderConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class FilterBuilderConfiguration implements Cloneable
{
    /** The last file. */
    @XmlElement
    private File myLastFile;

    /** The last loaded directory. */
    @XmlElement
    private File myLastLoadDir;

    /** The last opened directory. */
    @XmlElement
    private File myLastOpenDir;

    /** The last save directory. */
    @XmlElement
    private File myLastSaveDir;

    /** The current file. */
    private transient File myCurrentFile;

    /** The X location. */
    @XmlElement
    private int myXLoc;

    /** The Y location. */
    @XmlElement
    private int myYLoc;

    /** The Height. */
    @XmlElement
    private int myHeight;

    /** The Width. */
    @XmlElement
    private int myWidth;

    /** The Debug flag. True for debug mode */
    private boolean myDebugFlag;

    /**
     * Instantiates a new filter builder configuration.
     */
    public FilterBuilderConfiguration()
    {
        // empty constructor
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
        FilterBuilderConfiguration other = (FilterBuilderConfiguration)obj;
        //@formatter:off
        return Objects.equals(myLastFile, other.myLastFile)
                && Objects.equals(myLastLoadDir, other.myLastLoadDir)
                && Objects.equals(myLastOpenDir, other.myLastOpenDir)
                && Objects.equals(myLastSaveDir, other.myLastSaveDir);
        //@formatter:on
    }

    /**
     * Gets the current file.
     *
     * @return the current file
     */
    public File getCurrentFile()
    {
        return myCurrentFile;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the last file.
     *
     * @return the last file
     */
    public File getLastFile()
    {
        return myLastFile;
    }

    /**
     * Gets the last load directory.
     *
     * @return the last load directory
     */
    public File getLastLoadDir()
    {
        return myLastLoadDir;
    }

    /**
     * Gets the last open directory.
     *
     * @return the last open directory
     */
    public File getLastOpenDir()
    {
        return myLastOpenDir;
    }

    /**
     * Gets the last save directory.
     *
     * @return the last save directory
     */
    public File getLastSaveDir()
    {
        return myLastSaveDir;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Gets the x location.
     *
     * @return the x location
     */
    public int getXLoc()
    {
        return myXLoc;
    }

    /**
     * Gets the y location.
     *
     * @return the y location
     */
    public int getYLoc()
    {
        return myYLoc;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myLastFile);
        result = prime * result + HashCodeHelper.getHashCode(myLastLoadDir);
        result = prime * result + HashCodeHelper.getHashCode(myLastOpenDir);
        result = prime * result + HashCodeHelper.getHashCode(myLastSaveDir);
        return result;
    }

    /**
     * Checks if is debug flag.
     *
     * @return true, if is debug flag
     */
    public boolean isDebugFlag()
    {
        return myDebugFlag;
    }

    /**
     * Sets the current file.
     *
     * @param pCurrentFile the new current file
     */
    public void setCurrentFile(File pCurrentFile)
    {
        myCurrentFile = pCurrentFile;
        myLastFile = myCurrentFile;
    }

    /**
     * Sets the debug flag.
     *
     * @param debugFlag the new debug flag
     */
    public void setDebugFlag(boolean debugFlag)
    {
        myDebugFlag = debugFlag;
    }

    /**
     * Sets the height.
     *
     * @param pHeight the new height
     */
    public void setHeight(int pHeight)
    {
        myHeight = pHeight;
    }

    /**
     * Sets the last file.
     *
     * @param pLastFile the new last file
     */
    public void setLastFile(File pLastFile)
    {
        myLastFile = pLastFile;
    }

    /**
     * Sets the last load directory.
     *
     * @param pLastLoadDir the new last load directory
     */
    public void setLastLoadDir(File pLastLoadDir)
    {
        myLastLoadDir = pLastLoadDir;
    }

    /**
     * Sets the last open directory.
     *
     * @param pLastOpenDir the new last open directory
     */
    public void setLastOpenDir(File pLastOpenDir)
    {
        myLastOpenDir = pLastOpenDir;
    }

    /**
     * Sets the last save directory.
     *
     * @param pLastSaveDir the new last save directory
     */
    public void setLastSaveDir(File pLastSaveDir)
    {
        myLastSaveDir = pLastSaveDir;
    }

    /**
     * Sets the width.
     *
     * @param pWidth the new width
     */
    public void setWidth(int pWidth)
    {
        myWidth = pWidth;
    }

    /**
     * Sets the x location.
     *
     * @param pXLoc the new x location
     */
    public void setXLoc(int pXLoc)
    {
        myXLoc = pXLoc;
    }

    /**
     * Sets the y location.
     *
     * @param pYLoc the new y location
     */
    public void setYLoc(int pYLoc)
    {
        myYLoc = pYLoc;
    }

    @Override
    public FilterBuilderConfiguration clone()
    {
        try
        {
            return (FilterBuilderConfiguration)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
