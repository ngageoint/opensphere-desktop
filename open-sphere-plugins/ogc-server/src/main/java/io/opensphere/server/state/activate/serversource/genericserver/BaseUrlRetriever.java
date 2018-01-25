package io.opensphere.server.state.activate.serversource.genericserver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.collections.New;

/**
 * Base class for the UrlRetrievers that provides the base node walking
 * functionality.
 */
public abstract class BaseUrlRetriever
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(BaseUrlRetriever.class);

    /**
     * Gets the list of Urls contained in the layer nodes within the specified
     * node.
     *
     * @param node The state node to inspect for urls.
     * @return The list of urls.
     */
    public List<URL> getUrls(Node node)
    {
        List<URL> urls = New.list();

        List<String> layerPaths = getLayerPaths();

        for (String layerPath : layerPaths)
        {
            try
            {
                NodeList childNodes = StateXML.getChildNodes(node, layerPath);

                if (childNodes != null)
                {
                    for (int i = 0; i < childNodes.getLength(); i++)
                    {
                        Node childNode = childNodes.item(i);

                        URL url = getUrlFromNode(childNode);
                        if (url != null)
                        {
                            urls.add(url);
                        }
                    }
                }
            }
            catch (XPathExpressionException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return urls;
    }

    /**
     * Gets the list of xpaths for the layer nodes to get url values for.
     *
     * @return The list of xpaths.
     */
    protected abstract List<String> getLayerPaths();

    /**
     * Gets the url from the specified node.
     *
     * @param node The layer node.
     * @return The url contained in the layer node, or null if there wasn't one.
     */
    private URL getUrlFromNode(Node node)
    {
        URL url = null;

        try
        {
            Node urlNode = StateXML.getChildNode(node, "child:::url");

            if (urlNode != null)
            {
                NodeList childNodes = urlNode.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node childNode = childNodes.item(i);

                    if (childNode instanceof Text)
                    {
                        Text textChild = (Text)childNode;
                        if (!textChild.isElementContentWhitespace())
                        {
                            try
                            {
                                String urlString = transformUrl(textChild.getWholeText());
                                url = new URI(urlString).normalize().toURL();
                                textChild.setData(url.toString());
                            }
                            catch (URISyntaxException e)
                            {
                                LOGGER.error(e.getMessage(), e);
                            }

                            break;
                        }
                    }
                }
            }
        }
        catch (XPathExpressionException | MalformedURLException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return url;
    }

    /**
     * Transforms the url string to one that is valid for a given retriever.
     *
     * @param url The url to transform.
     * @return the transformed string or url if transformation did not need to
     *         occur.
     */
    protected String transformUrl(String url)
    {
        return url;
    }
}
