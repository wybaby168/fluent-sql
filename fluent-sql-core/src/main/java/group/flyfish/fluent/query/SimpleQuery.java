package group.flyfish.fluent.query;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.utils.sql.ConcatSegment;
import group.flyfish.fluent.utils.sql.SFunction;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static group.flyfish.fluent.query.ConcatCandidate.AND;
import static group.flyfish.fluent.query.ConcatCandidate.OR;

/**
 * 查询构建器api
 *
 * @author wangyu
 */
class SimpleQuery extends ConcatSegment<SimpleQuery> implements Query {

    // 参数源
    private final Collection<Object> parameters = new ArrayList<>();

    /**
     * @return 得到sql片段
     */
    @Override
    public String get() {
        return segments.stream().map(SQLSegment::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    /**
     * 获取参数源
     *
     * @return 结果
     */
    @Override
    @Nullable
    public Collection<Object> getParameters() {
        if (segments.isEmpty()) {
            return null;
        }
        return parameters;
    }

    /**
     * 以与连接下一个条件
     *
     * @param getter 字段lambda
     * @return 构建操作
     */
    @Override
    public <T> Condition and(SFunction<T, ?> getter) {
        return new SimpleCondition(getter, this::and);
    }

    /**
     * 直接连接其他条件
     *
     * @param condition 其他条件
     * @return 查询操作
     */
    @Override
    public Query and(Condition condition) {
        if (condition.isEmpty()) {
            return this;
        }
        addParameters(condition);
        return concat(AND).concat(condition);
    }

    /**
     * 嵌套其他查询条件
     *
     * @param query 查询条件
     * @return 结果
     */
    @Override
    public Query and(Query query) {
        if (query.isEmpty()) {
            return this;
        }
        addParameters(query);
        return concat(AND).concat(query);
    }

    /**
     * 以或连接下一个条件
     *
     * @param getter 字段lambda
     * @return 构建操作
     */
    @Override
    public <T> Condition or(SFunction<T, ?> getter) {
        return new SimpleCondition(getter, this::or);
    }

    /**
     * 以或直接连接其他条件
     *
     * @param condition 其他条件
     * @return 查询操作
     */
    @Override
    public Query or(Condition condition) {
        if (condition.isEmpty()) {
            return this;
        }
        addParameters(condition);
        return concat(OR).concat(condition);
    }

    /**
     * 以或嵌套其他查询条件
     *
     * @param query 查询条件
     * @return 结果
     */
    @Override
    public Query or(Query query) {
        if (query.isEmpty()) {
            return this;
        }
        addParameters(query);
        return concat(OR).concat(query);
    }

    /**
     * 添加参数
     *
     * @param parameterized 带参数的对象
     */
    private void addParameters(Parameterized parameterized) {
        Collection<Object> params = parameterized.getParameters();
        if (!CollectionUtils.isEmpty(params)) {
            // 优先拼接参数
            parameters.addAll(params);
        }
    }
}
