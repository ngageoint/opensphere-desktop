High Level Architecture
-----------------------
OpenSphere Desktop is built as a standalone application using the Java language. At the heart of the application is a rendering engine, written in OpenGL, which is wrapped and exposed using the Java OpenGL (JOGL) libraries. Built atop these libraries is the Core, in which the user interface, data registry and related functionality are implemented. Wrapped around the Core is the Mantle, in which Data Types and Data Groups are implemented, along with cross-application IPC and eventing. Beyond the Core and the Mantle, application functionality is implemented using plugins, each of which are outlined as separate projects on this page.

![High Level Architecture](images/BasicArchitecture.png "Basic Architecture") 

Concepts and Details
--------------------

### Data Element
 * Represents a row inside of a layer
 * Includes metadata
 * Represents something on the map (a point, polygon, etc).

### Data Element Cache
 * Wraps around the Data Registry
 * Part of Mantle 
 * Stores elements for a Data Type Info instance

### Data Group
* A group of zero or more Data Types, that are coupled together
    * Consider the following use case: When you initially load a layer, you get a WMS representation of the data as a summary over time, rendered as tiles, and can load the WFS for a given bounding box. The WFS and WMS components are represented by Data Types, and both are contained within a Data Group.
        * Created within a mantle controller that can translate from source format to DataGroup or Data Type.

### Data Type
 * Represents a Layer of data
 * A single data type is a layer definition (e.g.: WMS or WFS)
 * Contained within a Data Group
 
### Data Registry
 * A two level cache, consisting of in-memory storage, and an H2 database
 * Accessible via the System Toolbox
 * When getting data, asks for data in memory first, and in case of cache miss, then H2DB
    * If no data is available in the H2DB, then uses an Envoy to query external data sources
        * External Source Request is optional, and can be disabled
 * Searches for an Envoy that can query data of the requested type
        * No results are returned if no envoys are found
    * Results are aggregated if more than one envoy is found
 * H2 Tables are created dynamically at runtime
 * When code makes a deposit to the data registry, it supplies one or more Property Descriptors with the data.

### Envoy
An envoy is a component that retrieves data and creates models to represent them in the system. The models are published to the data registry. The models are of arbitrary type by design to allow for serialized objects from external sources to be used. Though the envoy interface specifies that results should be placed into the data registry, interaction with and population of the Data Registry occurs through the DataRegistryDataProvider interface. An envoy instance is bound to a single server when the envoy is instantiated. Envoys should be registered with the Envoy Registry when instantiated.

 * Used to load a specific type of external data
 * Essentially a Cache Miss handler used with the Data Registry
 * Accessed through the Envoy Registry (the Envoy Registry is part of the System Toolbox)
 * Queries the external data source, puts results into the Data Registry, by way of a Query Receiver 
 
### Transformer
 * Translates from a source-specific format to a geometry object
 * Can listen for deposits to the Data Registry
 * Can be called directly
 * Managed by plugins
 * Takes data out of the Data Registry and makes it into renderable items
 
### UI Components

#### Server Manager

#### Layer Manager

#### Add Data


## Detailed Architecture:

![Detailed Architecture](images/ArchitectureDetail.png "Detailed Architecture") 