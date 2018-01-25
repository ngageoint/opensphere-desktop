package io.opensphere.mantle.data.merge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;
import io.opensphere.mantle.data.impl.encoder.DiskEncodeHelper;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class KeySpecification.
 */
@XmlRootElement(name = "KeySpecification")
@XmlAccessorType(XmlAccessType.FIELD)
public class KeySpecification
{
    /** Logger reference. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(KeySpecification.class);

    /** The Class name. */
    @XmlAttribute(name = "className")
    private String myClassName;

    /** The Key name. */
    @XmlAttribute(name = "keyName")
    private String myKeyName;

    /** The Special key class name. */
    @XmlAttribute(name = "specialKeyClassName", required = false)
    private String mySpecialKeyClassName;

    /**
     * Creates the key specification list from a {@link MetaDataInfo}.
     *
     * @param mdi the mdi
     * @return the list
     */
    public static List<KeySpecification> createKeySpecification(MetaDataInfo mdi)
    {
        List<KeySpecification> result = new ArrayList<>();
        if (mdi != null)
        {
            for (String key : mdi.getKeyNames())
            {
                Class<?> keyClass = mdi.getKeyClassType(key);
                SpecialKey sk = mdi.getSpecialTypeForKey(key);

                KeySpecification ks = new KeySpecification(key, keyClass == null ? null : keyClass.getName(),
                        sk == null ? null : sk.getClass().getName());

                result.add(ks);
            }
        }
        return result;
    }

    /**
     * Gets the key class for class name.
     *
     * @param className the class name
     * @param defaultClass the default class to return if not able to get class.
     * @return the key class for class name or defaultClass
     */
    public static Class<?> getKeyClassForClassName(String className, Class<?> defaultClass)
    {
        Class<?> colClass = null;

        String modifiedClassName = className;
        if ("io.opensphere.core.model.TimeSpan".equals(className))
        {
            modifiedClassName = "io.opensphere.core.model.time.TimeSpan";
        }

        try
        {
            colClass = Class.forName(modifiedClassName);
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.error("Could not find column class: " + modifiedClassName + " defaulting to string.", e);
            colClass = defaultClass;
        }
        return colClass;
    }

    /**
     * Gets the special key for special key class name.
     *
     * @param specialKeyClassName the special key class name
     * @return the special key for special key class name
     */
    public static SpecialKey getSpecialKeyForSpecialKeyClassName(String specialKeyClassName)
    {
        SpecialKey sk = null;
        if (specialKeyClassName != null)
        {
            try
            {
                Class<?> cl = Class.forName(specialKeyClassName);
                sk = (SpecialKey)cl.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                LOGGER.error("Could not find SpecialKey class: " + specialKeyClassName, e);
                sk = null;
            }
            catch (InstantiationException e)
            {
                LOGGER.error("Could not instantiate SpecialKey class: " + specialKeyClassName, e);
                sk = null;
            }
            catch (IllegalAccessException e)
            {
                LOGGER.error(e);
                sk = null;
            }
        }
        return sk;
    }

    /**
     * Instantiates a new merge key specification.
     */
    public KeySpecification()
    {
    }

    /**
     * Copy constructor (Deep copy).
     *
     * @param other the MergeKeySpecification to copy.
     */
    public KeySpecification(KeySpecification other)
    {
        this(other.myKeyName, other.myClassName, other.mySpecialKeyClassName);
    }

    /**
     * Instantiates a new merge key specification.
     *
     * @param keyName the key name
     * @param className the class name
     */
    public KeySpecification(String keyName, String className)
    {
        this(keyName, className, null);
    }

    /**
     * Instantiates a new merge key specification.
     *
     * @param keyName the key name
     * @param className the class name
     * @param skClassName the sk type
     */
    public KeySpecification(String keyName, String className, String skClassName)
    {
        myKeyName = keyName;
        mySpecialKeyClassName = skClassName;
        myClassName = className;
    }

    /**
     * Decode.
     *
     * @param ois the ObjectInputStream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void decode(ObjectInputStream ois) throws IOException
    {
        EncodeType et = EncodeType.decode(ois);
        myKeyName = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
        et = EncodeType.decode(ois);
        myClassName = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
        et = EncodeType.decode(ois);
        mySpecialKeyClassName = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
    }

    /**
     * Encode.
     *
     * @param oos the ObjectOutputStream
     * @return the number of bytes written.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public int encode(ObjectOutputStream oos) throws IOException
    {
        int numWritten = 0;

        numWritten += myKeyName == null ? EncodeType.NULL.encode(oos) : DiskEncodeHelper.encodeString(oos, myKeyName);

        numWritten += myClassName == null ? EncodeType.NULL.encode(oos) : DiskEncodeHelper.encodeString(oos, myClassName);

        numWritten += mySpecialKeyClassName == null ? EncodeType.NULL.encode(oos)
                : DiskEncodeHelper.encodeString(oos, mySpecialKeyClassName);

        return numWritten;
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
        KeySpecification other = (KeySpecification)obj;
        return EqualsHelper.equals(myClassName, other.myClassName) && EqualsHelper.equals(myKeyName, other.myKeyName)
                && EqualsHelper.equals(mySpecialKeyClassName, other.mySpecialKeyClassName);
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName()
    {
        return myClassName;
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
     * Gets the special key.
     *
     * @return the special key
     */
    public String getSpecialKeyClassName()
    {
        return mySpecialKeyClassName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myClassName == null ? 0 : myClassName.hashCode());
        result = prime * result + (myKeyName == null ? 0 : myKeyName.hashCode());
        result = prime * result + (mySpecialKeyClassName == null ? 0 : mySpecialKeyClassName.hashCode());
        return result;
    }

    /**
     * Sets the class name.
     *
     * @param className the new class name
     */
    public void setClassName(String className)
    {
        myClassName = className;
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
     * Sets the special key class name.
     *
     * @param specialKeyClassName the new special key type
     */
    public void setSpecialKeyClassName(String specialKeyClassName)
    {
        mySpecialKeyClassName = specialKeyClassName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append(" KeyName[").append(myKeyName).append("] ClassName[").append(myClassName)
                .append("] SpecialKeyClassName[").append(mySpecialKeyClassName).append(']');
        return sb.toString();
    }
}
