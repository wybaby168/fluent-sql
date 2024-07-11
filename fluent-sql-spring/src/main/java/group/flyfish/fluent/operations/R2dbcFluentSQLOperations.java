package group.flyfish.fluent.operations;

import group.flyfish.fluent.entity.DataPage;
import group.flyfish.fluent.entity.SQLEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class R2dbcFluentSQLOperations implements ReactiveFluentSQLOperations {

    private final DatabaseClient databaseClient;

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
    public <T> Mono<T> selectOne(SQLEntity entity, Class<T> clazz) {
        return null;
    }

    /**
     * 执行一条sql，并且查询出所有行
     *
     * @param entity sql实体
     * @param clazz  目标类型
     * @return 返回的列表
     */
    @Override
    public <T> Flux<T> select(SQLEntity entity, Class<T> clazz) {
        return null;
    }

    /**
     * 分页查询
     *
     * @param entity sql实体
     * @param clazz  目标类型
     * @return 返回的分页对象
     */
    @Override
    public <T> Mono<DataPage<T>> selectPage(SQLEntity entity, Class<T> clazz) {
        return null;
    }

    /**
     * 直接执行sql，根据update count返回更新行数，如果是查询，永远返回0
     *
     * @param entity sql实体
     * @return 更新行数
     */
    @Override
    public Mono<Integer> execute(SQLEntity entity) {
        return null;
    }
}
