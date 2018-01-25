package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * Unit test for {@link SimpleFeatureActionController}.
 */
public class SimpleFeatureActionControllerTest
{
    /**
     * The available columns for test.
     */
    private static final List<String> ourColumns = New.list("COLUMN1", "COLUMN2", "COLUMN3");

    /**
     * The test layer id.
     */
    private static final String ourLayerId = "I am layer";

    /**
     * Tests populating the available columns.
     */
    @SuppressWarnings("unused")
    @Test
    public void testAvailableColumns()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeController typeController = createTypeController(support);

        support.replayAll();

        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simple = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simple);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        new SimpleFeatureActionController(typeController, actions, group, simple);

        assertEquals(ourColumns, simple.getAvailableColumns());

        support.verifyAll();
    }

    /**
     * Tests copying an action.
     */
    @Test
    public void testCopy()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeController typeController = createTypeController(support);

        support.replayAll();

        FeatureAction action = new FeatureAction();
        action.setName("Action 1");
        action.setEnabled(true);
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.CYAN);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        group.getActions().add(simpleAction);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria("USER NAME", Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionController controller = new SimpleFeatureActionController(typeController, actions, group,
                simpleAction);

        controller.copy();

        assertEquals(2, group.getActions().size());
        SimpleFeatureAction copied = group.getActions().get(1);
        assertEquals("Action 1 Copy", copied.getFeatureAction().getName());
        assertTrue(copied.getFeatureAction().isEnabled());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), copied.getColor());
        assertEquals(22, copied.getIconId());
        assertEquals("USER NAME", copied.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, copied.getOption().get());
        assertEquals("B*", copied.getValue().get());

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests removing an action.
     */
    @Test
    public void testRemove()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeController typeController = createTypeController(support);

        support.replayAll();

        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simple = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simple);
        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionController controller = new SimpleFeatureActionController(typeController, actions, group, simple);
        controller.remove();
        assertTrue(group.getActions().isEmpty());
        assertFalse(actions.getFeatureGroups().isEmpty());

        support.verifyAll();
    }

    /**
     * Tests removing an action.
     */
    @Test
    public void testRemoveMulti()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeController typeController = createTypeController(support);

        support.replayAll();

        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simple = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simple);

        FeatureAction action2 = new FeatureAction();
        SimpleFeatureAction simple2 = new SimpleFeatureAction(action2);
        SimpleFeatureActionGroup group2 = new SimpleFeatureActionGroup();
        group2.getActions().add(simple2);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);
        actions.getFeatureGroups().add(group2);

        SimpleFeatureActionController controller = new SimpleFeatureActionController(typeController, actions, group, simple);
        controller.remove();
        assertTrue(group.getActions().isEmpty());
        assertFalse(actions.getFeatureGroups().isEmpty());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @return The mocked controller.
     */
    private DataTypeController createTypeController(EasyMockSupport support)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        List<String> columnNames = New.list(ourColumns);
        Collections.reverse(columnNames);
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(columnNames);

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getMetaDataInfo()).andReturn(metadataInfo);

        DataTypeController controller = support.createMock(DataTypeController.class);
        EasyMock.expect(controller.getDataTypeInfoForType(ourLayerId)).andReturn(layer);

        return controller;
    }
}
