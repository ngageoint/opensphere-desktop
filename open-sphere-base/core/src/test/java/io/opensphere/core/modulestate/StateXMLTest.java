package io.opensphere.core.modulestate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Tests for {@link StateXML}. */
public class StateXMLTest
{
    /**
     * Tests the get child nodes method no create.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad xpath.
     */
    @Test
    public void testGetChildNodeNoCreate() throws ParserConfigurationException, XPathExpressionException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node parent = doc.appendChild(doc.createElement("parent"));

        Node child = StateXML.getChildNode(parent, "/parent/child");

        assertNull(child);
        assertEquals(0, parent.getChildNodes().getLength());

        Node expectedChild = parent.appendChild(doc.createElement("child"));

        child = StateXML.getChildNode(parent, "/parent/child");

        assertEquals(expectedChild, child);
    }

    /**
     * Tests the getChildNodes.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad xpath.
     */
    @Test
    public void testGetChildNodes() throws ParserConfigurationException, XPathExpressionException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node parent = doc.appendChild(doc.createElement("parent"));

        NodeList child = StateXML.getChildNodes(parent, "/parent/child");

        assertEquals(0, child.getLength());

        Node expectedChild1 = parent.appendChild(doc.createElement("child"));
        Node expectedChild2 = parent.appendChild(doc.createElement("child"));

        child = StateXML.getChildNodes(parent, "/parent/child");

        assertEquals(2, child.getLength());
        assertEquals(expectedChild1, child.item(0));
        assertEquals(expectedChild2, child.item(1));
    }
}
