package io.opensphere.osh.model;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;

/** An vector data record field. */
public class VectorField extends Field
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The field. */
    private final List<Field> myFields = New.list();

    /**
     * Constructor.
     *
     * @param field The field to set it from
     */
    public VectorField(Field field)
    {
        super(field.getName());
        setFullPath(field.getFullPath());
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public List<Field> getFields()
    {
        return myFields;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("super", super.toString());
        int i = 0;
        for (Field field : myFields)
        {
            helper.add("field" + i++, field);
        }
        return helper.toString();
    }
}
