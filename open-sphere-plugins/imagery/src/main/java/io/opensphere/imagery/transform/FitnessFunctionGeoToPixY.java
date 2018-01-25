package io.opensphere.imagery.transform;

import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;

/**
 * The Class FitnessFunctionGeoToPixY.
 */
public class FitnessFunctionGeoToPixY extends FitnessFunctionBaseWarp
{
    /**
     * Instantiates a new fitness function geo to pix y.
     *
     * @param list the list
     */
    public FitnessFunctionGeoToPixY(List<GroundControlPoint> list)
    {
        super(list);
    }

    @Override
    protected void findError(Candidate cand, ImageryTransformNthOrder itno, double[] coef)
    {
        getGp().setYCoefficients(coef);

        cand.setFitness(itno.errorGeoToPixelY(getOriginalList()));
    }
}
