package io.opensphere.filterbuilder.state;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.bitsys.fade.mist.state.v4.QueryEntryType;

import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/** Test for {@link FilterManagerStateController}. */
public class FilterManagerStateControllerTest
{
    /**
     * Test for
     * {@link FilterManagerStateController#canActivateState(org.w3c.dom.Node)}.
     *
     * @throws ParserConfigurationException Configuration error.
     */
    @Test
    public void testCanActivateState() throws ParserConfigurationException
    {
        Document doc = XMLUtilities.newDocument();
        FilterManagerStateController controller = new FilterManagerStateController(null, null, null, null, null);
        Assert.assertFalse(controller.canActivateState(doc));
        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(doc.createElementNS(ModuleStateController.STATE_NAMESPACE, "filters"));
        Assert.assertTrue(controller.canActivateState(doc));
    }

    /**
     * Test for
     * {@link FilterManagerStateController#combineQueryEntries(java.util.List)}.
     */
    @Test
    public void testCombineQueryEntries()
    {
        List<QueryEntryType> entries = New.list();
        entries.add(newQueryEntry("l1", null, "f1"));
        entries.add(newQueryEntry("l1", "a1", null));
        entries.add(newQueryEntry("l1", "a1", "f2"));
        entries.add(newQueryEntry("l1", null, null));
        entries.add(newQueryEntry("l3", "a3", "f3"));
        FilterManagerStateController.combineQueryEntries(entries);
        Assert.assertEquals(3, entries.size());
        Assert.assertEquals(null, entries.get(0).getAreaId());
        Assert.assertEquals("f1", entries.get(0).getFilterId());
        Assert.assertEquals("a1", entries.get(1).getAreaId());
        Assert.assertEquals("f2", entries.get(1).getFilterId());
        Assert.assertEquals("a3", entries.get(2).getAreaId());
        Assert.assertEquals("f3", entries.get(2).getFilterId());
    }

    /**
     * Creates a new query entry.
     *
     * @param layer the layer ID
     * @param area the area ID
     * @param filter the filter ID
     * @return the query entry
     */
    private static QueryEntryType newQueryEntry(String layer, String area, String filter)
    {
        QueryEntryType entry = new QueryEntryType();
        entry.setLayerId(layer);
        entry.setAreaId(area);
        entry.setFilterId(filter);
        return entry;
    }
}
