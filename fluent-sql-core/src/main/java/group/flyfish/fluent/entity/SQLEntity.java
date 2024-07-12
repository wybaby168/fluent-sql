package group.flyfish.fluent.entity;

import org.springframework.lang.NonNull;

import java.util.function.Supplier;

/**
 * sql运行实体
 *
 * @author wangyu
 */
public class SQLEntity {

    private static final Supplier<Object[]> EMPTY_PARAMETERS = () -> new Object[]{};

    // sql提供者
    @NonNull
    private final Supplier<String> sql;

    // sql参数表提供者
    private final Supplier<Object[]> parameters;

    private SQLEntity(Supplier<String> sql, Supplier<Object[]> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public static SQLEntity of(Supplier<String> sqlProvider) {
        return of(sqlProvider, EMPTY_PARAMETERS);
    }

    public static SQLEntity of(Supplier<String> sql, Supplier<Object[]> parameters) {
        return new SQLEntity(sql, parameters);
    }

    public String getSql() {
        return sql.get();
    }

    public Object[] getParameters() {
        return parameters.get();
    }
}
