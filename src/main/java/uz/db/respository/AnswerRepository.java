package uz.db.respository;

import lombok.Getter;
import uz.core.base.respository.BaseRepository;
import uz.core.logger.LogManager;
import uz.db.entity.AnswerEntity;

public class AnswerRepository extends BaseRepository<AnswerEntity, Long> {
    @Getter
    private static final AnswerRepository instance = new AnswerRepository();
    private static final LogManager _logger = new LogManager(AnswerRepository.class);

    private AnswerRepository() {
        super("selectAnswer",
                "updateAnswer",
                "insertAnswer",
                "deleteAnswer");
    }

}
