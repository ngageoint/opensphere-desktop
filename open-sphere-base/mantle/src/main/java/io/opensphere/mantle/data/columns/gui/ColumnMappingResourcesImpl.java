package io.opensphere.mantle.data.columns.gui;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import io.opensphere.core.datafilter.columns.MutableColumnMappingController;
import io.opensphere.core.util.cache.SimpleCache;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseOrientationKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.LineOfBearingKey;

/** The column mapping resources. */
public class ColumnMappingResourcesImpl implements ColumnMappingResources
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ColumnMappingResourcesImpl.class);

    /** The controller. */
    private final MutableColumnMappingController myController;

    /** The parent frame provider. */
    private final Supplier<? extends JFrame> myParentFrameProvider;

    /** The data group controller. */
    private final DataGroupController groupCtrl;

    /** The data type cache. */
    private final SimpleCache<String, DataTypeInfo> myDataTypeCache;

    /**
     * Constructor.
     *
     * @param controller the controller
     * @param parentFrameProvider the parent frame provider
     * @param dataGroupController the data group controller
     */
    public ColumnMappingResourcesImpl(MutableColumnMappingController controller, Supplier<? extends JFrame> parentFrameProvider,
            DataGroupController dataGroupController)
    {
        myController = controller;
        myParentFrameProvider = parentFrameProvider;
        groupCtrl = dataGroupController;
        myDataTypeCache = new SimpleCache<>(New.map(), groupCtrl::findMemberById);
    }

    @Override
    public MutableColumnMappingController getController()
    {
        return myController;
    }

    @Override
    public JFrame getParentFrame()
    {
        return myParentFrameProvider.get();
    }

    @Override
    public List<DataTypeRef> getLayers()
    {
        return groupCtrl.findMembers(t -> hasMetadata(t), false).stream()
                .map(t -> new DataTypeRef(t, groupCtrl.isTypeActive(t)))
                .collect(Collectors.toList());
    }

    /**
     * As the name suggests.
     * @param t a type
     * @return as the name suggests
     */
    private static boolean hasMetadata(DataTypeInfo t)
    {
        MetaDataInfo meta = t.getMetaDataInfo();
        return meta != null && meta.getKeyCount() > 0;
    }

    @Override
    public String getLayerDisplayName(String layerKey)
    {
        String displayName = layerKey;
        DataTypeInfo dataType = myDataTypeCache.apply(layerKey);
        if (dataType != null)
        {
            displayName = dataType.getDisplayName();
        }
        return displayName;
    }

    @Override
    public List<String> getLayerColumns(String layerKey)
    {
        if (layerKey == null)
        {
            return Collections.emptyList();
        }
        DataTypeInfo dataType = myDataTypeCache.apply(layerKey);
        if (dataType == null)
        {
            return Collections.emptyList();
        }
        MetaDataInfo metaData = dataType.getMetaDataInfo();
        if (metaData == null)
        {
            return Collections.emptyList();
        }
        return metaData.getKeyNames();
    }

    @Override
    public String getType(String layerKey, String layerColumn)
    {
        String type = null;
        DataTypeInfo dataType = myDataTypeCache.apply(layerKey);
        if (dataType != null)
        {
            SpecialKey specialType = dataType.getMetaDataInfo().getSpecialTypeForKey(layerColumn);
            Class<?> classType = dataType.getMetaDataInfo().getKeyClassType(layerColumn);
            type = specialType != null ? getType(specialType) : getType(classType);
        }
        return type;
    }

    /**
     * Gets the data type.
     *
     * @param classType the class type
     * @return the data type
     */
    private static String getType(Class<?> classType)
    {
        if (classType == String.class)
        {
            return "string";
        }
        else if (classType == Double.class)
        {
            return "double";
        }
        else if (classType == java.util.Date.class)
        {
            return "time";
        }
        else if (classType == com.vividsolutions.jts.geom.Geometry.class)
        {
            return "geometry";
        }

        LOGGER.warn("unknown class: " + classType);
        return "string";
    }

    /**
     * Gets the data type.
     *
     * @param specialType the special type
     * @return the data type
     */
    private static String getType(SpecialKey specialType)
    {
        if (specialType instanceof EllipseOrientationKey)
        {
            return "orientation";
        }
        else if (specialType instanceof EllipseSemiMajorAxisKey)
        {
            return "semi-major";
        }
        else if (specialType instanceof EllipseSemiMinorAxisKey)
        {
            return "semi-minor";
        }
        else if (specialType instanceof LineOfBearingKey)
        {
            return "line-of-bearing";
        }
        else
        {
            return specialType.getKeyName().toLowerCase();
        }
    }
}
