package io.opensphere.wfs.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A collection of utilities to handle common DOM functions.
 */
public final class DOMUtilities
{
    /**
     * Gets the first child node with a specified name.
     *
     * @param parent the parent node to search
     * @param name the name of the child element node to search for
     * @return the child node with the given name or null if no child was found
     */
    public static Node getChildByElementName(Node parent, String name)
    {
        if (parent != null && parent.hasChildNodes())
        {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                Node child = children.item(i);
                if (child.getLocalName() != null && child.getLocalName().equals(name))
                {
                    return child;
                }
                Node childResult = getChildByElementName(child, name);
                if (childResult != null)
                {
                    return childResult;
                }
            }
        }

        // Didn't find a match, so return null
        return null;
    }

    /** Disallow instantiation of utility class. */
    private DOMUtilities()
    {
    }
}
