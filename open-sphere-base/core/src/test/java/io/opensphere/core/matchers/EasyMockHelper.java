package io.opensphere.core.matchers;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.easymock.IExpectationSetters;
import org.easymock.LogicalOperator;

import io.opensphere.core.util.XMLUtilities;

/**
 * Contains utility methods that build argument matchers for specific types of
 * arguments.
 */
@SuppressWarnings("PMD.ShortMethodName")
public final class EasyMockHelper
{
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
     * Match a URL argument by comparing the string representation of the URLs
     * passed in. *
     *
     * @param url The expected url.
     * @return null.
     */
    public static URL eq(URL url)
    {
        return EasyMock.cmp(url, new URLComparator(), LogicalOperator.EQUAL);
    }

    /**
     * Builds an easy mock InputStream containing the marshalled jaxb Object and
     * setups up an input stream comparison on the easy mock object.
     *
     * @param jaxbObject The jaxb object to marshal.
     * @return Null.
     * @throws JAXBException On marshal error.
     */
    public static InputStream eqXmlStream(Object jaxbObject) throws JAXBException
    {
        return EasyMock.cmp(XMLUtilities.writeXMLObjectToInputStreamSync(jaxbObject), new InputStreamComparator(),
                LogicalOperator.EQUAL);
    }

    /**
     * Expect the last call to a mock object and count down the given latch when
     * the call is made.
     *
     * @param latch The latch.
     */
    public static void expectLastCallAndCountDownLatch(final CountDownLatch latch)
    {
        expectLastCallAndCountDownLatch(latch, (Void)null);
    }

    /**
     * Expect the last call to a mock object and count down the given latch when
     * the call is made.
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
     * An {@link IArgumentMatcher} that matches the value from a {@link Capture}
     * object.
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
     * An {@link IArgumentMatcher} that matches a {@link Collection} by checking
     * {@link Collection#size()} and {@link Collection#containsAll(Collection)}.
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
