/*
 * This file is generated by jOOQ.
 */
package stroom.processor.impl.db.jooq.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import stroom.processor.impl.db.jooq.Indexes;
import stroom.processor.impl.db.jooq.Keys;
import stroom.processor.impl.db.jooq.Stroom;
import stroom.processor.impl.db.jooq.tables.records.ProcessorFeedRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProcessorFeed extends TableImpl<ProcessorFeedRecord> {

    private static final long serialVersionUID = -612792887;

    /**
     * The reference instance of <code>stroom.processor_feed</code>
     */
    public static final ProcessorFeed PROCESSOR_FEED = new ProcessorFeed();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProcessorFeedRecord> getRecordType() {
        return ProcessorFeedRecord.class;
    }

    /**
     * The column <code>stroom.processor_feed.id</code>.
     */
    public final TableField<ProcessorFeedRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>stroom.processor_feed.name</code>.
     */
    public final TableField<ProcessorFeedRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * Create a <code>stroom.processor_feed</code> table reference
     */
    public ProcessorFeed() {
        this(DSL.name("processor_feed"), null);
    }

    /**
     * Create an aliased <code>stroom.processor_feed</code> table reference
     */
    public ProcessorFeed(String alias) {
        this(DSL.name(alias), PROCESSOR_FEED);
    }

    /**
     * Create an aliased <code>stroom.processor_feed</code> table reference
     */
    public ProcessorFeed(Name alias) {
        this(alias, PROCESSOR_FEED);
    }

    private ProcessorFeed(Name alias, Table<ProcessorFeedRecord> aliased) {
        this(alias, aliased, null);
    }

    private ProcessorFeed(Name alias, Table<ProcessorFeedRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> ProcessorFeed(Table<O> child, ForeignKey<O, ProcessorFeedRecord> key) {
        super(child, key, PROCESSOR_FEED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Stroom.STROOM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PROCESSOR_FEED_NAME, Indexes.PROCESSOR_FEED_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ProcessorFeedRecord, Integer> getIdentity() {
        return Keys.IDENTITY_PROCESSOR_FEED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProcessorFeedRecord> getPrimaryKey() {
        return Keys.KEY_PROCESSOR_FEED_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProcessorFeedRecord>> getKeys() {
        return Arrays.<UniqueKey<ProcessorFeedRecord>>asList(Keys.KEY_PROCESSOR_FEED_PRIMARY, Keys.KEY_PROCESSOR_FEED_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessorFeed as(String alias) {
        return new ProcessorFeed(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessorFeed as(Name alias) {
        return new ProcessorFeed(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ProcessorFeed rename(String name) {
        return new ProcessorFeed(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ProcessorFeed rename(Name name) {
        return new ProcessorFeed(name, null);
    }
}
