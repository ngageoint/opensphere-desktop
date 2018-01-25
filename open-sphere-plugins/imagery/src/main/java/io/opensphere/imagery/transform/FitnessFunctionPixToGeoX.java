package io.opensphere.imagery.transform;

import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;

/**
 * The Class FitnessFunctionPixToGeoX.
 */
public class FitnessFunctionPixToGeoX extends FitnessFunctionBaseWarp
{
    /**
     * Instantiates a new fitness function pix to geo x.
     *
     * @param list the list
     */
    public FitnessFunctionPixToGeoX(List<GroundControlPoint> list)
    {
        super(list);
    }

    @Override
    protected void findError(Candidate cand, ImageryTransformNthOrder itno, double[] coef)
    {
        getPg().setXCoefficients(coef);

        cand.setFitness(itno.errorPixelToGeoX(getOriginalList()));
    }
}
