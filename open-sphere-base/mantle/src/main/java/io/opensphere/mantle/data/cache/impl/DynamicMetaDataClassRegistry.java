package io.opensphere.mantle.data.cache.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.util.compiler.DynamicCompiler;
import io.opensphere.mantle.util.compiler.DynamicCompilerUnavailableException;

/**
 * A singleton registry for dynamic meta data classes. This needs to be a true
 * singleton because it dynamically compiles classes based on the type key, and
 * only one class of that dynamic name can be available per data type at each
 * time. If new version of the MetaDataInfo are required it can compile new
 * versions of the dynamic classes and older versions are stored for reference.
 */
public final class DynamicMetaDataClassRegistry implements ClassProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DynamicMetaDataClassRegistry.class);

    /** The registry. */
    private static final DynamicMetaDataClassRegistry ourRegistry = new DynamicMetaDataClassRegistry();

    /** The my can compile. */
    private boolean myCanCompile;

    /**
     * The map of dynamically created classes.
     */
    private final Map<String, Class<?>> myClasses = Collections.synchronizedMap(New.<String, Class<?>>map());

    /** The my compile lock. */
    private final ReentrantLock myCompileLock;

    /** The my compiler. */
    private DynamicCompiler myCompiler;

    /** The data type key to class list map. */
    private final Map<String, List<Class<DynamicMetaDataList>>> myDataTypeKeyToClassListMap;

    /** The my data type key to data type base class name map. */
    private final Map<String, String> myDataTypeKeyToDataTypeBaseClassNameMap;

    /** The my dti hash to class map. */
    private final ConcurrentHashMap<Integer, Class<DynamicMetaDataList>> myDTIHashToClassMap;

    /** The my type counter. */
    private final AtomicInteger myTypeCounter;

    /**
     * Generate data type info hash code.
     *
     * @param dti the dti
     * @return the int
     */
    public static int generateDataTypeInfoHashCode(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        final int prime = 31;
        int result = 1;
        result = prime * result + (dti.getTypeKey() == null ? 0 : dti.getTypeKey().hashCode());
        result = prime * result + dti.getMetaDataInfo().getKeyCount();
        result = prime * result
                + (dti.getMetaDataInfo().getKeyNames() == null ? 0 : dti.getMetaDataInfo().getKeyNames().hashCode());
        result = prime * result + (dti.getMetaDataInfo().getKeyClassTypeMap() == null ? 0
                : dti.getMetaDataInfo().getKeyClassTypeMap().hashCode());
        return result;
    }

    /**
     * Gets the single instance of DynamicMetaDataClassRegistry.
     *
     * @return single instance of DynamicMetaDataClassRegistry
     */
    public static DynamicMetaDataClassRegistry getInstance()
    {
        return ourRegistry;
    }

    /**
     * Instantiates a new dynamic meta data class registry.
     */
    private DynamicMetaDataClassRegistry()
    {
        myTypeCounter = new AtomicInteger(0);
        myDTIHashToClassMap = new ConcurrentHashMap<>();
        myDataTypeKeyToDataTypeBaseClassNameMap = new ConcurrentHashMap<>();
        myDataTypeKeyToClassListMap = new ConcurrentHashMap<>();
        myCompileLock = new ReentrantLock();

        if (Boolean.getBoolean("opensphere.dynamicCompile"))
        {
            final long start = System.nanoTime();
            try
            {
                myCompiler = new DynamicCompiler();
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(StringUtilities.formatTimingMessage("Dynamic data type class compiler initialized in ",
                            System.nanoTime() - start));
                }
                myCanCompile = true;
            }
            catch (DynamicCompilerUnavailableException | ExceptionInInitializerError e)
            {
                LOGGER.error("Failed to initialize dynamic data type class compiler.", e);
                myCanCompile = false;
            }
        }
        else
        {
            myCanCompile = false;
        }
    }

    /**
     * Adds the data type.
     *
     * @param dti the dti
     * @return true if successful, false if not
     */
    public boolean addOrAdjustDataType(DataTypeInfo dti)
    {
        return compileNewDynamicClassForType(dti);
    }

    /**
     * Returns true if the compiler is functional. False if not.
     *
     * @return true, if available.
     */
    public boolean canCompile()
    {
        return myCanCompile;
    }

    @Override
    public Class<?> getClass(String className)
    {
        return myClasses.get(className);
    }

    /**
     * Gets all of the dynamic classes for the specified DataTypeInfo key.
     *
     * @param dtiKey the DataTypeInfo key.
     * @return the dynamic classes for the key or null if not found.
     */
    public List<Class<DynamicMetaDataList>> getDynamicClassesForDataTypeKey(String dtiKey)
    {
        List<Class<DynamicMetaDataList>> retVal = null;
        if (myDataTypeKeyToClassListMap.containsKey(dtiKey))
        {
            retVal = Collections.unmodifiableList(myDataTypeKeyToClassListMap.get(dtiKey));
        }
        return retVal;
    }

    /**
     * Gets the class for the specified hash code, or null if there is none.
     *
     * @param code the code
     * @return the class for hash code
     */
    public Class<DynamicMetaDataList> getDynamicClassForHashCode(int code)
    {
        return myDTIHashToClassMap.get(code);
    }

    /**
     * Gets the latest version of the dynamic class for the specified
     * DataTypeInfo key.
     *
     * @param dtiKey the the DataTypeInfo key.
     * @return the latest version of the class or null if no previous version or
     *         the type is not found.
     */
    public Class<DynamicMetaDataList> getLatestDynamicClassForDataTypeKey(String dtiKey)
    {
        Class<DynamicMetaDataList> retVal = null;
        if (myDataTypeKeyToClassListMap.containsKey(dtiKey))
        {
            final List<Class<DynamicMetaDataList>> typeClassList = myDataTypeKeyToClassListMap.get(dtiKey);
            retVal = typeClassList.isEmpty() ? null : typeClassList.get(typeClassList.size() - 1);
        }
        return retVal;
    }

    /**
     * Gets the previous version of the dynamic class for the specified
     * DataTypeInfo key.
     *
     * @param dtiKey the the DataTypeInfo key.
     * @return the previous version of the class or null if no previous version
     *         or the type is not found.
     */
    public Class<DynamicMetaDataList> getPreviousDynamicClassForDataTypeKey(String dtiKey)
    {
        Class<DynamicMetaDataList> retVal = null;
        if (myDataTypeKeyToClassListMap.containsKey(dtiKey))
        {
            final List<Class<DynamicMetaDataList>> typeClassList = myDataTypeKeyToClassListMap.get(dtiKey);
            final int size = typeClassList.size();
            if (size > 1)
            {
                retVal = typeClassList.get(typeClassList.size() - 2);
            }
        }
        return retVal;
    }

    /**
     * Compile new dynamic class for type.
     *
     * @param dti the dti
     * @return true if compiled or class already available, false if not.
     */
    @SuppressWarnings("unchecked")
    private boolean compileNewDynamicClassForType(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        if (dti.getMetaDataInfo() == null)
        {
            throw new IllegalArgumentException("Cannot compile data types without MetaDataInfo.");
        }
        if (myCanCompile)
        {
            myCompileLock.lock();
            try
            {
                final String dtiKey = dti.getTypeKey();
                String className = myDataTypeKeyToDataTypeBaseClassNameMap.get(dti.getTypeKey());
                if (className == null)
                {
                    className = "DynamicMetaData_Type" + myTypeCounter.incrementAndGet();
                    myDataTypeKeyToDataTypeBaseClassNameMap.put(dtiKey, className);
                }

                List<Class<DynamicMetaDataList>> typeClassList = myDataTypeKeyToClassListMap.get(dtiKey);
                if (typeClassList == null)
                {
                    typeClassList = new ArrayList<>();
                    myDataTypeKeyToClassListMap.put(dtiKey, typeClassList);
                }

                final int nextVersion = typeClassList.size();
                final int dtiHashCode = generateDataTypeInfoHashCode(dti);
                if (myDTIHashToClassMap.containsKey(dtiHashCode))
                {
                    final Class<DynamicMetaDataList> cl = myDTIHashToClassMap.get(dtiHashCode);
                    if (Utilities.sameInstance(typeClassList.get(typeClassList.size() - 1), cl))
                    {
                        if (LOGGER.isTraceEnabled())
                        {
                            LOGGER.trace("No dynamic class required current version is still valid for type " + dti.getTypeKey());
                        }
                        return true;
                    }
                    else
                    {
                        if (LOGGER.isTraceEnabled())
                        {
                            LOGGER.trace("Previous dynamic class version is still valid for type " + dti.getTypeKey()
                                    + " setting to latest.");
                        }
                        typeClassList.add(cl);
                        return true;
                    }
                }
                else
                {
                    final DynamicMetaDataListCodeGenerator gen = new DynamicMetaDataListCodeGenerator(className, nextVersion, dti,
                            dtiHashCode);
                    final String source = gen.generateSource();
                    final String fullyQualifiedClasssName = gen.getFullyQualifiedClassName();
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(source);
                    }

                    try
                    {
                        final Class<?> cl = myCompiler.compileToClass(fullyQualifiedClasssName, source);
                        if (cl != null)
                        {
                            final DynamicMetaDataList tdc = (DynamicMetaDataList)cl.getDeclaredConstructor().newInstance();
                            typeClassList.add((Class<DynamicMetaDataList>)cl);
                            myDTIHashToClassMap.put(Integer.valueOf(dtiHashCode), (Class<DynamicMetaDataList>)cl);
                            myClasses.put(fullyQualifiedClasssName, cl);
                            if (LOGGER.isTraceEnabled())
                            {
                                LOGGER.trace("Created dynamic meta data type " + tdc.getClass().getName());
                            }
                            return true;
                        }
                    }
                    catch (ReflectiveOperationException | ClassCastException e)
                    {
                        LOGGER.error(e);
                    }
                }
            }
            finally
            {
                myCompileLock.unlock();
            }
        }
        return false;
    }
}
