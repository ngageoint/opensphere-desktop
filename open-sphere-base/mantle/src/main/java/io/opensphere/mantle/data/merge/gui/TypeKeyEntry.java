package io.opensphere.mantle.data.merge.gui;

import java.awt.datatransfer.DataFlavor;

import io.opensphere.mantle.data.SpecialKey;

/**
 * The Class TypeKeyEntry.
 */
public class TypeKeyEntry
{
    /** The our data flavor. */
    public static final DataFlavor ourDataFlavor = new DataFlavor(TypeKeyEntry.class, TypeKeyEntry.class.getName());

    /** The Class type. */
    private String myClassType;

    /** The Data type. */
    private DTINameKeyPair myDTINameKeyPair;

    /** The Key name. */
    private String myKeyName;

    /** The Owner. */
    private Object myOwner;

    /** The Special key type. */
    private SpecialKey mySpecialKeyType;

    /**
     * Gets the simple name.
     *
     * @param longClassName the long class name
     * @return the simple name
     */
    public static String extractSimpleClassName(String longClassName)
    {
        String simpleName = longClassName;
        if (simpleName != null)
        {
            int lastIndexOfDot = simpleName.lastIndexOf('.');
            if (lastIndexOfDot != -1)
            {
                simpleName = simpleName.substring(lastIndexOfDot + 1);
            }
        }
        return simpleName;
    }

    /**
     * Instantiates a new type key entry.
     *
     * @param dtiNameKeyPair the dti name key pair
     * @param keyName the key name
     * @param classType the class type
     */
    public TypeKeyEntry(DTINameKeyPair dtiNameKeyPair, String keyName, Class<?> classType)
    {
        this(dtiNameKeyPair, keyName, classType.getName(), null);
    }

    /**
     * Instantiates a new type key entry.
     *
     * @param dtiNameKeyPair the dti name key pair
     * @param keyName the key name
     * @param classType the class type
     * @param specialKey the special key
     */
    public TypeKeyEntry(DTINameKeyPair dtiNameKeyPair, String keyName, Class<?> classType, SpecialKey specialKey)
    {
        this(dtiNameKeyPair, keyName, classType.getName(), specialKey);
    }

    /**
     * Instantiates a new type key entry.
     *
     * @param dtiNameKeyPair the dti name key pair
     * @param keyName the key name
     * @param classType the class type
     * @param specialKey the special key
     */
    private TypeKeyEntry(DTINameKeyPair dtiNameKeyPair, String keyName, String classType, SpecialKey specialKey)
    {
        myKeyName = keyName;
        myDTINameKeyPair = dtiNameKeyPair;
        mySpecialKeyType = specialKey;
        myClassType = classType;
    }

    /**
     * Gets the class type.
     *
     * @return the class type
     */
    public String getClassType()
    {
        return myClassType;
    }

    /**
     * Gets the data type disp name.
     *
     * @return the data type disp name
     */
    public String getDataTypeDispName()
    {
        return myDTINameKeyPair == null ? null : myDTINameKeyPair.getDataTypeInfoDispName();
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public String getDataTypeKey()
    {
        return myDTINameKeyPair == null ? null : myDTINameKeyPair.getDataTypeInfoKey();
    }

    /**
     * Gets the dTI name key pair.
     *
     * @return the dTI name key pair
     */
    public DTINameKeyPair getDTINameKeyPair()
    {
        return myDTINameKeyPair;
    }

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    public String getKeyName()
    {
        return myKeyName;
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public Object getOwner()
    {
        return myOwner;
    }

    /**
     * Gets the special key type.
     *
     * @return the special key type
     */
    public SpecialKey getSpecialKeyType()
    {
        return mySpecialKeyType;
    }

    /**
     * Sets the class type.
     *
     * @param classType the new class type
     */
    public void setClassType(String classType)
    {
        myClassType = classType;
    }

    /**
     * Sets the data type.
     *
     * @param dataType the new data type
     */
    public void setDTINameKeyPair(DTINameKeyPair dataType)
    {
        myDTINameKeyPair = dataType;
    }

    /**
     * Sets the key name.
     *
     * @param keyName the new key name
     */
    public void setKeyName(String keyName)
    {
        myKeyName = keyName;
    }

    /**
     * Sets the owner.
     *
     * @param owner the new owner
     */
    public void setOwner(Object owner)
    {
        myOwner = owner;
    }

    /**
     * Sets the special key type.
     *
     * @param specialKeyType the new special key type
     */
    public void setSpecialKeyType(SpecialKey specialKeyType)
    {
        mySpecialKeyType = specialKeyType;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(myKeyName);
        if (myClassType != null)
        {
            sb.append(" [").append(extractSimpleClassName(myClassType)).append(']');
        }
        if (mySpecialKeyType != null)
        {
            sb.append(" [").append(mySpecialKeyType.getClass().getSimpleName()).append(']');
        }
        return sb.toString();
    }
}
