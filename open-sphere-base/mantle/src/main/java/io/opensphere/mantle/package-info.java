/**
 * The Mantle Plugin defines interfaces and tools for easily integrating new
 * data types into the application without the need for extensive knowledge of the
 * underlying rendering, 3D Mapping, or core plugin architecture.
 *
 * The Mantle is centered around several core concepts used for organizing and
 * relating data, injecting data into the tool, and manipulating that data once
 * injected.
 *
 * The Mantle is also optimized to work with Feature (also called
 * {@link io.opensphere.mantle.data.element.DataElement}s) data and provides
 * all the necessary services to visualize and manipulate feature type data with
 * minimal plugin work, for instance the Mantle provides the transformer and
 * handling for feature data types, freeing the developer from needing to
 * re-develop their own {@link io.opensphere.core.api.Transformer} to get
 * their data onto the map.
 *
 * All the required services for the Mantle are provided via the
 * {@link io.opensphere.mantle.MantleToolbox} which can be retrieved from the
 * core {@link io.opensphere.core.Toolbox} through the
 * {@link io.opensphere.core.PluginToolboxRegistry}.
 *
 * <h3>Retrieving the MantleToolBox From the Core Toolbox</h3>
 * <p>
 * To retrieve the {@link io.opensphere.mantle.MantleToolbox} from the core
 * {@link io.opensphere.core.Toolbox} you may use one of two methods give that
 * you have an instance of the core toolbox <code>Toolbox tb</code>.
 *
 * Method 1: Using the Core Toolbox
 * <code> MantleToolbox mtb =  tb.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class); </code>
 * </p>
 * <p>
 * Method 2: Using the {@link io.opensphere.mantle.util.MantleToolboxUtils}
 * <code> MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(tb);</code>
 * </p>
 * <h3>Data Organization</h3>
 * <p>
 * Data is organized based on two key types:
 * {@link io.opensphere.mantle.data.DataGroupInfo} and
 * {@link io.opensphere.mantle.data.DataTypeInfo}.
 * </p>
 * <p>
 * DataTypeInfo defines a singular data type, it provides the definition
 * including the name, the type key, information about how the type is used and
 * visualized within the tool. It provides a information about MetaData for the
 * data type and also if the type is visible or in use. Time and geographic
 * bounding information is also provided. A default implementation of the
 * DataTypeInfo interface is provided via
 * {@link io.opensphere.mantle.data.impl.DefaultDataTypeInfo}.
 * </p>
 * <p>
 * DataGroupInfo provides the ability to group multiple DataTypeInfo together
 * and to define the relationship between one group and another. The
 * DataGroupInfo has one or more member DataTypeInfo and has a tree like
 * structure relationship to other DataGroupInfo. A default implementation of
 * the DataGroupInfo interface is provided via
 * {@link io.opensphere.mantle.data.impl.DefaultDataGroupInfo}.
 * </p>
 * <p>
 * The Mantle works with externally provided DataTypeInfo through the
 * {@link io.opensphere.mantle.controller.DataTypeController} which is
 * available from the {@link io.opensphere.mantle.MantleToolbox}. Once a new
 * {@link io.opensphere.mantle.data.DataTypeInfo} is registered with the
 * {@link io.opensphere.mantle.controller.DataTypeController} individual
 * feature data for that data type can then be injected into the system through
 * the {@link io.opensphere.mantle.controller.DataTypeController} as well.
 * </p>
 * <p>
 * The Mantle works with externally provided DataGroupInfo through the
 * {@link io.opensphere.mantle.controller.DataGroupController} also available
 * from the {@link io.opensphere.mantle.MantleToolbox}.
 * </p>
 */
package io.opensphere.mantle;
