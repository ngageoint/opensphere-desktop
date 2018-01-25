package io.opensphere.core.common.json;

import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;

public interface JSONSaxHandler
{
    /**
     * Notification of the start of a document
     */
    public void documentStart();

    /**
     * Notification that end of document or input stream has been reached.
     */
    public void documentEnd();

    /**
     * Notification of object start.
     */
    public void objectStart();

    /**
     * Notification of object end.
     */
    public void objectEnd();

    /**
     * Notification of array start.
     */
    public void arrayStart();

    /**
     * Notification of array end.
     */
    public void arrayEnd();

    /**
     * Notification of an array element separator
     */
    public void arrayElementSeparator();

    /**
     * Notification of a key in a key-value pair, where the value could be a
     * string value, the start of an object, or the start of an array.
     *
     * @param keyValue - the key value.
     */
    public void key(String keyValue);

    /**
     * Notification of a key/value separator (the character between the key and
     * the value)
     */
    public void keyValueSeparator();

    /**
     * Notification of the separator between key/value pairs.
     */
    public void keyValuePairSeparator();

    /**
     * The value portion of a value if it is a string. This could represent a
     * text value, a number, a boolean or a string.
     *
     * @param value the value.
     */
    public void value(JSONSaxPrimitiveValue value);

    /**
     * A warning that a problem was encountered while parsing the document.
     *
     * @param e
     */
    public void warning(JSONSaxParseException e);

    /**
     * A notification that an error was encountered while parsing the document,
     * all subsequent notifications should be considered suspect.
     *
     * @param e
     */
    public void error(JSONSaxParseException e);

    /**
     * A fatal error has occurred while parsing the document, parsing will halt.
     * This is not recoverable.
     *
     * @param e
     */
    public void fatalError(JSONSaxParseException e);

    /**
     * Any ignorable white space characters including space, line feed, and
     * carrage return.
     *
     * @param whiteSpaceChars
     */
    public void ignorableWhiteSpace(String whiteSpaceChars);

}
