package io.opensphere.core.control.action;

/**
 * Manages events related to context menus and subscriptions for menu button
 * providers.
 */
public interface ContextActionManager
{
    /**
     * De-register any single action provider which is not the default provider
     * for the context and notify it that it has been invalidated.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param <R> a context menu key type.
     */
    <R> void clearContextSingleActionProvider(String contextId, Class<R> keyType);

    /**
     * De-register a provider of menus for a context.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param provider The provider to de-register.
     * @param <R> a context menu key type.
     */
    <R> void deregisterContextMenuItemProvider(String contextId, Class<R> keyType, ContextMenuProvider<R> provider);

    /**
     * De-register a provider of a single action for a context.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param provider The provider to de-register.
     * @param <R> a context menu key type.
     */
    <R> void deregisterContextSingleActionProvider(String contextId, Class<R> keyType, ContextSingleActionProvider<R> provider);

    /**
     * De-register a provider of a default action for a context.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param provider The provider to de-register.
     * @param <R> a context menu key type.
     */
    <R> void deregisterDefaultContextActionProvider(String contextId, Class<R> keyType, ContextActionProvider<R> provider);

    /**
     * Gets the {@link ActionContext} for the provided context Identifier, if no
     * context exists yet for the identifier one will be created.
     *
     * @param contextIdentifier the context identifier to be used to get the
     *            context.
     * @param keyType the type of the context key associated with the context
     *            ID.
     * @param <R> A context menu key type.
     * @return the control popup menu option context for the identifier.
     */
    <R> ActionContext<R> getActionContext(String contextIdentifier, Class<R> keyType);

    /**
     * Register a provider of menus for a context. Only a weak reference is held
     * to the provider.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param provider The provider to register.
     * @param <R> a context menu key type.
     */
    <R> void registerContextMenuItemProvider(String contextId, Class<R> keyType, ContextMenuProvider<R> provider);

    /**
     * Register a non-default provider of a single action for a context. Only
     * one may be registered at a time, so if one is already registered it will
     * be notified that it has been invalidated. Only a weak reference is held
     * to the provider.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param provider The provider to register.
     * @param <R> a context menu key type.
     */
    <R> void registerContextSingleActionProvider(String contextId, Class<R> keyType, ContextSingleActionProvider<R> provider);

    /**
     * Register a default provider of a single action for a context. A default
     * provider will only be called if another non-default provider isn't
     * currently registered. Multiple defaults may be registered; they will be
     * called in order of registration until one of them accepts the action.
     * Only a weak reference is held to the provider.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param provider The provider to register.
     * @param <R> a context menu key type.
     */
    <R> void registerDefaultContextActionProvider(String contextId, Class<R> keyType, ContextActionProvider<R> provider);
}
