package group.flyfish.fluent.query;

import group.flyfish.fluent.utils.context.AliasComposite;
import group.flyfish.fluent.utils.sql.SFunction;
import group.flyfish.fluent.utils.sql.SqlNameUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * 基于字符串列名的条件
 */
class StringCondition implements Condition {

    private final String column;

    private final Function<Condition, Query> callback;

    private Object value;

    private ConditionCandidate candidate;

    public StringCondition(String column, SimpleQuery query) {
        this(column, query::and);
    }

    public StringCondition(String column, Function<Condition, Query> callback) {
        this.column = column;
        this.callback = callback;
    }

    @Override
    @Nullable
    public Collection<Object> getParameters() {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        if (value instanceof Collection) {
            return castCollection(value);
        }
        if (value instanceof SFunction) {
            return Collections.emptyList();
        }
        return Collections.singletonList(value);
    }

    @Override
    public String get() {
        String qualified = qualify(column);
        String compiled = candidate.compile(qualified, value);
        if (value instanceof SFunction) {
            return compiled.replace("?", ((SFunction<?, ?>) value).getName());
        }
        return compiled;
    }

    private String qualify(String raw) {
        // 已携带点号前缀（可能是表或别名），直接按原样包裹最后一段
        if (raw.contains(".")) {
            String[] parts = raw.split("\\.", 2);
            String left = parts[0];
            String right = parts[1];
            // 如果左侧是已知别名或表名，则直接使用
            if (AliasComposite.has(left) || AliasComposite.findAliasByTable(left).isPresent()) {
                String alias = AliasComposite.has(left) ? AliasComposite.get(left) : AliasComposite.findAliasByTable(left).get();
                return alias + "." + SqlNameUtils.wrap(right);
            }
            // 否则不强加限定，按用户写法包裹右侧
            return left + "." + SqlNameUtils.wrap(right);
        }
        // 未携带前缀，若上下文只有一个表别名，则使用该别名限定
        return AliasComposite.singleAlias().map(a -> a + "." + SqlNameUtils.wrap(raw))
                .orElse(SqlNameUtils.wrap(raw));
    }

    @Override
    public Query eq(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.EQ;
        return callback.apply(this);
    }

    @Override
    public <T> Query eq(SFunction<T, ?> ref) {
        this.value = ref;
        this.candidate = ConditionCandidate.EQ;
        return callback.apply(this);
    }

    @Override
    public Query like(String pattern) {
        this.value = pattern;
        this.candidate = ConditionCandidate.LIKE;
        return callback.apply(this);
    }

    @Override
    public Query gt(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.GT;
        return callback.apply(this);
    }

    @Override
    public Query gte(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.GTE;
        return callback.apply(this);
    }

    @Override
    public Query lt(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.LT;
        return callback.apply(this);
    }

    @Override
    public Query lte(Object value) {
        this.value = value;
        this.candidate = ConditionCandidate.LTE;
        return callback.apply(this);
    }

    @Override
    public Query notNull() {
        this.value = null;
        this.candidate = ConditionCandidate.NOT_NULL;
        return callback.apply(this);
    }

    @Override
    public Query isNull() {
        this.value = null;
        this.candidate = ConditionCandidate.IS_NULL;
        return callback.apply(this);
    }

    @Override
    public Query in(Collection<?> collection) {
        this.value = collection;
        this.candidate = ConditionCandidate.IN;
        return callback.apply(this);
    }

    @SuppressWarnings("unchecked")
    private static <T> Collection<T> castCollection(Object obj) {
        return (Collection<T>) obj;
    }
}


