package io.opensphere.imagery.transform;

import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;

/**
 * The Class FitnessFunctionGeoToPixX.
 */
public class FitnessFunctionGeoToPixX extends FitnessFunctionBaseWarp
{
    /**
     * Instantiates a new fitness function geo to pix x.
     *
     * @param list the list
     */
    public FitnessFunctionGeoToPixX(List<GroundControlPoint> list)
    {
        super(list);
    }

    @Override
    protected void findError(Candidate cand, ImageryTransformNthOrder itno, double[] coef)
    {
        getGp().setXCoefficients(coef);
        cand.setFitness(itno.errorGeoToPixelX(getOriginalList()));
    }
}
