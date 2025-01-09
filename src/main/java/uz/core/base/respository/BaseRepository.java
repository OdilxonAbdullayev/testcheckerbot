package uz.core.base.respository;

import uz.core.Constants;
import uz.core.base.entity.BaseEntity;
import uz.core.base.entity.DDLResponse;
import uz.core.logger.LogManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.htmlunit.websocket.client.Main;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseRepository<ENTITY extends BaseEntity, ID> {
    protected static SqlSessionFactory sqlSessionFactory;
    private static final LogManager _logger = new LogManager(BaseRepository.class);

    @Getter
    @Setter
    private String SQL_SELECT;
    @Getter
    @Setter
    private String SQL_UPDATE;
    @Getter
    @Setter
    private String SQL_INSERT;
    @Getter
    @Setter
    private String SQL_DELETE;


    public BaseRepository(String sqlSelect, String sqlUpdate, String sqlInsert, String sqlDelete) {
        SQL_SELECT = sqlSelect;
        SQL_UPDATE = sqlUpdate;
        SQL_INSERT = sqlInsert;
        SQL_DELETE = sqlDelete;
    }

    public BaseRepository(String sqlSelect, String sqlDelete, String sqlInsert) {
        SQL_SELECT = sqlSelect;
        SQL_DELETE = sqlDelete;
        SQL_INSERT = sqlInsert;
    }

    public BaseRepository(String sqlSelect) {
        this.SQL_SELECT = sqlSelect;
    }

    static {
        try {
            InputStream stream = Resources.getResourceAsStream(Main.class.getClassLoader(), "db/mybatis.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(stream);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public DDLResponse<List<ENTITY>> getList(Map<String, Object> filters) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            if (SQL_SELECT == null) {
                return new DDLResponse<>(false, 404, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.ERROR_SQL_SELECT);
            }

            _logger.info(SQL_SELECT + ": " + filters);
            List<ENTITY> data = session.selectList(SQL_SELECT, filters);
            session.commit();
            return new DDLResponse<>(true, 200, data, null, null);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return new DDLResponse<>(false, 500, null, Constants.ResponseMessage.SYSTEM_ERROR, "ERROR: " + e.getMessage());
        }
    }

    public DDLResponse<Optional<ENTITY>> getOne(Map<String, Object> filters) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            if (SQL_SELECT == null) {
                return new DDLResponse<>(false, 404, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.ERROR_SQL_SELECT);
            }

            _logger.info(SQL_SELECT + ": " + filters);
            Optional<ENTITY> data = Optional.ofNullable(session.selectOne(SQL_SELECT, filters));
            session.commit();
            return new DDLResponse<>(true, 200, data, null, null);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return new DDLResponse<>(false, 500, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.SYSTEM_ERROR + e.getMessage());
        }
    }

    public DDLResponse<ENTITY> insert(ENTITY entity) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            if (SQL_INSERT == null) {
                return new DDLResponse<>(false, 404, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.ERROR_SQL_INSERT);
            }

            _logger.info(SQL_INSERT + ": " + entity.toString());
            session.insert(SQL_INSERT, parse(entity));
            session.commit();
            return new DDLResponse<>(true, 200, entity, null, null);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return new DDLResponse<>(false, 500, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.SYSTEM_ERROR + e.getMessage());
        }
    }

    public DDLResponse<ENTITY> update(ENTITY entity) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            if (SQL_UPDATE == null) {
                return new DDLResponse<>(false, 404, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.ERROR_SQL_UPDATE);
            }

            _logger.info(SQL_UPDATE + ": " + entity.toString());
            session.update(SQL_UPDATE, parse(entity));
            session.commit();
            return new DDLResponse<>(true, 200, entity, null, null);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return new DDLResponse<>(false, 500, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.SYSTEM_ERROR + e.getMessage());
        }
    }


    public DDLResponse<ENTITY> delete(ID id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            if (SQL_DELETE == null) {
                return new DDLResponse<>(false, 404, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.ERROR_SQL_DELETE);
            }

            _logger.warning(SQL_DELETE + ": -> " + id);
            session.delete(SQL_DELETE, new HashMap<>() {{
                put("id", id);
            }});
            session.commit();
            return new DDLResponse<>(true, 200, null, null, null);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return new DDLResponse<>(false, 500, null, Constants.ResponseMessage.SYSTEM_ERROR, Constants.ResponseMessage.ERROR + e.getMessage());
        }
    }

    public Map<String, Object> parse(ENTITY entity) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = entity.getClass();

        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    map.put(field.getName(), field.get(entity));
                } catch (Exception e) {
                    _logger.error(e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }

        return map;
    }

}
