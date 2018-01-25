package io.opensphere.mantle.transformer.impl.worker;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class AbstractElementTransfomerDataRetriever.
 */
public abstract class AbstractElementTransfomerDataRetriever
{
    /** The DTI. */
    private final DataTypeInfo myDTI;

    /** The Ids of interest. */
    private final List<Long> myIdsOfInterest;

    /** The Retrieve meta data providers. */
    private final boolean myRetrieveMDPs;

    /** The Retrieve map geometry support. */
    private final boolean myRetrieveMGSs;

    /** The Retrieve time stamps. */
    private final boolean myRetrieveTSs;

    /** The Retrieve visualization state. */
    private final boolean myRetrieveVS;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new element transformer data retriever.
     *
     * @param tb the {@link Toolbox}
     * @param dti the {@link DataTypeInfo}
     * @param idsOfInterest the ids of interest
     * @param retrieveVS the retrieve VisualizationState
     * @param retrieveTSs the retrieve time stamp
     * @param retrieveMDPs the retrieve meta data provider
     * @param retrieveMGS the retrieve map geometry support
     */
    public AbstractElementTransfomerDataRetriever(Toolbox tb, DataTypeInfo dti, List<Long> idsOfInterest, boolean retrieveVS,
            boolean retrieveTSs, boolean retrieveMDPs, boolean retrieveMGS)
    {
        myDTI = dti;
        myRetrieveTSs = retrieveTSs;
        myRetrieveVS = retrieveVS;
        myRetrieveMDPs = retrieveMDPs;
        myRetrieveMGSs = retrieveMGS;
        myIdsOfInterest = idsOfInterest;
        myToolbox = tb;
    }

    /**
     * Retrieve data.
     */
    public abstract void retrieveData();

    /**
     * Gets the dTI.
     *
     * @return the dTI
     */
    protected DataTypeInfo getDTI()
    {
        return myDTI;
    }

    /**
     * Gets the ids of interest.
     *
     * @return the ids of interest
     */
    protected List<Long> getIdsOfInterest()
    {
        return myIdsOfInterest;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Checks if is retrieve meta data providers.
     *
     * @return true, if is retrieve meta data providers
     */
    protected boolean isRetrieveMDPs()
    {
        return myRetrieveMDPs;
    }

    /**
     * Checks if is retrieve map geometry support.
     *
     * @return true, if is retrieve map geometry support
     */
    protected boolean isRetrieveMGSs()
    {
        return myRetrieveMGSs;
    }

    /**
     * Checks if is retrieve time stamp.
     *
     * @return true, if is retrieve time stamp
     */
    protected boolean isRetrieveTSs()
    {
        return myRetrieveTSs;
    }

    /**
     * Checks if is retrieve VisualizationState.
     *
     * @return true, if is retrieve VisualizationState
     */
    protected boolean isRetrieveVS()
    {
        return myRetrieveVS;
    }
}
