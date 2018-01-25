package io.opensphere.core.common.json.handlers;

import io.opensphere.core.common.json.JSONSaxHandler;
import io.opensphere.core.common.json.JSONSaxParseException;
import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;

public class JSONSaxEmptyHandler implements JSONSaxHandler
{
    @Override
    public void arrayEnd()
    {
    }

    @Override
    public void arrayStart()
    {
    }

    @Override
    public void documentEnd()
    {
    }

    @Override
    public void documentStart()
    {
    }

    @Override
    public void error(JSONSaxParseException e)
    {
    }

    @Override
    public void fatalError(JSONSaxParseException e)
    {
    }

    @Override
    public void ignorableWhiteSpace(String whiteSpaceChars)
    {
    }

    @Override
    public void key(String keyValue)
    {
    }

    @Override
    public void objectEnd()
    {
    }

    @Override
    public void objectStart()
    {
    }

    @Override
    public void value(JSONSaxPrimitiveValue value)
    {
    }

    @Override
    public void warning(JSONSaxParseException e)
    {
    }

    @Override
    public void arrayElementSeparator()
    {
    }

    @Override
    public void keyValuePairSeparator()
    {
    }

    @Override
    public void keyValueSeparator()
    {
    }

}
