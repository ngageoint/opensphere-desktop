#Toolboxes

Generically speaking, a toolbox is a mechanism through which a collection of tools to be used by plug-ins to interact with the rest of the OpenSphere system is housed. There are two types of toolboxes in the system: The System Toolbox, and plugin-specific Toolboxes.

## System Toolbox
The System Toolbox is provided by the application when invoked, and is available to all plugins in the architecture. The functionality provide by the toolbox is defined in the `io.opensphere.core.Toolbox` interface, and includes accessor methods to retrieve various managers and registries. The System Toolbox is instantiated in the `io.opensphere.core.appl.Kernel` during application initiation, and is passed to each plugin when loaded.

## Plugin-Specific Toolboxes
Each plugin may define a unique and specific toolbox to encapsulate managers, registries, and other resources if necessary (not all plugins require a unique toolbox). When doing so, all plugin-specific toolboxes must implement the `io.opensphere.core.PluginToolbox` interface (and in practice, should define a plugin-specific interface that extends `PluginToolbox`). The plugin-specific toolbox must also be registered in the Plugin Toolbox Registry, accessed through the System Toolbox. Accessing the plugin toolbox instance is also performed using the Plugin Toolbox Registry.     

Example of Registering a new Mantle Toolbox:

    myToolbox.getPluginToolboxRegistry().registerPluginToolbox(new MantleToolboxImpl(myToolbox, pluginProperties));

Example of Accessing an existing Mantle Toolbox:

    MantleToolbox pluginTb = tb.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);    
