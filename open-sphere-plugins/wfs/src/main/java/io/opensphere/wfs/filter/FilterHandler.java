package io.opensphere.wfs.filter;

import java.awt.Color;
import java.util.List;

import org.xml.sax.Attributes;

import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.wfs.gml311.AbstractGmlGeometryHandler;
import io.opensphere.wfs.gml311.GeometryHandlerFactory;

/** WFS filter geometry SAX handler. */
public class FilterHandler extends BetterDefaultHandler
{
    /** Sub-handler for parsing feature geometries. */
    private AbstractGmlGeometryHandler myCurrentGeometryHandler;

    /** The geometries. */
    private final List<AbstractMapGeometrySupport> myGeometries = New.list();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wfs.filter.BetterDefaultHandler#startElement(String,
     *      String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        super.startElement(uri, localName, qName, attributes);

        if (GeometryHandlerFactory.GML_POLYGON_TAG.equals(localName))
        {
            myCurrentGeometryHandler = GeometryHandlerFactory.getGeometryHandler(localName, false);
        }
        else if (EnvelopeHandler.ENVELOPE_TAG.equals(localName))
        {
            myCurrentGeometryHandler = new EnvelopeHandler();
        }
        if (myCurrentGeometryHandler != null)
        {
            myCurrentGeometryHandler.handleOpeningTag(localName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wfs.filter.BetterDefaultHandler#endElement(String,
     *      String, String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (myCurrentGeometryHandler != null)
        {
            myCurrentGeometryHandler.handleClosingTag(localName, getCurrentValue());
        }

        AbstractMapGeometrySupport geometry = null;
        boolean isEndTag = false;
        if (GeometryHandlerFactory.GML_POLYGON_TAG.equals(localName) || EnvelopeHandler.ENVELOPE_TAG.equals(localName))
        {
            isEndTag = true;
            geometry = myCurrentGeometryHandler.getGeometry();
        }

        if (isEndTag)
        {
            if (geometry != null)
            {
                boolean isDisjoint = getElementStack().stream().anyMatch(elem -> "Disjoint".equals(elem.getLocalName()));
                geometry.setColor(isDisjoint ? Color.RED : Colors.QUERY_REGION, null);
                myGeometries.add(geometry);
            }
            myCurrentGeometryHandler = null;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * Gets the geometries.
     *
     * @return the geometries
     */
    public List<AbstractMapGeometrySupport> getGeometries()
    {
        return myGeometries;
    }
}
