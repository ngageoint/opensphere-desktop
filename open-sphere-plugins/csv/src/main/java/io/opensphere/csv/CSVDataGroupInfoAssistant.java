package io.opensphere.csv;

import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * The Class CSVDataGroupInfoAssistant.
 */
public class CSVDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The controller. */
    private final CSVFileDataSourceController myController;

    /**
     * Instantiates a new default data group info assistant.
     *
     * @param controller the controller
     */
    public CSVDataGroupInfoAssistant(CSVFileDataSourceController controller)
    {
        super();
        myController = controller;
    }

    @Override
    public boolean canDeleteGroup(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public boolean canReImport(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
        if (dgi.hasMembers(false))
        {
            DataTypeInfo dti = dgi.getMembers(false).iterator().next();
            if (dti instanceof CSVDataTypeInfo)
            {
                CSVDataTypeInfo cdti = (CSVDataTypeInfo)dti;
                myController.removeSource(cdti.getFileSource(), true, null);
            }
        }
    }

    @Override
    public void reImport(DataGroupInfo dgi, Object source)
    {
        if (dgi.hasMembers(false))
        {
            DataTypeInfo dti = dgi.getMembers(false).iterator().next();
            if (dti instanceof CSVDataTypeInfo)
            {
                final CSVDataTypeInfo cdti = (CSVDataTypeInfo)dti;
                final CSVDataSource csvSource = cdti.getFileSource();

                Set<String> namesInUse = New.set();
                for (IDataSource src : myController.getSourceList())
                {
                    namesInUse.add(src.getName());
                }
                namesInUse.remove(csvSource.getName());

                ThreadUtilities.runBackground(() -> myController.getFileImporter().importSource(csvSource, namesInUse, null));
            }
        }
    }
}
