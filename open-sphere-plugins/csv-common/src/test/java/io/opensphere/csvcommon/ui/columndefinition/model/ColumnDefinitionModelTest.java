package io.opensphere.csvcommon.ui.columndefinition.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Observer;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;

/**
 * Tests the ColumnDefinitionModel class.
 *
 */
public class ColumnDefinitionModelTest
{
    /**
     * Tests setting the list of available data types.
     */
    @Test
    public void testSetAvailableDataTypes()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionModel.AVAILABLE_DATA_TYPES_PROPERTY);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.addObserver(observer);

        List<String> availableTypes = New.list();
        model.setAvailableDataTypes(availableTypes);

        assertEquals(availableTypes, model.getAvailableDataTypes());

        support.verifyAll();
    }

    /**
     * Tests setting the list of available formats.
     */
    @Test
    public void testSetAvailableFormats()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionModel.AVAILABLE_FORMATS_PROPERTY);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.addObserver(observer);

        List<String> availableFormats = New.list();
        model.setAvailableFormats(availableFormats);

        assertEquals(availableFormats, model.getAvailableFormats());

        support.verifyAll();
    }

    /**
     * Tests setting the error message.
     */
    @Test
    public void testSetErrorMessage()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionModel.ERROR_MESSAGE_PROPERTY);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.addObserver(observer);

        String errorMessage = "error message";
        model.setErrorMessage(errorMessage);

        assertEquals(errorMessage, model.getErrorMessage());

        support.verifyAll();
    }

    /**
     * Tests setting the selected definition.
     */
    @Test
    public void testSetSelectedDefinition()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.addObserver(observer);

        ColumnDefinitionRow definition = new ColumnDefinitionRow();
        model.setSelectedDefinition(definition);

        assertEquals(definition, model.getSelectedDefinition());

        support.verifyAll();
    }

    /**
     * Tests setting the warning message.
     */
    @Test
    public void testSetWarningMessage()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionModel.WARNING_MESSAGE_PROPERTY);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.addObserver(observer);

        String warningMessage = "warning";
        model.setWarningMessage(warningMessage);

        assertEquals(warningMessage, model.getWarningMessage());

        support.verifyAll();
    }

    /**
     * Tests setting the can add formats.
     */
    @Test
    public void testSetCanAddFormats()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionModel.CAN_ADD_FORMATS_PROPERTY);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.addObserver(observer);

        model.setCanAddFormats(true);

        assertTrue(model.canAddFormats());

        support.verifyAll();
    }
}
