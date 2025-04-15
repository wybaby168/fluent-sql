package group.flyfish.fluent.chain.execution;

import group.flyfish.fluent.entity.DataPage;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 已经绑定的异步实体
 *
 * @author wangyu
 */
public interface ReactiveBoundEntitySpec<T> {

    /**
     * 执行一条sql，并且序列化为对象
     * 注意，如果查询不止一条，该方法仅返回第一条数据
     * 如果没有结果，将返回null
     *
     * @return 查询结果
     */
    @Nullable
    Mono<T> one();

    /**
     * 执行一条sql，并且查询出所有行
     *
     * @return 返回的列表
     */
    @NonNull
    Flux<T> all();

    /**
     * 忽略查询字段，查询数量
     *
     * @return 按当前sql执行户的条数
     */
    Mono<Integer> count();

    /**
     * 分页查询
     *
     * @return 返回的分页对象
     */
    @NonNull
    Mono<DataPage<T>> page(DataPage<T> page);

    /**
     * 直接执行sql，根据update count返回更新行数，如果是查询，永远返回0
     *
     * @return 更新行数
     */
    Mono<Long> execute();
}
