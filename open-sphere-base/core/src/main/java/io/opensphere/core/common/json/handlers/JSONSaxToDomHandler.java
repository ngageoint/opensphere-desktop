package io.opensphere.core.common.json.handlers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.common.json.JSONSaxHandler;
import io.opensphere.core.common.json.JSONSaxParseException;
import io.opensphere.core.common.json.obj.JSONComposite;
import io.opensphere.core.common.json.obj.JSONSaxArray;
import io.opensphere.core.common.json.obj.JSONSaxObject;
import io.opensphere.core.common.json.obj.JSONSaxPair;
import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;

public class JSONSaxToDomHandler implements JSONSaxHandler
{
    JSONSaxObject myRootObject;

    List<JSONSaxParseException> myErrors;

    List<JSONSaxParseException> myWarnings;

    List<JSONSaxParseException> myFatalErrors;

    LinkedList<JSONComposite> myCompositeStack;

    public JSONSaxToDomHandler()
    {
    }

    public JSONSaxObject getDocument()
    {
        return myRootObject;
    }

    public List<JSONSaxParseException> getErrors()
    {
        if (myErrors == null)
        {
            return null;
        }
        else
        {
            return Collections.unmodifiableList(myErrors);
        }
    }

    public List<JSONSaxParseException> getWarnings()
    {
        if (myWarnings == null)
        {
            return null;
        }
        else
        {
            return Collections.unmodifiableList(myWarnings);
        }
    }

    public List<JSONSaxParseException> getFatalErrors()
    {
        if (myFatalErrors == null)
        {
            return null;
        }
        else
        {
            return Collections.unmodifiableList(myFatalErrors);
        }
    }

    @Override
    public void documentStart()
    {
        myRootObject = new JSONSaxObject();
        myErrors = new LinkedList<>();
        myWarnings = new LinkedList<>();
        myFatalErrors = new LinkedList<>();
        myCompositeStack = new LinkedList<>();
    }

    @Override
    public void documentEnd()
    {
    }

    @Override
    public void arrayElementSeparator()
    {
    }

    @Override
    public void arrayStart()
    {
        myCompositeStack.push(new JSONSaxArray());
    }

    @Override
    public void arrayEnd()
    {
        // Pop this array off the stack.
        JSONSaxArray value = (JSONSaxArray)myCompositeStack.pop();

        JSONComposite parent = myCompositeStack.peek();
        if (parent != null)
        {
            if (parent instanceof JSONSaxPair)
            {
                JSONSaxPair pair = (JSONSaxPair)parent;
                pair.setValue(value);
                // Completed pair.
                myCompositeStack.pop();

                // Pairs can only happen under objects so the next composite
                // in the stack must be an object.
                JSONSaxObject parentJObj = (JSONSaxObject)myCompositeStack.peek();

                // Add the pair to the object
                parentJObj.add(pair);
            }
            else if (parent instanceof JSONSaxArray)
            {
                JSONSaxArray jArray = (JSONSaxArray)parent;
                jArray.add(value);
            }
        }
    }

    @Override
    public void key(String keyValue)
    {
        myCompositeStack.push(new JSONSaxPair(keyValue));
    }

    @Override
    public void keyValuePairSeparator()
    {
    }

    @Override
    public void keyValueSeparator()
    {
    }

    @Override
    public void objectStart()
    {
        if (myCompositeStack.isEmpty())
        {
            myRootObject = new JSONSaxObject();
            myCompositeStack.push(myRootObject);
        }
        else
        {
            myCompositeStack.push(new JSONSaxObject());
        }
    }

    @Override
    public void objectEnd()
    {
        // Pop this object off the stack.
        JSONSaxObject value = (JSONSaxObject)myCompositeStack.pop();

        JSONComposite parent = myCompositeStack.peek();
        if (parent != null)
        {
            if (parent instanceof JSONSaxPair)
            {
                JSONSaxPair pair = (JSONSaxPair)parent;
                pair.setValue(value);
                // Completed pair.
                myCompositeStack.pop();

                // Pairs can only happen under objects so the next composite
                // in the stack must be an object.
                JSONSaxObject parentJObj = (JSONSaxObject)myCompositeStack.peek();

                // Add the pair to the object
                parentJObj.add(pair);
            }
            else if (parent instanceof JSONSaxArray)
            {
                JSONSaxArray jArray = (JSONSaxArray)parent;
                jArray.add(value);
            }
        }
    }

    @Override
    public void value(JSONSaxPrimitiveValue value)
    {
        JSONComposite comp = myCompositeStack.peek();
        if (comp instanceof JSONSaxPair)
        {
            JSONSaxPair pair = (JSONSaxPair)comp;
            pair.setValue(value);
            // Completed pair.
            myCompositeStack.pop();

            // Pairs can only happen under objects so the next composite
            // in the stack must be an object.
            JSONSaxObject jObj = (JSONSaxObject)myCompositeStack.peek();

            // Add the pair to the object
            jObj.add(pair);
        }
        else if (comp instanceof JSONSaxArray)
        {
            JSONSaxArray jArray = (JSONSaxArray)comp;
            jArray.add(value);
        }
    }

    @Override
    public void error(JSONSaxParseException e)
    {
        myErrors.add(e);
    }

    @Override
    public void fatalError(JSONSaxParseException e)
    {
        myFatalErrors.add(e);
    }

    @Override
    public void ignorableWhiteSpace(String whiteSpaceChars)
    {
    }

    @Override
    public void warning(JSONSaxParseException e)
    {
        myWarnings.add(e);
    }

}
