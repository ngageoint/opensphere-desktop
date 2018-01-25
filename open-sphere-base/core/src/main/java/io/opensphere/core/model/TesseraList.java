package io.opensphere.core.model;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.model.Tessera.TesseraVertex;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * Comprises a list of blocks of like same (same number of vertices per tessera
 * within each block) that represent a tessellation.
 *
 * @param <S> The type of vertices in the list.
 */
public class TesseraList<S extends TesseraVertex<?>>
{
    /** All blocks of tesserae that make up this list. */
    private final List<TesseraBlock<? extends S>> myTesseraBlocks = new ArrayList<>(2);

    /**
     * Construct the tessera list.
     *
     * @param vertices The list of vertices making up the tessellation.
     * @param tesseraVertexCount The number of vertices in each tessera.
     * @param petrified When true this comes from a set of tesserae which are
     *            immutable and may span multiple projection snapshots.
     */
    public TesseraList(List<? extends S> vertices, int tesseraVertexCount, boolean petrified)
    {
        this(vertices, null, tesseraVertexCount, petrified);
    }

    /**
     * Construct the tessera list with only one block.
     *
     * @param vertices The list of vertices making up the tessellation.
     * @param indices The optional list of indices into the list of vertices
     *            specifying an alternative draw order. If this is
     *            <code>null</code>, the vertices are drawn in sequence.
     * @param tesseraVertexCount The number of vertices in each tessera.
     * @param petrified When true this comes from a set of tesserae which are
     *            immutable and may span multiple projection snapshots.
     */
    public TesseraList(List<? extends S> vertices, PetrifyableTIntList indices, int tesseraVertexCount, boolean petrified)
    {
        TesseraBlock<S> block = new TesseraBlock<>(vertices, indices, tesseraVertexCount, petrified);
        myTesseraBlocks.add(block);
    }

    /**
     * Construct the tessera list with the given blocks.
     *
     * @param blocks Blocks of tesserae.
     */
    public TesseraList(List<TesseraBlock<S>> blocks)
    {
        myTesseraBlocks.addAll(blocks);
    }

    /**
     * Get the tesseraBlocks.
     *
     * @return the tesseraBlocks
     */
    public List<TesseraBlock<? extends S>> getTesseraBlocks()
    {
        return myTesseraBlocks;
    }

    /**
     * Comprises a list of vertices and indices that represent part of a
     * tessellation.
     *
     * @param <S> The type of vertices in the list.
     */
    public static class TesseraBlock<S>
    {
        /** The list of indices. */
        private final PetrifyableTIntList myIndices;

        /**
         * True when this block comes from a set of petrified tesserae and may
         * span multiple projection snapshots.
         */
        private final boolean myPetrified;

        /** The count of vertices in each Tessera in the collection. */
        private final int myTesseraVertexCount;

        /** The vertices. */
        private final List<? extends S> myVertices;

        /**
         * Construct the tessera list.
         *
         * @param vertices The list of vertices making up the tessellation.
         * @param indices The optional list of indices into the list of vertices
         *            specifying an alternative draw order. If this is
         *            <code>null</code>, the vertices are drawn in sequence.
         * @param tesseraVertexCount The number of vertices in each tessera.
         * @param petrified When true this block comes from a set of tesserae
         *            which are immutable and may span multiple projection
         *            snapshots.
         */
        public TesseraBlock(List<? extends S> vertices, PetrifyableTIntList indices, int tesseraVertexCount, boolean petrified)
        {
            assert !vertices.isEmpty();
            myPetrified = petrified;
            myVertices = vertices;
            myIndices = indices;
            if (myIndices != null)
            {
                myIndices.petrify();
            }
            myTesseraVertexCount = tesseraVertexCount;
        }

        /**
         * Constructor.
         *
         * @param builder Builder for a tessera block.
         * @param petrified When true this block comes from a set of tesserae
         *            which are immutable and may span multiple projection
         *            snapshots.
         */
        public TesseraBlock(TesseraBlockBuilder<? extends S> builder, boolean petrified)
        {
            myPetrified = petrified;
            myVertices = builder.getBlockVertices();
            if (builder.getBlockIndices().isEmpty())
            {
                myIndices = null;
            }
            else
            {
                myIndices = builder.getBlockIndices();
                myIndices.petrify();
            }
            myTesseraVertexCount = builder.getBlockTesseraVertexCount();
        }

        /**
         * Get the list of indices. If this is <code>null</code>, the vertices
         * are to be drawn in order.
         *
         * @return The list of indices.
         */
        public PetrifyableTIntList getIndices()
        {
            return myIndices;
        }

        /**
         * Get the number of vertices in each tessera. (For example, a triangle
         * tessera has three vertices.)
         *
         * @return The number of vertices in each tessera.
         */
        public int getTesseraVertexCount()
        {
            return myTesseraVertexCount;
        }

        /**
         * Get the list of vertices.
         *
         * @return The list of vertices.
         */
        public List<? extends S> getVertices()
        {
            return myVertices;
        }

        /**
         * Get the petrified.
         *
         * @return the petrified
         */
        public boolean isPetrified()
        {
            return myPetrified;
        }
    }

    /**
     * The set required to hold the tessera pieces while building up the block.
     *
     * @param <S> The type of vertices in the list.
     */
    public abstract static class TesseraBlockBuilder<S>
    {
        /** The list of indices. */
        private final PetrifyableTIntList myBlockIndices = new PetrifyableTIntArrayList();

        /** The count of vertices in each Tessera in the collection. */
        private final int myBlockTesseraVertexCount;

        /** The vertices. */
        private final List<S> myBlockVertices = new ArrayList<>();

        /** The index which is to be used for the next vertex. */
        private int myCurrentIndex;

        /**
         * Constructor.
         *
         * @param tesseraVertexCount The number of vertices per tessera.
         */
        public TesseraBlockBuilder(int tesseraVertexCount)
        {
            myBlockTesseraVertexCount = tesseraVertexCount;
        }

        /**
         * Get the blockIndices.
         *
         * @return the blockIndices
         */
        public PetrifyableTIntList getBlockIndices()
        {
            return myBlockIndices;
        }

        /**
         * Get the blockTesseraVertexCount.
         *
         * @return the blockTesseraVertexCount
         */
        public int getBlockTesseraVertexCount()
        {
            return myBlockTesseraVertexCount;
        }

        /**
         * Get the blockVertices.
         *
         * @return the blockVertices
         */
        public List<S> getBlockVertices()
        {
            return myBlockVertices;
        }

        /**
         * Get the next available empty vertex and increment the current index.
         *
         * @return The next available index.
         */
        protected int getNextIndex()
        {
            return myCurrentIndex++;
        }
    }
}
