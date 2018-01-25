package io.opensphere.city.model.json;

/**
 * The type json object.
 */
public class Type
{
    /**
     * The id.
     */
    private int myId;

    /**
     * The name.
     */
    private String myName;

    /**
     * Getter.
     *
     * @return the id
     */
    public int getId()
    {
        return myId;
    }

    /**
     * Getter.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Setter.
     *
     * @param id the id to set
     */
    public void setId(int id)
    {
        myId = id;
    }

    /**
     * Setter.
     *
     * @param name the name to set
     */
    public void setName(String name)
    {
        myName = name;
    }
}
