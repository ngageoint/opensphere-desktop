package io.opensphere.core.common.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

import io.opensphere.core.common.json.obj.JSONSaxBooleanValue;
import io.opensphere.core.common.json.obj.JSONSaxNullValue;
import io.opensphere.core.common.json.obj.JSONSaxNumberValue;
import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;
import io.opensphere.core.common.json.obj.JSONSaxTextValue;

public class JSONSaxParser
{
    public static final char OBJECT_START = '{';

    public static final char OBJECT_END = '}';

    public static final char ARRAY_START = '[';

    public static final char ARRAY_END = ']';

    public static final char TEXT_START_OR_END = '"';

    public static final char TEXT_END = '"';

    public static final char REVERSE_SOLIDUS = '\\';

    public static final char PAIR_SEPARATOR = ',';

    public static final char ARRAY_ELEMENT_SEPARATOR = ',';

    public static final char KEY_VALUE_SEPARATOR = ':';

    public static final char WHITE_SPACE = ' ';

    public static final char NEW_LINE = '\n';

    public static final char TAB = '\t';

    public static final char CARRIAGE_RETURN = '\r';

    public static final String VALUE_MATCH_PATTERN = "\\d|\\.|e|\\-";

    /**
     * The current state of the parsing engine.
     */
    private enum State
    {
        DOCUMENT, OBJECT, ARRAY, KEY, VALUE, TEXT_VALUE, NUMBER_VALUE, NULL_VALUE, BOOLEAN_VALUE
    }

    /**
     * Value type for non-object/non-array values in object key-value pairs or
     * array elements.
     */
    private enum ValueType
    {
        NUMBER, NULL, TEXT, BOOLEAN
    }

    /**
     * The {@link JSONSaxHandler} registered for parsing events.
     */
    JSONSaxHandler myHandler;

    /**
     * Constructor with a {@link JSONSaxHandler}
     *
     * @param handler - the handler to recieve parsing events.
     */
    public JSONSaxParser(JSONSaxHandler handler)
    {
        myHandler = handler;
    }

    /**
     * Parses a text file filled with JSON content.
     *
     * @param JSONFile - the file to open.
     * @throws IOException - if an error is encountered opening or reading the
     *             file
     * @throws JSONSaxParseException - if a fatal exception is encountered
     *             halting the parsing engine.
     */
    public void parse(File JSONFile) throws IOException, JSONSaxParseException
    {
        parse(new FileReader(JSONFile));
    }

    /**
     * Parses a text string of JSON format content.
     *
     * @param JSONtext the JSON format text to be parsed.
     * @return true if the string was parsed, false if there was an error
     * @throws JSONSaxParseException - if a fatal exception is encountered
     *             halting the parsing engine.
     */
    public boolean parse(String JSONtext) throws JSONSaxParseException
    {
        boolean status = true;
        try
        {
            parse(new StringReader(JSONtext));
        }
        catch (IOException e)
        {
            status = false;
        }
        return status;
    }

    /**
     * Parses an {@link InputStream} composed of only JSON format content. The
     * stream will be parsed until it ends.
     *
     * @param jsonInputStream - the input stream to parse.
     * @throws IOException - if an error is encountered processing the
     *             {@link InputStream}
     * @throws JSONSaxParseException - if a fatal exception is encountered
     *             halting the parsing engine.
     */
    public void parse(InputStream jsonInputStream) throws IOException, JSONSaxParseException
    {
        parse(new InputStreamReader(jsonInputStream));
    }

