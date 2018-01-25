package io.opensphere.core.hud.awt;

import io.opensphere.core.model.GeographicBoxAnchor;

/** Internal frame to be displayed as a HUD window. */
public class HUDJInternalFrame implements HUDFrame
{
    /**
     * The geographic position to which the frame is attached. This may be null
     * when the frame is not attached to a geographic position.
     */
    private final GeographicBoxAnchor myGeoAnchor;

    /** The Internal frame which is to be displayed. */
    private final AbstractInternalFrame myInternalFrame;

    /**
     * Constructor.
     *
     * @param builder Builder which contains my settings.
     */
    public HUDJInternalFrame(Builder builder)
    {
        myInternalFrame = builder.getInternalFrame();
        myGeoAnchor = builder.getGeographicAnchor();
    }

    /**
     * Get the geographicAnchor.
     *
     * @return the geographicAnchor
     */
    public GeographicBoxAnchor getGeographicAnchor()
    {
        return myGeoAnchor;
    }

    /**
     * Get the internalFrame.
     *
     * @return the internalFrame
     */
    public AbstractInternalFrame getInternalFrame()
    {
        return myInternalFrame;
    }

    @Override
    public String getTitle()
    {
        return myInternalFrame.getTitle();
    }

    @Override
    public boolean isVisible()
    {
        return myInternalFrame.isVisible();
    }

    @Override
    public void setVisible(boolean visible)
    {
        myInternalFrame.setVisible(visible);
    }

    /** Builder for the internal frame. */
    public static class Builder
    {
        /**
         * The geographic position to which the frame is attached. This may be
         * null when the frame is not attached to a geographic position.
         */
        private GeographicBoxAnchor myGeographicAnchor;

        /** The Internal frame which is to be displayed. */
        private AbstractInternalFrame myInternalFrame;

        /**
         * Get the geographicAnchor.
         *
         * @return the geographicAnchor
         */
        public GeographicBoxAnchor getGeographicAnchor()
        {
            return myGeographicAnchor;
        }

        /**
         * Get the internalFrame.
         *
         * @return the internalFrame
         */
        public AbstractInternalFrame getInternalFrame()
        {
            return myInternalFrame;
        }

        /**
         * Set the geographicAnchor.
         *
         * @param geographicAnchor the geographicAnchor to set
         * @return the builder
         */
        public Builder setGeographicAnchor(GeographicBoxAnchor geographicAnchor)
        {
            myGeographicAnchor = geographicAnchor;
            return this;
        }

        /**
         * Set the internalFrame.
         *
         * @param internalFrame the internalFrame to set
         * @return the builder
         */
        public Builder setInternalFrame(AbstractInternalFrame internalFrame)
        {
            myInternalFrame = internalFrame;
            return this;
        }
    }
}
