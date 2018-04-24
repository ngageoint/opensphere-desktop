package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility.TreeOptions;
import io.opensphere.mantle.data.impl.GroupCategorizer;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByActiveZOrderTreeBuilder extends GroupByDefaultTreeBuilder
{
    @Override
    public String getGroupByName()
    {
        return "Z-Order";
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return new GroupCategorizer()
        {
            @Override
            public List<String> getAllCategories()
            {
                List<String> zOrderCats = New.list();
                addCategoriesFromFamily(zOrderCats, DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY);
                addCategoriesFromFamily(zOrderCats, DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY);
                addCategoriesFromFamily(zOrderCats, DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY);
                addCategoriesFromFamily(zOrderCats, DefaultOrderCategory.DEFAULT_MY_PLACES_LAYER_FAMILY);
                zOrderCats.retainAll(getCategories());
                return zOrderCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dgi)
            {
                return Collections.<String>emptySet();
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                if (dti == null || dti.getOrderKey() == null || dti.getMapVisualizationInfo() == null)
                {
                    return null;
                }

                String cat = dti.getOrderKey().getCategory().getCategoryId();
                boolean zOrderable = dti.getMapVisualizationInfo().isZOrderable();
                if (zOrderable)
                {
                    getCategories().add(cat);
                    return Collections.singleton(cat);
                }
                else
                {
                    return null;
                }
            }

            private void addCategoriesFromFamily(List<String> zOrderCats, String orderFamily)
            {
                Set<OrderCategory> ocSet = getToolbox().getOrderManagerRegistry().getCategoriesForFamily(orderFamily);
                if (CollectionUtilities.hasContent(ocSet))
                {
                    List<OrderCategory> ocList = New.list(ocSet);
                    Collections.sort(ocList, Collections.reverseOrder(OrderCategory.ourCompareByOrderRange));
                    for (OrderCategory cat : ocList)
                    {
                        zOrderCats.add(cat.getCategoryId());
                    }
                }
            }
        };
    }

    @Override
    public Comparator<? super DataGroupInfo> getGroupComparator()
    {
        return null;
    }

    @Override
    public TreeOptions getTreeOptions()
    {
        return new TreeOptions(false, true);
    }

    @Override
    public Comparator<? super DataTypeInfo> getTypeComparator()
    {
        return new Comparator<DataTypeInfo>()
        {
            @Override
            public int compare(DataTypeInfo o1, DataTypeInfo o2)
            {
                if (o1.getOrderKey() == null)
                {
                    return o2.getOrderKey() == null ? 0 : 1;
                }

                if (o2.getOrderKey() == null)
                {
                    return -1;
                }

                OrderManagerRegistry registry = getToolbox().getOrderManagerRegistry();
                int order1 = registry.getOrderManager(o1.getOrderKey()).getOrder(o1.getOrderKey());
                int order2 = registry.getOrderManager(o2.getOrderKey()).getOrder(o2.getOrderKey());

                return order1 < order2 ? 1 : order1 == order2 ? 0 : -1;
            }
        };
    }
}
