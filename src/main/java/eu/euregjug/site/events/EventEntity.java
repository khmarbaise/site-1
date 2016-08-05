/*
 * Copyright 2015-2016 EuregJUG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.euregjug.site.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.euregjug.site.posts.PostEntity;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents events for the EuregJUG.
 *
 * @author Michael J. Simons, 2015-12-26
 */
@Entity
@Table(
        name = "events",
        uniqueConstraints = {
            @UniqueConstraint(name = "events_uk", columnNames = {"held_on", "name"})
        }
)
@JsonInclude(NON_NULL)
@EqualsAndHashCode(of = {"heldOn", "name"})
public class EventEntity implements Serializable {

    private static final long serialVersionUID = 2005305860095134425L;

    /**
     * Types of events
     */
    public enum Type {

        talk, meetup
    }

    /**
     * Primary key of this event.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Getter(onMethod = @__(@JsonProperty))
    private Integer id;

    /**
     * Date and time when this event will be held or was held.
     */
    @Column(name = "held_on", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Getter
    private Calendar heldOn;

    /**
     * Name of this event. Must be unique on a given {@link #heldOn date}.
     */
    @Column(name = "name", length = 512, nullable = false)
    @NotBlank
    @Size(max = 512)
    @Getter
    private String name;

    /**
     * Description of this event.
     */
    @Column(name = "description", length = 2048, nullable = false)
    @NotBlank
    @Size(max = 2048)
    @Getter @Setter
    private String description;

    /**
     * A flag if a guest needs to register for this event. Defaults to
     * {@code false}.
     */
    @Column(name = "needs_registration", nullable = false)
    @Getter @Setter
    private boolean needsRegistration = false;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @NotNull
    @Getter @Setter
    private Type type = Type.talk;

    /**
     * Optional duration in minutes.
     */
    @Column(name = "duration")
    @Getter @Setter
    private Integer duration;

    /**
     * The speaker on that event (Optional).
     */
    @Column(name = "speaker", length = 256)
    @Size(max = 256)
    @Getter @Setter
    private String speaker;

    /**
     * Location of this event, usually an unstructured address.
     */
    @Column(name = "location", length = 2048)
    @Size(max = 2048)
    @Getter @Setter
    private String location;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    @JsonIgnore
    @Getter(onMethod = @__(@JsonProperty)) @Setter(onMethod = @__(@JsonIgnore))
    private PostEntity post;

    /**
     * Creation date of this post.
     */
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    @Getter
    private Calendar createdAt;

    /**
     * Needed for Hibernate, not to be called by application code.
     */
    @SuppressWarnings({"squid:S2637"})
    protected EventEntity() {
    }

    /**
     * Creates a new Event on the given {@link #heldOn date} with the name
     * {@code name}.
     *
     * @param heldOn Date for the new event (can be in the past).
     * @param name Name for the new event.
     * @param description Description for the new event.
     */
    public EventEntity(final Calendar heldOn, final String name, final String description) {
        this.heldOn = heldOn;
        this.name = name;
        this.description = description;
    }

    @PrePersist
    @PreUpdate
    void prePersistAndUpdate() {
        if (this.createdAt == null) {
            this.createdAt = Calendar.getInstance();
        }
    }

    /**
     * @return True if the event is still open for registration
     */
    @JsonIgnore
    public boolean isOpen() {
        return this.heldOn.after(Calendar.getInstance());
    }
}
