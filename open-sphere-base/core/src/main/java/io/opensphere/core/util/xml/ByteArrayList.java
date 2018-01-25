package io.opensphere.core.util.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB class for a list of byte arrays. This class is not thread-safe.
 */
@XmlRootElement(name = "ByteArrayList")
@XmlAccessorType(XmlAccessType.NONE)
public class ByteArrayList
{
    /**
     * The list.
     */
    @XmlElement(name = "Value")
    private final List<byte[]> myArrays = new ArrayList<>();

    /**
     * Constructor.
     */
    public ByteArrayList()
    {
    }

    /**
     * Constructor.
     *
     * @param arrays The arrays in the list.
     */
    public ByteArrayList(List<byte[]> arrays)
    {
        setArrays(arrays);
    }

    /**
     * Get the arrays in the list.
     *
     * @return The arrays.
     */
    public List<byte[]> getArrays()
    {
        List<byte[]> result = new ArrayList<>(myArrays.size());
        for (byte[] arr : myArrays)
        {
            result.add(arr.clone());
        }
        return result;
    }

    /**
     * Set the arrays in the list.
     *
     * @param arrays The arrays.
     */
    public final void setArrays(List<byte[]> arrays)
    {
        myArrays.clear();
        for (byte[] arr : arrays)
        {
            myArrays.add(arr.clone());
        }
    }

    @Override
    public String toString()
    {
        return myArrays.toString();
    }
}
