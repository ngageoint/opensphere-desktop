package io.opensphere.osh.model;

import java.util.Date;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;

/** A mutable SOS offering. */
@NotThreadSafe
public class MutableOffering
{
    /** The ID. */
    private String myId;

    /** The name. */
    private String myName;

    /** The description. */
    private String myDescription;

    /** The procedure. */
    private String myProcedure;

    /** The start date. */
    private Date myStartDate;

    /** The end date. */
    private Date myEndDate;

    /** The observable properties. */
    private final List<String> myObservableProperties = New.list();

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Gets the procedure.
     *
     * @return the procedure
     */
    public String getProcedure()
    {
        return myProcedure;
    }

    /**
     * Sets the procedure.
     *
     * @param procedure the procedure
     */
    public void setProcedure(String procedure)
    {
        myProcedure = procedure;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(Date startDate)
    {
        myStartDate = Utilities.clone(startDate);
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date
     */
    public void setEndDate(Date endDate)
    {
        myEndDate = Utilities.clone(endDate);
    }

    /**
     * Gets the span.
     *
     * @return the span
     */
    public TimeSpan getSpan()
    {
        return TimeSpan.get(myStartDate, myEndDate);
    }

    /**
     * Gets the observableProperties.
     *
     * @return the observableProperties
     */
    public List<String> getObservableProperties()
    {
        return myObservableProperties;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("id", myId);
        helper.add("name", myName);
        helper.add("description", myDescription);
        helper.add("procedure", myProcedure);
        helper.add("span", getSpan());
        helper.add("properties", myObservableProperties);
        return helper.toString();
    }
}
