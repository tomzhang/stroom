/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;

import stroom.index.impl.db.tables.Rack;


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
public class RackRecord extends UpdatableRecordImpl<RackRecord> implements Record7<Integer, Byte, String, Long, String, Long, String> {

    private static final long serialVersionUID = 561444291;

    /**
     * Setter for <code>stroom.rack.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>stroom.rack.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>stroom.rack.ver</code>.
     */
    public void setVer(Byte value) {
        set(1, value);
    }

    /**
     * Getter for <code>stroom.rack.ver</code>.
     */
    public Byte getVer() {
        return (Byte) get(1);
    }

    /**
     * Setter for <code>stroom.rack.created_by</code>.
     */
    public void setCreatedBy(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>stroom.rack.created_by</code>.
     */
    public String getCreatedBy() {
        return (String) get(2);
    }

    /**
     * Setter for <code>stroom.rack.created_at</code>.
     */
    public void setCreatedAt(Long value) {
        set(3, value);
    }

    /**
     * Getter for <code>stroom.rack.created_at</code>.
     */
    public Long getCreatedAt() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>stroom.rack.updated_by</code>.
     */
    public void setUpdatedBy(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>stroom.rack.updated_by</code>.
     */
    public String getUpdatedBy() {
        return (String) get(4);
    }

    /**
     * Setter for <code>stroom.rack.updated_at</code>.
     */
    public void setUpdatedAt(Long value) {
        set(5, value);
    }

    /**
     * Getter for <code>stroom.rack.updated_at</code>.
     */
    public Long getUpdatedAt() {
        return (Long) get(5);
    }

    /**
     * Setter for <code>stroom.rack.name</code>.
     */
    public void setName(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>stroom.rack.name</code>.
     */
    public String getName() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<Integer, Byte, String, Long, String, Long, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<Integer, Byte, String, Long, String, Long, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Rack.RACK.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field2() {
        return Rack.RACK.VER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Rack.RACK.CREATED_BY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field4() {
        return Rack.RACK.CREATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Rack.RACK.UPDATED_BY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field6() {
        return Rack.RACK.UPDATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Rack.RACK.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte component2() {
        return getVer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getCreatedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component4() {
        return getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getUpdatedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component6() {
        return getUpdatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value2() {
        return getVer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getCreatedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value4() {
        return getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getUpdatedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value6() {
        return getUpdatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value2(Byte value) {
        setVer(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value3(String value) {
        setCreatedBy(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value4(Long value) {
        setCreatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value5(String value) {
        setUpdatedBy(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value6(Long value) {
        setUpdatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord value7(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RackRecord values(Integer value1, Byte value2, String value3, Long value4, String value5, Long value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RackRecord
     */
    public RackRecord() {
        super(Rack.RACK);
    }

    /**
     * Create a detached, initialised RackRecord
     */
    public RackRecord(Integer id, Byte ver, String createdBy, Long createdAt, String updatedBy, Long updatedAt, String name) {
        super(Rack.RACK);

        set(0, id);
        set(1, ver);
        set(2, createdBy);
        set(3, createdAt);
        set(4, updatedBy);
        set(5, updatedAt);
        set(6, name);
    }
}
