package uz.db.respository;

import uz.core.base.respository.BaseRepository;
import uz.core.logger.LogManager;
import uz.db.entity.UserAnswer;
import uz.db.entity.UserEntity;
import lombok.Getter;


public class UserAnswerRepository extends BaseRepository<UserAnswer, Long> {
    @Getter
    private static final UserAnswerRepository instance = new UserAnswerRepository();
    private static final LogManager _logger = new LogManager(UserAnswerRepository.class);

    private UserAnswerRepository() {
        super("selectUserAnswers",
                "updateUserAnswers",
                "insertUserAnswers",
                "");
    }


}

