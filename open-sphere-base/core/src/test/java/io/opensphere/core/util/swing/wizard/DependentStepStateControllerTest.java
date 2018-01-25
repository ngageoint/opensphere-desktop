package io.opensphere.core.util.swing.wizard;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.swing.wizard.model.WizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;

/** Test for {@link DependentStepStateController}. */
public class DependentStepStateControllerTest
{
    /** Test for {@link DependentStepStateController}. */
    @Test
    public void test()
    {
        String targetTitle = "target";
        String stepTwo = "two";
        String stepOne = "one";
        List<String> others = Arrays.asList(stepOne, stepTwo);

        DependentStepStateController controller = new DependentStepStateController(targetTitle, others);

        // Test all valid

        WizardStepListModel model = EasyMock.createMock(WizardStepListModel.class);
        EasyMock.expect(model.getCurrentStepTitle()).andReturn(stepOne);
        EasyMock.expect(model.getStepState(stepOne)).andReturn(StepState.VALID);
        EasyMock.expect(model.getStepState(stepTwo)).andReturn(StepState.VALID);
        EasyMock.expect(model.getStepState(targetTitle)).andReturn(StepState.VALID);

        EasyMock.replay(model);

        controller.stepStateChanged(model, 0, null, null);

        EasyMock.verify(model);

        // Test with step one invalid

        EasyMock.reset(model);

        EasyMock.expect(model.getCurrentStepTitle()).andReturn(stepOne);
        EasyMock.expect(model.getStepState(stepOne)).andReturn(StepState.INVALID).atLeastOnce();
        model.setStepState(targetTitle, StepState.DISABLED);

        EasyMock.replay(model);

        controller.stepStateChanged(model, 0, null, null);

        EasyMock.verify(model);

        // Test with the target step as the current step

        EasyMock.reset(model);

        EasyMock.expect(model.getCurrentStepTitle()).andReturn(targetTitle);

        EasyMock.replay(model);

        controller.stepStateChanged(model, 0, null, null);

        EasyMock.verify(model);

        // Test step two invalid

        EasyMock.reset(model);

        EasyMock.expect(model.getCurrentStepTitle()).andReturn(stepOne);
        EasyMock.expect(model.getStepState(stepOne)).andReturn(StepState.VALID);
        EasyMock.expect(model.getStepState(stepTwo)).andReturn(StepState.INVALID).atLeastOnce();
        model.setStepState(targetTitle, StepState.DISABLED);

        EasyMock.replay(model);

        controller.stepStateChanged(model, 0, null, null);

        EasyMock.verify(model);

        // Test all valid except the target

        EasyMock.reset(model);

        EasyMock.expect(model.getCurrentStepTitle()).andReturn(stepOne);
        EasyMock.expect(model.getStepState(stepOne)).andReturn(StepState.VALID);
        EasyMock.expect(model.getStepState(stepTwo)).andReturn(StepState.VALID);
        EasyMock.expect(model.getStepState(targetTitle)).andReturn(StepState.DISABLED);
        model.setStepState(targetTitle, StepState.INDETERMINATE);

        EasyMock.replay(model);

        controller.stepStateChanged(model, 0, null, null);

        EasyMock.verify(model);

        // Test with a warning step
        EasyMock.reset(model);

        EasyMock.expect(model.getCurrentStepTitle()).andReturn(stepOne);
        EasyMock.expect(model.getStepState(stepOne)).andReturn(StepState.VALID);
        EasyMock.expect(model.getStepState(stepTwo)).andReturn(StepState.WARNING).atLeastOnce();
        EasyMock.expect(model.getStepState(targetTitle)).andReturn(StepState.VALID);

        EasyMock.replay(model);

        controller.stepStateChanged(model, 0, null, null);

        EasyMock.verify(model);
    }
}
