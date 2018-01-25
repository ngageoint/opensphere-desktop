package io.opensphere.core.pipeline.util;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Constants;

/**
 * Helper class for getting video memory availability and usage information. The
 * information provided by the driver may not be accurate at any particular time
 * and should be used as a guideline. The ATI extensions return 4 values rather
 * than one; for each category, these are (in order) : total free memory in the
 * pool, largest available free block in the pool, total auxiliary memory free,
 * largest auxiliary free block.
 */
public final class GPUMemoryHelper
{
    /**
     * Current available dedicated video memory in kilobytes, currently unused
     * GPU memory.
     */
    public static final int GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX = 0x9049;

    /** Dedicated video memory, total size in kilobytes of the GPU memory. */
    public static final int GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX = 0x9047;

    /** Size of total video memory evicted in kilobytes. */
    public static final int GPU_MEMORY_INFO_EVICTED_MEMORY_NVX = 0x904B;

    /** Count of total evictions seen by the system. */
    public static final int GPU_MEMORY_INFO_EVICTION_COUNT_NVX = 0x904A;

    /**
     * Total video memory, total size in kilobytes of the memory available for
     * allocations.
     */
    public static final int GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX = 0x9048;

    /** Memory available for render buffer usage. */
    public static final int RENDERBUFFER_FREE_MEMORY_ATI = 0x87FD;

    /** Memory available for texture usage. */
    public static final int TEXTURE_FREE_MEMORY_ATI = 0x87FC;

    /** Memory available for VBO usage. */
    public static final int VBO_FREE_MEMORY_ATI = 0x87FB;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(GPUMemoryHelper.class);

    /**
     * The type of GPU in use. This is used to determine which proprietary
     * extensions to use for memory access.
     */
    private static GPUType ourType = GPUType.UNCHECKED;

    /**
     * Get the amount of available GPU memory in bytes.
     *
     * @param rc The render context.
     * @return The number of bytes of dedicated video memory currently
     *         available.
     */
    public static long getGPUMemoryAvailableBytes(RenderContext rc)
    {
        final GPUType type = getGPUType(rc);
        long availMem = 0L;
        if (type == GPUType.NVIDIA)
        {
            final int[] glMem = new int[1];
            rc.getGL().glGetIntegerv(GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX, glMem, 0);
            availMem = glMem[0] * (long)Constants.BYTES_PER_KILOBYTE;
        }
        else if (type == GPUType.AMD)
        {
            final int[] gpuMem = new int[4];
            rc.getGL().glGetIntegerv(TEXTURE_FREE_MEMORY_ATI, gpuMem, 0);
            availMem = gpuMem[0] * (long)Constants.BYTES_PER_KILOBYTE;
        }
        else
        {
            LOGGER.warn("Could not get available GPU memory for GPU type: " + ourType);
        }

        return availMem;
    }

    /**
     * Get the amount of GPU memory in bytes. For ATI cards this will return the
     * amount of free memory at the time of the call.
     *
     * @param rc The render context.
     * @return The number of bytes of dedicated video memory.
     */
    public static long getGPUMemorySizeBytes(RenderContext rc)
    {
        final Long override = Long.getLong("opensphere.pipeline.gpuMemorySizeBytes");
        if (override != null)
        {
            LOGGER.info("Using GPU memory override.");
            return override.longValue();
        }

        final GPUType type = getGPUType(rc);
        long gpuMemory;
        if (type == GPUType.NVIDIA)
        {
            final int[] glMem = new int[1];
            rc.getGL().glGetIntegerv(GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX, glMem, 0);
            gpuMemory = glMem[0] * (long)Constants.BYTES_PER_KILOBYTE;
        }
        else if (type == GPUType.AMD)
        {
            final int[] gpuMem = new int[4];
            rc.getGL().glGetIntegerv(TEXTURE_FREE_MEMORY_ATI, gpuMem, 0);
            gpuMemory = gpuMem[0] * (long)Constants.BYTES_PER_KILOBYTE;
        }
        else
        {
            gpuMemory = 0L;
        }

        // If the reported memory is less than 100 MB, assume it's wrong and use
        // the default value.
        if (gpuMemory < 100 * Constants.BYTES_PER_MEGABYTE)
        {
            if (type == GPUType.UNKNOWN)
            {
                LOGGER.warn("Could not get GPU memory for GPU type: " + type);
            }
            else
            {
                LOGGER.warn("Reported GPU memory for GPU type [" + type + "] is " + gpuMemory
                        + "B, which is lower than the minimum threshold of 100MB.");
            }
            LOGGER.warn("Using opensphere.pipeline.gpuMemorySizeBytesDefault system property.");
            gpuMemory = Long.getLong("opensphere.pipeline.gpuMemorySizeBytesDefault", 1 << 31).longValue();
        }
        return gpuMemory;
    }

    /**
     * Get the type of GPU in use by the system, determine by checking for
     * available extensions if necessary.
     *
     * @param rc The render context.
     * @return The GPU type.
     */
    private static GPUType getGPUType(RenderContext rc)
    {
        if (ourType == GPUType.UNCHECKED)
        {
            if (rc.isExtensionAvailable("GL_NVX_gpu_memory_info"))
            {
                ourType = GPUType.NVIDIA;
            }
            else if (rc.isExtensionAvailable("GL_ATI_meminfo"))
            {
                ourType = GPUType.AMD;
            }
            else
            {
                ourType = GPUType.UNKNOWN;
            }
        }
        return ourType;
    }

    /** Disallow instantiation. */
    private GPUMemoryHelper()
    {
    }

    /** The type of GPU currently in use by the system. */
    public enum GPUType
    {
        /** AMD GPU. */
        AMD,

        /** NVIDIA GPU. */
        NVIDIA,

        /** The type has not yet been checked. */
        UNCHECKED,

        /** The type has been checked, but is an unknown type. */
        UNKNOWN;
    }
}
