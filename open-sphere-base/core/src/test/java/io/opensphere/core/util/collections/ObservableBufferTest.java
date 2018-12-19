package io.opensphere.core.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests the {@link ObservableBuffer} class. */
public class ObservableBufferTest
{
    /** The object on which tests are performed. */
    private ObservableBuffer<String> myObservableBuffer;

    /** Sets up the test. */
    @Before
    public void setUp()
    {
        myObservableBuffer = new ObservableBuffer<>(5);
    }

    /** Cleans up after each test. */
    @After
    public void tearDown()
    {
        myObservableBuffer = null;
    }

    /** Test method for {@link ObservableBuffer#size()}. */
    @Test
    public void testSizeZero()
    {
        assertEquals(0, myObservableBuffer.size());
    }

    /** Test method for {@link ObservableBuffer#size()}. */
    @Test
    public void testSizeOne()
    {
        myObservableBuffer.offer("one");

        assertEquals(1, myObservableBuffer.size());
    }

    /** Test method for {@link ObservableBuffer#size()}. */
    @Test
    public void testSizeMultipleUnderCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");

        assertEquals(4, myObservableBuffer.size());
    }

    /** Test method for {@link ObservableBuffer#size()}. */
    @Test
    public void testSizeMultipleOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");

        assertEquals(5, myObservableBuffer.size());
    }

    /** Test method for {@link ObservableBuffer#offer(Object)}. */
    @Test
    public void testOfferAndPeekOne()
    {
        myObservableBuffer.offer("one");

        assertEquals(1, myObservableBuffer.size());

        assertEquals("one", myObservableBuffer.peek());
    }

    /** Test method for {@link ObservableBuffer#offer(Object)}. */
    @Test
    public void testOfferUnderCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");

        // examine items in order:
        Object[] array = myObservableBuffer.toArray();
        assertEquals(4, array.length);
        assertEquals("one", array[0]);
        assertEquals("two", array[1]);
        assertEquals("three", array[2]);
        assertEquals("four", array[3]);
    }

    /** Test method for {@link ObservableBuffer#offer(Object)}. */
    @Test
    public void testOfferOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");

        // examine items in order:
        Object[] array = myObservableBuffer.toArray();
        assertEquals(5, array.length);
        assertEquals("three", array[0]);
        assertEquals("four", array[1]);
        assertEquals("five", array[2]);
        assertEquals("six", array[3]);
        assertEquals("seven", array[4]);

    }

    /** Test method for {@link ObservableBuffer#add(Object)}. */
    @Test
    public void testAddOne()
    {
        myObservableBuffer.add("one");

        // examine items in order:
        Object[] array = myObservableBuffer.toArray();
        assertEquals(1, array.length);
        assertEquals("one", array[0]);
    }

    /** Test method for {@link ObservableBuffer#add(Object)}. */
    @Test
    public void testAddUnderCapacity()
    {
        myObservableBuffer.add("one");
        myObservableBuffer.add("two");
        myObservableBuffer.add("three");
        myObservableBuffer.add("four");

        // examine items in order:
        Object[] array = myObservableBuffer.toArray();
        assertEquals(4, array.length);
        assertEquals("one", array[0]);
        assertEquals("two", array[1]);
        assertEquals("three", array[2]);
        assertEquals("four", array[3]);
    }

    /** Test method for {@link ObservableBuffer#add(Object)}. */
    @Test(expected = IllegalStateException.class)
    public void testAddOverCapacity()
    {
        myObservableBuffer.add("one");
        myObservableBuffer.add("two");
        myObservableBuffer.add("three");
        myObservableBuffer.add("four");
        myObservableBuffer.add("five");
        myObservableBuffer.add("six");
        myObservableBuffer.add("seven");
    }

    /** Test method for {@link ObservableBuffer#remove()}. */
    @Test
    public void testRemoveOneAndOne()
    {
        myObservableBuffer.offer("one");

        String removed = myObservableBuffer.remove();
        assertEquals("one", removed);

        Object[] array = myObservableBuffer.toArray();
        assertEquals(0, array.length);
    }

    /** Test method for {@link ObservableBuffer#remove()}. */
    @Test
    public void testRemoveTwoAndOne()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");

        String removed = myObservableBuffer.remove();
        assertEquals("two", removed);

        Object[] array = myObservableBuffer.toArray();
        assertEquals(1, array.length);
        assertEquals("one", array[0]);
    }

    /** Test method for {@link ObservableBuffer#remove()}. */
    @Test
    public void testRemoveAtCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");

        String removed = myObservableBuffer.remove();
        assertEquals("five", removed);

        Object[] array = myObservableBuffer.toArray();
        assertEquals(4, array.length);
        assertEquals("one", array[0]);
        assertEquals("two", array[1]);
        assertEquals("three", array[2]);
        assertEquals("four", array[3]);
    }

    /** Test method for {@link ObservableBuffer#remove()}. */
    @Test
    public void testRemoveOneOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");

        String removed = myObservableBuffer.remove();
        assertEquals("seven", removed);

        Object[] array = myObservableBuffer.toArray();
        assertEquals(4, array.length);
        assertEquals("three", array[0]);
        assertEquals("four", array[1]);
        assertEquals("five", array[2]);
        assertEquals("six", array[3]);
    }

    /** Test method for {@link ObservableBuffer#remove()}. */
    @Test
    public void testRemoveMultipleOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");

        String removedOne = myObservableBuffer.remove();
        assertEquals("seven", removedOne);
        String removedTwo = myObservableBuffer.remove();
        assertEquals("six", removedTwo);
        String removedThree = myObservableBuffer.remove();
        assertEquals("five", removedThree);

        Object[] array = myObservableBuffer.toArray();
        assertEquals(2, array.length);
        assertEquals("three", array[0]);
        assertEquals("four", array[1]);
    }

    /** Test method for {@link ObservableBuffer#poll()}. */
    @Test
    public void testPollEmpty()
    {
        assertNull(myObservableBuffer.poll());
    }

    /** Test method for {@link ObservableBuffer#poll()}. */
    @Test
    public void testPollOneAndOne()
    {
        myObservableBuffer.offer("one");
        assertEquals("one", myObservableBuffer.poll());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(0, array.length);
    }

    /** Test method for {@link ObservableBuffer#poll()}. */
    @Test
    public void testPollEmptyMultipleAndOne()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        assertEquals("two", myObservableBuffer.poll());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(1, array.length);
        assertEquals("one", array[0]);
    }

    /** Test method for {@link ObservableBuffer#poll()}. */
    @Test
    public void testPollEmptyMultipleAndMultiple()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        assertEquals("two", myObservableBuffer.poll());
        assertEquals("one", myObservableBuffer.poll());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(0, array.length);

        assertNull(myObservableBuffer.poll());
    }

    /** Test method for {@link ObservableBuffer#element()}. */
    @Test(expected = NoSuchElementException.class)
    public void testElementEmpty()
    {
        myObservableBuffer.element();
    }

    /** Test method for {@link ObservableBuffer#element()}. */
    @Test
    public void testElementOne()
    {
        myObservableBuffer.offer("one");
        assertEquals("one", myObservableBuffer.element());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(1, array.length);
        assertEquals("one", array[0]);
    }

    /** Test method for {@link ObservableBuffer#element()}. */
    @Test
    public void testElementMultipleUnderCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        assertEquals("four", myObservableBuffer.element());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(4, array.length);
        assertEquals("one", array[0]);
        assertEquals("two", array[1]);
        assertEquals("three", array[2]);
        assertEquals("four", array[3]);
    }

    /** Test method for {@link ObservableBuffer#element()}. */
    @Test
    public void testElementMultipleOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");
        assertEquals("seven", myObservableBuffer.element());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(5, array.length);
        assertEquals("three", array[0]);
        assertEquals("four", array[1]);
        assertEquals("five", array[2]);
        assertEquals("six", array[3]);
        assertEquals("seven", array[4]);
    }

    /** Test method for {@link ObservableBuffer#peek()}. */
    @Test
    public void testPeekEmpty()
    {
        assertNull(myObservableBuffer.peek());
    }

    /** Test method for {@link ObservableBuffer#peek()}. */
    @Test
    public void testPeekOne()
    {
        myObservableBuffer.offer("one");
        assertEquals("one", myObservableBuffer.peek());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(1, array.length);
        assertEquals("one", array[0]);
    }

    /** Test method for {@link ObservableBuffer#peek()}. */
    @Test
    public void testPeekMultipleUnderCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        assertEquals("four", myObservableBuffer.peek());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(4, array.length);
        assertEquals("one", array[0]);
        assertEquals("two", array[1]);
        assertEquals("three", array[2]);
        assertEquals("four", array[3]);
    }

    /** Test method for {@link ObservableBuffer#peek()}. */
    @Test
    public void testPeekMultipleOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");
        assertEquals("seven", myObservableBuffer.peek());

        Object[] array = myObservableBuffer.toArray();
        assertEquals(5, array.length);
        assertEquals("three", array[0]);
        assertEquals("four", array[1]);
        assertEquals("five", array[2]);
        assertEquals("six", array[3]);
        assertEquals("seven", array[4]);
    }

    /** Test method for {@link ObservableBuffer#get(int)}. */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIntNegative()
    {
        myObservableBuffer.get(-1);
    }

    /** Test method for {@link ObservableBuffer#get(int)}. */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIntZeroEmpty()
    {
        myObservableBuffer.get(0);
    }

    /** Test method for {@link ObservableBuffer#get(int)}. */
    @Test
    public void testGetIntUnderCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");

        assertEquals("one", myObservableBuffer.get(0));
        assertEquals("two", myObservableBuffer.get(1));
        assertEquals("three", myObservableBuffer.get(2));
        assertEquals("four", myObservableBuffer.get(3));
    }

    /** Test method for {@link ObservableBuffer#get(int)}. */
    @Test
    public void testGetIntOverCapacity()
    {
        myObservableBuffer.offer("one");
        myObservableBuffer.offer("two");
        myObservableBuffer.offer("three");
        myObservableBuffer.offer("four");
        myObservableBuffer.offer("five");
        myObservableBuffer.offer("six");
        myObservableBuffer.offer("seven");

        assertEquals("three", myObservableBuffer.get(0));
        assertEquals("four", myObservableBuffer.get(1));
        assertEquals("five", myObservableBuffer.get(2));
        assertEquals("six", myObservableBuffer.get(3));
        assertEquals("seven", myObservableBuffer.get(4));
    }
}
