package io.opensphere.subterrain.xraygoggles.ui;

import java.awt.Color;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.PolylineGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * The window that shows the borders of the xray viewing area.
 */
public class XrayWindow extends DefaultTransformer implements Observer
{
    /**
     * The previously published window geometry.
     */
    private Geometry myGeometry;

    /**
     * The model containing the screen coordinates of the window.
     */
    private final XrayGogglesModel myModel;

    /**
     * Constructs a new xray view window.
     *
     * @param dataRegistry The data registry.
     * @param model The model containing the screen coordinates of the window.
     */
    public XrayWindow(DataRegistry dataRegistry, XrayGogglesModel model)
    {
        super(dataRegistry);
        myModel = model;
    }

    @Override
    public void close()
    {
        myModel.deleteObserver(this);
        super.close();
    }

    @Override
    public void open()
    {
        super.open();
        publishUnpublishWindow();
        myModel.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (XrayGogglesModel.SCREEN_POSITION.equals(arg))
        {
            publishUnpublishWindow();
        }
    }

    /**
     * Draws or removes the xray view window from the screen.
     */
    private synchronized void publishUnpublishWindow()
    {
        List<Geometry> unpublish = New.list();
        List<Geometry> publish = New.list();

        if (myGeometry != null)
        {
            unpublish.add(myGeometry);
            myModel.setWindowGeometry(null);
        }

        if (myModel.getLowerLeft() != null && myModel.getLowerRight() != null && myModel.getUpperLeft() != null
                && myModel.getUpperRight() != null)
        {
            Builder<ScreenPosition> builder = new Builder<>();
            builder.setVertices(
                    New.list(myModel.getUpperLeft(), myModel.getUpperRight(), myModel.getLowerRight(), myModel.getLowerLeft(), myModel.getUpperLeft()));

            DefaultPolylineRenderProperties renderProperties = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z,
                    true, true);
            renderProperties.setColor(Color.WHITE);
            renderProperties.setWidth(5);

            myGeometry = new PolylineGeometry(builder, renderProperties, null);
            myModel.setWindowGeometry(myGeometry);
            publish.add(myGeometry);
        }

        publishGeometries(publish, unpublish);
    }
}
