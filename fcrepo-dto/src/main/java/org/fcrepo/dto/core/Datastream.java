
package org.fcrepo.dto.core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Datastream within a {@link FedoraObject}.
 *
 * @see <a href="package-summary.html#working">Working With DTO Classes</a>
 */
public class Datastream extends FedoraDTO {

    private final SortedSet<DatastreamVersion> versions = new TreeSet<>(new DSVComparator());

    private final String id;

    private State state;

    private ControlGroup controlGroup;

    private Boolean versionable;

    /**
     * Creates an empty instance with an id. An id is the only required field of a datastream. It is immutable and must
     * be provided at construction time.
     *
     * @param id the id of the datastream (not null, immutable), which will be string-normalized.
     * @throws NullPointerException if the normalized id is <code>null</code>.
     */
    public Datastream(final String id) {
        this.id = Util.normalize(id);
        if (this.id == null) { throw new NullPointerException(); }
    }

    /**
     * Creates an instance based on the current state of this one.
     *
     * @return a deep copy.
     */
    public Datastream copy() {
        final Datastream copy = new Datastream(id).state(state).controlGroup(controlGroup).versionable(versionable);
        for (final DatastreamVersion version : versions) {
            copy.versions().add(version.copy());
        }
        return copy;
    }

    /**
     * Creates an instance based on the current state of this one, but with a different id.
     *
     * @param id the datastream id
     * @return a deep copy.
     */
    public Datastream copy(final String id) {
        final Datastream copy = new Datastream(id).state(state).controlGroup(controlGroup).versionable(versionable);
        for (final DatastreamVersion version : versions) {
            copy.versions().add(version.copy());
        }
        return copy;
    }

    /**
     * Gets the id.
     *
     * @return the value, never <code>null</code>.
     */
    public String id() {
        return id;
    }

    /**
     * Gets the state.
     *
     * @return the state, possibly <code>null</code>.
     */
    public State state() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public Datastream state(final State state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the control group.
     *
     * @return the value, possibly <code>null</code>.
     */
    public ControlGroup controlGroup() {
        return controlGroup;
    }

    /**
     * Sets the control group.
     *
     * @param controlGroup the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public Datastream controlGroup(final ControlGroup controlGroup) {
        this.controlGroup = controlGroup;
        return this;
    }

    /**
     * Gets the versionable value.
     *
     * @return the value, possibly <code>null</code>.
     */
    public Boolean versionable() {
        return versionable;
    }

    /**
     * Sets the versionable value.
     *
     * @param versionable the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public Datastream versionable(final Boolean versionable) {
        this.versionable = versionable;
        return this;
    }

    /**
     * Gets the (mutable) set of datastream versions for this datastream. Iterators over the elements of the set will
     * provide the values in order:
     * <ul>
     * <li>First, any datastreams whose creation date is undefined will be provided in ascending order of their ids.
     * </li>
     * <li>Then, any datastreams whose creation date is defined will be provided in descending order of dates. If
     * multiple datastreams have the same creation date, they will occur in ascending order of their ids.</li>
     * </ul>
     *
     * @return the set, possibly empty, never <code>null</code>.
     */
    public SortedSet<DatastreamVersion> versions() {
        return versions;
    }

    /**
     * Creates and adds a new datastream version with an automatically generated id that is unique within the existing
     * versions. The id will start with <code>this.id() + "."</code> and have a numeric suffix.
     *
     * @param createdDate the created date to use for the new datastream version, possibly <code>null</code>.
     * @return the new version.
     */
    public DatastreamVersion addVersion(final LocalDateTime createdDate) {
        int n = versions.size();
        while (hasVersion(id + "." + n)) {
            n++;
        }
        final DatastreamVersion dsv = new DatastreamVersion(id + "." + n, createdDate);
        versions.add(dsv);
        return dsv;
    }

    private boolean hasVersion(final String id) {
        for (final DatastreamVersion dsv : versions) {
            if (dsv.id().equals(id)) return true;
        }
        return false;
    }

    @Override
    Object[] getEqArray() {
        return new Object[] {id, state, controlGroup, versionable, versions};
    }

    private static class DSVComparator implements Comparator<DatastreamVersion>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(final DatastreamVersion a, final DatastreamVersion b) {
            final LocalDateTime aDate = a.createdDate();
            final LocalDateTime bDate = b.createdDate();
            if (aDate == null) {
                if (bDate == null) { return b.id().compareTo(a.id()); }
                return -1;
            }
            if (bDate == null) { return 1; }
            return bDate.compareTo(aDate);
        }
    }

}
