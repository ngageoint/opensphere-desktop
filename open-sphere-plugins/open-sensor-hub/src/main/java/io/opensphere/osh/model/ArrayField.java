package io.opensphere.osh.model;

import io.opensphere.core.util.lang.ToStringHelper;

/** An array data record field. */
public class ArrayField extends Field
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The name of the count field. */
    private String myCountField;

    /** The field. */
    private Field myField;

    /**
     * Constructor.
     *
     * @param field The field to set it from
     */
    public ArrayField(Field field)
    {
        super(field.getName());
        setFullPath(field.getFullPath());
    }

    /**
     * Gets the countField.
     *
     * @return the countField
     */
    public String getCountField()
    {
        return myCountField;
    }

    /**
     * Sets the countField.
     *
     * @param countField the countField
     */
    public void setCountField(String countField)
    {
        myCountField = countField;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public Field getField()
    {
        return myField;
    }

    /**
     * Sets the field.
     *
     * @param field the field
     */
    public void setField(Field field)
    {
        myField = field;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("super", super.toString());
        helper.add("countField", myCountField);
        helper.add("field", myField);
        return helper.toString();
    }
}
