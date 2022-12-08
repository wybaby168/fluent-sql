package group.flyfish.fluent.chain;

import group.flyfish.fluent.chain.common.AfterJoinSqlChain;
import group.flyfish.fluent.chain.common.HandleSqlChain;
import group.flyfish.fluent.chain.common.PreSqlChain;
import group.flyfish.fluent.chain.select.AfterOrderSqlChain;
import group.flyfish.fluent.chain.select.AfterWhereSqlChain;
import group.flyfish.fluent.chain.update.AfterSetSqlChain;
import group.flyfish.fluent.debug.FluentSqlDebugger;
import group.flyfish.fluent.entity.SQLEntity;
import group.flyfish.fluent.operations.FluentSQLOperations;
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
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询工具类
 *
 * @author wangyu
 */
final class SQLImpl extends ConcatSegment<SQLImpl> implements SQLOperations, PreSqlChain, HandleSqlChain, AfterJoinSqlChain, AfterSetSqlChain {

    // 共享的操作
    private static FluentSQLOperations SHARED_OPERATIONS;
    // 参数map，有序
    private final List<Object> parameters = new ArrayList<>();

    // 主表class，默认是第一个from的表为主表
    private Class<?> primaryClass;

    /**
     * 绑定实现类
     *
     * @param operations jdbc操作
     */
    public static void bind(FluentSQLOperations operations) {
        SHARED_OPERATIONS = operations;
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
        String linker = !segments.isEmpty() ? "," : "SELECT";
        if (fields != null && fields.length != 0) {
            return this.concat(linker)
                    .concat(() -> Arrays.stream(fields).map(SFunction::getSelect).collect(Collectors.joining(",")));
        }
        return this.concat(linker).concat("*");
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
        String mapped = AliasComposite.add(type, alias);
        return concat("FROM")
                .concat(() -> EntityNameUtils.getTableName(type))
                .concat(() -> SqlNameUtils.wrap(mapped));
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
        String mapped = AliasComposite.add(clazz, alias);
        return concat(type)
                .concat(() -> EntityNameUtils.getTableName(clazz))
                .concat(() -> SqlNameUtils.wrap(mapped));
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
     * 不带连接条件
     *
     * @return 处理链
     */
    @Override
    public HandleSqlChain then() {
        return this;
    }

    /**
     * 拼接查询条件
     *
     * @param query 条件
     * @return 结果
     */
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
                    .concat(() -> Arrays.stream(orders).map(SQLSegment::get).collect(Collectors.joining(",")));
        }
        return this;
    }


    @Override
    public <T> T one() {
        return one(SqlNameUtils.cast(primaryClass));
    }

    /**
     * 执行并获取结果
     *
     * @param clazz 结果类
     * @param <T>   泛型
     */
    public <T> T one(Class<T> clazz) {
        return SHARED_OPERATIONS.selectOne(entity(), clazz);
    }

    @Override
    public <T> List<T> list() {
        return list(SqlNameUtils.cast(primaryClass));
    }

    /**
     * 执行并获取多条结果
     *
     * @param clazz 结果类
     * @return 结果列表
     */
    @Override
    public <T> List<T> list(Class<T> clazz) {
        return SHARED_OPERATIONS.select(entity(), clazz);
    }

    /**
     * 执行并获取更新条数
     *
     * @return 更新条数
     */
    @Override
    public int execute() {
        return SHARED_OPERATIONS.execute(entity());
    }

    /**
     * 构建sql
     *
     * @return 构建结果
     */
    private String sql() {
        Assert.notNull(SHARED_OPERATIONS, "未指定执行数据源！");
        String sql = segments.stream().map(SQLSegment::get).collect(Collectors.joining(" "));
        if (FluentSqlDebugger.enabled()) {
            System.out.println("prepared sql: " + sql);
            System.out.println("prepared args:" + parameters.stream().map(ParameterUtils::convert).map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        AliasComposite.flush();
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
        return SQLEntity.of(this::sql, this::parsedParameters);
    }
}
