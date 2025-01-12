package uz.db.respository;

import uz.core.base.respository.BaseRepository;
import uz.core.logger.LogManager;
import uz.db.entity.UserEntity;
import lombok.Getter;


public class UserRepository extends BaseRepository<UserEntity, Long> {
    @Getter
    private static final UserRepository instance = new UserRepository();
    private static final LogManager _logger = new LogManager(UserRepository.class);

    private UserRepository() {
        super("selectUsers",
                "updateUser",
                "insertUser",
                "");
    }


}
