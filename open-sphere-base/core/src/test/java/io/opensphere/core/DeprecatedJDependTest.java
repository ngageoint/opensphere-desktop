package io.opensphere.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * A test for JDepend metrics.
 */
public class DeprecatedJDependTest
{
    /**
     * Test to see if there are any package cycles.
     */
    @Test
    @Ignore
    public void testCycles()
    {
        try
        {
            List<String> lines = runJdeps();
            Map<String, Collection<String>> dependencyMap = processLines(lines);
            Collection<List<String>> cycles = findCycles(dependencyMap);
            if (!cycles.isEmpty())
            {
                Assert.fail(formatMessage(cycles));
            }
        }
        catch (IOException e)
        {
            Assert.fail(e.toString());
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
        String classesDir = System.getProperty("classes.dir");
        Process process = Runtime.getRuntime().exec("jdeps " + classesDir);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StringUtilities.DEFAULT_CHARSET)))
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
        Map<String, Collection<String>> dependencyMap = LazyMap.create(New.<String, Collection<String>>map(), String.class,
                New.<String>setFactory());

        Pattern packagePattern = Pattern.compile("^\\s+(io\\.opensphere\\..*?) ");
        Pattern dependencyPattern = Pattern.compile("-> (io\\.opensphere\\..*?) ");

        String currentPackage = null;
        for (String line : lines)
        {
            Matcher packageMatcher = packagePattern.matcher(line);
            Matcher dependencyMatcher = dependencyPattern.matcher(line);
            if (packageMatcher.find())
            {
                currentPackage = packageMatcher.group(1);
            }
            else if (currentPackage != null && dependencyMatcher.find())
            {
                String dependency = dependencyMatcher.group(1);
                dependencyMap.get(currentPackage).add(dependency);
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
        Collection<List<String>> cycles = New.list();
        Set<String> searchedPackages = New.set();
        for (Map.Entry<String, Collection<String>> entry : New.list(dependencyMap.entrySet()))
        {
            for (String dependency : entry.getValue())
            {
                findCycles(cycles, searchedPackages, dependencyMap, New.list(), dependency);
            }
        }

        Collection<List<String>> reducedCycles = New.set();
        for (List<String> cycle : cycles)
        {
            reducedCycles.add(subListStartingWith(cycle, cycle.get(cycle.size() - 1)));
        }

        List<List<String>> sortedCycles = CollectionUtilities.sort(reducedCycles, (l1, l2) -> l1.size() - l2.size());
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
            cycles.add(CollectionUtilities.concatObjs(New.listFactory(), path, currentPackage));
        }
        else if (!searchedPackages.contains(currentPackage))
        {
            searchedPackages.add(currentPackage);

            for (String dependency : dependencyMap.get(currentPackage))
            {
                findCycles(cycles, searchedPackages, dependencyMap,
                        CollectionUtilities.concatObjs(New.listFactory(), path, currentPackage), dependency);
            }
        }
    }

    /**
     * Formats the cycles into a message.
     *
     * @param cycles the cycles
     * @return the message
     */
    private String formatMessage(Collection<List<String>> cycles)
    {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("Found cycles:");
        for (List<String> cycle : cycles)
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
        int index = list.indexOf(start);
        return index == -1 ? list : list.subList(index, list.size());
    }
}
