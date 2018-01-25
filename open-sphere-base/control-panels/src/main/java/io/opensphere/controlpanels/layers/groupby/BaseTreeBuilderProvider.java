package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.lang.StringUtils;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.impl.GroupByTreeBuilder;

/**
 * An abstract tree builder provider class that contains common logic used by
 * other tree providers.
 *
 * @param <T> The type of the tree builder.
 */
public abstract class BaseTreeBuilderProvider<T extends GroupByTreeBuilder>
{
    /**
     * Gets the tree builder given the specified type.
     *
     * @param groupByType The group by type the user has selected.
     * @return The tree builder for the specified group by type.
     */
    public T getBuilder(String groupByType)
    {
        T builder = null;

        String wantedBuilderType = groupByType.split("-")[0].split("_")[0].split(" ")[0];
        List<T> builders = loadTreeBuilders();
        if (!builders.isEmpty())
        {
            builder = builders.get(0);
            for (T aBuilder : builders)
            {
                String groupByName = aBuilder.getGroupByName().split("-")[0].split("_")[0].split(" ")[0];
                if (StringUtils.equalsIgnoreCase(groupByName, wantedBuilderType))
                {
                    builder = aBuilder;
                    break;
                }
            }
        }

        return builder;
    }

    /**
     * Gets the available group by types the user can select from.
     *
     * @return The group by types.
     */
    public List<String> getGroupByTypes()
    {
        List<String> groupBys = New.list();

        for (T builder : loadTreeBuilders())
        {
            groupBys.add(builder.getGroupByName());
        }

        Collections.sort(groupBys);
        return groupBys;
    }

    /**
     * The type of tree builder this provider provides.
     *
     * @return The tree builder type.
     */
    protected abstract Class<T> getTreeBuilderType();

    /**
     * Loads the different tree builders currently installed.
     *
     * @return The list of tree builders.
     */
    private List<T> loadTreeBuilders()
    {
        Iterator<T> buildersIterator = ServiceLoader.load(getTreeBuilderType()).iterator();

        List<T> builders = New.list();
        while (buildersIterator.hasNext())
        {
            builders.add(buildersIterator.next());
        }

        return builders;
    }
}
