package pl.kajeto.archfixture;

import java.util.UUID;
import pl.kajeto.jobs.domain.Job;

/** Fixture: always scopes by owner. The rule must accept it. */
public class CompliantService {

  private final ScopedRepository repository;

  public CompliantService(ScopedRepository repository) {
    this.repository = repository;
  }

  public Job load(UUID id, UUID ownerId) {
    return repository.findByIdAndOwnerId(id, ownerId).orElseThrow();
  }
}
