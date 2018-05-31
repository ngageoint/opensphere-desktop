package io.opensphere.infinity.json;

public class Terms
{
    private String myField;

    private int mySize;

    private long myMissing;

    public Terms()
    {
    }

    public Terms(String field, int size, long missing)
    {
        myField = field;
        mySize = size;
        myMissing = missing;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Sets the field.
     *
     * @param field the field
     */
    public void setField(String field)
    {
        myField = field;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Sets the size.
     *
     * @param size the size
     */
    public void setSize(int size)
    {
        mySize = size;
    }

    /**
     * Gets the missing.
     *
     * @return the missing
     */
    public long getMissing()
    {
        return myMissing;
    }

    /**
     * Sets the missing.
     *
     * @param missing the missing
     */
    public void setMissing(long missing)
    {
        myMissing = missing;
    }
}
