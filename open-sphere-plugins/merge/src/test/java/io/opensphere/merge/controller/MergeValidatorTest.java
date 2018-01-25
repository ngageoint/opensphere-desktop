package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.merge.model.MergeModel;

/**
 * Unit test for {@link MergeValidator}.
 */
public class MergeValidatorTest
{
    /**
     * The test merge layer.
     */
    private static final String ourLayerName = "Merge Layer";

    /**
     * Tests the new layer name starting as empty then going valid.
     */
    @Test
    public void testChanged()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController groupController = createGroupController(support, null);

        support.replayAll();

        MergeModel model = new MergeModel(New.list());
        MergeValidator validator = new MergeValidator(groupController, model);

        assertEquals(ValidationStatus.ERROR, validator.getValidatorSupport().getValidationStatus());
        assertEquals("Please type in a new layer name.", validator.getValidatorSupport().getValidationMessage());

        model.getNewLayerName().set(ourLayerName);

        assertEquals(ValidationStatus.VALID, validator.getValidatorSupport().getValidationStatus());
        assertNull(validator.getValidatorSupport().getValidationMessage());

        support.verifyAll();
    }

    /**
     * Tests when it is changed to a duplicate layer.
     */
    @Test
    public void testChangedDuplicate()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);
        DataGroupController groupController = createGroupController(support, dataGroup);
        EasyMock.expect(groupController.getDataGroupInfo(EasyMock.cmpEq("New layer"))).andReturn(null);

        support.replayAll();

        MergeModel model = new MergeModel(New.list());
        model.getNewLayerName().set(ourLayerName);
        MergeValidator validator = new MergeValidator(groupController, model);

        assertEquals(ValidationStatus.ERROR, validator.getValidatorSupport().getValidationStatus());
        assertEquals("Layer name already exists.", validator.getValidatorSupport().getValidationMessage());

        model.getNewLayerName().set("New layer");

        assertEquals(ValidationStatus.VALID, validator.getValidatorSupport().getValidationStatus());
        assertNull(validator.getValidatorSupport().getValidationMessage());

        support.verifyAll();
    }

    /**
     * Tests when the layer name changes to empty.
     */
    @Test
    public void testChangedEmpty()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController groupController = createGroupController(support, null);

        support.replayAll();

        MergeModel model = new MergeModel(New.list());
        model.getNewLayerName().set(ourLayerName);
        MergeValidator validator = new MergeValidator(groupController, model);

        assertEquals(ValidationStatus.VALID, validator.getValidatorSupport().getValidationStatus());
        assertNull(validator.getValidatorSupport().getValidationMessage());

        model.getNewLayerName().set("");

        assertEquals(ValidationStatus.ERROR, validator.getValidatorSupport().getValidationStatus());
        assertEquals("Please type in a new layer name.", validator.getValidatorSupport().getValidationMessage());

        support.verifyAll();
    }

    /**
     * Create a group controller.
     *
     * @param support Used to create the mock.
     * @param toReturn The group to return.
     * @return The group controller.
     */
    private DataGroupController createGroupController(EasyMockSupport support, DataGroupInfo toReturn)
    {
        DataGroupController groupController = support.createMock(DataGroupController.class);

        EasyMock.expect(groupController.getDataGroupInfo(EasyMock.cmpEq(ourLayerName))).andReturn(toReturn);

        return groupController;
    }
}
