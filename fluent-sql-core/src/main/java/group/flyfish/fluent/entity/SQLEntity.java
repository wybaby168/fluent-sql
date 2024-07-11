package group.flyfish.fluent.entity;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.function.Supplier;

/**
 * sql运行实体
 *
 * @param <T> 结果类型泛型
 * @author wangyu
 */
public class SQLEntity<T> {

    private static final Supplier<Object[]> EMPTY_PARAMETERS = () -> new Object[]{};

    // sql提供者
    @NonNull
    private final Supplier<String> sql;

    // sql参数表提供者
    private final Supplier<Object[]> parameters;

    // 结果类型
    @NonNull
    @Getter
    private final Class<T> resultType;

    private SQLEntity(Class<T> resultType, Supplier<String> sql, Supplier<Object[]> parameters) {
        this.resultType = resultType;
        this.sql = sql;
        this.parameters = parameters;
    }

    public static <T> SQLEntity<T> of(Class<T> resultType, Supplier<String> sqlProvider) {
        return of(resultType, sqlProvider, EMPTY_PARAMETERS);
    }

    public static <T> SQLEntity<T> of(Class<T> resultType, Supplier<String> sql, Supplier<Object[]> parameters) {
        return new SQLEntity<T>(resultType, sql, parameters);
    }

    public String getSql() {
        return sql.get();
    }

    public Object[] getParameters() {
        return parameters.get();
    }
}
