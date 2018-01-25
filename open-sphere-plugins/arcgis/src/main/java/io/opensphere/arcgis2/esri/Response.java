package io.opensphere.arcgis2.esri;

import java.util.List;

/** A response. */
public class Response
{
    /** The features. */
    private List<Feature> myFeatures;

    /**
     * Gets the features.
     *
     * @return the features
     */
    public List<Feature> getFeatures()
    {
        return myFeatures;
    }

    /**
     * Sets the features.
     *
     * @param features the features
     */
    public void setFeatures(List<Feature> features)
    {
        myFeatures = features;
    }

    @Override
    public String toString()
    {
        return "Response [" + myFeatures + "]";
    }
}
