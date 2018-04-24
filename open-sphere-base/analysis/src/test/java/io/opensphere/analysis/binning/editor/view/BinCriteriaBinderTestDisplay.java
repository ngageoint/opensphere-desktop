package io.opensphere.analysis.binning.editor.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.core.util.collections.New;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * Unit test for the {@link BinCriteriaBinder}.
 */
public class BinCriteriaBinderTestDisplay
{
    /** Initializes the JavaFX platform. */
    @Before
    public void initialize()
    {
        try
        {
            Platform.startup(() ->
            {
            });
        }
        catch (IllegalStateException e)
        {
            // Platform already started; ignore
        }
    }

    /**
     * Tests adding a bin criteria.
     */
    @Test
    public void testAdd()
    {
        EasyMockSupport support = new EasyMockSupport();

        ListView<BinCriteriaElement> listView = new ListView<>();
        BinCriteriaView view = createView(support, listView);

        support.replayAll();

        BinCriteriaModel model = new BinCriteriaModel("datatypeId", New.list(), new BinCriteria());
        BinCriteriaBinder binder = new BinCriteriaBinder(view, model);

        view.getAddButton().fire();

        assertEquals(1, model.getCriteria().getCriterias().size());
        assertNotNull(model.getCriteria().getCriterias().get(0));
        assertEquals(1, listView.getItems().size());
        assertEquals(model.getCriteria().getCriterias().get(0), listView.getItems().get(0));

        binder.close();

        view.getAddButton().fire();

        support.verifyAll();
    }

    /**
     * Tests adding and deleting criterias.
     */
    @Test
    public void testChanges()
    {
        EasyMockSupport support = new EasyMockSupport();

        ListView<BinCriteriaElement> listView = new ListView<>();
        BinCriteriaView view = createView(support, listView);

        support.replayAll();

        BinCriteriaModel model = new BinCriteriaModel("datatypeId", New.list(), new BinCriteria());
        BinCriteriaBinder binder = new BinCriteriaBinder(view, model);

        BinCriteriaElement element = new BinCriteriaElement();
        listView.getItems().add(element);

        assertEquals(element, model.getCriteria().getCriterias().get(0));

        listView.getItems().remove(element);

        assertTrue(model.getCriteria().getCriterias().isEmpty());

        binder.close();

        listView.getItems().add(element);
        assertTrue(model.getCriteria().getCriterias().isEmpty());

        support.verifyAll();
    }

    /**
     * Verifies that is shows already existing criterias at start up.
     */
    @Test
    public void testInitialValues()
    {
        EasyMockSupport support = new EasyMockSupport();

        ListView<BinCriteriaElement> listView = new ListView<>();
        BinCriteriaView view = createView(support, listView);

        support.replayAll();

        BinCriteria criteria = new BinCriteria();
        BinCriteriaElement element = new BinCriteriaElement();
        criteria.getCriterias().add(element);
        BinCriteriaModel model = new BinCriteriaModel("datatypeId", New.list(), criteria);
        BinCriteriaBinder binder = new BinCriteriaBinder(view, model);

        assertEquals(element, model.getCriteria().getCriterias().get(0));

        binder.close();

        listView.getItems().add(element);
        assertEquals(1, model.getCriteria().getCriterias().size());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked view.
     *
     * @param support Used to create the view.
     * @param listView The list view showing the criterias.
     * @return The mocked bin criteria view.
     */
    private BinCriteriaView createView(EasyMockSupport support, ListView<BinCriteriaElement> listView)
    {
        BinCriteriaView view = support.createMock(BinCriteriaView.class);
        Button addButton = new Button();

        EasyMock.expect(view.getCriterionView()).andReturn(listView).atLeastOnce();
        EasyMock.expect(view.getAddButton()).andReturn(addButton).atLeastOnce();

        return view;
    }
}
