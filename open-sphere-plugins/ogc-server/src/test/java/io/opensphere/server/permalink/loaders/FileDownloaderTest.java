package io.opensphere.server.permalink.loaders;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Tests the StateFileDownloader class.
 */
public class FileDownloaderTest
{
    /**
     * The test url.
     */
    private static final String ourUrl = "http://somehost/tools/fileDownload.do?fileId=12";

    /**
     * Tests the download file.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad sax.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testDownloadFile() throws IOException, SAXException, ParserConfigurationException, URISyntaxException
    {
        HttpServer server = createServer();

        EasyMock.replay(server);

        FileDownloader downloader = new FileDownloader(server, ourUrl);
        InputStream state = downloader.downloadFile();

        Element stateFile = XMLUtilities.newDocumentBuilderNS().parse(state).getDocumentElement();

        assertEquals("state", stateFile.getLocalName());

        EasyMock.verify(server);
    }

    /**
     * Creates the state file stream to return.
     *
     * @return The state file stream.
     * @throws ParserConfigurationException Bad parse.
     */
    private CancellableInputStream createReturnStream() throws ParserConfigurationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Node stateFile = doc.appendChild(doc.createElement("state"));

        String state = XMLUtilities.format(stateFile);
        ByteArrayInputStream stream = new ByteArrayInputStream(state.getBytes(StringUtilities.DEFAULT_CHARSET));

        return new CancellableInputStream(stream, null);
    }

    /**
     * Creates the server for test.
     *
     * @return The rest server.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    private HttpServer createServer() throws IOException, URISyntaxException
    {
        HttpServer server = EasyMock.createMock(HttpServer.class);
        IAnswer<? extends CancellableInputStream> answer = new IAnswer<CancellableInputStream>()
        {
            @Override
            public CancellableInputStream answer() throws ParserConfigurationException
            {
                ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
                response.setResponseCode(HttpURLConnection.HTTP_OK);

                return createReturnStream();
            }
        };
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(new URL(ourUrl)), EasyMock.isA(ResponseValues.class))).andAnswer(answer);

        return server;
    }
}
