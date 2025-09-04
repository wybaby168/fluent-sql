package group.flyfish.fluent.chain;

import group.flyfish.fluent.chain.common.AfterJoinSqlChain;
import group.flyfish.fluent.chain.common.HandleSqlChain;
import group.flyfish.fluent.chain.common.PreSqlChain;
import group.flyfish.fluent.chain.execution.BoundEntitySpec;
import group.flyfish.fluent.chain.execution.BoundProxy;
import group.flyfish.fluent.chain.execution.ReactiveBoundEntitySpec;
import group.flyfish.fluent.chain.select.AfterOrderSqlChain;
import group.flyfish.fluent.chain.select.AfterWhereSqlChain;
import group.flyfish.fluent.chain.select.PieceSqlChain;
import group.flyfish.fluent.chain.update.AfterSetSqlChain;
import group.flyfish.fluent.debug.FluentSqlDebugger;
import group.flyfish.fluent.entity.BoundSQLEntity;
import group.flyfish.fluent.entity.DataPage;
import group.flyfish.fluent.entity.SQLEntity;
import group.flyfish.fluent.operations.FluentSQLOperations;
import group.flyfish.fluent.operations.ReactiveFluentSQLOperations;
import group.flyfish.fluent.query.JoinCandidate;
import group.flyfish.fluent.query.Parameterized;
import group.flyfish.fluent.query.Query;
import group.flyfish.fluent.update.Update;
import group.flyfish.fluent.update.UpdateImpl;
import group.flyfish.fluent.utils.context.AliasComposite;
import group.flyfish.fluent.utils.data.ParameterUtils;
import group.flyfish.fluent.utils.sql.ConcatSegment;
import group.flyfish.fluent.utils.sql.EntityNameUtils;
import group.flyfish.fluent.utils.sql.SFunction;
import group.flyfish.fluent.utils.sql.SqlNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static group.flyfish.fluent.utils.cache.CachedWrapper.wrap;
import java.util.Map;

/**
 * 查询工具类
 *
 * @author wangyu
 */
final class SQLImpl extends ConcatSegment<SQLImpl> implements SQLOperations, PreSqlChain, HandleSqlChain, AfterJoinSqlChain, AfterSetSqlChain {

    // 共享的操作
    private static FluentSQLOperations SHARED_OPERATIONS;

    // 共享的异步操作
    private static ReactiveFluentSQLOperations SHARED_REACTIVE_OPERATIONS;

    // 是否正在计数
    private final AtomicBoolean counting = new AtomicBoolean(false);

    // 参数map，有序
    private final List<Object> parameters = new ArrayList<>();

    // 为select字段存放的集合
    private final List<SQLSegment> selections = new ArrayList<>();

    // 主表class，默认是第一个from的表为主表
    private Class<?> primaryClass;

    // sql实体引用，区分计数sql
    private final Supplier<SQLEntity> entityRef = wrap(this::entity, counting::get);

    // 参数引用
    private final Supplier<Object[]> parametersRef = wrap(this::parsedParameters);

    /**
     * 绑定实现类
     *
     * @param operations jdbc操作
     */
    public static void bind(FluentSQLOperations operations) {
        SHARED_OPERATIONS = operations;
    }

    /**
     * 绑定实现类
     *
     * @param operations r2dbc操作
     */
    public static void bind(ReactiveFluentSQLOperations operations) {
        SHARED_REACTIVE_OPERATIONS = operations;
    }

    /**
     * 查询起手
     *
     * @param fields 字段列表，不传代表所有字段
     * @return 预查询链
     */
    @SafeVarargs
    @Override
    public final <T> PreSqlChain select(SFunction<T, ?>... fields) {
        // 追加字段
        if (fields != null && fields.length > 0) {
            if (!selections.isEmpty()) {
                selections.add(() -> ",");
            }
            selections.add(() -> Arrays.stream(fields)
                    .map(SFunction::getSelect).collect(Collectors.joining(",")));
        }
        // 首个项，添加SELECT
        if (segments.isEmpty()) {
            return this.concat("SELECT");
        }
        return this;
    }

    /**
     * 更新起手
     *
     * @param clazz 具体表
     * @return 链式调用
     */
    @Override
    public <T> Update update(Class<T> clazz) {
        return new UpdateImpl(update -> {
            if (withoutParameter(update)) return this;
            return this.concat("UPDATE")
                    .concat(() -> EntityNameUtils.getTableName(clazz))
                    .concat("SET")
                    .concat(update);
        });
    }

    /**
     * 从表里查
     *
     * @param type 类型
     * @return 链式调用
     */
    public HandleSqlChain from(Class<?> type) {
        return from(type, null);
    }

