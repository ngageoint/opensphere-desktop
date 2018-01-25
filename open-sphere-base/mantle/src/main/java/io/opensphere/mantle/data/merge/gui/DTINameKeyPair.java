package io.opensphere.mantle.data.merge.gui;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * The Class DTINameKeyPair.
 */
public class DTINameKeyPair
{
    /** The Data type info disp name. */
    private final String myDataTypeInfoDispName;

    /** The Data type info key. */
    private final String myDataTypeInfoKey;

    /**
     * Instantiates a new dTI name key pair.
     *
     * @param name the name
     * @param key the key
     */
    public DTINameKeyPair(String name, String key)
    {
        myDataTypeInfoKey = key;
        myDataTypeInfoDispName = name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DTINameKeyPair other = (DTINameKeyPair)obj;
        return Objects.equals(myDataTypeInfoDispName, other.myDataTypeInfoDispName)
                && Objects.equals(myDataTypeInfoKey, other.myDataTypeInfoKey);
    }

    /**
     * Gets the data type info disp name.
     *
     * @return the data type info disp name
     */
    public String getDataTypeInfoDispName()
    {
        return myDataTypeInfoDispName;
    }

    /**
     * Gets the data type info key.
     *
     * @return the data type info key
     */
    public String getDataTypeInfoKey()
    {
        return myDataTypeInfoKey;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myDataTypeInfoDispName == null ? 0 : myDataTypeInfoDispName.hashCode());
        result = prime * result + (myDataTypeInfoKey == null ? 0 : myDataTypeInfoKey.hashCode());
        return result;
    }

    /**
     * The Class CompareByDisplayName.
     */
    public static class CompareByDisplayName implements Comparator<DTINameKeyPair>, Serializable
    {
        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(DTINameKeyPair o1, DTINameKeyPair o2)
        {
            return o1.getDataTypeInfoDispName().compareTo(o2.getDataTypeInfoDispName());
        }
    }
}
