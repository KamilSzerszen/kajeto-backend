package pl.kajeto.jobs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Common columns of every business table: client-generated UUID, owner (tenant), optimistic lock
 * version, timestamps and a soft-delete marker. Prepared for offline synchronization.
 *
 * <p>Subclasses add {@code @SQLRestriction("deleted_at IS NULL")} so deleted rows are invisible.
 */
@MappedSuperclass
public abstract class BaseEntity {

  @Id private UUID id;

  @Column(name = "owner_id", nullable = false, updatable = false)
  private UUID ownerId;

  /**
   * Wrapper type on purpose: {@code null} tells Spring Data the entity is new (INSERT, no SELECT).
   */
  @Version private Long version;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected BaseEntity() {}

  protected BaseEntity(UUID id, TenantId owner) {
    this.id = Objects.requireNonNull(id, "id");
    this.ownerId = Objects.requireNonNull(owner, "owner").value();
  }

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /** Soft delete: the row stays so that synchronization knows what disappeared. */
  public void markDeleted() {
    if (deletedAt == null) {
      this.deletedAt = Instant.now();
    }
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public Long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }
}
