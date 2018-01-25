/**
 * Package containing classes that allow different types of servers to be
 * treated differently by OGC server plugins. As new server types are added, a
 * new customization can be added to the package and the factory that manages
 * them should pick it up automatically when added to the appropriate
 * ServiceLoader configuration file.
 */
package io.opensphere.server.customization;
