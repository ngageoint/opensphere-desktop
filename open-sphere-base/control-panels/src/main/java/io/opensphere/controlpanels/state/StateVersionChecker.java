package io.opensphere.controlpanels.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Checks the version of the state file and notifies the user if it is a version
 * we do not recognize.
 */
public class StateVersionChecker
{
    /**
     * Checks the version of the state file and notifies the user if it is a
     * version we do not recognize.
     *
     * @param stateStream The stream to the state file data.
     * @param notifyUser True if the user should be notified of an unrecognized
     *            version.
     * @return A kosier stream that can be by our state file module, and the
     *         version namespace.
     * @throws IOException If the sateStream could not be read.
     * @throws ParserConfigurationException If the state file could not be be
     *             parsed.
     * @throws SAXException If the state file could not be parsed.
     */
    public Pair<InputStream, String> checkVersion(InputStream stateStream, boolean notifyUser)
        throws IOException, SAXException, ParserConfigurationException
    {
        // Ensure the stream is markable
        InputStream streamToUse = new NonCloseableInputStream(stateStream);
        if (!streamToUse.markSupported())
        {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
            new StreamReader(streamToUse).readStreamToOutputStream(outStream);
            streamToUse = new ByteArrayInputStream(outStream.toByteArray());
        }
        else
        {
            streamToUse.mark(streamToUse.available() + 1);
        }

        // Read the namespace
        Document doc = XMLUtilities.newDocumentBuilderNS().parse(streamToUse);
        String namespace = doc.getFirstChild().getNamespaceURI();

        // Reset
        streamToUse.reset();

        // Get a stream to return that may have the namespace substituted
        InputStream returnStream = streamToUse;
        if (ModuleStateController.STATE_NAMESPACE_V3.equals(namespace))
        {
            String stateString = new StreamReader(streamToUse).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
            stateString = stateString.replaceAll(namespace, ModuleStateController.STATE_NAMESPACE);
            returnStream = new ByteArrayInputStream(stateString.getBytes(StringUtilities.DEFAULT_CHARSET));
        }

        // Notify user of unknown version
        if (notifyUser && !ModuleStateController.KNOWN_NAMESPACES.contains(namespace))
        {
            Notify.warn("Unrecognized state file version, there may be issues loading the data from this file.", Method.TOAST);
        }

        return new Pair<>(returnStream, namespace);
    }
}
