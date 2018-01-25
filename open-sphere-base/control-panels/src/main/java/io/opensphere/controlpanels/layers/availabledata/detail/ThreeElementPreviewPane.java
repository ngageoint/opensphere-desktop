package io.opensphere.controlpanels.layers.availabledata.detail;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * A custom JavaFX {@link Region} in which three components are rendered
 * appropriately, two image preview components, and one text area.
 */
public class ThreeElementPreviewPane extends Region
{
    /**
     * The area in which an image preview is rendered.
     */
    private final ObjectProperty<ImageView> myImageViewProperty = new SimpleObjectProperty<>();

    /**
     * The area in which a map preview is rendered.
     */
    private final ObjectProperty<ImageView> myMapViewProperty = new SimpleObjectProperty<>();

    /**
     * A scroll pane in which the remaining area is contained.
     */
    private final ObjectProperty<TextArea> myTextViewProperty = new SimpleObjectProperty<>();

    /**
     * A scroll pane in which the remaining area is contained.
     */
    private final ObjectProperty<ScrollPane> myTextViewScrollPaneProperty = new SimpleObjectProperty<>();

    /**
     * A progress indicator used to inform the user that the map image is being
     * loaded.
     */
    private final ObjectProperty<ProgressIndicator> myMapProgressIndicator = new SimpleObjectProperty<>();

    /**
     * A progress indicator used to inform the user that the preview image is
     * being loaded.
     */
    private final ObjectProperty<ProgressIndicator> myImageProgressIndicator = new SimpleObjectProperty<>();

    /**
     * A flag used to change the image progress indicator's state from not
     * loading to loading, and visa versa.
     */
    private final ObjectProperty<Boolean> myLoadingImageProperty = new SimpleObjectProperty<>();

    /**
     * A flag used to change the map progress indicator's state from not loading
     * to loading, and visa versa.
     */
    private final ObjectProperty<Boolean> myLoadingMapProperty = new SimpleObjectProperty<>();

    /**
     * Creates a new preview pane, initializing all necessary components.
     *
     * @param pImageView the view in which preview images are rendered.
     * @param pMapView the view in which map footprints are rendered.
     * @param pTextArea the view in which text is rendered.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ThreeElementPreviewPane(ImageView pImageView, ImageView pMapView, TextArea pTextArea)
    {
        myImageViewProperty.addListener((observable, oldValue, newValue) -> viewChange(oldValue, newValue));
        myImageViewProperty.set(pImageView);

        myMapViewProperty.addListener((observable, oldValue, newValue) -> viewChange(oldValue, newValue));
        myMapViewProperty.set(pMapView);

        myTextViewProperty.set(pTextArea);

        myTextViewScrollPaneProperty.addListener((observable, oldValue, newValue) -> viewChange(oldValue, newValue));

        ScrollPane scrollPane = new ScrollPane(pTextArea);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        myTextViewScrollPaneProperty.set(scrollPane);

        myImageProgressIndicator.addListener((observable, oldValue, newValue) -> viewChange(oldValue, newValue));
        myImageProgressIndicator.set(new ProgressIndicator());

        myMapProgressIndicator.addListener((observable, oldValue, newValue) -> viewChange(oldValue, newValue));
        myMapProgressIndicator.set(new ProgressIndicator());

        myLoadingImageProperty
                .addListener((observable, oldValue, newValue) -> loadingViewChange(newValue, myImageProgressIndicator.get()));
        myLoadingMapProperty
                .addListener((observable, oldValue, newValue) -> loadingViewChange(newValue, myMapProgressIndicator.get()));
    }

    /**
     * An event handler method, used as the target of a lambda expression on the
     * {@link #loadingImageProperty()} and {@link #loadingMapProperty()}. If the
     * <code>loadingState</code> is true, and the progress indicator is not
     * already a member of the parent component, it will be added to the
     * children.
     *
     * @param loadingState the loading state of the component.
     * @param indicator the indicator to add or remove.
     */
    protected void loadingViewChange(Boolean loadingState, ProgressIndicator indicator)
    {
        if (loadingState != null && loadingState && !getChildren().contains(indicator))
        {
            getChildren().add(indicator);
        }
        else
        {
            getChildren().remove(indicator);
        }
    }

