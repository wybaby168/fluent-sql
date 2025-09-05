package group.flyfish.fluent.query;

import group.flyfish.fluent.utils.sql.EntityNameUtils;
import group.flyfish.fluent.utils.sql.SFunction;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * 查询条件
 *
 * @author wangyu
 */
class SimpleCondition implements Condition {

    private final SFunction<?, ?> target;

    private final Function<Condition, Query> callback;

    private Object value;

    private ConditionCandidate candidate;

    public SimpleCondition(SFunction<?, ?> target, SimpleQuery query) {
        this(target, query::and);
    }

    public SimpleCondition(SFunction<?, ?> target, Function<Condition, Query> callback) {
        this.target = target;
        this.callback = callback;
    }

    @Override
    @Nullable
    public Collection<Object> getParameters() {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        if (value instanceof Collection) {
            return cast(value);
        }
        if (value instanceof SFunction) {
            return Collections.emptyList();
        }
        return Collections.singletonList(value);
    }

    /**
     * @return 得到sql片段
     */
    @Override
    public String get() {
        String compiled = candidate.compile(target.getName(), value);
        // 值属于引用时，替换参数为引用
        if (value instanceof SFunction) {
            return compiled.replace("?", EntityNameUtils.toName(cast(value)));
        }
        return compiled;
    }

    /**
     * 等于条件
     *
     * @param value 值
     * @return 查询链
     */
    @Override
    public Query eq(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.EQ;
        return callback.apply(this);
    }

    /**
     * 等于另外一个字段
     *
     * @param ref 引用
     * @return 查询链
     */
    @Override
    public <T> Query eq(SFunction<T, ?> ref) {
        this.value = ref;
        this.candidate = ConditionCandidate.EQ;
        return callback.apply(this);
    }

    /**
     * 模糊查询
     *
     * @param pattern 关键字
     * @return 查询链
     */
    @Override
    public Query like(String pattern) {
        this.value = pattern;
        this.candidate = ConditionCandidate.LIKE;
        return callback.apply(this);
    }

    /**
     * 大于
     * @param value 值
     * @return 查询链
     */
    @Override
    public Query gt(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.GT;
        return callback.apply(this);
    }

    /**
     * 大于等于
     * @param value 值
     * @return 查询链
     */
    @Override
    public Query gte(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.GTE;
        return callback.apply(this);
    }

    /**
     * 小于
     * @param value 值
     * @return 查询链
     */
    @Override
    public Query lt(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.LT;
        return callback.apply(this);
    }

    /**
     * 小于等于
     * @param value 值
     * @return 查询链
     */
    @Override
    public Query lte(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.LTE;
        return callback.apply(this);
    }

    /**
     * 不为空
     * @return 查询链
     */
    @Override
    public Query notNull() {
        this.value = null;
        this.candidate = ConditionCandidate.NOT_NULL;
        return callback.apply(this);
    }

    /**
     * 为空
     * @return 查询链
     */
    @Override
    public Query isNull() {
        this.value = null;
        this.candidate = ConditionCandidate.IS_NULL;
        return callback.apply(this);
    }

    /**
     * 在集合内
     *
     * @param collection 任意内容集合
     * @return 查询链
     */
    @Override
    public Query in(Collection<?> collection) {
        this.value = collection;
        this.candidate = ConditionCandidate.IN;
        return callback.apply(this);
    }
}
