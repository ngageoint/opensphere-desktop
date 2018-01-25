package io.opensphere.mantle.data.element;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * The Interface DynamicMetaDataList.
 */
public interface DynamicMetaDataList extends List<Object>, MetaDataProvider, Serializable
{
    /**
     * Decode.
     *
     * @param ois the ois
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void decode(ObjectInputStream ois) throws IOException;

    /**
     * Encode.
     *
     * @param oos the oos
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void encode(ObjectOutputStream oos) throws IOException;

    /**
     * Gets the class at the specified index.
     *
     * @param index the index
     * @return the class at the index.
     * @throws IndexOutOfBoundsException if index is out of bounds.
     */
    Class<?> getClass(int index);

    /**
     * Gets the DataTypeInfo key.
     *
     * @return the data type info key
     */
    String getDataTypeInfoKey();

    /**
     * Gets the hash code for the specific data type setup which is a
     * combination of the data type key, and the number and names of the
     * columns, and the classes of the columns. This will be unique per class.
     *
     * @return the type hash code
     */
    int getTypeHashCode();

    /**
     * Gets the version of the object.
     *
     * @return the version
     */
    int getVersion();

    /**
     * Returns a new instance of this DynamicMetaDataList in an filled out with
     * the state of the {@link MetaDataProvider}.
     *
     * @param provider the provider to copy
     * @return the MetaDataProvider from which to copy contents.
     */
    DynamicMetaDataList newCopy(MetaDataProvider provider);

    /**
     * Creates a new DynamicMetaDataList by decoding a object input stream.
     *
     * @param ois the object input stream.
     * @return a new DynamicMetaDataList decoded from the stream.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    DynamicMetaDataList newFromDecode(ObjectInputStream ois) throws IOException;

    /**
     * Returns a new instance of this DynamicMetaDataList in an unfilled out
     * state.
     *
     * @return the dynamic meta data list
     */
    DynamicMetaDataList newInstance();

    /**
     * Sets all the values in this DynamicMetaDataList that have keys in the
     * provided provider.
     *
     * @param provider the provider of the values
     */
    void setEqualTo(MetaDataProvider provider);
}
