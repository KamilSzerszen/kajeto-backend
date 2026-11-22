package pl.kajeto.jobs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BaseEntityTest {

  private static final class Sample extends BaseEntity {
    Sample(UUID id, TenantId owner) {
      super(id, owner);
    }
  }

  private final UUID id = UUID.randomUUID();
  private final TenantId owner = new TenantId(UUID.randomUUID());

  @Test
  void keepsIdAndOwnerFromConstructor() {
    Sample entity = new Sample(id, owner);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getOwnerId()).isEqualTo(owner.value());
    assertThat(entity.getVersion()).isNull();
    assertThat(entity.isDeleted()).isFalse();
  }

  @Test
  void requiresIdAndOwner() {
    assertThatThrownBy(() -> new Sample(null, owner)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new Sample(id, null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void onCreateSetsBothTimestampsToTheSameInstant() {
    Sample entity = new Sample(id, owner);

    entity.onCreate();

    assertThat(entity.getCreatedAt()).isNotNull().isEqualTo(entity.getUpdatedAt());
  }

  @Test
  void onUpdateMovesUpdatedAtButNotCreatedAt() throws InterruptedException {
    Sample entity = new Sample(id, owner);
    entity.onCreate();
    Instant created = entity.getCreatedAt();
    Thread.sleep(2);

    entity.onUpdate();

    assertThat(entity.getCreatedAt()).isEqualTo(created);
    assertThat(entity.getUpdatedAt()).isAfter(created);
  }

  @Test
  void markDeletedIsIdempotent() throws InterruptedException {
    Sample entity = new Sample(id, owner);

    entity.markDeleted();
    Instant first = entity.getDeletedAt();
    Thread.sleep(2);
    entity.markDeleted();

    assertThat(entity.isDeleted()).isTrue();
    assertThat(entity.getDeletedAt()).isEqualTo(first);
  }
}
