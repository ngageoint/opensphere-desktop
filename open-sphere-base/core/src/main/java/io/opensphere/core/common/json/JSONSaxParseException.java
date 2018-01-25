package io.opensphere.core.common.json;

@SuppressWarnings("serial")
public class JSONSaxParseException extends Exception
{
    long line;

    long characterInLine;

    public JSONSaxParseException(long line, long charInLine, String message)
    {
        super(message);
        this.line = line;
        this.characterInLine = charInLine;
    }

    public long getCharacterInLine()
    {
        return characterInLine;
    }

    public long getLine()
    {
        return line;
    }
}
