package group.flyfish.fluent.operations;

import group.flyfish.fluent.entity.BoundSQLEntity;
import group.flyfish.fluent.entity.DataPage;
import group.flyfish.fluent.mapping.ReactiveSQLMappedRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
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
     * @return 查询结果
     */
    @Override
    public <T> Mono<T> selectOne(BoundSQLEntity<T> entity) {
        return forSelect(entity).one();

    }

    /**
     * 执行一条sql，并且查询出所有行
     *
     * @param entity sql实体
     * @return 返回的列表
     */
    @Override
    public <T> Flux<T> select(BoundSQLEntity<T> entity) {
        return forSelect(entity).all();
    }

    /**
     * 分页查询
     *
     * @param entity sql实体
     * @return 返回的分页对象
     */
    @Override
    public <T> Mono<DataPage<T>> selectPage(BoundSQLEntity<T> entity) {
        return Mono.empty();
    }

    /**
     * 直接执行sql，根据update count返回更新行数，如果是查询，永远返回0
     *
     * @param entity sql实体
     * @return 更新行数
     */
    @Override
    public <T> Mono<Integer> execute(BoundSQLEntity<T> entity) {
        return resolve(entity).fetch().rowsUpdated();
    }

    /**
     * 解析sql实体
     *
     * @param entity 实体信息
     * @return 结果
     */
    private <T> DatabaseClient.GenericExecuteSpec resolve(BoundSQLEntity<T> entity) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(entity);
        Object[] parameters = entity.getParameters();
        if (null != parameters) {
            for (int i = 0; i < parameters.length; i++) {
                spec = spec.bind(i, parameters[i]);
            }
        }
        return spec;
    }

    private <T> RowsFetchSpec<T> forSelect(BoundSQLEntity<T> entity) {
        return resolve(entity)
                .map(ReactiveSQLMappedRowMapper.newInstance(entity.getResultType()));
    }
}
