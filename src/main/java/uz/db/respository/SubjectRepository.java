package uz.db.respository;

import lombok.Getter;
import uz.core.base.respository.BaseRepository;
import uz.core.logger.LogManager;
import uz.db.entity.SubjectEntity;

public class SubjectRepository extends BaseRepository<SubjectEntity, Long> {
    @Getter
    private static final SubjectRepository instance = new SubjectRepository();
    private static final LogManager _logger = new LogManager(SubjectRepository.class);

    private SubjectRepository() {
        super("selectSubject",
                "updateSubject",
                "insertSubject",
                "deleteSubject");
    }


}
