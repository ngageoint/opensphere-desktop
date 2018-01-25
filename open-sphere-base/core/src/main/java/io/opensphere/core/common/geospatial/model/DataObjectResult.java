package io.opensphere.core.common.geospatial.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.common.monitor.QueryTracker;

/**
 * Represents data transfer object result for a given request. Used as a
 * transfer object between the ogc server and any clients that want to acquire
 * feature results in a more optimzed way. This class is used on both client and
 * server side.
 *
 */
public class DataObjectResult implements Serializable
{
    public static final long serialVersionUID = 1;

    /**
     * Error message from server is something went awry
     */
    private String errorMessage = null;

    /**
     * List of data objects contains the results
     */
    private List<DataObject> dataObjectList = null;

    /**
     * List of query tracker objects for query tracking
     */
    private List<QueryTracker> queryTrackerList = null;

    /**
     * Default constructor needed to be serialization friendly
     */
    public DataObjectResult()
    {
        dataObjectList = new ArrayList<DataObject>();
        queryTrackerList = new ArrayList<QueryTracker>();
    }

    public List<DataObject> getDataObjectList()
    {
        return dataObjectList;
    }

    public void setDataObjectList(List<DataObject> dataObjectList)
    {
        this.dataObjectList = dataObjectList;
    }

    public void addToDataObjectList(List<DataObject> dataObjectList)
    {
        this.dataObjectList.addAll(dataObjectList);
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public List<QueryTracker> getQueryTrackerList()
    {
        return queryTrackerList;
    }

    public void addToQueryTrackerList(List<QueryTracker> queryTrackerList)
    {
        this.queryTrackerList.addAll(queryTrackerList);
    }

    public void setQueryTrackerList(List<QueryTracker> queryTrackerList)
    {
        this.queryTrackerList = queryTrackerList;
    }

}