    /**
     * Updates the component in reaction to a view change, processing both the
     * old and new views. If the old view is present, it will be removed. If the
     * new view is present, it will be added.
     *
     * @param oldView the old view to process, may be null.
     * @param newView the new view to process, may be null.
     */
    protected void viewChange(Node oldView, Node newView)
    {
        if (oldView != null)
        {
            getChildren().remove(oldView);
        }

        if (newView != null)
        {
            getChildren().add(newView);
        }
    }

    /**
     * Gets the value of the {@link #myLoadingMapProperty} field.
     *
     * @return the value stored in the {@link #myLoadingMapProperty} field.
     */
    public ObjectProperty<Boolean> loadingMapProperty()
    {
        return myLoadingMapProperty;
    }

    /**
     * Gets the value of the {@link #myLoadingImageProperty} field.
     *
     * @return the value stored in the {@link #myLoadingImageProperty} field.
     */
    public ObjectProperty<Boolean> loadingImageProperty()
    {
        return myLoadingImageProperty;
    }

    /**
     * Gets the image property of the image view. Used to shortcut long calls
     * for binding images to other properties.
     *
     * @return the image property from the image view.
     */
    public ObjectProperty<Image> imageProperty()
    {
        return myImageViewProperty.get().imageProperty();
    }

    /**
     * Gets the image property of the map view. Used to shortcut long calls for
     * binding images to other properties.
     *
     * @return the image property from the map view.
     */
    public ObjectProperty<Image> mapProperty()
    {
        return myMapViewProperty.get().imageProperty();
    }

    /**
     * Gets the text property of the view. Used to shortcut long calls for
     * binding text to other properties.
     *
     * @return the text property of the view.
     */
    public StringProperty textProperty()
    {
        return myTextViewProperty.get().textProperty();
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.Parent#layoutChildren()
     */
    @Override
    protected void layoutChildren()
    {
        double containerWidth = getWidth();
        double containerHeight = getHeight();

        double preferredHeight = containerHeight / 3;
        double verticalOffset = 0.0;
        verticalOffset += layoutComponent(myImageViewProperty, verticalOffset, preferredHeight, myImageProgressIndicator,
                myLoadingImageProperty);
        verticalOffset += layoutComponent(myMapViewProperty, verticalOffset, preferredHeight, myMapProgressIndicator,
                myLoadingMapProperty);

        ScrollPane textView = myTextViewScrollPaneProperty.get();
        if (textView != null)
        {
            textView.setPrefWidth(containerWidth);
            textView.setPrefHeight(containerHeight - verticalOffset);

            layoutInArea(textView, 0, verticalOffset, containerWidth, containerHeight, 0, HPos.CENTER, VPos.BOTTOM);
        }

        super.layoutChildren();
    }

    /**
     * Lays out the image view contained within the supplied <code>pView</code>
     * component, making use of the other parameters to determine size and
     * placement.
     *
     * @param pView the property in which the view component is contained.
     * @param pVerticalOffset the vertical offset by which to move the rendering
     *            area.
     * @param pPreferredHeight the preferred height of the component.
     * @param pProgressIndicator the progress indicator to optionally render, if
     *            needed.
     * @param pLoadingProperty the property used to determine if the progress
     *            indicator should be rendered.
     * @return the height of the rendered component.
     */
    protected double layoutComponent(ObjectProperty<ImageView> pView, double pVerticalOffset, double pPreferredHeight,
            ObjectProperty<ProgressIndicator> pProgressIndicator, ObjectProperty<Boolean> pLoadingProperty)
    {
        ImageView view = pView.get();

        double containerWidth = getWidth();
        double containerHeight = getHeight();

        double returnValue = 0;
        if (view != null)
        {
            view.setFitWidth(containerWidth);
            view.setFitHeight(pPreferredHeight);
            layoutInArea(view, 0, pVerticalOffset, containerWidth, containerHeight, 0, HPos.CENTER, VPos.TOP);
            returnValue = view.getBoundsInParent().getHeight();
        }

        if (pLoadingProperty.get())
        {
            ProgressIndicator indicator = pProgressIndicator.get();
            indicator.setPrefHeight(pPreferredHeight);
            indicator.setPrefWidth(containerWidth);

            layoutInArea(indicator, 0, pVerticalOffset, containerWidth, containerHeight, 0, HPos.CENTER, VPos.TOP);
        }

        return returnValue;
    }
}
