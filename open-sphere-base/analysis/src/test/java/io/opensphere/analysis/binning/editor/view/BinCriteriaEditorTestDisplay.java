package io.opensphere.analysis.binning.editor.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * Unit test for {@link BinCriteriaEditor}.
 */
public class BinCriteriaEditorTestDisplay
{
    /**
     * The double column.
     */
    private static final String ourDoubleColumn = "column1";

    /**
     * The test layer id.
     */
    private static final String ourDtiKey = "iamkey";

    /**
     * The expected range bin type text.
     */
    private static final String ourRangeText = "Range";

    /**
     * The string column.
     */
    private static final String ourStringColumn = "acolumn";

    /**
     * The expected unique bin type text.
     */
    private static final String ourUniqueText = "Unique";

    /**
     * Tests when the user creates a new bin criteria.
     */
    @Test
    public void testCreate()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);

        support.replayAll();

        BinCriteriaEditor editor = new BinCriteriaEditor(typeController, ourDtiKey);

        ListView<BinCriteriaElement> criteriaView = editor.getCriterionView();

        editor.getAddButton().fire();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<CriterionCell> flow = (VirtualFlow<CriterionCell>)criteriaView.getChildrenUnmodifiable().get(0);

        assertEquals(2, flow.getCellCount());

        CriterionCell cell = flow.getCell(1);

        cell.getFieldBox().setValue(ourStringColumn);

        BinCriteria criteria = editor.getCriteria();

        assertEquals(ourDtiKey, criteria.getDataTypeKey());
        assertEquals(2, criteria.getCriterias().size());

        BinCriteriaElement element = criteria.getCriterias().get(0);
        assertEquals(ourStringColumn, element.getField());
        assertEquals(ourUniqueText, element.getCriteriaType().getCriteriaType());
    }

    /**
     * Tests when the user deletes a bin criteria element.
     */
    @Test
    public void testDelete()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);
        BinCriteria criteria = createExistingUniqueAndRange();

        support.replayAll();

        BinCriteriaEditor editor = new BinCriteriaEditor(typeController, ourDtiKey, criteria);

        ListView<BinCriteriaElement> criteriaView = editor.getCriterionView();

        JFXPanel panel = new JFXPanel();
        Scene scene = new Scene(editor);
        panel.setScene(FXUtilities.addDesktopStyle(scene));
        @SuppressWarnings("unchecked")
        VirtualFlow<CriterionCell> flow = (VirtualFlow<CriterionCell>)criteriaView.getChildrenUnmodifiable().get(0);

        assertEquals(2, flow.getCellCount());

        CriterionCell cell = flow.getCell(0);
        assertEquals(ourStringColumn, cell.getFieldBox().getValue());
        assertEquals(New.list(ourStringColumn, ourDoubleColumn), cell.getFieldBox().getItems());

        assertEquals(ourUniqueText, cell.getBinTypeBox().getValue());
        assertEquals(New.list(ourUniqueText), cell.getBinTypeBox().getItems());
        assertFalse(cell.getTolerance().isVisible());

        cell = flow.getCell(1);
        assertEquals(ourDoubleColumn, cell.getFieldBox().getValue());
        assertEquals(New.list(ourStringColumn, ourDoubleColumn), cell.getFieldBox().getItems());

        assertEquals(ourRangeText, cell.getBinTypeBox().getValue());
        assertEquals(New.list(ourUniqueText, ourRangeText), cell.getBinTypeBox().getItems());
        assertEquals("20.2", cell.getTolerance().getText());
        assertTrue(cell.getTolerance().isVisible());

        cell.getRemoveButton().fire();

        FXUtilities.addDesktopStyle(scene);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        assertEquals(1, flow.getCellCount());

        support.verifyAll();
    }

    /**
     * Tests when the user just wants to read the bin criteria.
     */
    @Test
    public void testRead()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);
        BinCriteria criteria = createExistingUniqueAndRange();

        support.replayAll();

        BinCriteriaEditor editor = new BinCriteriaEditor(typeController, ourDtiKey, criteria);

        ListView<BinCriteriaElement> criteriaView = editor.getCriterionView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));
        @SuppressWarnings("unchecked")
        VirtualFlow<CriterionCell> flow = (VirtualFlow<CriterionCell>)criteriaView.getChildrenUnmodifiable().get(0);

        assertEquals(2, flow.getCellCount());

        CriterionCell cell = flow.getCell(0);
        assertEquals(ourStringColumn, cell.getFieldBox().getValue());
        assertEquals(New.list(ourStringColumn, ourDoubleColumn), cell.getFieldBox().getItems());

        assertEquals(ourUniqueText, cell.getBinTypeBox().getValue());
        assertEquals(New.list(ourUniqueText), cell.getBinTypeBox().getItems());
        assertFalse(cell.getTolerance().isVisible());

        cell = flow.getCell(1);
        assertEquals(ourDoubleColumn, cell.getFieldBox().getValue());
        assertEquals(New.list(ourStringColumn, ourDoubleColumn), cell.getFieldBox().getItems());

        assertEquals(ourRangeText, cell.getBinTypeBox().getValue());
        assertEquals(New.list(ourUniqueText, ourRangeText), cell.getBinTypeBox().getItems());
        assertEquals("20.2", cell.getTolerance().getText());
        assertTrue(cell.getTolerance().isVisible());

        support.verifyAll();
    }

    /**
     * Tests when the user updates a bin criteria.
     */
    @Test
    public void testUpdate()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);
        BinCriteria criteria = createExistingUniqueAndRange();

        support.replayAll();

        BinCriteriaEditor editor = new BinCriteriaEditor(typeController, ourDtiKey, criteria);

        ListView<BinCriteriaElement> criteriaView = editor.getCriterionView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));
        @SuppressWarnings("unchecked")
        VirtualFlow<CriterionCell> flow = (VirtualFlow<CriterionCell>)criteriaView.getChildrenUnmodifiable().get(0);

        assertEquals(2, flow.getCellCount());

        CriterionCell cell = flow.getCell(0);
        assertEquals(ourStringColumn, cell.getFieldBox().getValue());
        assertEquals(New.list(ourStringColumn, ourDoubleColumn), cell.getFieldBox().getItems());

        assertEquals(ourUniqueText, cell.getBinTypeBox().getValue());
        assertEquals(New.list(ourUniqueText), cell.getBinTypeBox().getItems());
        assertFalse(cell.getTolerance().isVisible());

        cell = flow.getCell(1);
        assertEquals(ourDoubleColumn, cell.getFieldBox().getValue());
        assertEquals(New.list(ourStringColumn, ourDoubleColumn), cell.getFieldBox().getItems());

        assertEquals(ourRangeText, cell.getBinTypeBox().getValue());
        assertEquals(New.list(ourUniqueText, ourRangeText), cell.getBinTypeBox().getItems());
        assertEquals("20.2", cell.getTolerance().getText());
        assertTrue(cell.getTolerance().isVisible());

        cell.getBinTypeBox().setValue(ourUniqueText);
        assertFalse(cell.getTolerance().isVisible());

        assertEquals(ourUniqueText, criteria.getCriterias().get(1).getCriteriaType().getCriteriaType());
        assertEquals(ourDoubleColumn, criteria.getCriterias().get(1).getField());

        support.verifyAll();
    }

    /**
     * Creates an existing unique {@link BinCriteria}.
     *
     * @return The bin criteria.
     */
    private BinCriteria createExistingUnique()
    {
        BinCriteria criteria = new BinCriteria();
        criteria.setDataTypeKey(ourDtiKey);

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourStringColumn);
        element.setCriteriaType(new UniqueCriteria());

        criteria.getCriterias().add(element);

        return criteria;
    }

    /**
     * Creates an existing unique and range {@link BinCriteria}.
     *
     * @return The bin criteria.
     */
    private BinCriteria createExistingUniqueAndRange()
    {
        BinCriteria criteria = createExistingUnique();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourDoubleColumn);
        RangeCriteria criteriaType = new RangeCriteria();
        criteriaType.setBinWidth(20.20);
        element.setCriteriaType(criteriaType);

        criteria.getCriterias().add(element);

        return criteria;
    }

    /**
     * Creates a mock data type controller.
     *
     * @param support Used to create the mock.
     * @return The mock data type controller.
     */
    private DataGroupController createTypeController(EasyMockSupport support)
    {
        Map<String, Class<?>> columnMap = New.map();
        columnMap.put(ourDoubleColumn, Double.class);
        columnMap.put(ourStringColumn, String.class);

        MetaDataInfo metadata = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadata.getKeyClassTypeMap()).andReturn(columnMap);

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metadata);

        DataGroupController groupController = support.createMock(DataGroupController.class);
        EasyMock.expect(groupController.findMemberById(EasyMock.cmpEq(ourDtiKey))).andReturn(dataType);

        return groupController;
    }
}
