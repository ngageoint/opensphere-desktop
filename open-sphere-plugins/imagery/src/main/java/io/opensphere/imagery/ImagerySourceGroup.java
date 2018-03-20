package io.opensphere.imagery;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.ref.WeakReference;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.zip.Zip;
import io.opensphere.core.util.zip.ZipByteArrayInputAdapter;
import io.opensphere.core.util.zip.ZipFileInputAdapter;
import io.opensphere.core.util.zip.ZipInputAdapter;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.imagery.util.ImageryMetaGeometryUtil;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;

/**
 * Configuration of a single image file source.
 */
@XmlRootElement(name = "AdvancedImageSourceGroup")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.GodClass")
public class ImagerySourceGroup extends AbstractDataSource
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImagerySourceGroup.class);

    /** The our unique id counter. */
    @XmlTransient
    private static AtomicInteger ourUniqueIdCounter = new AtomicInteger(1000);

    /** The Data type info. */
    @XmlTransient
    private DataGroupInfo myDataGroupInfo;

    /** The enabled. */
    @XmlAttribute(name = "enabled", required = true)
    private boolean myEnabled = true;

    /** The load error. */
    @XmlAttribute(name = "loadError", required = false)
    private boolean myHadLoadError;

    /** The id. */
    @XmlTransient
    private String myId = "";

    /** The Imagery envoy. */
    @XmlTransient
    private WeakReference<ImageryEnvoy> myImageryEnvoyWR;

    /** The image sources. */
    @XmlElement(name = "imagerySource")
    private final List<ImageryFileSource> myImageSources = new ArrayList<>();

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName = "";

    /** The Region boundary preview. */
    @XmlTransient
    private PolygonGeometry myRegionBoundary;

    /** The Show boundary on load. */
    @XmlAttribute(name = "showBoundaryOnLoad", required = false)
    private boolean myShowBoundaryOnLoad;

    /**
     * Default constructor.
     */
    public ImagerySourceGroup()
    {
        super();
        myId = getClass().getSimpleName() + "_" + Integer.toString(ourUniqueIdCounter.incrementAndGet());
    }

    /**
     * Instantiates a new advanced image source group.
     *
     * @param source the source
     */
    public ImagerySourceGroup(ImageryFileSource source)
    {
        this();
        getImageSources().add(source);
    }

    /**
     * Instantiates a new advanced image source group.
     *
     * @param other the other
     */
    public ImagerySourceGroup(ImagerySourceGroup other)
    {
        super();
        setEqualTo(other);
    }

    /**
     * Instantiates a new advanced image source group.
     *
     * @param name the name
     */
    public ImagerySourceGroup(String name)
    {
        this();
        setName(name);
    }

    /**
     * Instantiates a new advanced image source group.
     *
     * @param name the name
     * @param source the source
     */
    public ImagerySourceGroup(String name, ImageryFileSource source)
    {
        this(name);
        getImageSources().add(source);
    }

    /**
     * Instantiates a new advanced image source group.
     *
     * @param name the name
     * @param sources the sources
     */
    public ImagerySourceGroup(String name, List<ImageryFileSource> sources)
    {
        this(name);
        getImageSources().addAll(sources);
    }

    /**
     * Center on group.
     *
     * @param tb the tb
     * @param fly the fly
     */
    public void centerOnGroup(Toolbox tb, boolean fly)
    {
        final GeographicBoundingBox gbb = getGroupCenterMapBox();
        final DynamicViewer view = tb.getMapManager().getStandardViewer();
        final ViewerAnimator animator = new ViewerAnimator(view, gbb.getCenter());

        if (fly)
        {
            animator.start();
        }
        else
        {
            animator.snapToPosition();
        }
    }

    /**
     * Clean cache.
     *
     * @param tb the {@link Toolbox}
     */
    public void cleanCache(Toolbox tb)
    {
        for (final ImageryFileSource src : myImageSources)
        {
            final String key = src.generateTypeKey();
            final DataModelCategory dmc = new DataModelCategory(null, Image.class.getName(), key);
            final long[] modelsRemoved = tb.getDataRegistry().removeModels(dmc, false);
            if (modelsRemoved != null)
            {
                LOGGER.info("Cleaned " + modelsRemoved.length + " items for layer " + key);
            }
        }
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
        final ImagerySourceGroup other = (ImagerySourceGroup)obj;
        return myEnabled == other.myEnabled && myShowBoundaryOnLoad == other.myShowBoundaryOnLoad
                && myHadLoadError == other.myHadLoadError && EqualsHelper.equals(myId, other.myId)
                && EqualsHelper.equals(myImageSources, other.myImageSources) && EqualsHelper.equals(myName, other.myName);
    }

    @Override
    public boolean exportsAsBundle()
    {
        return true;
    }

    @Override
    public void exportToFile(final File selectedFile, final Component parent, final ActionListener callback)
    {
        final long totalBytes = getGroupAggregateImageSize();
        final double totalUsageMB = (double)totalBytes / (1024 * 1024);

        if (totalUsageMB > 500)
        {
            final int choice = JOptionPane.showConfirmDialog(parent,
                    "Exporting this source will result in a ~" + (int)totalUsageMB
                            + "MB file.\nAre you sure you want to continue?",
                    "Large File Write Confirmation", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.NO_OPTION)
            {
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        callback.actionPerformed(new ActionEvent(ImagerySourceGroup.this, 0, EXPORT_SUCCESS));
                    }
                });
                return;
            }
        }

        final ImagerySourceGroup copySource = new ImagerySourceGroup(this);
        copySource.setActive(false);
        copySource.setBusy(false, null);
        copySource.setFrozen(false, null);
        if (copySource.myImageSources != null && !copySource.myImageSources.isEmpty())
        {
            for (final ImageryFileSource src : copySource.myImageSources)
            {
                final File csvFile = new File(src.getPath());
                src.setPath(csvFile.getName());
            }
        }

        try (ByteArrayOutputStream cfgXMLBAOS = new ByteArrayOutputStream())
        {
            XMLUtilities.writeXMLObject(copySource, cfgXMLBAOS);
            zipResult(selectedFile, parent, totalBytes, cfgXMLBAOS);
        }
        catch (final JAXBException exc)
        {
            LOGGER.error(exc, exc);
        }
        catch (final IOException e)
        {
            LOGGER.error("Failed to close output stream");
        }

        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                callback.actionPerformed(new ActionEvent(ImagerySourceGroup.this, 0, EXPORT_SUCCESS));
            }
        });
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDataGroupInfo;
    }

    /**
     * Gets the group aggregate image size.
     *
     * @return the group aggregate image size
     */
    public long getGroupAggregateImageSize()
    {
        long totalUsageInBytes = 0;
        if (myImageSources != null && !myImageSources.isEmpty())
        {
            for (final ImageryFileSource src : myImageSources)
            {
                totalUsageInBytes += src.getImageSize();
            }
        }
        return totalUsageInBytes;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the imagery envoy.
     *
     * @return the imagery envoy
     */
    public ImageryEnvoy getImageryEnvoy()
    {
        return myImageryEnvoyWR == null ? null : myImageryEnvoyWR.get();
    }

    /**
     * Gets the image sources.
     *
     * @return the image sources
     */
    public List<ImageryFileSource> getImageSources()
    {
        return myImageSources;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myEnabled);
        result = prime * result + HashCodeHelper.getHashCode(myShowBoundaryOnLoad);
        result = prime * result + HashCodeHelper.getHashCode(myId);
        result = prime * result + HashCodeHelper.getHashCode(myImageSources);
        result = prime * result + HashCodeHelper.getHashCode(myHadLoadError);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        return result;
    }

    @Override
    public boolean isActive()
    {
        return isEnabled();
    }

    /**
     * Checks if source group is enabled.
     *
     * @return true if enabled, false if not.
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Checks if is group bounds visible.
     *
     * @return true, if visible
     */
    public boolean isGroupBoundsVisible()
    {
        return myRegionBoundary != null;
    }

    /**
     * Checks if is show boundary on load.
     *
     * @return true, if is show boundary on load
     */
    public final boolean isShowBoundaryOnLoad()
    {
        return myShowBoundaryOnLoad;
    }

    @Override
    public boolean loadError()
    {
        return myHadLoadError;
    }

    @Override
    public void setActive(boolean active)
    {
        setEnabled(active);
    }

    /**
     * Sets the {@link DataGroupInfo}.
     *
     * @param dgi the new DataGroupInfo
     */
    public final void setDataGroupInfo(DataGroupInfo dgi)
    {
        if (myDataGroupInfo != null && !Utilities.sameInstance(dgi, myDataGroupInfo))
        {
            throw new IllegalStateException("Data group cannot be changed once set.");
        }
        myDataGroupInfo = dgi;
    }

    /**
     * Sets the source group to enabled.
     *
     * @param enabled true is enabled, false is not.
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Sets this {@link ImageryFileSource} equal to another ImageFileSource.
     *
     * @param other the other shape file source
     * @throws NullPointerException if other is null
     */
    public final void setEqualTo(ImagerySourceGroup other)
    {
        Utilities.checkNull(other, "other");
        myName = other.myName;
        myHadLoadError = other.myHadLoadError;
        myEnabled = other.myEnabled;
        myShowBoundaryOnLoad = other.myShowBoundaryOnLoad;
        myId = other.myId;
        myImageSources.clear();
        for (final ImageryFileSource src : other.myImageSources)
        {
            myImageSources.add(new ImageryFileSource(src));
        }
        setDataGroupInfo(other.myDataGroupInfo);
        myImageryEnvoyWR = other.myImageryEnvoyWR;
        myRegionBoundary = other.myRegionBoundary;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Sets the imagery envoy.
     *
     * @param imageryEnvoy the new imagery envoy
     */
    public void setImageryEnvoy(ImageryEnvoy imageryEnvoy)
    {
        myImageryEnvoyWR = new WeakReference<>(imageryEnvoy);
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myHadLoadError = error;
        fireDataSourceChanged(new DataSourceChangeEvent(this, IDataSource.SOURCE_LOAD_ERROR_CHANGED, source));
    }

    @Override
    public final void setName(String name)
    {
        myName = name;

        for (final ImageryFileSource src : myImageSources)
        {
            src.setGroupName(name);
        }
    }

    /**
     * Sets the show boundary on load.
     *
     * @param showBoundaryOnLoad the new show boundary on load
     */
    public final void setShowBoundaryOnLoad(boolean showBoundaryOnLoad)
    {
        myShowBoundaryOnLoad = showBoundaryOnLoad;
    }

    /**
     * Show group bounds on the map.
     *
     * @param tb the {@link Toolbox}.
     * @param show - true to show, false to hide.
     */
    public void showGroupBounds(Toolbox tb, boolean show)
    {
        Utilities.checkNull(tb, "tb");
        if (myRegionBoundary != null)
        {
            tb.getGeometryRegistry().removeGeometriesForSource(myId, Collections.singletonList(myRegionBoundary));
            myRegionBoundary = null;
        }

        if (show)
        {
            myRegionBoundary = ImageryMetaGeometryUtil.createGeometry(getGroupCenterMapBox(), Color.white, 2, null);
            tb.getGeometryRegistry().addGeometriesForSource(myId, Collections.singletonList(myRegionBoundary));
        }
    }

    @Override
    public boolean supportsFileExport()
    {
        return true;
    }

    @Override
    public void updateDataLocations(File destDataDir)
    {
        if (myImageSources != null && !myImageSources.isEmpty())
        {
            for (final ImageryFileSource src : myImageSources)
            {
                src.setPath(destDataDir.getAbsolutePath() + File.separator + src.getPath());
            }
        }
    }

    /**
     * Update source group references.
     */
    public void updateSourceGroupReferences()
    {
        for (final ImageryFileSource src : myImageSources)
        {
            src.setGroup(this);
        }
    }

    /**
     * Zoom to group.
     *
     * @param tb the tb
     */
    public void zoomToGroup(Toolbox tb)
    {
        final GeographicBoundingBox gbb = getGroupCenterMapBox();
        final DynamicViewer view = tb.getMapManager().getStandardViewer();
        ViewerAnimator animator;
        if (gbb.getWidth() > 0.0 || gbb.getHeight() > 0.0)
        {
            animator = new ViewerAnimator(view, gbb.getVertices(), true);
            animator.snapToPosition();
        }
    }

    /**
     * Gets the group center map box.
     *
     * @return the group center map box
     */
    protected GeographicBoundingBox getGroupCenterMapBox()
    {
        double minLat = 90.0;
        double minLon = 180.0;
        double maxLat = -90.0;
        double maxLon = -180.0;

        for (final ImageryFileSource src : myImageSources)
        {
            if (src.isEnabled())
            {
                minLat = Math.min(minLat, src.getLowerRightLat());
                minLat = Math.min(minLat, src.getUpperLeftLat());

                maxLat = Math.max(maxLat, src.getLowerRightLat());
                maxLat = Math.max(maxLat, src.getUpperLeftLat());

                minLon = Math.min(minLon, src.getLowerRightLon());
                minLon = Math.min(minLon, src.getUpperLeftLon());

                maxLon = Math.max(maxLon, src.getLowerRightLon());
                maxLon = Math.max(maxLon, src.getUpperLeftLon());
            }
        }
        return new GeographicBoundingBox(LatLonAlt.createFromDegrees(minLat, minLon),
                LatLonAlt.createFromDegrees(maxLat, maxLon));
    }

    /**
     * Gets the file list.
     *
     * @return the file list
     */
    List<File> getFileList()
    {
        final List<File> result = New.list();
        if (myImageSources != null && !myImageSources.isEmpty())
        {
            for (final ImageryFileSource src : myImageSources)
            {
                result.add(new File(src.getPath()));
            }
        }
        return result;
    }

    /**
     * Zip result.
     *
     * @param selectedFile the selected file
     * @param parent the parent
     * @param totalBytes the total bytes
     * @param cfgXMLBAOS the cfg xmlbaos
     */
    private void zipResult(final File selectedFile, final Component parent, final long totalBytes,
            ByteArrayOutputStream cfgXMLBAOS)
    {
        final ArrayList<ZipInputAdapter> inputAdapters = new ArrayList<>();
        inputAdapters.add(new ZipByteArrayInputAdapter("source.opensphere3d", null, cfgXMLBAOS.toByteArray(), ZipEntry.DEFLATED));

        if (myImageSources != null && !myImageSources.isEmpty())
        {
            for (final ImageryFileSource src : myImageSources)
            {
                inputAdapters.add(new ZipFileInputAdapter("data", new File(src.getPath()), ZipEntry.STORED));
            }
        }

        final ProgressMonitor progressMon = new ProgressMonitor(parent,
                "Writing Image Source Export File: " + selectedFile.getName(), "Writing", 0, (int)totalBytes);
        progressMon.setMillisToPopup(0);

        ThreadUtilities.runBackground(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Zip.zipfiles(selectedFile, inputAdapters, progressMon, false);
                }
                catch (final IOException e)
                {
                    if (!selectedFile.delete() && LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Failed to delete file: " + selectedFile.getAbsolutePath(), e);
                    }
                    JOptionPane.showMessageDialog(parent, "Error encountered while saving export file", "File Save Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                progressMon.close();
            }
        });
    }
}
