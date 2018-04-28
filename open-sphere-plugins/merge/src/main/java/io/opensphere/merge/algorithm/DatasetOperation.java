package io.opensphere.merge.algorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.merge.model.MergedDataRow;

/**
 * The dataset operation.
 */
public class DatasetOperation
{
    /** Used to log messages. */
    private static final Logger LOGGER = Logger.getLogger(DatasetOperation.class);

    /** All of the data. */
    private final List<MergedDataRow> allData = new LinkedList<>();

    /** Equal columns. */
    protected final List<List<Col>> equiv = new LinkedList<>();

    /** New columns. */
    protected List<String> newKeys;

    /** Support. */
    private EnvSupport supp;

    /** Facilitates error reporting. */
    protected String errorMessage;

    /**
     * If an error message is reported, save it for later retrieval and return
     * false. Otherwise, everything is fine, for now.
     *
     * @param msg an error message or null
     * @return true if and only if no error is present
     */
    protected boolean croakOnError(String msg)
    {
        errorMessage = msg;
        return errorMessage == null;
    }

    /**
     * Gets the columns for the data type.
     *
     * @param t The data type.
     * @return The columns.
     */
    protected static List<Col> getCols(DataTypeInfo t)
    {
        return t.getMetaDataInfo().getKeyNames().stream().map(k -> Util.getColumn(t, k)).collect(Collectors.toList());
    }

    /**
     * Create a new mutable List initially containing at most one element.
     *
     * @param t the sole element of the List, if non-null
     * @return the List
     */
    private static <T> List<T> singleton(T t)
    {
        List<T> ret = new LinkedList<>();
        if (t != null)
        {
            ret.add(t);
        }
        return ret;
    }

    /**
     * Getter.
     *
     * @return the error message, if any
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * @return the allData
     */
    public List<MergedDataRow> getAllData()
    {
        return allData;
    }

    /**
     * @return the supp
     */
    public EnvSupport getSupp()
    {
        return supp;
    }

    /**
     * @param supp the supp to set
     */
    public void setSupp(EnvSupport supp)
    {
        this.supp = supp;
    }

    /**
     * Checks to see if ti matches.
     *
     * @param c
     * @param equiv
     * @return True if it belongs, false otherwise.
     */
    protected boolean belongs(Col c, List<Col> equiv)
    {
        for (Col e : equiv)
        {
            String joinedName = supp.columnMatch(c, e);
            if (StringUtils.isNotEmpty(joinedName))
            {
                c.definedName = joinedName;
                e.definedName = joinedName;
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the map geometry for the element. To be precise, create a copy of
     * the geometry held by the argument DataElement, if it exists.
     *
     * @param element The element to get the map geometry for.
     * @return The map geometry or null if there isn't one.
     */
    protected MapGeometrySupport getMapGeometry(DataElement element)
    {
        if (!(element instanceof MapDataElement))
        {
            return null;
        }
        MapGeometrySupport geometry = ((MapDataElement)element).getMapGeometrySupport();
        if (geometry == null)
        {
            return null;
        }
        return serialClone(geometry);
    }

    /**
     * Create a copy of a Serializable Object by writing it out to a buffer and
     * then reading it back in. Precisely why we need to do this, who can say?
     *
     * @param t the Serializable Object
     * @return the clone of <i>t</i>
     */
    @SuppressWarnings("unchecked")
    private static <T> T serialClone(T t)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(t);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ObjectInputStream objectIn = new ObjectInputStream(in);
            return (T)objectIn.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            LOGGER.error(e, e);
        }
        return null;
    }

    /**
     * Inserts the equivalent columns.
     *
     * @param c A column.
     */
    protected void insertEquiv(Col c)
    {
        List<Col> found = null;
        Iterator<List<Col>> it = equiv.iterator();
        while (it.hasNext())
        {
            List<Col> eq = it.next();
            if (!belongs(c, eq))
            {
                continue;
            }
            if (found == null)
            {
                found = eq;
                found.add(c);
            }
            else
            {
                found.addAll(eq);
                it.remove();
            }
        }
        if (found == null)
        {
            equiv.add(singleton(c));
        }
    }
}