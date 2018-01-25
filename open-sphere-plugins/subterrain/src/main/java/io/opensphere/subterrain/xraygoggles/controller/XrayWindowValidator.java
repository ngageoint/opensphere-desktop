package io.opensphere.subterrain.xraygoggles.controller;

import io.opensphere.core.model.ScreenPosition;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;
import io.opensphere.subterrain.xraygoggles.model.XrayModelValidator;

/**
 * Validates new screen positions within the xray model. If they are invalid it
 * sets the values back to the previous ones.
 */
public class XrayWindowValidator implements XrayModelValidator
{
    /**
     * The model to validate.
     */
    private final XrayGogglesModel myModel;

    /**
     * Constructs a new validator.
     *
     * @param model The model to validate.
     */
    public XrayWindowValidator(XrayGogglesModel model)
    {
        myModel = model;
        myModel.setValidator(this);
    }

    /**
     * Stops listening for model changes.
     */
    public void close()
    {
        myModel.setValidator(null);
    }

    @Override
    public boolean isValid(ScreenPosition upperLeft, ScreenPosition upperRight, ScreenPosition lowerLeft,
            ScreenPosition lowerRight)
    {
        boolean isValid = true;

        if (upperLeft != null && (upperLeft.getX() <= 3 || upperLeft.getY() <= 0 || lowerLeft.getX() >= lowerRight.getX()
                || upperLeft.getX() > upperRight.getX() || !stillTrapezoid(upperLeft, upperRight, lowerLeft, lowerRight)))
        {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Checks to see if the xray window is still in a trapezoid shape.
     *
     * @param upperLeft The upper left screen position of the xray window.
     * @param upperRight The upper right screen position of the xray window.
     * @param lowerLeft The lower left screen position of the xray window.
     * @param lowerRight The lower right screen position of the xray window.
     * @return True if the xray window is still in a trapezoid shap, false
     *         otherwise.
     */
    private boolean stillTrapezoid(ScreenPosition upperLeft, ScreenPosition upperRight, ScreenPosition lowerLeft,
            ScreenPosition lowerRight)
    {
        boolean isTrapezoid = true;

        if (lowerLeft != null && upperLeft != null && upperRight != null && lowerRight != null)
        {
            int leftDelta = (int)(lowerLeft.getX() - upperLeft.getX());
            int rightDelta = (int)(upperRight.getX() - lowerRight.getX());

            isTrapezoid = Math.abs(leftDelta - rightDelta) < 4;

            if (isTrapezoid)
            {
                isTrapezoid = upperLeft.getX() < lowerLeft.getX() && upperRight.getX() > lowerRight.getX();
            }
        }

        return isTrapezoid;
    }
}