    /**
     * 从字符串表查询
     *
     * @param table 表名
     * @return 链式调用
     */
    public HandleSqlChain from(String table) {
        return from(table, null);
    }

    /**
     * 从指定表查询，附加别名
     * 该接口适用于同一张表多次from的情况，可以从自表进行多次查询
     * 大部分情况下，您都不需要指定别名
     *
     * @param type  类型
     * @param alias 别名
     * @return 处理环节
     */
    @Override
    public HandleSqlChain from(Class<?> type, String alias) {
        this.primaryClass = type;
        String key = type.getCanonicalName();
        return this
                .ctxPut(ctx -> ctx.put(key, AliasComposite.add(type, alias)))
                .concat(this::applySelections)
                .concat("FROM")
                .concat(() -> EntityNameUtils.getTableName(type))
                .concat(() -> SqlNameUtils.wrap(this.ctx(key)));
    }

    /**
     * 从字符串表查询，附加别名
     *
     * @param table 表名（未包裹反引号）
     * @param alias 别名
     * @return 处理环节
     */
    public HandleSqlChain from(String table, String alias) {
        this.primaryClass = Map.class; // 对于字符串表，结果映射通常需要 as(Class) 指定
        String key = table;
        return this
                .ctxPut(ctx -> ctx.put(key, AliasComposite.add(table, alias)))
                .concat(this::applySelections)
                .concat("FROM")
                .concat(() -> SqlNameUtils.wrap(table))
                .concat(() -> SqlNameUtils.wrap(this.ctx(key)));
    }

    /**
     * 添加选择项的逻辑
     */
    private String applySelections() {
        // 判断渲染模式
        if (counting.get()) {
            // 设置后立即回正
            counting.set(false);
            return "COUNT(1)";
        }
        if (selections.isEmpty()) {
            // 选择项为空，查询全部字段
            return "*";
        } else {
            // 选择项不为空，查询指定字段
            return selections.stream().map(SQLSegment::get).collect(Collectors.joining());
        }
    }

    /**
     * 全量的join连接支持
     *
     * @param type  连接类型
     * @param clazz 目标表
     * @param alias 别名
     * @return join后的操作
     */
    @Override
    public AfterJoinSqlChain join(JoinCandidate type, Class<?> clazz, String alias) {
        String key = clazz.getCanonicalName();
        return ctxPut(ctx -> ctx.put(key, AliasComposite.add(clazz, alias)))
                .concat(type)
                .concat(() -> EntityNameUtils.getTableName(clazz))
                .concat(() -> SqlNameUtils.wrap(ctx(key)));
    }

    @Override
    public AfterJoinSqlChain join(JoinCandidate type, String table, String alias) {
        String key = table;
        return ctxPut(ctx -> ctx.put(key, AliasComposite.add(table, alias)))
                .concat(type)
                .concat(() -> SqlNameUtils.wrap(table))
                .concat(() -> SqlNameUtils.wrap(ctx(key)));
    }

    /**
     * 连接条件
     *
     * @param query 查询
     * @return 处理链
     */
    @Override
    public HandleSqlChain on(Query query) {
        if (withoutParameter(query)) return this;
        return concat("ON").concat(query);
    }

    /**
     * 下一步操作
     *
     * @return 结果
     */
    @Override
    public HandleSqlChain then() {
        return this;
    }

    /**
     * 不带连接条件
     *
     * @return 处理链
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> BoundProxy<T> fetch() {
        // 通过主类构建实体
        return new DefaultBoundProxy<>((Class<T>) primaryClass);
    }

    /**
     * 拼接查询条件
     *
     * @param query 条件
     * @return 结果
     */
    @Override
    public AfterWhereSqlChain matching(Query query) {
        if (withoutParameter(query)) return this;
        return concat("WHERE").concat(query);
    }

    /**
     * 拼接排序条件
     *
     * @param orders 排序
     * @return 结果
     */
    @Override
    public AfterOrderSqlChain order(Order... orders) {
        if (null != orders && orders.length != 0) {
            return concat("ORDER BY")
                    .concat(() -> Arrays.stream(orders).map(SQLSegment::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(",")));
        }
        return this;
    }


    /**
     * 获取实体，做下一步的事情
     *
     * @return 结果
     */
    @Override
    public <T> BoundProxy<T> as(Class<T> type) {
        return new DefaultBoundProxy<>(type);
    }

