package io.opensphere.mantle.icon;

import java.util.Collection;
import java.util.List;

import javafx.scene.Node;

/**
 * A service that allows one or more icons to be loaded from a local or remote
 * source.
 */
public interface IconSource<M extends IconSourceModel>
{
    /**
     * Gets the human-readable name of the icon source.
     *
     * @return the human readable name of the icon source.
     */
    String getName();

    /**
     * Gets the {@link Collection} of {@link IconProvider}s from the icon
     * source. Each provider corresponds to one icon.
     *
     * @return the Collection of icon providers from the icon source.
     */
    List<IconProvider> getIconProviders();

    /**
     * Gets the model instance to which a user interface will be bound.
     *
     * @return the model instance to which a user interface will be bound.
     */
    M getModel();

    /**
     * Gets the user interface item through which the icon source is configured.
     * @param parent the parent node to which a user popup will be bound.
     */
    void getUserInput(Node parent);
}
