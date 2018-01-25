package io.opensphere.core.util.collections;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link CollectionUtilities}.
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.AvoidDuplicateLiterals" })
public class CollectionUtilitiesTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CollectionUtilitiesTest.class);

    /**
     * Test for {@link CollectionUtilities#addIfNotContained(Collection, Collection, BiPredicate)}.
     */
    @Test
    public void testAddIfNotContained()
    {
        List<String> list = New.list("abc");
        BiPredicate<String, String> predicate = (o1, o2) -> o1.charAt(1) == o2.charAt(1);

        CollectionUtilities.addIfNotContained(list, "abd", predicate);
        Assert.assertEquals(Arrays.asList("abc"), list);

        CollectionUtilities.addIfNotContained(list, "acd", predicate);
        Assert.assertEquals(Arrays.asList("abc", "acd"), list);

        CollectionUtilities.addIfNotContained(list, Arrays.asList("bcd", "bdd"), predicate);
        Assert.assertEquals(Arrays.asList("abc", "acd", "bdd"), list);
    }

    /**
     * Test for {@link CollectionUtilities#addSorted(List, Collection)}.
     */
    @Test
    public void testAddSorted()
    {
        List<Integer> list = New.list();
        CollectionUtilities.addSorted(list, Arrays.asList(1, 2, 3));
        Assert.assertEquals(Arrays.asList(1, 2, 3), list);

        list = New.list(1, 3, 5);
        CollectionUtilities.addSorted(list, Arrays.asList(2, 4, 6));
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), list);
    }

    /**
     * Test for
     * {@link CollectionUtilities#concat(CollectionProvider, Collection...)}.
     */
    @Test
    public void testConcatCollection()
    {
        int size1 = 20;
        Object[] objs1 = createObjectArray(size1);
        int size2 = 30;
        Object[] objs2 = createObjectArray(size2);
        int size3 = 40;
        Object[] objs3 = createObjectArray(size3);

        List<Object> list1 = Arrays.asList(objs1);
        List<Object> list2 = Arrays.asList(objs2);
        List<Object> list3 = Arrays.asList(objs3);

        Collection<Object> result = CollectionUtilities.concat(New.collectionFactory(), list1, list2, list3);
        Assert.assertEquals(size1 + size2 + size3, result.size());
        Iterator<Object> iter = result.iterator();
        for (int index = 0; index < size1;)
        {
            Assert.assertSame(objs1[index++], iter.next());
        }
        for (int index = 0; index < size2;)
        {
            Assert.assertSame(objs2[index++], iter.next());
        }
        for (int index = 0; index < size3;)
        {
            Assert.assertSame(objs3[index++], iter.next());
        }

        Assert.assertEquals(list2, CollectionUtilities.concat(New.collectionFactory(), list2));

        Assert.assertTrue(CollectionUtilities.concat(New.collectionFactory()).isEmpty());
    }

    /**
     * Test for {@link CollectionUtilities#concatObjs(Iterable, Object...)}.
     */
    @Test
    public void testConcatIterableObjectArray()
    {
        int size1 = 20;
        Object[] objs1 = createObjectArray(size1);
        int size2 = 2;
        Object[] objs2 = createObjectArray(size2);

        List<Object> list1 = Arrays.asList(objs1);

        Collection<Object> result = CollectionUtilities.concatObjs(list1, objs2[0], objs2[1]);
        Assert.assertEquals(size1 + size2, result.size());
        Iterator<Object> iter = result.iterator();
        for (int index = 0; index < size1;)
        {
            Assert.assertSame(objs1[index++], iter.next());
        }
        for (int index = 0; index < size2;)
        {
            Assert.assertSame(objs2[index++], iter.next());
        }

        Assert.assertEquals(list1, CollectionUtilities.concatObjs(list1, new Object[0]));
    }

    /**
     * Test for {@link CollectionUtilities#concat(ListProvider, Collection...)}.
     */
    @Test
    public void testConcatList()
    {
        int size1 = 20;
        Object[] objs1 = createObjectArray(size1);
        int size2 = 30;
        Object[] objs2 = createObjectArray(size2);
        int size3 = 40;
        Object[] objs3 = createObjectArray(size3);

        List<Object> list1 = Arrays.asList(objs1);
        List<Object> list2 = Arrays.asList(objs2);
        List<Object> list3 = Arrays.asList(objs3);

        List<Object> result = CollectionUtilities.concat(New.listFactory(), list1, list2, list3);
        Assert.assertEquals(size1 + size2 + size3, result.size());
        Iterator<Object> iter = result.iterator();
        for (int index = 0; index < size1;)
        {
            Assert.assertSame(objs1[index++], iter.next());
        }
        for (int index = 0; index < size2;)
        {
            Assert.assertSame(objs2[index++], iter.next());
        }
        for (int index = 0; index < size3;)
        {
            Assert.assertSame(objs3[index++], iter.next());
        }

        Assert.assertEquals(list2, CollectionUtilities.concat(New.listFactory(), list2));

        Assert.assertTrue(CollectionUtilities.concat(New.listFactory()).isEmpty());
    }

    /**
     * Test for {@link CollectionUtilities#concat(SetProvider, Collection...)}.
     */
    @Test
    public void testConcatSet()
    {
        int size1 = 20;
        Object[] objs1 = createObjectArray(size1);
        int size2 = 30;
        Object[] objs2 = createObjectArray(size2);
        int size3 = 40;
        Object[] objs3 = createObjectArray(size3);

        List<Object> list1 = Arrays.asList(objs1);
        List<Object> list2 = Arrays.asList(objs2);
        List<Object> list3 = Arrays.asList(objs3);

        Set<Object> result = CollectionUtilities.concat(New.setFactory(), list1, list2, list3);
        Assert.assertEquals(size1 + size2 + size3, result.size());
        for (int index = 0; index < size1;)
        {
            Assert.assertTrue(result.contains(objs1[index++]));
        }
        for (int index = 0; index < size2;)
        {
            Assert.assertTrue(result.contains(objs2[index++]));
        }
        for (int index = 0; index < size3;)
        {
            Assert.assertTrue(result.contains(objs3[index++]));
        }

        Set<Object> set = CollectionUtilities.concat(New.setFactory(), list2);
        Assert.assertEquals(list2.size(), set.size());

        Assert.assertTrue(CollectionUtilities.concat(New.setFactory()).isEmpty());
    }

    /**
     * Test for {@link CollectionUtilities#containsAny(Collection, Collection)}.
     */
    @Test
    public void testContainsAny()
    {
        Assert.assertEquals(true, CollectionUtilities.containsAny(New.list("Baba", "Ganoush"), New.list("Joey", "Ganoush")));
        Assert.assertEquals(false, CollectionUtilities.containsAny(New.list("Baba", "Ganoush"), New.list("Joey")));
        Assert.assertEquals(false, CollectionUtilities.containsAny(New.list("Baba", "Ganoush"), New.list()));
        Assert.assertEquals(false, CollectionUtilities.containsAny(New.list(), New.list()));
    }

    /**
     * Test
     * {@link CollectionUtilities#determineClassTypesInCollection(Set, Collection)}
     * .
     */
    @Test
    @SuppressWarnings("PMD.LooseCoupling")
    public void testDetermineClassTypesInCollection()
    {
        Collection<Collection<?>> items = new ArrayList<>();
        items.add(new ArrayList<Object>());
        items.add(new HashSet<Object>());
        items.add(new ArrayList<Object>());
        items.add(new LinkedList<Object>());

        Set<Class<?>> types = new HashSet<>();
        Set<Class<?>> result = CollectionUtilities.determineClassTypesInCollection(types, items);

        Assert.assertSame(types, result);
        Assert.assertEquals(3, types.size());
        Assert.assertTrue(types.contains(ArrayList.class));
        Assert.assertTrue(types.contains(HashSet.class));
        Assert.assertTrue(types.contains(LinkedList.class));

        Set<Class<? extends Object>> result2 = CollectionUtilities.determineClassTypesInCollection(null, Collections.emptyList());
        Assert.assertTrue(result2.isEmpty());
    }

    /**
     * Test {@link CollectionUtilities#difference(Collection, Collection)}.
     */
    @Test
    public void testDifference()
    {
        Assert.assertEquals(Arrays.asList("A"),
                CollectionUtilities.difference(Arrays.asList("A", "B", "C"), Arrays.asList("B", "C", "D")));
        Assert.assertEquals(Arrays.asList("A", "B", "C"),
                CollectionUtilities.difference(Arrays.asList("A", "B", "C"), Collections.emptyList()));
        Assert.assertEquals(Collections.emptyList(),
                CollectionUtilities.difference(Collections.emptyList(), Arrays.asList("B", "C", "D")));
    }

    /**
     * Test {@link CollectionUtilities#filterDowncast(Collection, Class)}.
     */
    @Test
    public void testFilterDowncast()
    {
        Collection<Serializable> serializables = new ArrayList<>();
        Collection<Number> numbers = new ArrayList<>();
        Collection<Integer> integers = new ArrayList<>();
        serializables.add("string1");
        serializables.add(Double.valueOf(4.5));
        numbers.add(Double.valueOf(4.5));
        serializables.add(Integer.valueOf(10));
        numbers.add(Integer.valueOf(10));
        integers.add(Integer.valueOf(10));
        serializables.add("string2");
        serializables.add(Integer.valueOf(2));
        numbers.add(Integer.valueOf(2));
        integers.add(Integer.valueOf(2));
        serializables.add(Float.valueOf(20));
        numbers.add(Float.valueOf(20));
        serializables.add(Integer.valueOf(9));
        numbers.add(Integer.valueOf(9));
        integers.add(Integer.valueOf(9));
        serializables.add(Long.valueOf(30));
        numbers.add(Long.valueOf(30));

        Collection<Number> numbersResult = CollectionUtilities.filterDowncast(serializables, Number.class);
        Assert.assertEquals(numbers.size(), numbersResult.size());
        Assert.assertTrue(numbersResult.containsAll(numbers));

        Collection<Integer> integersResult = CollectionUtilities.filterDowncast(serializables, Integer.class);
        Assert.assertEquals(integers.size(), integersResult.size());
        Assert.assertTrue(integersResult.containsAll(integers));
    }

    /**
     * Test {@link CollectionUtilities#filterDowncast(Iterator, Class)}.
     */
    @Test
    public void testFilterDowncastIterator()
    {
        Collection<Serializable> serializables = new ArrayList<>();
        Collection<Number> numbers = new ArrayList<>();
        Collection<Integer> integers = new ArrayList<>();
        serializables.add(null);
        serializables.add("string1");
        serializables.add(Double.valueOf(4.5));
        numbers.add(Double.valueOf(4.5));
        serializables.add(Integer.valueOf(10));
        numbers.add(Integer.valueOf(10));
        integers.add(Integer.valueOf(10));
        serializables.add("string2");
        serializables.add(Integer.valueOf(2));
        numbers.add(Integer.valueOf(2));
        integers.add(Integer.valueOf(2));
        serializables.add(Float.valueOf(20));
        numbers.add(Float.valueOf(20));
        serializables.add(Integer.valueOf(9));
        numbers.add(Integer.valueOf(9));
        integers.add(Integer.valueOf(9));
        serializables.add(Long.valueOf(30));
        numbers.add(Long.valueOf(30));
        serializables.add(null);

        Iterator<Number> numbersIter1 = CollectionUtilities.filterDowncast(serializables.iterator(), Number.class);
        Iterator<Number> iter1 = numbers.iterator();
        while (iter1.hasNext())
        {
            Assert.assertTrue(numbersIter1.hasNext());
            Assert.assertEquals(iter1.next(), numbersIter1.next());
        }

        // Test again without calling hasNext.
        Iterator<Number> numbersIter2 = CollectionUtilities.filterDowncast(serializables.iterator(), Number.class);
        Iterator<Number> iter2 = numbers.iterator();
        while (iter2.hasNext())
        {
            Assert.assertEquals(iter2.next(), numbersIter2.next());
        }

        Iterator<Integer> integersIter1 = CollectionUtilities.filterDowncast(serializables.iterator(), Integer.class);
        Iterator<Integer> iter3 = integers.iterator();
        while (iter3.hasNext())
        {
            Assert.assertTrue(integersIter1.hasNext());
            Assert.assertEquals(iter3.next(), integersIter1.next());
        }

        // Test again without calling hasNext.
        Iterator<Integer> integersIter2 = CollectionUtilities.filterDowncast(serializables.iterator(), Integer.class);
        Iterator<Integer> iter4 = integers.iterator();
        while (iter4.hasNext())
        {
            Assert.assertEquals(iter4.next(), integersIter2.next());
        }
    }

    /**
     * Test {@link CollectionUtilities#getItem(Iterable, int)}.
     */
    @Test
    public void testGetItem()
    {
        String item0 = "zero";
        String item1 = "one";
        String item2 = "two";

        List<String> list = new ArrayList<>();
        list.add(item0);
        list.add(item1);
        list.add(item2);
        Assert.assertEquals(item0, CollectionUtilities.getItem(list, 0));
        Assert.assertEquals(item1, CollectionUtilities.getItem(list, 1));
        Assert.assertEquals(item2, CollectionUtilities.getItem(list, 2));

        Set<String> set = new LinkedHashSet<>();
        set.add(item0);
        set.add(item1);
        set.add(item2);

        Assert.assertEquals(item0, CollectionUtilities.getItem(set, 0));
        Assert.assertEquals(item1, CollectionUtilities.getItem(set, 1));
        Assert.assertEquals(item2, CollectionUtilities.getItem(set, 2));
    }

    /**
     * Test {@link CollectionUtilities#getItemOrNull(Iterable, int)}.
     */
    @Test
    public void testGetItemOrNull()
    {
        String item0 = "zero";
        String item1 = "one";
        String item2 = "two";

        List<String> list = new ArrayList<>();
        list.add(item0);
        list.add(item1);
        list.add(item2);
        Assert.assertEquals(item0, CollectionUtilities.getItemOrNull(list, 0));
        Assert.assertEquals(item1, CollectionUtilities.getItemOrNull(list, 1));
        Assert.assertEquals(item2, CollectionUtilities.getItemOrNull(list, 2));
        Assert.assertNull(CollectionUtilities.getItemOrNull(list, 3));

        Set<String> set = new LinkedHashSet<>();
        set.add(item0);
        set.add(item1);
        set.add(item2);

        Assert.assertEquals(item0, CollectionUtilities.getItemOrNull(set, 0));
        Assert.assertEquals(item1, CollectionUtilities.getItemOrNull(set, 1));
        Assert.assertEquals(item2, CollectionUtilities.getItemOrNull(set, 2));
        Assert.assertNull(CollectionUtilities.getItemOrNull(set, 3));

        Assert.assertNull(CollectionUtilities.getItemOrNull(Collections.emptySet(), 0));
    }

    /**
     * Test {@link CollectionUtilities#getLastItemOrNull(List)}.
     */
    @Test
    public void testGetLastItemOrNull()
    {
        String item0 = "zero";
        String item1 = "one";
        String item2 = "two";

        Assert.assertEquals(item2, CollectionUtilities.getLastItemOrNull(Arrays.asList(item0, item1, item2)));
        Assert.assertNull(CollectionUtilities.getLastItemOrNull(Collections.emptyList()));
    }

    /** Test {@link CollectionUtilities#iterate(Iterator, Iterator)}. */
    @Test
    public void testIterate()
    {
        Iterator<? extends String> iter1 = Arrays.asList("one", "two", "three").iterator();
        Iterator<? extends String> iter2 = Arrays.asList("four", "five", "six").iterator();

        List<String> values = new ArrayList<>();
        Iterator<String> result = CollectionUtilities.iterate(iter1, iter2);
        while (result.hasNext())
        {
            values.add(result.next());
        }

        Assert.assertEquals(6, values.size());
        Assert.assertEquals("one", values.get(0));
        Assert.assertEquals("two", values.get(1));
        Assert.assertEquals("three", values.get(2));
        Assert.assertEquals("four", values.get(3));
        Assert.assertEquals("five", values.get(4));
        Assert.assertEquals("six", values.get(5));
    }

    /**
     * Test {@link CollectionUtilities#indexOf(Comparable, List)} .
     */
    @Test
    public void testIndexOf()
    {
        List<String> values = Arrays.asList("Ardbeg", "Bowmore", "Bruichladdich", "Lagavulin", "Laphroaig");
        Assert.assertEquals(0, CollectionUtilities.indexOf("Aberlour", values));
        Assert.assertEquals(1, CollectionUtilities.indexOf("Ben Nevis", values));
        Assert.assertEquals(5, CollectionUtilities.indexOf("Oban", values));
        Assert.assertEquals(1, CollectionUtilities.indexOf("Bowmore", values));
    }

    /**
     * Test
     * {@link CollectionUtilities#partition(Collection, java.util.function.Function)}
     * .
     */
    @Test
    public void testPartition()
    {
        Collection<String> values = Arrays.asList("Ardbeg", "Bowmore", "Bruichladdich", "Lagavulin", "Laphroaig");
        Map<Character, List<String>> map = CollectionUtilities.partition(values, s -> Character.valueOf(s.charAt(0)));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(Arrays.asList("Ardbeg"), map.get(Character.valueOf('A')));
        Assert.assertEquals(Arrays.asList("Bowmore", "Bruichladdich"), map.get(Character.valueOf('B')));
        Assert.assertEquals(Arrays.asList("Lagavulin", "Laphroaig"), map.get(Character.valueOf('L')));
    }

    /**
     * Test
     * {@link CollectionUtilities#map(Collection, java.util.function.Function)}
     * .
     */
    @Test
    public void testMap()
    {
        Collection<String> values = Arrays.asList("Ardbeg", "Bowmore", "Bruichladdich", "Lagavulin", "Laphroaig");
        Map<Character, String> map = CollectionUtilities.map(values, s -> Character.valueOf(s.charAt(0)));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("Ardbeg", map.get(Character.valueOf('A')));
        Assert.assertEquals("Bruichladdich", map.get(Character.valueOf('B')));
        Assert.assertEquals("Laphroaig", map.get(Character.valueOf('L')));
    }

    /**
     * Test {@link CollectionUtilities#removeAll(Collection, Collection)} using
     * various collections and sizes.
     */
    @Test
    public void testRemoveAll()
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            List<Constructor<?>> constructors = new ArrayList<>();
            constructors.add(getArrayListConstructor());
            constructors.add(getLinkedListConstructor());
            constructors.add(getHashSetConstructor());
            for (int cIndex1 = 0; cIndex1 < constructors.size(); ++cIndex1)
            {
                Constructor<?> constructor1 = constructors.get(cIndex1);
                for (int cIndex2 = cIndex1; cIndex2 < constructors.size(); ++cIndex2)
                {
                    Constructor<?> constructor2 = constructors.get(cIndex2);
                    for (int size1 : new int[] { 0, 1, 2000, 4999, 5000 })
                    {
                        for (int size2 : new int[] { 0, 1, 2000, 4999, 5000 })
                        {
                            doTestRemoveAll(size1, size2, constructor1, constructor2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Test for {@link CollectionUtilities#removeFirst(Iterable, Predicate)}.
     */
    @Test
    public void testRemoveFirst()
    {
        List<String> input = New.list("Baba", "Ganoush", "Baba");
        Assert.assertEquals("Baba", CollectionUtilities.removeFirst(input, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return "Baba".equals(s);
            }
        }));
        Assert.assertEquals(New.list("Ganoush", "Baba"), input);

        input = New.list("Baba", "Ganoush");
        Assert.assertNull(CollectionUtilities.removeFirst(input, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return "Joey".equals(s);
            }
        }));
        Assert.assertEquals(New.list("Baba", "Ganoush"), input);
    }

    /**
     * Test {@link CollectionUtilities#subtract(Collection, Collection)} with
     * two collections of the same size.
     */
    @Test
    public void testSubtractEqual()
    {
        Constructor<?> arrayListConstructor = getArrayListConstructor();
        doTestSubtract(10000, 10000, arrayListConstructor, arrayListConstructor);

        Constructor<?> hashSetConstructor = getHashSetConstructor();
        doTestSubtract(100000, 100000, hashSetConstructor, hashSetConstructor);

        Constructor<?> linkedListConstructor = getLinkedListConstructor();
        doTestSubtract(5000, 5000, linkedListConstructor, linkedListConstructor);

        doTestSubtract(10000, 10000, arrayListConstructor, hashSetConstructor);
        doTestSubtract(10000, 10000, hashSetConstructor, arrayListConstructor);
        doTestSubtract(5000, 5000, arrayListConstructor, linkedListConstructor);
        doTestSubtract(10000, 10000, linkedListConstructor, arrayListConstructor);
        doTestSubtract(5000, 5000, hashSetConstructor, linkedListConstructor);
        doTestSubtract(10000, 10000, linkedListConstructor, hashSetConstructor);
    }

    /**
     * Test {@link CollectionUtilities#subtract(Collection, Collection)} with
     * the second collection half the size of the first.
     */
    @Test
    public void testSubtractHalf()
    {
        Constructor<?> arrayListConstructor = getArrayListConstructor();
        doTestSubtract(10000, 5000, arrayListConstructor, arrayListConstructor);

        Constructor<?> hashSetConstructor = getHashSetConstructor();
        doTestSubtract(100000, 50000, hashSetConstructor, hashSetConstructor);

        Constructor<?> linkedListConstructor = getLinkedListConstructor();
        doTestSubtract(10000, 5000, linkedListConstructor, linkedListConstructor);

        doTestSubtract(20000, 10000, arrayListConstructor, hashSetConstructor);
        doTestSubtract(40000, 20000, hashSetConstructor, arrayListConstructor);
        doTestSubtract(5000, 2500, arrayListConstructor, linkedListConstructor);
        doTestSubtract(10000, 5000, linkedListConstructor, arrayListConstructor);
        doTestSubtract(100000, 50000, hashSetConstructor, linkedListConstructor);
        doTestSubtract(100000, 50000, linkedListConstructor, hashSetConstructor);
    }

    /**
     * Test {@link CollectionUtilities#subtract(Collection, Collection)} with
     * the second collection having one less object that the first.
     */
    @Test
    public void testSubtractMinusOne()
    {
        Constructor<?> arrayListConstructor = getArrayListConstructor();
        doTestSubtract(10001, 10000, arrayListConstructor, arrayListConstructor);

        Constructor<?> hashSetConstructor = getHashSetConstructor();
        doTestSubtract(100001, 100000, hashSetConstructor, hashSetConstructor);

        Constructor<?> linkedListConstructor = getLinkedListConstructor();
        doTestSubtract(10001, 10000, linkedListConstructor, linkedListConstructor);

        doTestSubtract(10001, 10000, arrayListConstructor, hashSetConstructor);
        doTestSubtract(10001, 10000, hashSetConstructor, arrayListConstructor);
        doTestSubtract(10001, 10000, arrayListConstructor, linkedListConstructor);
        doTestSubtract(10001, 10000, linkedListConstructor, arrayListConstructor);
        doTestSubtract(10001, 10000, hashSetConstructor, linkedListConstructor);
        doTestSubtract(10001, 10000, linkedListConstructor, hashSetConstructor);
    }

    /**
     * Test {@link CollectionUtilities#subtract(Collection, Collection)} with
     * the first collection containing one object.
     */
    @Test
    public void testSubtractOne()
    {
        Constructor<?> arrayListConstructor = getArrayListConstructor();
        doTestSubtract(50000, 1, arrayListConstructor, arrayListConstructor);

        Constructor<?> hashSetConstructor = getHashSetConstructor();
        doTestSubtract(100000, 1, hashSetConstructor, hashSetConstructor);

        Constructor<?> linkedListConstructor = getLinkedListConstructor();
        doTestSubtract(100000, 1, linkedListConstructor, linkedListConstructor);

        doTestSubtract(100000, 1, arrayListConstructor, hashSetConstructor);
        doTestSubtract(50000, 1, hashSetConstructor, arrayListConstructor);
        doTestSubtract(100000, 1, arrayListConstructor, linkedListConstructor);
        doTestSubtract(50000, 1, linkedListConstructor, arrayListConstructor);
        doTestSubtract(100000, 1, hashSetConstructor, linkedListConstructor);
        doTestSubtract(100000, 1, linkedListConstructor, hashSetConstructor);
    }

    /**
     * Test {@link CollectionUtilities#subtract(Collection, Collection)} with
     * the second collection containing two objects.
     */
    @Test
    public void testSubtractTwo()
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            Constructor<?> arrayListConstructor = getArrayListConstructor();
            doTestSubtract(1000000, 2, arrayListConstructor, arrayListConstructor);

            Constructor<?> hashSetConstructor = getHashSetConstructor();
            doTestSubtract(100000, 2, hashSetConstructor, hashSetConstructor);

            Constructor<?> linkedListConstructor = getLinkedListConstructor();
            doTestSubtract(1000000, 2, linkedListConstructor, linkedListConstructor);

            doTestSubtract(1000000, 2, arrayListConstructor, hashSetConstructor);
            doTestSubtract(100000, 2, hashSetConstructor, arrayListConstructor);
            doTestSubtract(1000000, 2, arrayListConstructor, linkedListConstructor);
            doTestSubtract(1000000, 2, linkedListConstructor, arrayListConstructor);
            doTestSubtract(100000, 2, hashSetConstructor, linkedListConstructor);
            doTestSubtract(1000000, 2, linkedListConstructor, hashSetConstructor);
        }
    }

    /**
     * Test for {@link CollectionUtilities#toIntArray(Collection)}.
     */
    @Test
    public void testToIntArray()
    {
        Random rand = new Random();
        int count = 100;
        Collection<Number> col = new ArrayList<>(count);
        for (int index = 0; index < count; ++index)
        {
            col.add(Long.valueOf(rand.nextLong()));
            col.add(Integer.valueOf(rand.nextInt()));
        }

        int[] result = CollectionUtilities.toIntArray(col);
        Assert.assertEquals(col.size(), result.length);

        Iterator<Number> iter = col.iterator();
        for (int index = 0; index < count; ++index)
        {
            Assert.assertEquals(iter.next().intValue(), result[index]);
        }
    }

    /**
     * Test for {@link CollectionUtilities#toLongArray(Collection)}.
     */
    @Test
    public void testToLongArray()
    {
        Random rand = new Random();
        int count = 100;
        Collection<Number> col = new ArrayList<>(count);
        for (int index = 0; index < count; ++index)
        {
            col.add(Long.valueOf(rand.nextLong()));
            col.add(Integer.valueOf(rand.nextInt()));
        }

        long[] result = CollectionUtilities.toLongArray(col);
        Assert.assertEquals(col.size(), result.length);

        Iterator<Number> iter = col.iterator();
        for (int index = 0; index < count; ++index)
        {
            Assert.assertEquals(iter.next().longValue(), result[index]);
        }
    }

    /**
     * Create a collection.
     *
     * @param constructor The constructor for the collection.
     * @param list Contents for the collection.
     * @return The new collection.
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> createCollection(Constructor<?> constructor, Collection<?> list)
    {
        try
        {
            return (Collection<Object>)constructor.newInstance(list);
        }
        catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            LOGGER.error(e, e);
            return null;
        }
    }

    /**
     * Create a collection of objects of a certain size.
     *
     * @param size The size of the output collection.
     * @return The output collection.
     */
    private List<Object> createList(int size)
    {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            Object obj = new Object();
            list.add(obj);
        }
        return list;
    }

    /**
     * Create an array of objects.
     *
     * @param size The size of the array.
     * @return The array.
     */
    private Object[] createObjectArray(int size)
    {
        Object[] arr = new Object[size];
        for (int index = 0; index < size;)
        {
            arr[index++] = new Object();
        }
        return arr;
    }

    /**
     * Helper method that creates a collection containing the given number of
     * objects selected from the input collection.
     *
     * @param <T> The type of objects in the list.
     *
     * @param list The input list.
     * @param size The number of objects to put in the sub list.
     * @return The sub list.
     */
    private <T> List<T> createSubList(List<T> list, int size)
    {
        List<T> subList = new ArrayList<>(size);
        if (size == 1)
        {
            subList.add(list.get(list.size() - 1));
        }
        else if (size > 0)
        {
            int skip = list.size() / size;

            for (int i = 0; subList.size() < size; i += skip)
            {
                subList.add(list.get(i));
            }
        }
        return subList;
    }

    /**
     * Helper method that creates the collections with the input sizes and
     * removes the elements in one collection from the other using both
     * {@link CollectionUtilities} and the Java API. It times them both and
     * checks that the {@link CollectionUtilities} version is faster.
     *
     * @param size1 The size for the first collection.
     * @param size2 The size for the second collection.
     * @param constructor1 The constructor for the first collection.
     * @param constructor2 The constructor for the second collection.
     */
    private void doTestRemoveAll(int size1, int size2, Constructor<?> constructor1, Constructor<?> constructor2)
    {
        List<Object> list1 = createList(size1);
        List<Object> list2;
        if (size2 > size1)
        {
            list2 = new ArrayList<>(size2);
            list2.addAll(list1);
            while (list2.size() < size2)
            {
                list2.add(new Object());
            }
            Collections.shuffle(list2);
        }
        else
        {
            list2 = createSubList(list1, size2);
        }

        Collection<Object> col1 = createCollection(constructor1, list1);
        Collection<Object> col2 = createCollection(constructor2, list2);

        final int iterations = 5;
        long time1 = 0L;
        long time2 = 0L;
        for (int i = 0; i < iterations; i++)
        {
            Collection<Object> copy1 = createCollection(constructor1, col1);
            Collection<Object> copy2 = createCollection(constructor2, col2);
            time1 -= System.nanoTime();
            CollectionUtilities.removeAll(copy1, copy2);
            time1 += System.nanoTime();
            Assert.assertEquals(size2 > size1 ? 0 : size1 - size2, copy1.size());

            Collection<Object> copy3 = createCollection(constructor1, col1);
            time2 -= System.nanoTime();
            copy3.removeAll(copy2);
            time2 += System.nanoTime();

            Assert.assertEquals(copy3.size(), copy1.size());
            Assert.assertEquals(col2, copy2);
        }

        // Allow it to be up to 10 ms slower.
        long buffer = (long)1e7 * iterations;
        Assert.assertTrue(
                "Time using " + CollectionUtilities.class.getSimpleName() + " (" + time1 / 1e9 / iterations
                        + ") should have been less than time using JDK collections (" + time2 / 1e9 / iterations + ")",
                time1 < time2 + buffer);
    }

    /**
     * Helper method that creates the collections with the input sizes and
     * subtracts them using both {@link CollectionUtilities} and the Java API.
     * It times them both and checks that the {@link CollectionUtilities}
     * version is faster.
     *
     * @param size1 The size for the first collection.
     * @param size2 The size for the second collection.
     * @param constructor1 The constructor for the first collection.
     * @param constructor2 The constructor for the second collection.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void doTestSubtract(int size1, int size2, Constructor<?> constructor1, Constructor<?> constructor2)
    {
        List<Object> list1 = createList(size1);
        List<Object> list2 = createSubList(list1, size2);

        Collection<Object> col1 = createCollection(constructor1, list1);
        Collection<Object> col2 = createCollection(constructor2, list2);

        final int iterations = 4;
        long time1 = 0L;
        long time2 = 0L;
        for (int i = 0; i < iterations; i++)
        {
            Collection<Object> copy1;
            Collection<Object> copy2;
            copy1 = createCollection(constructor1, col1);
            copy2 = createCollection(constructor2, col2);
            time1 -= System.nanoTime();
            CollectionUtilities.subtract(copy1, copy2);
            time1 += System.nanoTime();
            Assert.assertEquals(Math.abs(col1.size() - col2.size()), col1.size() > col2.size() ? copy1.size() : copy2.size());
            Assert.assertEquals(0, col1.size() > col2.size() ? copy2.size() : copy1.size());
            Collection list = new LinkedList<>(copy1);
            list.retainAll(col2 instanceof Set ? col2 : new HashSet(col2));
            Assert.assertTrue(list.isEmpty());

            Collection<Object> copy3 = createCollection(constructor1, col1);
            Collection<Object> copy4 = createCollection(constructor2, col2);
            time2 -= System.nanoTime();
            copy3.removeAll(copy4);
            // Allow this to cheat a little bit to speed up the test. This
            // assumes that copy3 contains everything in copy4, but the method
            // under test does not make that assumption.
            for (Iterator<Object> iter4 = copy4.iterator(); iter4.hasNext();)
            {
                iter4.next();
                iter4.remove();
            }
            time2 += System.nanoTime();

            Assert.assertEquals(copy3.size(), copy1.size());
            Assert.assertEquals(copy4.size(), copy2.size());
        }

        // Allow it to be up to 50 ms slower.
        long buffer = (long)5e7 * iterations;
        Assert.assertTrue(
                "Time using " + CollectionUtilities.class.getSimpleName() + " (" + time1 / 1e9 / iterations
                        + ") should have been less than time using JDK collections (" + time2 / 1e9 / iterations + ")",
                time1 < time2 + buffer);
    }

    /**
     * Get a constructor for an array list.
     *
     * @return The array list constructor.
     */
    @SuppressWarnings({ "PMD.LooseCoupling", "rawtypes" })
    private Constructor<ArrayList> getArrayListConstructor()
    {
        try
        {
            return ArrayList.class.getConstructor(Collection.class);
        }
        catch (SecurityException | NoSuchMethodException e)
        {
            LOGGER.error(e, e);
            return null;
        }
    }

    /**
     * Get a constructor for a hash set.
     *
     * @return The hash set constructor.
     */
    @SuppressWarnings({ "PMD.LooseCoupling", "rawtypes" })
    private Constructor<HashSet> getHashSetConstructor()
    {
        try
        {
            return HashSet.class.getConstructor(Collection.class);
        }
        catch (SecurityException | NoSuchMethodException e)
        {
            LOGGER.error(e, e);
            return null;
        }
    }

    /**
     * Get a constructor for a linked list.
     *
     * @return The linked list constructor.
     */
    @SuppressWarnings({ "PMD.LooseCoupling", "rawtypes" })
    private Constructor<LinkedList> getLinkedListConstructor()
    {
        try
        {
            return LinkedList.class.getConstructor(Collection.class);
        }
        catch (SecurityException | NoSuchMethodException e)
        {
            LOGGER.error(e, e);
            return null;
        }
    }
}
