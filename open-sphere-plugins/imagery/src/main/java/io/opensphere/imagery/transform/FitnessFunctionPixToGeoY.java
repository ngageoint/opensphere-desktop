package io.opensphere.imagery.transform;

import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;

/**
 * The Class FitnessFunctionPixToGeoY.
 */
public class FitnessFunctionPixToGeoY extends FitnessFunctionBaseWarp
{
    /**
     * Instantiates a new fitness function pix to geo y.
     *
     * @param list the list
     */
    public FitnessFunctionPixToGeoY(List<GroundControlPoint> list)
    {
        super(list);
    }

    @Override
    protected void findError(Candidate cand, ImageryTransformNthOrder itno, double[] coef)
    {
        getPg().setYCoefficients(coef);

        cand.setFitness(itno.errorPixelToGeoY(getOriginalList()));
    }
}
