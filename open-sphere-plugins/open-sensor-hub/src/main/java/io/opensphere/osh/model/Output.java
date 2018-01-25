package io.opensphere.osh.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;

/** SML output object. */
public class Output implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
    private final String myName;

    /** The fields. */
    private final List<Field> myFields = New.list();

    /** The properties. */
    private final List<String> myProperties = New.list();

    /** The time span. */
    private TimeSpan mySpan;

    /** The encoding. */
    private Encoding myEncoding;

    /**
     * Constructor.
     *
     * @param name The name
     */
    public Output(String name)
    {
        myName = name;
    }

    /**
     * Sets the span.
     *
     * @param span the span
     */
    public void setSpan(TimeSpan span)
    {
        mySpan = span;
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
     * Gets the first-level fields.
     *
     * @return the first-level fields
     */
    public List<Field> getFields()
    {
        return myFields;
    }

    /**
     * Gets all fields.
     *
     * @return all fields
     */
    public List<Field> getAllFields()
    {
        List<Field> allFields = New.list();
        for (Field field : myFields)
        {
            allFields.addAll(getAllFields(field));
        }
        return allFields;
    }

    /**
     * Gets all fields.
     *
     * @param field the field
     * @return all fields
     */
    private List<Field> getAllFields(Field field)
    {
        List<Field> allFields = New.list();
        allFields.add(field);
        if (field instanceof VectorField)
        {
            for (Field child : ((VectorField)field).getFields())
            {
                allFields.addAll(getAllFields(child));
            }
        }
        else if (field instanceof ArrayField)
        {
            allFields.addAll(getAllFields(((ArrayField)field).getField()));
        }
        return allFields;
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public List<String> getProperties()
    {
        return myProperties;
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
     * Gets the encoding.
     *
     * @return the encoding
     */
    public Encoding getEncoding()
    {
        return myEncoding;
    }

    /**
     * Sets the encoding.
     *
     * @param encoding the encoding
     */
    public void setEncoding(Encoding encoding)
    {
        myEncoding = encoding;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("name", myName);
        helper.add("span", mySpan);
        int i = 0;
        for (Field field : myFields)
        {
            helper.add("field" + i++, field);
        }
        i = 0;
        for (String property : myProperties)
        {
            helper.add("property" + i++, property);
        }
        helper.add("encoding", myEncoding);
        return helper.toStringMultiLine();
    }
}
