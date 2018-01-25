package io.opensphere.mantle.crust;

import org.apache.log4j.Logger;

import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * Contains some utility methods that can be performed on {@link DataTypeInfo}.
 */
public final class DataTypeInfoUtilities
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DataTypeInfoUtilities.class);

    /**
     * Private constructor used to prevent instantiation.
     */
    private DataTypeInfoUtilities()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Copies information metadata information from toCopy to theCopy.
     *
     * @param toCopy The layer to take information from.
     * @param theCopy The layer to put information into.
     * @param source The object making the copy.
     */
    public static synchronized void copyDataTypeInfo(DataTypeInfo toCopy, DefaultDataTypeInfo theCopy, Object source)
    {
        boolean isActive = toCopy.getParent().activationProperty().isActive();
        if (!isActive)
        {
            activate(toCopy);
        }

        theCopy.getMetaDataInfo().setGeometryColumn(toCopy.getMetaDataInfo().getGeometryColumn());
        theCopy.getMetaDataInfo().setDataTypeInfo(theCopy);

        // Copy the columns into the new data type.
        for (String colName : toCopy.getMetaDataInfo().getKeyNames())
        {
            if (toCopy.getMetaDataInfo().getSpecialTypeForKey(colName) != null)
            {
                theCopy.getMetaDataInfo().setSpecialKey(colName, toCopy.getMetaDataInfo().getSpecialTypeForKey(colName), source);
            }
            theCopy.getMetaDataInfo().addKey(colName, toCopy.getMetaDataInfo().getKeyClassType(colName), source);
        }

        theCopy.getMapVisualizationInfo().setVisualizationType(toCopy.getMapVisualizationInfo().getVisualizationType());

        if (!isActive)
        {
            deactivate(toCopy);
        }
    }

    /**
     * Ensures the data type has meta data by triggering it's listeners (which
     * will fetch columns in the case of WFS).
     *
     * @param info the data type
     */
    public static synchronized void ensureHasMetadata(DataTypeInfo info)
    {
        boolean isActive = info.getParent().activationProperty().isActive();
        if (!isActive)
        {
            activate(info);
            deactivate(info);
        }
    }

    /**
     * Activates the given data type.
     *
     * @param info The data type to activate.
     */
    private static void activate(DataTypeInfo info)
    {
        DataGroupActivationProperty property = new DataGroupActivationProperty(info.getParent());
        try
        {
            for (ActivationListener listener : info.getParent().activationProperty().getListeners())
            {
                listener.preCommit(property, ActivationState.ACTIVE, null);
            }
        }
        catch (PropertyChangeException | InterruptedException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Deactivates the data type.
     *
     * @param info The data type to deactivate.
     */
    private static void deactivate(DataTypeInfo info)
    {
        DataGroupActivationProperty property = new DataGroupActivationProperty(info.getParent());
        for (ActivationListener listener : info.getParent().activationProperty().getListeners())
        {
            listener.commit(property, ActivationState.INACTIVE, null);
        }
    }
}
