package io.opensphere.core.geometry.renderproperties;

/** A render property that specifies point roundness only. */
public interface PointRoundnessRenderProperty extends RenderProperties, Comparable<PointRoundnessRenderProperty>
{
    @Override
    PointRoundnessRenderProperty clone();

    /**
     * Get if the point should be round (otherwise it should be square).
     *
     * @return If the point is round.
     */
    boolean isRound();

    /**
     * Set if the point should be round (otherwise it should be square).
     *
     * @param round If the point is round.
     */
    void setRound(boolean round);
}
