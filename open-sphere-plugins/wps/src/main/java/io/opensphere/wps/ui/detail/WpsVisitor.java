package io.opensphere.wps.ui.detail;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.util.Visitor;
import io.opensphere.core.util.collections.New;
import io.opensphere.wps.source.HashMapEntryType;
import io.opensphere.wps.source.WPSRequest;

/**
 * A visitor implementation used to gather data from individual components.
 */
public class WpsVisitor implements Visitor<WPSRequest>
{
    /**
     * The map into which results are collected.
     */
    private final Map<String, String> myValues;

    /**
     * Creates a new visitor.
     */
    public WpsVisitor()
    {
        myValues = New.map();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitor#setValue(java.lang.String, java.lang.String)
     */
    @Override
    public void setValue(String pParameterName, String pValue)
    {
        myValues.put(pParameterName, pValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitor#getValues()
     */
    @Override
    public Map<String, String> getValues()
    {
        return myValues;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitor#getResult()
     */
    @Override
    public WPSRequest getResult()
    {
        WPSRequest request = new WPSRequest();
        List<HashMapEntryType> dataInputs = New.list();

        if (myValues.containsKey("IDENTIFIER"))
        {
            request.setIdentifier(myValues.remove("IDENTIFIER"));
        }

        for (Entry<String, String> entry : myValues.entrySet())
        {
            HashMapEntryType dataInput = new HashMapEntryType(entry);
            dataInputs.add(dataInput);
        }

        request.setDataInput(dataInputs);

        return request;
    }
}
