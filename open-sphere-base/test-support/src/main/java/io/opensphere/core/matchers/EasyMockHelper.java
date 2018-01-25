package io.opensphere.core.matchers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.easymock.IExpectationSetters;
import org.easymock.LogicalOperator;

/**
 * Contains utility methods that build argument matchers for specific types of arguments.
 */
@SuppressWarnings("PMD.ShortMethodName")
public final class EasyMockHelper
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(EasyMockHelper.class);

    /**
     * Report a new {@link CaptureArgumentMatcher} to EasyMock.
     *
     * @param <T> The type of the capture.
     * @param capture The capture object to match.
     * @return {@code null}
     */
    public static <T> T eq(Capture<T> capture)
    {
        EasyMock.reportMatcher(new CaptureArgumentMatcher<T>(capture));
        return null;
    }

    /**
     * Report a new {@link CollectionArgumentMatcher} to EasyMock.
     *
     * @param <T> The type of the elements in the collection.
     * @param collection The collection to match.
     * @return {@code null}
     */
    public static <T> Collection<? extends T> eq(Collection<? extends T> collection)
    {
        EasyMock.reportMatcher(new CollectionArgumentMatcher(collection));
        return null;
    }

    /**
     * Match a URL argument by comparing the string representation of the URLs passed in. *
     *
     * @param url The expected url.
     * @return null.
     */
    public static URL eq(URL url)
    {
        return EasyMock.cmp(url, new URLComparator(), LogicalOperator.EQUAL);
    }

    /**
     * Builds an easy mock InputStream containing the marshalled jaxb Object and setups up an input stream comparison on the easy
     * mock object.
     *
     * @param jaxbObject The jaxb object to marshal.
     * @return Null.
     * @throws JAXBException On marshal error.
     */
    public static InputStream eqXmlStream(Object jaxbObject) throws JAXBException
    {
        return EasyMock.cmp(writeXMLObjectToInputStreamSync(jaxbObject), new InputStreamComparator(), LogicalOperator.EQUAL);
    }

    /**
     * Get an input stream that provides the marshalled content of a JAXB object. Using this method will marshal the JAXB object
     * inline and then return a stream that provides the marshalled data.
     *
     * @param jaxbElement The object.
     * @param classes The classes to load in the JAXB context. If no classes are provided, the class of the <tt>jaxbElement</tt>
     *            will be used for the context.
     * @return The input stream that will provide the XML.
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static InputStream writeXMLObjectToInputStreamSync(final Object jaxbElement, Class<?>... classes) throws JAXBException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeXMLObject(jaxbElement, os, classes);
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * Write a JAXB object to a stream.
     *
     * @param jaxbElement The object.
     * @param output The stream.
     * @param classes The classes to load in the JAXB context. If no classes are provided, the class of the <tt>jaxbElement</tt>
     *            will be used for the context.
     *
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static void writeXMLObject(Object jaxbElement, OutputStream output, Class<?>... classes) throws JAXBException
    {
        Class<?>[] contextClasses;
        if (classes == null || classes.length == 0)
        {
            contextClasses = new Class<?>[] { jaxbElement.getClass() };
        }
        else
        {
            contextClasses = classes;
        }
        StringWriter stringWriter = new StringWriter();
        JAXBContextHelper.getCachedContext(contextClasses).createMarshaller().marshal(jaxbElement, stringWriter);
        format(new StringReader(stringWriter.toString()), output, null);
    }

    /**
     * Format XML from a reader and send it to an output stream.
     *
     * @param reader The input.
     * @param output The output.
     * @param doctypeSystem Optional system identifier for the document type declaration.
     */
    public static void format(Reader reader, OutputStream output, String doctypeSystem)
    {
        format(new StreamSource(reader), new StreamResult(output), doctypeSystem);
    }

    /**
     * Format XML.
     *
     * @param xmlInput XML source.
     * @param xmlOutput XML result.
     * @param doctypeSystem Optional system identifier for the document type declaration.
     */
    public static void format(Source xmlInput, Result xmlOutput, String doctypeSystem)
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            if (doctypeSystem != null)
            {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
            }
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(3));
            transformer.transform(xmlInput, xmlOutput);
        }
        catch (TransformerFactoryConfigurationError | TransformerException e)
        {
            LOG.error("Failed to transform XML: " + e, e);
        }
    }

    /**
     * Expect the last call to a mock object and count down the given latch when the call is made.
     *
     * @param latch The latch.
     */
    public static void expectLastCallAndCountDownLatch(final CountDownLatch latch)
    {
        expectLastCallAndCountDownLatch(latch, (Void)null);
    }

    /**
     * Expect the last call to a mock object and count down the given latch when the call is made.
     *
     * @param <T> The type of the return object.
     * @param latch The latch.
     * @param returnObject The object to return in the easy mock call.
     */
    public static <T> void expectLastCallAndCountDownLatch(final CountDownLatch latch, final T returnObject)
    {
        IExpectationSetters<Object> expectation = EasyMock.expectLastCall();
        expectation.andAnswer(new IAnswer<T>()
        {
            @Override
            public T answer()
            {
                latch.countDown();
                return returnObject;
            }
        });
    }

    /**
     * Not constructible.
     */
    private EasyMockHelper()
    {
    }

    /**
     * An {@link IArgumentMatcher} that matches the value from a {@link Capture} object.
     *
     * @param <E> The type of the capture.
     */
    private static class CaptureArgumentMatcher<E> implements IArgumentMatcher
    {
        /** The capture to match. */
        private final Capture<E> myCapture;

        /**
         * Constructor.
         *
         * @param capture The capture object to match.
         */
        public CaptureArgumentMatcher(Capture<E> capture)
        {
            myCapture = capture;
        }

        @Override
        public void appendTo(StringBuffer sb)
        {
            sb.append("matches(Capture.getValue())");
        }

        @Override
        public boolean matches(Object obj)
        {
            return obj == null ? myCapture.getValue() == null : obj.equals(myCapture.getValue());
        }
    }

    /**
     * An {@link IArgumentMatcher} that matches a {@link Collection} by checking {@link Collection#size()} and
     * {@link Collection#containsAll(Collection)}.
     */
    private static class CollectionArgumentMatcher implements IArgumentMatcher
    {
        /** The collection to match. */
        private final Collection<?> myCollection;

        /**
         * Constructor.
         *
         * @param collection The collection to match.
         */
        public CollectionArgumentMatcher(Collection<?> collection)
        {
            myCollection = collection;
        }

        @Override
        public void appendTo(StringBuffer sb)
        {
            sb.append("matches(").append(myCollection).append(')');
        }

        @Override
        public boolean matches(Object obj)
        {
            if (obj == null)
            {
                return myCollection == null;
            }
            else if (myCollection == null)
            {
                return false;
            }
            else if (!(obj instanceof Collection))
            {
                return false;
            }
            Collection<?> col = (Collection<?>)obj;
            if (myCollection.size() != col.size())
            {
                return false;
            }
            return myCollection.containsAll(col);
        }
    }
}
