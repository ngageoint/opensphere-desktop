package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities that emulate Java 8 style stream operations.
 */
public final class StreamUtilities
{
    /**
     * Determines if any of the elements of the input match the given filter.
     *
     * @param <T> the type of the elements
     * @param input the input
     * @param filter the filter
     * @return if any match
     */
    public static <T> boolean anyMatch(Iterable<? extends T> input, Predicate<? super T> filter)
    {
        for (T in : input)
        {
            if (filter.test(in))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a collection consisting of the elements of the input that match
     * the given filter.
     *
     * @param <T> the type of the elements
     * @param input the input collection
     * @param predicate the predicate
     * @return the filtered collection
     */
    public static <T> List<T> filter(Collection<? extends T> input, Predicate<? super T> predicate)
    {
        List<T> list;
        if (predicate != null)
        {
            list = CollectionUtilities.hasContent(input) ? input.stream().filter(predicate).collect(Collectors.toList())
                    : New.list();
        }
        else
        {
            list = New.list(input);
        }
        return list;
    }

    /**
     * Returns a collection consisting of the elements of the input that match
     * the given filter.
     *
     * @param <T> the type of the elements
     * @param input the input collection
     * @param predicate the predicate
     * @return the filtered collection
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] filter(T[] input, Predicate<? super T> predicate)
    {
        // Use two-thirds as the starting size as this requires only one
        // increase in array list capacity.
        List<T> output = New.list(2 * input.length / 3);
        for (T in : input)
        {
            if (predicate.test(in))
            {
                output.add(in);
            }
        }
        return New.array(output, (Class<T>)input.getClass().getComponentType());
    }

    /**
     * Returns the first elements of the input that matches the given filter.
     *
     * @param <T> the type of the elements
     * @param input the input
     * @param predicate the predicate
     * @return the filtered object, or {@code null}
     */
    public static <T> T filterOne(Collection<? extends T> input, Predicate<? super T> predicate)
    {
        return predicate != null ? input.stream().filter(predicate).findFirst().orElse(null) : null;
    }

    /**
     * Filter a stream into a new stream that only contains objects that
     * are instances of the given type.
     *
     * @param <T> The type of the returned objects.
     * @param input The input stream.
     * @param type The desired result type.
     * @return The stream of objects of type T.
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<T> filterDowncast(Stream<?> input, Class<T> type)
    {
        return input.filter(obj -> type.isInstance(obj)).map(obj -> (T)obj);
    }

    /**
     * Returns a collection consisting of the results of applying the given
     * function to the elements of the input collection.
     *
     * @param <T> the element type of the input collection
     * @param <R> the element type of the output collection
     * @param input the input collection
     * @param mapper a non-interfering, stateless function to apply to each
     *            element
     * @param collectionProvider the provider for the output collection
     * @return the new collection
     */
    public static <T, R> Collection<R> map(Collection<? extends T> input, Function<? super T, ? extends R> mapper,
            CollectionProvider<R> collectionProvider)
    {
        return input.isEmpty() ? collectionProvider.get(0) : map((Iterable<? extends T>)input, mapper, collectionProvider);
    }

    /**
     * Returns a collection consisting of the results of applying the given
     * function to the elements of the input collection.
     *
     * @param <T> the element type of the input collection
     * @param <R> the element type of the output collection
     * @param input the input collection
     * @param mapper a non-interfering, stateless function to apply to each
     *            element
     * @return the new collection
     */
    @SuppressWarnings("unchecked")
    public static <T, R> List<R> map(Iterable<? extends T> input, Function<? super T, ? extends R> mapper)
    {
        return (List<R>)map(input, mapper, New.listFactory());
    }

    /**
     * Returns a collection consisting of the results of applying the given
     * function to the elements of the input collection.
     *
     * @param <T> the element type of the input collection
     * @param <R> the element type of the output collection
     * @param input the input collection
     * @param mapper a non-interfering, stateless function to apply to each
     *            element
     * @param collectionProvider the provider for the output collection
     * @return the new collection
     */
    private static <T, R> Collection<R> map(Iterable<? extends T> input, Function<? super T, ? extends R> mapper,
            CollectionProvider<R> collectionProvider)
    {
        Collection<R> output = collectionProvider.get(input instanceof Collection ? ((Collection<?>)input).size() : 10);
        for (T in : input)
        {
            output.add(mapper.apply(in));
        }
        return output;
    }

    /** Disallow construction. */
    private StreamUtilities()
    {
    }
}
