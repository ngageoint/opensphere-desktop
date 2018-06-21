package io.opensphere.osh.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;

/** An immutable SOS offering. */
@Immutable
public class Offering implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The ID. */
    private final String myId;

    /** The name. */
    private final String myName;

    /** The description. */
    private final String myDescription;

    /** The procedure. */
    private final String myProcedure;

    /** The observable properties. */
    private final List<String> myObservableProperties;

    /** The time span. */
    private final TimeSpan mySpan;

    /**
     * Constructor.
     *
     * @param offering the offering
     */
    public Offering(MutableOffering offering)
    {
        myId = offering.getId();
        myName = offering.getName();
        myDescription = offering.getDescription();
        myProcedure = offering.getProcedure();
        myObservableProperties = Collections.unmodifiableList(New.list(offering.getObservableProperties()));
        mySpan = offering.getSpan();
    }

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
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
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
     * Gets the procedure.
     *
     * @return the procedure
     */
    public String getProcedure()
    {
        return myProcedure;
    }

    /**
     * Gets the span.
     *
     * @return the span
     */
    public TimeSpan getSpan()
    {
        return mySpan;
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
        helper.add("span", mySpan);
        helper.add("properties", myObservableProperties);
        return helper.toString();
    }
}
