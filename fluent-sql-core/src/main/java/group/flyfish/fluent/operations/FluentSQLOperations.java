package group.flyfish.fluent.operations;

import group.flyfish.fluent.entity.BoundSQLEntity;
import group.flyfish.fluent.entity.DataPage;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * sql query操作
 *
 * @author wangyu
 */
public interface FluentSQLOperations {

    /**
     * 执行一条sql，并且序列化为对象
     * 注意，如果查询不止一条，该方法仅返回第一条数据
     * 如果没有结果，将返回null
     *
     * @param entity sql实体
     * @param <T>    目标泛型
     * @return 查询结果
     */
    @Nullable
    <T> T selectOne(BoundSQLEntity<T> entity);

    /**
     * 执行一条sql，并且查询出所有行
     *
     * @param entity sql实体
     * @param <T>    目标泛型
     * @return 返回的列表
     */
    <T> List<T> select(BoundSQLEntity<T> entity);

    /**
     * 分页查询
     *
     * @param entity sql实体
     * @param <T>    目标泛型
     * @return 返回的分页对象
     */
    <T> DataPage<T> selectPage(BoundSQLEntity<T> entity);

    /**
     * 直接执行sql，根据update count返回更新行数，如果是查询，永远返回0
     *
     * @param entity sql实体
     * @return 更新行数
     */
    <T> int execute(BoundSQLEntity<T> entity);
}
