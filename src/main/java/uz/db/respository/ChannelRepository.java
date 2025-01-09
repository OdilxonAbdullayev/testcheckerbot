package uz.db.respository;

import uz.core.base.respository.BaseRepository;
import uz.db.entity.ChannelEntity;
import lombok.Getter;


public class ChannelRepository extends BaseRepository<ChannelEntity, Long> {
    @Getter
    private static final ChannelRepository instance = new ChannelRepository();

    private ChannelRepository() {
        super(
                "selectChannel",
                "deleteChannel",
                "insertChannel"
        );
    }
}
