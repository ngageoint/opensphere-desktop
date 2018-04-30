package io.opensphere.merge.model;

import java.util.LinkedList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.util.collections.New;

/**
 * Model used to persist layer join configurations as XML. In this model, the
 * DataTypeInfo instances are represented by their type keys, which means that
 * they can be instantiated and used (to some extent) without the layers being
 * loaded or active.
 */
@XmlRootElement
public class MergePrefs
{
    /** List o' Join. */
    @XmlElement
    private final List<Join> joins = New.list();

    /** The merge models. */
    @XmlTransient
    private final ObservableList<MergeModel> myMerges = FXCollections
            .synchronizedObservableList(FXCollections.observableArrayList());

    /**
     * Getter.
     *
     * @return the list of join configs
     */
    public List<Join> getJoins()
    {
        return joins;
    }

    /**
     * Gets the merges.
     *
     * @return the merges
     */
    public ObservableList<MergeModel> getMerges()
    {
        return myMerges;
    }

    /**
     * Delete the named join configuration, if it exists.
     *
     * @param name join name
     */
    public void delete(String name)
    {
        joins.removeIf(j -> j.name.equals(name));
    }

    /**
     * Include a join config represented by a JoinModel.
     *
     * @param m the JoinModel
     * @return the created join
     */
    public Join addJoinModel(JoinModel m)
    {
        // remove any existing join with the same name
        delete(m.getJoinName());
        // now include the new one
        Join join = createJoin(m);
        joins.add(join);

        return join;
    }

    /**
     * Replace the existing Join with one newly created from the JoinModel.
     *
     * @param j old Join
     * @param m the new JoinModel
     * @return the new Join
     */
    public Join editJoinModel(Join j, JoinModel m)
    {
        Join newJ = createJoin(m);
        int k = joins.indexOf(j);
        if (k == -1)
        {
            joins.add(newJ);
        }
        else
        {
            joins.set(k, newJ);
        }
        return newJ;
    }

    /**
     * Create a Join from a JoinModel.
     *
     * @param m the JoinModel
     * @return the Join
     */
    private Join createJoin(JoinModel m)
    {
        Join j = new Join();
        j.name = m.getJoinName();
        j.useExact = m.isUseExact();
        for (JoinModel.Rec r : m.getParams())
        {
            LayerParam lp = new LayerParam();
            lp.primary = r.primary;
            lp.typeKey = r.type.getTypeKey();
            lp.column = r.column;
            j.params.add(lp);
        }
        return j;
    }

    /** Represents a single join config. */
    @XmlType
    public static class Join
    {
        /** Join name. */
        @XmlAttribute
        public String name;

        /** Exact match flag. */
        @XmlAttribute
        public boolean useExact;

        /** List of layer parameters. */
        @XmlElement
        public List<LayerParam> params = new LinkedList<>();
    }

    /** Parameters for a layer within a Join. */
    @XmlType
    public static class LayerParam
    {
        /** Identifier for the layer. */
        @XmlAttribute
        public String typeKey;

        /** Name of the join column. */
        @XmlAttribute
        public String column;

        /** Indicator of whether this is the primary layer. */
        @XmlAttribute
        public boolean primary;
    }
}
