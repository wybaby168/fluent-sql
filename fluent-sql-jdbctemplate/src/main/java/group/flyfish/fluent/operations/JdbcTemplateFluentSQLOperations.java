package group.flyfish.fluent.operations;

import group.flyfish.fluent.chain.SQL;
import group.flyfish.fluent.entity.SQLEntity;
import group.flyfish.fluent.mapping.SQLMappedRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.ClassUtils;

import java.util.List;

/**
 * jdbc template实现的查询操作
 *
 * @author wangyu
 */
public class JdbcTemplateFluentSQLOperations implements FluentSQLOperations {

    private final JdbcOperations jdbcOperations;

    /**
     * 实例化 + 自动绑定
     *
     * @param operations spring jdbc template
     */
    public JdbcTemplateFluentSQLOperations(JdbcOperations operations) {
        this.jdbcOperations = operations;
        SQL.bind(this);
    }

    /**
     * 执行一条sql，并且序列化为对象
     * 注意，如果查询不止一条，该方法仅返回第一条数据
     * 如果没有结果，将返回null
     *
     * @param entity sql实体
     * @param clazz  目标类型
     * @return 查询结果
     */
    @Override
    @SuppressWarnings("all")
    public <T> T selectOne(SQLEntity entity, Class<T> clazz) {
        try {
            String sql = entity.getSql().concat(" limit 1");
            if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
                return jdbcOperations.queryForObject(sql, clazz, entity.getParameters());
            }
            return jdbcOperations.queryForObject(sql, new SQLMappedRowMapper<>(clazz), entity.getParameters());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 执行一条sql，并且查询出所有行
     *
     * @param entity sql实体
     * @param clazz  目标类型
     * @return 返回的列表
     */
    @Override
    public <T> List<T> select(SQLEntity entity, Class<T> clazz) {
        return jdbcOperations.query(entity.getSql(), new SQLMappedRowMapper<>(clazz), entity.getParameters());
    }

    /**
     * 直接执行sql，根据update count返回更新行数，如果是查询，永远返回0
     *
     * @param entity sql实体
     * @return 更新行数
     */
    @Override
    public int execute(SQLEntity entity) {
        return jdbcOperations.update(entity.getSql(), entity.getParameters());
    }
}
