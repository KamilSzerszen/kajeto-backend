package pl.kajeto.archfixture;

import java.util.UUID;
import pl.kajeto.jobs.domain.Job;

/** Fixture: reads by id only, so tenant A could read tenant B's row. The rule must reject it. */
public class ViolatingService {

  private final ScopedRepository repository;

  public ViolatingService(ScopedRepository repository) {
    this.repository = repository;
  }

  public Job load(UUID id) {
    return repository.findById(id).orElseThrow();
  }

  public long total() {
    return repository.count();
  }
}
