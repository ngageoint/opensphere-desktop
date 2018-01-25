package io.opensphere.core.common.json.handlers;

import io.opensphere.core.common.json.JSONSaxHandler;
import io.opensphere.core.common.json.JSONSaxParseException;
import io.opensphere.core.common.json.JSONSaxParser;
import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;

public class JSONSaxTestHandler implements JSONSaxHandler
{

    @Override
    public void arrayEnd()
    {
        System.out.println("Array End");
    }

    @Override
    public void arrayStart()
    {
        System.out.println("Array Start");
    }

    @Override
    public void documentEnd()
    {
        System.out.println("Document End");
    }

    @Override
    public void documentStart()
    {
        System.out.println("Document Start");
    }

    @Override
    public void error(JSONSaxParseException e)
    {
        System.out.println("Error: " + e.getMessage());
    }

    @Override
    public void fatalError(JSONSaxParseException e)
    {
        System.out.println("Fatal Error: " + e.getMessage());
    }

    @Override
    public void ignorableWhiteSpace(String whiteSpaceChars)
    {
        whiteSpaceChars = whiteSpaceChars.replace(Character.toString(JSONSaxParser.CARRIAGE_RETURN), "\\cr");
        whiteSpaceChars = whiteSpaceChars.replace(Character.toString(JSONSaxParser.NEW_LINE), "\\n");
        System.out.println("WhiteSpace[" + whiteSpaceChars + "]");
    }

    @Override
    public void key(String keyValue)
    {
        System.out.println("Key[" + keyValue + "]");
    }

    @Override
    public void objectEnd()
    {
        System.out.println("Object End");
    }

    @Override
    public void objectStart()
    {
        System.out.println("Object Start");
    }

    @Override
    public void value(JSONSaxPrimitiveValue value)
    {
        System.out.println("Value[" + value.toString() + "]");
    }

    @Override
    public void warning(JSONSaxParseException e)
    {
        System.out.println("Warning: " + e.getMessage());
    }

    @Override
    public void arrayElementSeparator()
    {
        System.out.println("Array Element Separator");
    }

    @Override
    public void keyValuePairSeparator()
    {
        System.out.println("Key/Value Pair Separator");
    }

    @Override
    public void keyValueSeparator()
    {
        System.out.println("Key/Value Separator");
    }

}