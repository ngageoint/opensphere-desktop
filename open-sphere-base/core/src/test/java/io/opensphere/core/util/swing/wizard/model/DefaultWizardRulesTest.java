package io.opensphere.core.util.swing.wizard.model;

import java.util.EnumSet;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;

/** Test {@link DefaultWizardRules}. */
public class DefaultWizardRulesTest
{
    /** Test {@link DefaultWizardRules#canFinish()}. */
    @Test
    public void testCanFinish()
    {
        WizardStepListModel stepModel = EasyMock.createMock(WizardStepListModel.class);
        EasyMock.expect(Boolean.valueOf(stepModel.allStepsAreValid())).andReturn(Boolean.TRUE);
        EasyMock.replay(stepModel);
        Assert.assertTrue(new DefaultWizardRules(stepModel).canFinish());

        EasyMock.reset(stepModel);
        EasyMock.expect(Boolean.valueOf(stepModel.allStepsAreValid())).andReturn(Boolean.FALSE);
        EasyMock.replay(stepModel);
        Assert.assertFalse(new DefaultWizardRules(stepModel).canFinish());
    }

    /** Test {@link DefaultWizardRules#canGoNext()}. */
    @Test
    public void testCanGoNext()
    {
        // Last step.
        WizardStepListModel stepModel = EasyMock.createMock(WizardStepListModel.class);
        EasyMock.expect(Integer.valueOf(stepModel.getCurrentStep())).andReturn(Integer.valueOf(1));
        EasyMock.expect(Integer.valueOf(stepModel.getStepCount())).andReturn(Integer.valueOf(2));
        EasyMock.replay(stepModel);
        Assert.assertFalse(new DefaultWizardRules(stepModel).canGoNext());

        for (StepState state : EnumSet.complementOf(EnumSet.of(StepState.DISABLED)))
        {
            EasyMock.reset(stepModel);
            EasyMock.expect(Integer.valueOf(stepModel.getCurrentStep())).andReturn(Integer.valueOf(0)).anyTimes();
            EasyMock.expect(Integer.valueOf(stepModel.getStepCount())).andReturn(Integer.valueOf(2)).anyTimes();
            EasyMock.expect(stepModel.getStepState(1)).andReturn(state);
            EasyMock.replay(stepModel);
            Assert.assertTrue(new DefaultWizardRules(stepModel).canGoNext());
        }

        EasyMock.reset(stepModel);
        EasyMock.expect(Integer.valueOf(stepModel.getCurrentStep())).andReturn(Integer.valueOf(0)).anyTimes();
        EasyMock.expect(Integer.valueOf(stepModel.getStepCount())).andReturn(Integer.valueOf(2)).anyTimes();
        EasyMock.expect(stepModel.getStepState(1)).andReturn(StepState.DISABLED);
        EasyMock.replay(stepModel);
        Assert.assertFalse(new DefaultWizardRules(stepModel).canGoNext());
    }

    /** Test {@link DefaultWizardRules#canGoPrevious()}. */
    @Test
    public void testCanGoPrevious()
    {
        // First step.
        WizardStepListModel stepModel = EasyMock.createMock(WizardStepListModel.class);
        EasyMock.expect(Integer.valueOf(stepModel.getCurrentStep())).andReturn(Integer.valueOf(0));
        EasyMock.expect(Integer.valueOf(stepModel.getStepCount())).andReturn(Integer.valueOf(2));
        EasyMock.replay(stepModel);
        Assert.assertFalse(new DefaultWizardRules(stepModel).canGoPrevious());

        for (StepState state : EnumSet.complementOf(EnumSet.of(StepState.DISABLED)))
        {
            EasyMock.reset(stepModel);
            EasyMock.expect(Integer.valueOf(stepModel.getCurrentStep())).andReturn(Integer.valueOf(1)).anyTimes();
            EasyMock.expect(Integer.valueOf(stepModel.getStepCount())).andReturn(Integer.valueOf(2)).anyTimes();
            EasyMock.expect(stepModel.getStepState(0)).andReturn(state);
            EasyMock.replay(stepModel);
            Assert.assertTrue(new DefaultWizardRules(stepModel).canGoPrevious());
        }

        EasyMock.reset(stepModel);
        EasyMock.expect(Integer.valueOf(stepModel.getCurrentStep())).andReturn(Integer.valueOf(1)).anyTimes();
        EasyMock.expect(Integer.valueOf(stepModel.getStepCount())).andReturn(Integer.valueOf(2)).anyTimes();
        EasyMock.expect(stepModel.getStepState(0)).andReturn(StepState.DISABLED);
        EasyMock.replay(stepModel);
        Assert.assertFalse(new DefaultWizardRules(stepModel).canGoPrevious());
    }
}
