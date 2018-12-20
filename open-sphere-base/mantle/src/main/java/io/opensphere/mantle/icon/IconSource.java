package io.opensphere.mantle.icon;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import javafx.scene.Node;

/**
 * A service that allows one or more icons to be loaded from a local or remote
 * source.
 *
 * @param <M> the type of model serviced by the source.
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
     * @param toolbox the toolbox through which application state is accessed.
     * @return the Collection of icon providers from the icon source.
     */
    List<IconProvider> getIconProviders(Toolbox toolbox);

    /**
     * Gets the model instance to which a user interface will be bound.
     *
     * @return the model instance to which a user interface will be bound.
     */
    M getModel();

    /**
     * Gets the user interface item through which the icon source is configured.
     * The result of the operation should update the source model with the
     * user's selection. The method returns a boolean indicating the intentions
     * of the user, <code>true</code> if the user accepted the operation (e.g.:
     * chose a file), <code>false</code> if the user rejected the operation
     * (e.g.: chose cancel).
     *
     * @param parent the parent node to which a user popup will be bound.
     * @return the acceptance state of the operation (e.g.: if the user chose to
     *         cancel the operation, the method should return false).
     */
    boolean getUserInput(Node parent);
}