    /**
     * Parses the content from a Reader of only JSON format content.
     *
     * @param jsonReader - the {@link Reader} from which to read the content.
     * @throws IOException - if an error is encountered reading content from the
     *             Reader.
     * @throws JSONSaxParseException - if a fatal exception is encountered
     *             halting the parsing engine.
     */
    public void parse(Reader jsonReader) throws IOException, JSONSaxParseException
    {
        long startTime = System.currentTimeMillis();
        CharacterProcessor processor = new CharacterProcessor(myHandler);
        char[] charBuffer = new char[1024 * 100];
        int numChars = 0;
        while (numChars != -1)
        {
            numChars = jsonReader.read(charBuffer);
            if (numChars != -1)
            {
                processor.process(numChars, charBuffer);
            }
        }
        processor.complete();
        if (ourDebug)
        {
            System.err.println("Completed in " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    /**
     * This class processes each character and based on the character and a
     * state engine determines what each character represents and turns the
     * input character stream into events for the handler that notifies of
     * document state changes, and document content.
     */
    private static class CharacterProcessor
    {
        /**
         * A stack of states of the document. As new states are encountered they
         * are pushed onto the front of the stack, and as those elements are
         * completed they are poped off the front.
         */
        LinkedList<State> myState = new LinkedList<>();

        /**
         * Counter for the entire document of the number of characters
         * processed.
         */
        long overAllCharCounter = 0;

        /**
         * Count of the new line characters encountered while processing the
         * document.
         */
        long lineCounter = 1;

        /** Character count within the current line */
        long charCounter = 0;

        /** The Handler that will recieve events from the processor. */
        JSONSaxHandler myHandler;

        /**
         * A buffer of text that assembles keys, values, and white space
         * elements for eventual dispatch to the handler.
         */
        StringBuilder myTextBuffer;

        /** The last character that was processed. */
        char lastChar = WHITE_SPACE;

        /** The current character being processed. */
        char currChar = WHITE_SPACE;

        /** The last state that was fully completed. */
        State myLastCompleteState = null;

        /**
         * CTOR with the {@link JSONSaxHandler} to receive events.
         *
         * @param handler - the handler to receive events.
         */
        public CharacterProcessor(JSONSaxHandler handler)
        {
            myHandler = handler;
            myTextBuffer = new StringBuilder();
            myState.push(State.DOCUMENT);
            myHandler.documentStart();
        }

        /**
         * Notifies the CharacterProcessor that no more characters are to be
         * processed and any final buffered text should be sent to the handler
         * along with the document end event.
         */
        public void complete()
        {
            if (ourDebug)
            {
                System.err.println("COMPLETE State[" + myState + "]" + " LCS[" + myLastCompleteState + "]");
            }

            if (myState.peek() == State.DOCUMENT)
            {
                myHandler.documentEnd();
            }
            else
            {
                JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                        "Unexpected end of document line " + lineCounter);
                myHandler.error(e);
            }
        }

        /**
         * Instructs the processor to process a buffer of characters.
         *
         * @param numChars - the number of characters in the buffer to be
         *            processed
         * @param buff - the character buffer.
         * @throws JSONSaxParseException if a fatal error is encountered
         *             processing the document.
         */
        public void process(int numChars, char[] buff) throws JSONSaxParseException
        {
            for (int i = 0; i < numChars; i++)
            {
                currChar = buff[i];
                overAllCharCounter++;
                charCounter++;

                // Count any new lines for our line counter.
                if (currChar == '\n')
                {
                    charCounter = 0;
                    lineCounter++;
                }

                if (ourDebug)
                {
                    System.err.println("Char[" + currChar + "] State[" + myState + "]" + " LCS[" + myLastCompleteState + "]");
                }

                // Handle the characters based on the current document state.
                // Each state has certain content that is allowed, invalid, and
                // content that can
                // trigger a document state change. Send events to the handler
                // for all
                // necessary state changes, and all content of keys, values, and
                // whitespace
                // ( space, tab, new line, and carriage returns) encountered in
                // the file.
                switch (myState.peek())
                {
                    case DOCUMENT:
                        switch (currChar)
                        {
                            case WHITE_SPACE:
                            case NEW_LINE:
                            case TAB:
                            case CARRIAGE_RETURN:
                                getTextBuffer().append(currChar);
                                break;
                            case OBJECT_START:
                                sendTextBufferAsWhiteSpace();
                                myState.push(State.OBJECT);
                                myHandler.objectStart();
                                break;
                            default:
                                sendTextBufferAsWhiteSpace();
                                JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                        "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                + lineCounter);
                                myHandler.fatalError(e);
                                throw e;
                        }
                        break;
                    case OBJECT:
                        switch (currChar)
                        {
                            case WHITE_SPACE:
                            case NEW_LINE:
                            case TAB:
                            case CARRIAGE_RETURN:
                                getTextBuffer().append(currChar);
                                break;
                            case OBJECT_END:
                                sendTextBufferAsWhiteSpace();
                                if (myLastCompleteState == State.KEY)
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Key without value in object ending at " + charCounter + " line " + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                                myHandler.objectEnd();
                                if (myState.get(1) == State.VALUE)
                                {
                                    myState.pop();
                                }

                                myLastCompleteState = myState.pop();
                                break;
                            case PAIR_SEPARATOR: // ,
                                sendTextBufferAsWhiteSpace();
                                myHandler.keyValuePairSeparator();
                                if (myLastCompleteState != State.VALUE)
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Pair Separator Character '" + currChar + "' found at position " + charCounter
                                            + " without preceeding pair " + " line " + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                                break;
                            case KEY_VALUE_SEPARATOR: // :
                                sendTextBufferAsWhiteSpace();
                                myHandler.keyValueSeparator();
                                if (myLastCompleteState == State.KEY)
                                {
                                    myState.push(State.VALUE);
                                }
                                else
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Key/Value Pair Separator Character '" + currChar + "' found at position "
                                                    + charCounter + " line " + lineCounter + " without preceeding key.");
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                                break;
                            case TEXT_START_OR_END: // "
                                sendTextBufferAsWhiteSpace();
                                myState.push(State.KEY);
                                break;
                            default:
                                sendTextBufferAsWhiteSpace();
                                JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                        "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                + lineCounter);
                                myHandler.fatalError(e);
                                throw e;
                        }
                        break;
                    case ARRAY:
                        switch (currChar)
                        {
                            case WHITE_SPACE:
                            case NEW_LINE:
                            case TAB:
                            case CARRIAGE_RETURN:
                                getTextBuffer().append(currChar);
                                break;
                            case ARRAY_START:
                                sendTextBufferAsWhiteSpace();
                                myHandler.arrayStart();
                                myState.push(State.ARRAY);
                                break;
                            case ARRAY_END:
                                sendTextBufferAsWhiteSpace();
                                myHandler.arrayEnd();
                                if (myState.get(1) == State.VALUE)
                                {
                                    myState.pop();
                                }

                                myLastCompleteState = myState.pop();
                                break;
                            case ARRAY_ELEMENT_SEPARATOR:
                                sendTextBufferAsWhiteSpace();
                                myHandler.arrayElementSeparator();
                                break;
                            case OBJECT_START:
                                sendTextBufferAsWhiteSpace();
                                myState.push(State.OBJECT);
                                myHandler.objectStart();
                                break;
                            case TEXT_START_OR_END:
                                sendTextBufferAsWhiteSpace();
                                myState.push(State.TEXT_VALUE);
                                break;
                            default:
                                sendTextBufferAsWhiteSpace();
                                // Based on the character encountered we may
                                // need to
                                // process as a value type, by the JSON
                                // specification only certain
                                // characters can be found here that are valid.
                                // Anything else
                                // is not valid and should trigger a fatal
                                // exception because
                                // the document does not conform.
                                if (Character.isDigit(currChar) || currChar == '-')
                                {
                                    getTextBuffer().append(currChar);
                                    myState.push(State.NUMBER_VALUE);
                                }
                                else if (currChar == 'n' || currChar == 'N')
                                {
                                    getTextBuffer().append(currChar);
                                    myState.push(State.NULL_VALUE);
                                }
                                else if (currChar == 't' || currChar == 'T' || currChar == 'f' || currChar == 'F')
                                {
                                    getTextBuffer().append(currChar);
                                    myState.push(State.BOOLEAN_VALUE);
                                }
                                else
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                    + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                        }
                        break;
                    case KEY:
                    case TEXT_VALUE:
                        switch (currChar)
                        {
                            case TEXT_START_OR_END:
                                if (lastChar == REVERSE_SOLIDUS)
                                {
                                    // Allow escaped characters like " so they
                                    // don't
                                    // accidently trigger the end of the
                                    // key/text-value
                                    getTextBuffer().append(currChar);
                                }
                                else
                                {
                                    if (myState.peek() == State.KEY)
                                    {
                                        sendTextBufferAsKey();
                                    }
                                    else
                                    {
                                        sendTextBufferAsValue(ValueType.TEXT);
                                        if (myState.get(1) == State.VALUE)
                                        {
                                            myState.pop();
                                        }
                                    }
                                    myLastCompleteState = myState.pop();
                                }
                                break;
                            default:
                                getTextBuffer().append(currChar);
                        }
                        break;
                    case NULL_VALUE:
                        switch (currChar)
                        {
                            case 'u':
                            case 'l':
                                getTextBuffer().append(currChar);
                                break;
                            case WHITE_SPACE:
                            case CARRIAGE_RETURN:
                            case NEW_LINE:
                            case TAB:
                            case ARRAY_END:
                            case OBJECT_END:
                            case ARRAY_ELEMENT_SEPARATOR:
                                // Check to make sure that the "null" value was
                                // well formed as to this
                                // point we only know that the only letters
                                // allowed are "n", "u", "l", but
                                // we could have had content like "llun",
                                // "nulll", "nul" and none of those
                                // would be valid. Do allow "NULL" even though
                                // the specification says
                                // it should be lower case.
                                if (!getTextBuffer().toString().toLowerCase().equals("null"))
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Invalid value format '" + getTextBuffer().toString() + "' ending at position "
                                                    + charCounter + " line " + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                                sendTextBufferAsValue(ValueType.NULL);
                                // POP NULL_VALUE
                                myLastCompleteState = myState.pop();
                                switch (currChar)
                                {
                                    case ARRAY_END:
                                        // If in this state then double pop
                                        // document state
                                        // to get out of ARRAY state.
                                        // POP ARRAY
                                        myLastCompleteState = myState.pop();
                                        myHandler.arrayEnd();
                                        break;
                                    case OBJECT_END:
                                        // If in this state then double pop
                                        // document state
                                        // to get out of OBJECT state.
                                        // POP VALUE
                                        myState.pop();
                                        // POP OBJECT
                                        myLastCompleteState = myState.pop();
                                        myHandler.objectEnd();
                                        break;
                                    case WHITE_SPACE:
                                    case CARRIAGE_RETURN:
                                    case NEW_LINE:
                                    case TAB:
                                        getTextBuffer().append(currChar);
                                        break;
                                    case ARRAY_ELEMENT_SEPARATOR: // or
                                        // PAIR_SEPARATOR
                                        // Since this could also be the
                                        // OBJECT K/v pair separator
                                        // check the state one above the
                                        // current state so we
                                        // can properly make sure we change
                                        // to a valid state.
                                        // and send the correct event.
                                        if (myState.get(1) == State.OBJECT)
                                        {
                                            // VALUE
                                            myLastCompleteState = myState.pop();
                                            myHandler.keyValuePairSeparator();
                                        }
                                        else
                                        {
                                            myHandler.arrayElementSeparator();
                                        }
                                        break;
                                }
                                break;
                            default:
                                JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                        "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                + lineCounter);
                                myHandler.fatalError(e);
                                throw e;

                        }
                        break;
                    case BOOLEAN_VALUE:
                        switch (currChar)
                        {
                            case 'a':
                            case 'l':
                            case 's':
                            case 'e':
                            case 'r':
                            case 'u':
                                getTextBuffer().append(currChar);
                                break;
                            case WHITE_SPACE:
                            case CARRIAGE_RETURN:
                            case NEW_LINE:
                            case TAB:
                            case ARRAY_END:
                            case OBJECT_END:
                            case ARRAY_ELEMENT_SEPARATOR:
                                // We now need to check that we have a valid
                                // boolean value or "true" or "false"
                                // as to this point we have only collected the
                                // characters that make up each
                                // of those words and they could be in any order
                                // or with duplicate
                                // characters which would not be allowed. Do
                                // allow case insensitive checking
                                // even though the specification says they will
                                // be lower-case.
                                String lcString = getTextBuffer().toString().toLowerCase();
                                if (!(lcString.equals("true") || lcString.equals("false")))
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Invalid boolean format '" + getTextBuffer().toString() + "' ending at position "
                                                    + charCounter + " line " + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                                sendTextBufferAsValue(ValueType.BOOLEAN);
                                // POP BOOLEAN_VALUE
                                myLastCompleteState = myState.pop();
                                switch (currChar)
                                {
                                    case ARRAY_END:
                                        // In this case double pop the state
                                        // because
                                        // we have finished our current
                                        // array and need to get
                                        // that off the stack as well and
                                        // send the proper event.
                                        // ARRAY
                                        myLastCompleteState = myState.pop();
                                        myHandler.arrayEnd();
                                        break;
                                    case OBJECT_END:
                                        // In this case double pop the state
                                        // because
                                        // we have finished our current
                                        // object and need to get
                                        // that off the stack as well and
                                        // send the proper event.
                                        // POP VALUE
                                        myState.pop();
                                        // OBJECT
                                        myLastCompleteState = myState.pop();
                                        myHandler.objectEnd();
                                        break;
                                    case WHITE_SPACE:
                                    case TAB:
                                    case CARRIAGE_RETURN:
                                    case NEW_LINE:
                                        getTextBuffer().append(currChar);
                                        break;
                                    case ARRAY_ELEMENT_SEPARATOR: // ||
                                        // PAIR_SEPARATOR
                                        // Since the "," is both the pair
                                        // separator and the
                                        // array element separator and we
                                        // could be here while
                                        // processing either type we need to
                                        // peek one farther
                                        // up the stack so that we take the
                                        // correct action and
                                        // send the correct events.
                                        if (myState.get(1) == State.OBJECT)
                                        {
                                            // VALUE
                                            myLastCompleteState = myState.pop();
                                            myHandler.keyValuePairSeparator();
                                        }
                                        else
                                        {
                                            myHandler.arrayElementSeparator();
                                        }
                                        break;
                                }
                                break;
                            default:
                                JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                        "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                + lineCounter);
                                myHandler.fatalError(e);
                                throw e;

                        }
                        break;
                    case NUMBER_VALUE:
                        if (Character.isDigit(currChar))
                        {
                            getTextBuffer().append(currChar);
                        }
                        else
                        {
                            switch (currChar)
                            {
                                case '-':
                                case 'e':
                                case 'E':
                                case '.':
                                    getTextBuffer().append(currChar);
                                    break;
                                case WHITE_SPACE:
                                case CARRIAGE_RETURN:
                                case NEW_LINE:
                                case TAB:
                                case ARRAY_END:
                                case OBJECT_END:
                                case ARRAY_ELEMENT_SEPARATOR:
                                    // We have collected up only valid character
                                    // types for a number value
                                    // to this point but we have not verified
                                    // their order or composition.
                                    // check to see that what we have collected
                                    // is a valid number format
                                    if (!isNumberValid(getTextBuffer().toString()))
                                    {
                                        JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                                "Invalid number format '" + getTextBuffer().toString() + "' ending at position "
                                                        + charCounter + " line " + lineCounter);
                                        myHandler.fatalError(e);
                                        throw e;
                                    }
                                    sendTextBufferAsValue(ValueType.NUMBER);
                                    // POP NUMBER_VALUE
                                    myLastCompleteState = myState.pop();
                                    switch (currChar)
                                    {
                                        case ARRAY_END:
                                            // ARRAY
                                            myLastCompleteState = myState.pop();
                                            myHandler.arrayEnd();
                                            break;
                                        case OBJECT_END:
                                            // POP VALUE
                                            myState.pop();
                                            // OBJECT
                                            myLastCompleteState = myState.pop();
                                            myHandler.objectEnd();
                                            break;
                                        case WHITE_SPACE:
                                        case CARRIAGE_RETURN:
                                        case NEW_LINE:
                                        case TAB:
                                            getTextBuffer().append(currChar);
                                            break;
                                        case ARRAY_ELEMENT_SEPARATOR: // ||
                                            // PAIR_SEPARATOR
                                            // Since the "," is both the
                                            // pair separator and the
                                            // array element separator and
                                            // we could be here while
                                            // processing either type we
                                            // need to peek one farther
                                            // up the stack so that we take
                                            // the correct action and
                                            // send the correct events.
                                            if (myState.get(1) == State.OBJECT)
                                            {
                                                // VALUE
                                                myLastCompleteState = myState.pop();
                                                myHandler.keyValuePairSeparator();
                                            }
                                            else
                                            {
                                                myHandler.arrayElementSeparator();
                                            }
                                            break;
                                    }
                                    break;
                                default:
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                    + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                            }
                        }
                        break;
                    case VALUE:
                        switch (currChar)
                        {
                            case WHITE_SPACE:
                            case NEW_LINE:
                            case CARRIAGE_RETURN:
                            case TAB:
                                getTextBuffer().append(currChar);
                                break;
                            case ARRAY_START:
                                sendTextBufferAsWhiteSpace();
                                myHandler.arrayStart();
                                myState.push(State.ARRAY);
                                break;
                            case ARRAY_END:
                                sendTextBufferAsWhiteSpace();
                                myHandler.arrayEnd();
                                myLastCompleteState = myState.pop();
                                break;
                            case ARRAY_ELEMENT_SEPARATOR:
                                sendTextBufferAsWhiteSpace();
                                if (myState.get(1) == State.OBJECT)
                                {
                                    myHandler.keyValuePairSeparator();
                                }
                                else
                                {
                                    myHandler.arrayElementSeparator();
                                }
                                // VALUE
                                myLastCompleteState = myState.pop();
                                break;
                            case OBJECT_START:
                                sendTextBufferAsWhiteSpace();
                                myState.push(State.OBJECT);
                                myHandler.objectStart();
                                break;
                            case OBJECT_END:
                                sendTextBufferAsWhiteSpace();
                                // VALUE
                                myState.pop();
                                // OBJECT
                                myLastCompleteState = myState.pop();
                                myHandler.objectEnd();
                                break;
                            case TEXT_START_OR_END:
                                sendTextBufferAsWhiteSpace();
                                myState.push(State.TEXT_VALUE);
                                break;
                            default:
                                sendTextBufferAsWhiteSpace();
                                // Based on the character encountered we may
                                // need to
                                // process as a value type, by the JSON
                                // specification only certain
                                // characters can be found here that are valid.
                                // Anything else
                                // is not valid and should trigger a fatal
                                // exception because
                                // the document does not conform.
                                if (Character.isDigit(currChar) || currChar == '-')
                                {
                                    getTextBuffer().append(currChar);
                                    myState.push(State.NUMBER_VALUE);
                                }
                                else if (currChar == 'n' || currChar == 'N')
                                {
                                    getTextBuffer().append(currChar);
                                    myState.push(State.NULL_VALUE);
                                }
                                else if (currChar == 't' || currChar == 'T' || currChar == 'f' || currChar == 'F')
                                {
                                    getTextBuffer().append(currChar);
                                    myState.push(State.BOOLEAN_VALUE);
                                }
                                else
                                {
                                    JSONSaxParseException e = new JSONSaxParseException(lineCounter, charCounter,
                                            "Illegal character '" + currChar + "' at position " + charCounter + " line "
                                                    + lineCounter);
                                    myHandler.fatalError(e);
                                    throw e;
                                }
                        }
                        break;

                }
                lastChar = currChar;
            }
        }

        /**
         * Checks to see if a number text value can be converted into a double
         * to make sure the format is valid.
         *
         * @param text - the text to try to convert to a number
         * @return true if converted, false if error.
         */
        private boolean isNumberValid(String text)
        {
            boolean retVal = true;
            try
            {
                Double.parseDouble(text);
            }
            catch (NumberFormatException e)
            {
                retVal = false;
            }
            return retVal;
        }

        /**
         * Gets the current text buffer or if not buffer creates one.
         *
         * @return the buffer.
         */
        private StringBuilder getTextBuffer()
        {
            if (myTextBuffer == null)
            {
                myTextBuffer = new StringBuilder();
            }
            return myTextBuffer;
        }

        /**
         * Sends the current contents of the text buffer as a KEY to the
         * handler.
         */
        private void sendTextBufferAsKey()
        {
            if (myTextBuffer != null)
            {
                final String text = myTextBuffer.toString();
                myHandler.key(text);
            }
            myTextBuffer = null;
        }

        /**
         * Sends the current contents of the text buffer as a value to the
         * handler.
         *
         * @param type - the {@link ValueType} of value to send.
         */
        private void sendTextBufferAsValue(ValueType type)
        {
            if (myTextBuffer != null)
            {
                JSONSaxPrimitiveValue aValue = null;
                switch (type)
                {
                    case BOOLEAN:
                        aValue = new JSONSaxBooleanValue(myTextBuffer.toString());
                        break;
                    case NULL:
                        aValue = new JSONSaxNullValue(myTextBuffer.toString());
                        break;
                    case NUMBER:
                        aValue = new JSONSaxNumberValue(myTextBuffer.toString());
                        break;
                    case TEXT:
                        aValue = new JSONSaxTextValue(myTextBuffer.toString());
                        break;
                }
                final JSONSaxPrimitiveValue jtv = aValue;
                myHandler.value(jtv);
            }
            myTextBuffer = null;
        }

        /**
         * Sends the current contents of the text buffer as white space
         * characters to the handler.
         */
        private void sendTextBufferAsWhiteSpace()
        {
            if (myTextBuffer != null && myTextBuffer.length() > 0)
            {
                final String text = myTextBuffer.toString();
                myHandler.ignorableWhiteSpace(text);
            }
            myTextBuffer = null;
        }
    }

    public static boolean ourDebug = false;
}
