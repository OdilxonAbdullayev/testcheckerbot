package uz.db.respository;

import uz.core.base.respository.BaseRepository;
import uz.db.entity.AdminEntity;
import lombok.Getter;

public class AdminRepository extends BaseRepository<AdminEntity, Long> {
    @Getter
    public static final AdminRepository instance = new AdminRepository();

    private AdminRepository() {
        super(
                "selectAdmin",
                "updateAdmin",
                "insertAdmin",
                "deleteAdmin"
        );
    }

}
