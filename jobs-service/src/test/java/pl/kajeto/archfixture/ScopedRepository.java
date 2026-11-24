package pl.kajeto.archfixture;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kajeto.jobs.domain.Job;

/** Fixture for the ArchUnit rule tests; deliberately outside the application base package. */
public interface ScopedRepository extends JpaRepository<Job, UUID> {

  Optional<Job> findByIdAndOwnerId(UUID id, UUID ownerId);
}
