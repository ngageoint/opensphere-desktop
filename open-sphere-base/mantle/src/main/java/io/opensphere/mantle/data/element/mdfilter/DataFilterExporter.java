package io.opensphere.mantle.data.element.mdfilter;

import java.io.File;
import java.util.Collection;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.XMLUtilities;
import net.opengis.ogc._110.LogicOpsType;

/** Exporter for filters. */
public class DataFilterExporter extends AbstractExporter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DataFilterExporter.class);

    /**
     * Constructor.
     */
    public DataFilterExporter()
    {
    }

    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && (File.class.isAssignableFrom(target) || Node.class.isAssignableFrom(target))
                && getObjects().stream().allMatch(o -> o instanceof DataFilter);
    }

    @Override
    public void export(Node node) throws ExportException
    {
        Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
        Node filtersNode = node.appendChild(doc.createElement("filters"));

        @SuppressWarnings("unchecked")
        Collection<DataFilter> objects = (Collection<DataFilter>)getObjects();

        for (DataFilter filter : objects)
        {
            if (filter.getFilterGroup().getCriteria().size() + filter.getFilterGroup().getGroups().size() > 0)
            {
                try
                {
                    JAXBElement<? extends LogicOpsType> jaxbFilter = FilterToWFS110Converter.convert(filter);
                    CustomBinaryLogicOpType logicOps = new CustomBinaryLogicOpType(jaxbFilter);
                    logicOps.setId(Integer.toHexString(filter.hashCode()));
                    logicOps.setTitle(filter.getName());
                    logicOps.setActive(filter.isActive());
                    logicOps.setUrlKey(filter.getTypeKey());
                    logicOps.setServerName(filter.getServerName());
                    if (filter.getFilterDescription() != null)
                    {
                        logicOps.setFilterDescription(filter.getFilterDescription());
                    }

                    XMLUtilities.marshalJAXBObjectToElement(logicOps, filtersNode);
                }
                catch (FilterException | JAXBException e)
                {
                    throw new ExportException("Failed to export filter: " + e, e);
                }
            }
            else
            {
                LOGGER.warn("Filter " + filter.getName() + " is empty, skipping its export.");
            }
        }
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.XML;
    }
}