    /**
     * 构建sql
     *
     * @return 构建结果
     */
    private String sql() {
        String sql = this.get();
        // 拼接sql
        if (FluentSqlDebugger.enabled()) {
            System.out.println("prepared sql: " + sql);
            System.out.println("prepared args:" + parameters.stream().map(ParameterUtils::convert).map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        return sql;
    }

    /**
     * 解析后的参数
     *
     * @return 结果
     */
    private Object[] parsedParameters() {
        return parameters.stream().map(ParameterUtils::convert).toArray();
    }

    /**
     * 没有参数值
     *
     * @param params 参数
     * @return 为true，代表没有参数，不添加该片段
     */
    private boolean withoutParameter(Parameterized params) {
        if (params.isEmpty() || null == params.getParameters()) {
            return true;
        }
        parameters.addAll(params.getParameters());
        return false;
    }

    /**
     * 将本实体转换为sql实体
     *
     * @return 转换结果
     */
    private SQLEntity entity() {
        return SQLEntity.of(wrap(this::sql), parametersRef);
    }

    @Override
    public PieceSqlChain limit(int count) {
        return concat("LIMIT").concat(String.valueOf(count));
    }

    @Override
    public PieceSqlChain offset(int rows) {
        return concat("OFFSET").concat(String.valueOf(rows));
    }

    @RequiredArgsConstructor
    private class DefaultBoundProxy<T> implements BoundProxy<T> {

        private final Class<T> type;

        @Override
        public BoundEntitySpec<T> block() {
            return new DefaultBoundEntitySpec<>(type);
        }

        @Override
        public ReactiveBoundEntitySpec<T> reactive() {
            return new DefaultReactiveBoundEntitySpec<>(type);
        }
    }

    /**
     * 默认的绑定实体
     *
     * @param <T>
     */
    private class DefaultBoundEntitySpec<T> implements BoundEntitySpec<T> {

        private final BoundSQLEntity<T> entity;

        private DefaultBoundEntitySpec(Class<T> type) {
            Assert.notNull(SHARED_OPERATIONS, "未指定执行数据源！");
            this.entity = BoundSQLEntity.of(entityRef, type);
        }

        @Override
        public T one() {
            return SHARED_OPERATIONS.selectOne(entity);
        }

        @Override
        @NonNull
        public List<T> all() {
            return SHARED_OPERATIONS.select(entity);
        }

        /**
         * 忽略查询字段，查询当前条件下的数量
         *
         * @return 数量
         */
        @Override
        public int count() {
            counting.set(true);
            Integer result = SHARED_OPERATIONS.selectOne(BoundSQLEntity.of(entityRef, Integer.class));
            return null == result ? 0 : result;
        }

        @Override
        @NonNull
        public DataPage<T> page(DataPage<T> page) {
            int count = count();
            List<T> list = SHARED_OPERATIONS.select(entity.paged(page));
            page.setTotal(count);
            page.setList(list);
            return page;
        }

        @Override
        public int execute() {
            return SHARED_OPERATIONS.execute(entity);
        }
    }

    /**
     * 默认的异步绑定实体
     *
     * @param <T> 泛型
     */
    private class DefaultReactiveBoundEntitySpec<T> implements ReactiveBoundEntitySpec<T> {

        private final BoundSQLEntity<T> entity;

        private DefaultReactiveBoundEntitySpec(Class<T> type) {
            Assert.notNull(SHARED_REACTIVE_OPERATIONS, "未指定执行数据源！");
            this.entity = BoundSQLEntity.of(entityRef, type);
        }

        @Override
        public Mono<T> one() {
            return SHARED_REACTIVE_OPERATIONS.selectOne(entity);
        }

        @Override
        @NonNull
        public Flux<T> all() {
            return SHARED_REACTIVE_OPERATIONS.select(entity);
        }

        /**
         * 忽略查询字段，查询数量
         *
         * @return 按当前sql执行户的条数
         */
        @Override
        public Mono<Integer> count() {
            counting.set(true);
            return SHARED_REACTIVE_OPERATIONS.selectOne(BoundSQLEntity.of(entityRef, Integer.class));
        }

        /**
         * 分页查询
         *
         * @param page 分页对象
         * @return 返回的分页对象
         */
        @Override
        @NonNull
        public Mono<DataPage<T>> page(DataPage<T> page) {
            return count().flatMapMany(count -> {
                        page.setTotal(count);
                        return SHARED_REACTIVE_OPERATIONS.select(entity.paged(page));
                    })
                    .collectList()
                    .map(list -> {
                        page.setList(list);
                        return page;
                    });
        }

        @Override
        public Mono<Long> execute() {
            return SHARED_REACTIVE_OPERATIONS.execute(entity);
        }
    }
}
