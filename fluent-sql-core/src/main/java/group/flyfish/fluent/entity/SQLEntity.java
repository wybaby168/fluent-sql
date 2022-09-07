package group.flyfish.fluent.entity;

import java.util.function.Supplier;

/**
 * sql运行实体
 *
 * @author wangyu
 */
public class SQLEntity {

    private static final Supplier<Object[]> EMPTY_PARAMETERS = () -> new Object[]{};

    // sql提供者
    private Supplier<String> sqlProvider;

    // sql参数表提供者
    private Supplier<Object[]> parametersProvider;

    public static SQLEntity of(Supplier<String> sqlProvider) {
        return of(sqlProvider, EMPTY_PARAMETERS);
    }

    public static SQLEntity of(Supplier<String> sqlProvider, Supplier<Object[]> parametersProvider) {
        SQLEntity entity = new SQLEntity();
        entity.sqlProvider = sqlProvider;
        entity.parametersProvider = parametersProvider;
        return entity;
    }

    public String getSql() {
        return sqlProvider.get();
    }

    public Object[] getParameters() {
        return parametersProvider.get();
    }
}
