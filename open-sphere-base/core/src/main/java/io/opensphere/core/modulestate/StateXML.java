package io.opensphere.core.modulestate;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.xml.MutableNamespaceContext;

/**
 * Utilities for the module state XML.
 */
public final class StateXML
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StateXML.class);

    /**
     * Utility method to get the node for the given path. If the node does not
     * exist, it will be created and appended to the parent.
     *
     * @param rootNode The root node.
     * @param doc The document.
     * @param parent The immediate parent of the desired node.
     * @param childPath The full path of the desired node.
     * @param childName The name of the desired node.
     * @return The child node.
     * @throws XPathExpressionException when the child path is not a valid
     *             expression.
     */
    public static Node createChildNode(Node rootNode, Document doc, Node parent, String childPath, String childName)
        throws XPathExpressionException
    {
        Node child = getChildNode(rootNode, childPath);

        if (child == null)
        {
            child = parent.appendChild(createElement(doc, childName));
        }

        return child;
    }

    /**
     * Create an element using the given document and qualified name, using the
     * {@link ModuleStateController#STATE_NAMESPACE}.
     *
     * @param doc The document.
     * @param qname The qualified name.
     * @return The element.
     */
    public static Element createElement(Document doc, String qname)
    {
        return doc.createElementNS(ModuleStateController.STATE_NAMESPACE, qname);
    }

    /**
     * Utility method to get the node for the given path.
     *
     * @param rootNode The root node.
     * @param childPath The full path of the desired node.
     * @return The child node.
     * @throws XPathExpressionException when the child path is not a valid
     *             expression.
     */
    public static Node getChildNode(Node rootNode, String childPath) throws XPathExpressionException
    {
        return (Node)newXPath().evaluate(childPath, rootNode, XPathConstants.NODE);
    }

    /**
     * Gets a list of nodes that match the path.
     *
     * @param rootNode The root xml node.
     * @param xpath The xpath query string.
     * @return The list of nodes matching the specified xpath.
     * @throws XPathExpressionException If xpath cannot be evaluated.
     */
    public static NodeList getChildNodes(Node rootNode, String xpath) throws XPathExpressionException
    {
        return (NodeList)newXPath().evaluate(xpath, rootNode, XPathConstants.NODESET);
    }

    /**
     * Utility method to get the node for the given path.
     *
     * @param rootNode The root node.
     * @param childPathFragment The path fragment (after the state node) of the
     *            desired node.
     * @return The child node, or null.
     */
    public static Node getChildStateNode(Node rootNode, String childPathFragment)
    {
        Node resultNode = null;
        try
        {
            resultNode = getChildNode(rootNode, "/" + ModuleStateController.STATE_QNAME + childPathFragment);
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
        return resultNode;
    }

    /**
     * Determines if any nodes match the path.
     *
     * @param rootNode The root xml node.
     * @param xpath The xpath query string.
     * @return Whether any nodes match the path.
     */
    public static boolean anyMatch(Node rootNode, String xpath)
    {
        boolean anyMatch = false;
        try
        {
            NodeList childNodes = getChildNodes(rootNode, xpath);
            if (childNodes.getLength() > 0)
            {
                anyMatch = true;
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        return anyMatch;
    }

    /**
     * Gets the state object from the given DOM node.
     *
     * @param <T> the state object type
     * @param node the DOM node
     * @param childPathFragment The path fragment (after the state node) of the
     *            desired node.
     * @param target the state object type
     * @return the state object or null
     */
    public static <T> T getStateBean(Node node, String childPathFragment, Class<T> target)
    {
        T state = null;
        Node resultNode = getChildStateNode(node, childPathFragment);
        if (resultNode != null)
        {
            try
            {
                state = XMLUtilities.readXMLObject(resultNode, target);
            }
            catch (JAXBException e)
            {
                LOGGER.error("Failed to read state: " + e, e);
            }
        }
        return state;
    }

    /**
     * Create a new {@link XPath} that has a {@link MutableNamespaceContext}
     * pre-configured with {@link ModuleStateController#STATE_NAMESPACE} as the
     * default namespace.
     *
     * @return The XPath.
     */
    public static XPath newXPath()
    {
        MutableNamespaceContext nsContext = new MutableNamespaceContext();
        nsContext.addNamespace(ModuleStateController.STATE_NAMESPACE_PREFIX, ModuleStateController.STATE_NAMESPACE);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);
        return xpath;
    }

    /** Disallow instantiation. */
    private StateXML()
    {
    }
}
