package io.opensphere.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * A test for JDepend metrics.
 */
public class JDependTest
{
    /** The default charset. */
    public static final Charset DEFAULT_CHARSET = Charset.forName(System.getProperty("opensphere.charset", "UTF-8"));

    /**
     * Test to see if there are any package cycles.
     */
    @Test
    public void testCycles()
    {
        try
        {
            final List<String> lines = runJdeps();
            final Map<String, Collection<String>> dependencyMap = processLines(lines);
            final Collection<List<String>> cycles = findCycles(dependencyMap);
            if (!cycles.isEmpty())
            {
                Assert.fail(formatMessage(cycles));
            }
        }
        catch (final Exception e)
        {
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            Assert.fail(e.getMessage() + ":\n" + out.toString());
        }
    }

    /**
     * Runs jdeps for the current package.
     *
     * @return the output lines
     * @throws IOException if a problem occurred running jdeps
     */
    private List<String> runJdeps() throws IOException
    {
        List<String> lines;
        final String classesDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent()
                + File.separator + "classes";
        File jdepsCommand = Path.of(System.getProperty("java.home"), "bin", "jdeps").toFile();
        if (!jdepsCommand.isFile() || !jdepsCommand.canExecute())
        {
            throw new IOException("Unable to find / execute JDeps Command (tried '" + jdepsCommand.getAbsolutePath() + "')");
        }
        final Process process = Runtime.getRuntime().exec(jdepsCommand.getAbsolutePath() + " " + classesDir);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), DEFAULT_CHARSET)))
        {
            lines = in.lines().collect(Collectors.toList());
        }
        return lines;
    }

    /**
     * Processes the output of jdeps.
     *
     * @param lines the output lines
     * @return the map of each package to its dependencies
     */
    private Map<String, Collection<String>> processLines(Collection<String> lines)
    {
        final Map<String, Collection<String>> dependencyMap = new HashMap<>();

        String[] splitPackage = getClass().getName().split("\\.");
        final Pattern packagePattern = Pattern.compile("^\\s+(" + splitPackage[0] + "\\." + splitPackage[1] + "\\..*?) ");
        final Pattern dependencyPattern = Pattern.compile("-> (" + splitPackage[0] + "\\." + splitPackage[1] + "\\..*?) ");

        String currentPackage = null;
        for (final String line : lines)
        {
            final Matcher packageMatcher = packagePattern.matcher(line);
            final Matcher dependencyMatcher = dependencyPattern.matcher(line);
            if (packageMatcher.find())
            {
                currentPackage = packageMatcher.group(1);
            }
            else if (currentPackage != null && dependencyMatcher.find())
            {
                final String dependency = dependencyMatcher.group(1);
                dependencyMap.computeIfAbsent(currentPackage, k -> new HashSet<>()).add(dependency);
            }
        }

        return dependencyMap;
    }

    /**
     * Finds cycles in the map.
     *
     * @param dependencyMap the map of each package to its dependencies
     * @return the cycles found
     */
    private Collection<List<String>> findCycles(Map<String, Collection<String>> dependencyMap)
    {
        final Collection<List<String>> cycles = new ArrayList<>();
        final Set<String> searchedPackages = new HashSet<>();
        for (final Map.Entry<String, Collection<String>> entry : new ArrayList<>(dependencyMap.entrySet()))
        {
            for (final String dependency : entry.getValue())
            {
                findCycles(cycles, searchedPackages, dependencyMap, new ArrayList<>(), dependency);
            }
        }

        final Collection<List<String>> reducedCycles = new HashSet<>();
        for (final List<String> cycle : cycles)
        {
            reducedCycles.add(subListStartingWith(cycle, cycle.get(cycle.size() - 1)));
        }

        final List<List<String>> sortedCycles = new ArrayList<>(reducedCycles);
        Collections.sort(sortedCycles, (l1, l2) -> l1.size() - l2.size());

        return sortedCycles;
    }

    /**
     * Finds cycles in the map.
     *
     * @param cycles the cycles found
     * @param searchedPackages the packages that have already been searched
     * @param dependencyMap the map of each package to its dependencies
     * @param path the path to the current package
     * @param currentPackage the current package
     */
    private void findCycles(Collection<List<String>> cycles, Set<String> searchedPackages,
            Map<String, Collection<String>> dependencyMap, List<String> path, String currentPackage)
    {
        if (path.contains(currentPackage))
        {
            final List<String> currentPackageList = Arrays.asList(currentPackage);
            cycles.add(concat(path, currentPackageList));
        }
        else if (!searchedPackages.contains(currentPackage))
        {
            searchedPackages.add(currentPackage);
            for (final String dependency : dependencyMap.computeIfAbsent(currentPackage, k -> new HashSet<>()))
            {
                final List<String> currentPackageList = Arrays.asList(currentPackage);
                findCycles(cycles, searchedPackages, dependencyMap, concat(path, currentPackageList), dependency);
            }
        }
    }

    /**
     * Get a new collection that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param cols The collections to be added to the result.
     * @return The result collection.
     */
    @SafeVarargs
    public static <T> List<T> concat(Iterable<? extends T>... cols)
    {
        int size = 0;
        for (final Iterable<? extends T> col : cols)
        {
            size += col instanceof Collection ? ((Collection<?>)col).size() : 10;
        }

        final List<T> result = new ArrayList<>(size);
        for (final Iterable<? extends T> col : cols)
        {
            for (final T value : col)
            {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Formats the cycles into a message.
     *
     * @param cycles the cycles
     * @return the message
     */
    private String formatMessage(Collection<List<String>> cycles)
    {
        final StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("Found cycles:");
        for (final List<String> cycle : cycles)
        {
            joiner.add(cycle.toString());
        }
        return joiner.toString();
    }

    /**
     * Returns a sublist starting at the start element.
     *
     * @param list the list
     * @param start the starting element
     * @return the sub list
     */
    private static List<String> subListStartingWith(List<String> list, String start)
    {
        final int index = list.indexOf(start);
        return index == -1 ? list : list.subList(index, list.size());
    }
}
