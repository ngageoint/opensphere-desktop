package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.hud.widget.TextLabel;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * The Class ViewerPositionWindow.
 */
public class ViewerPositionWindow extends InfoOverlayWindow
{
    /** The Pitch label. */
    private TextLabel myPitchLabel;

    /** The Heading label. */
    private TextLabel myHeadingLabel;

    /** The Alt label. */
    private TextLabel myAltTextLabel;

    /**
     * Instantiates a new viewer position window.
     *
     * @param helper the helper
     * @param bbox the bbox
     * @param loc the loc
     * @param zOrder the z order
     */
    public ViewerPositionWindow(TransformerHelper helper, ScreenBoundingBox bbox, ToolLocation loc, int zOrder)
    {
        super(helper, bbox, loc, ResizeOption.RESIZE_KEEP_FIXED_SIZE, zOrder);
    }

    /**
     * Adds the labels.
     */
    @Override
    public void addLabels()
    {
        TextLabel.Builder viewerLabelBuilder = new TextLabel.Builder();
        viewerLabelBuilder.setText("");
        viewerLabelBuilder.setColor(Color.GREEN);
        viewerLabelBuilder.setFont(Font.SANS_SERIF + " PLAIN 12");
        GridLayoutConstraints constr = null;

        myPitchLabel = new TextLabel(this, viewerLabelBuilder);
        myPitchLabel.setVerticalAlignment(1f);
        myPitchLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(myPitchLabel, constr);

        myHeadingLabel = new TextLabel(this, viewerLabelBuilder);
        myHeadingLabel.setVerticalAlignment(1f);
        myHeadingLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 1), new ScreenPosition(0, 1)));
        add(myHeadingLabel, constr);

        myAltTextLabel = new TextLabel(this, viewerLabelBuilder);
        myAltTextLabel.setVerticalAlignment(1f);
        myAltTextLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 2), new ScreenPosition(0, 2)));
        add(myAltTextLabel, constr);
    }

    @Override
    public void addRenderable()
    {
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 4)));
        add(getInfoOverLayRenderable(), constr);
    }

    /**
     * Gets the alt label.
     *
     * @return the alt label
     */
    public TextLabel getAltLabel()
    {
        return myAltTextLabel;
    }

    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
    }

    /**
     * Gets the heading label.
     *
     * @return the heading label
     */
    public TextLabel getHeadingLabel()
    {
        return myHeadingLabel;
    }

    /**
     * Gets the pitch label.
     *
     * @return the pitch label
     */
    public TextLabel getPitchLabel()
    {
        return myPitchLabel;
    }

    @Override
    public void init()
    {
        initBorder();
        // set the layout
        setLayout(new GridLayout(1, 4, this));
        addLabels();
        addRenderable();
        getLayout().complete();
    }

    @Override
    public void repositionForInsets()
    {
    }
}
