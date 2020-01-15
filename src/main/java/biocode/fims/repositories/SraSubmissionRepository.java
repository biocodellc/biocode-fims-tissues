package biocode.fims.repositories;

import biocode.fims.models.SraSubmission;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This repositories provides CRUD operations for {@link biocode.fims.models.SraSubmission} objects
 */
@Transactional
public interface SraSubmissionRepository extends Repository<SraSubmission, Integer> {

    void save(SraSubmission submission);

    SraSubmission findOneById(int id);

    List<SraSubmission> getByStatus(SraSubmission.Status status);
}
