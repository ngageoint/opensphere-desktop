package io.opensphere.featureactions.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.filterbuilder.filter.v1.Filter;

/**
 * Unit test for {@link FeatureAction}.
 */
public class FeatureActionTest
{
    /**
     * Tests serializing the class to xml.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testXmlSerialization() throws JAXBException
    {
        Filter filter = new Filter();
        filter.setName("aFilter");
        FeatureAction action = new FeatureAction();

        assertTrue(action.isEnabled());
        assertTrue(action.isVisible());

        action.setFilter(filter);
        action.setEnabled(false);
        action.setName("anaction");
        action.setGroupName("groupofactions");
        action.setVisible(false);

        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setIconId(22);

        action.getActions().add(styleAction);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(action, output);

        FeatureAction actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), FeatureAction.class);

        assertEquals("aFilter", actual.getFilter().getName());
        StyleAction actualStyleAction = (StyleAction)actual.getActions().get(0);
        assertEquals(22, actualStyleAction.getStyleOptions().getIconId());
        assertFalse(actual.enabledProperty().get());
        assertEquals("anaction", actual.nameProperty().get());
        assertEquals(actual.nameProperty().get(), actual.getName());
        assertEquals("groupofactions", actual.groupNameProperty().get());
        assertEquals(actual.groupNameProperty().get(), actual.getGroupName());
        assertFalse(actual.isVisible());
    }
}
