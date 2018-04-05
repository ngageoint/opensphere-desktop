package io.opensphere.server.permalink.loaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.test.core.matchers.EasyMockHelper;

/**
 * Tests the StateFileUploader class.
 */
public class FileUploaderTest
{
    /**
     * The server url.
     */
    private static final String ourUrl = "http://somehost/file-store/v1";

    /**
     * The download url returned by the server.
     */
    private static final String ourDownloadUrl = ourUrl + "fileDownload.do";

    /**
     * Tests the upload function.
     *
     * @throws IOException Bad IO.
     * @throws ParserConfigurationException Bad parse.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testUpload() throws IOException, ParserConfigurationException, URISyntaxException
    {
        Node stateNode = createStateNode();
        String state = XMLUtilities.format(stateNode);
        File testFile = new File("testFile.state");
        testFile.deleteOnExit();
        boolean created = testFile.createNewFile();

        assertTrue(created);

        FileOutputStream output = new FileOutputStream(testFile);
        output.write(state.getBytes(StringUtilities.DEFAULT_CHARSET));
        output.close();

        HttpServer server = createServer();

        EasyMock.replay(server);

        FileUploader uploader = new FileUploader(testFile, server, ourUrl);
        String downloadUrl = uploader.upload();

        EasyMock.verify(server);

        assertEquals(ourDownloadUrl, downloadUrl);
    }

    /**
     * The return stream.
     *
     * @return The return stream.
     */
    private CancellableInputStream createReturnStream()
    {
        String jsonString = "{ \"success\": true, \"url\": \"" + ourDownloadUrl + "\" }";
        return new CancellableInputStream(new ByteArrayInputStream(jsonString.getBytes(StringUtilities.DEFAULT_CHARSET)), null);
    }

    /**
     * Creates an easy mocked server.
     *
     * @return The server.
     * @throws MalformedURLException Bad url.
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    private HttpServer createServer() throws MalformedURLException, IOException, URISyntaxException
    {
        HttpServer server = EasyMock.createMock(HttpServer.class);
        IAnswer<CancellableInputStream> answer = new IAnswer<CancellableInputStream>()
        {
            @Override
            public CancellableInputStream answer() throws SAXException, IOException, ParserConfigurationException
            {
                File file = (File)EasyMock.getCurrentArguments()[1];
                ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[2];
                response.setResponseCode(HttpURLConnection.HTTP_OK);

                Element state = XMLUtilities.newDocumentBuilderNS().parse(file).getDocumentElement();
                assertEquals("state", state.getLocalName());

                return createReturnStream();
            }
        };
        EasyMock.expect(
                server.postFile(EasyMockHelper.eq(new URL(ourUrl)), EasyMock.isA(File.class), EasyMock.isA(ResponseValues.class)))
                .andAnswer(answer);

        return server;
    }

    /**
     * Creates the test state node.
     *
     * @return The state node.
     * @throws ParserConfigurationException Bad parse.
     */
    private Node createStateNode() throws ParserConfigurationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        return doc.appendChild(doc.createElement("state"));
    }
}
