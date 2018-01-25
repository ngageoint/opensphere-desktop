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
 * The Class TimePositionWindow.
 */
public class TimePositionWindow extends InfoOverlayWindow
{
    /** The Pitch label. */
    private TextLabel myTimeLabel;

    /** The Horizontal alignment. */
    private static final float ourHorizAlignment = .3f;

    /**
     * Instantiates a new time position window.
     *
     * @param helper the helper
     * @param bbox the bbox
     * @param loc the loc
     * @param zOrder the z order
     */
    public TimePositionWindow(TransformerHelper helper, ScreenBoundingBox bbox, ToolLocation loc, int zOrder)
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

        myTimeLabel = new TextLabel(this, viewerLabelBuilder);
        myTimeLabel.setVerticalAlignment(1f);
        myTimeLabel.setHorizontalAlignment(ourHorizAlignment);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(myTimeLabel, constr);
    }

    /**
     * Adds the renderable.
     */
    @Override
    public void addRenderable()
    {
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 4)));
        add(getInfoOverLayRenderable(), constr);
    }

    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
    }

    /**
     * Gets the time label.
     *
     * @return the time label
     */
    public TextLabel getTimeLabel()
    {
        return myTimeLabel;
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
