package group.flyfish.fluent.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.text.MessageFormat;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BoundSQLEntity<T> implements Supplier<String> {

    @NonNull
    private final Supplier<SQLEntity> entity;

    // 结果类型
    @NonNull
    @Getter
    private final Class<T> resultType;

    // 末尾sql，用于支持分页
    private Supplier<String> lastSql;

    public static <T> BoundSQLEntity<T> of(Supplier<SQLEntity> entity, Class<T> resultType) {
        return new BoundSQLEntity<>(entity, resultType);
    }

    @Override
    public String get() {
        return getSql();
    }

    public BoundSQLEntity<T> paged(DataPage<T> page) {
        BoundSQLEntity<T> cloned = new BoundSQLEntity<>(entity, resultType);
        cloned.lastSql = () -> MessageFormat.format("LIMIT {0} OFFSET {1}", page.getSize(), (page.getPage() - 1) * page.getSize());
        return cloned;
    }

    public String getSql() {
        String built = entity.get().getSql();
        if (null != lastSql) {
            return built + " " + lastSql.get();
        }
        return built;
    }

    public Object[] getParameters() {
        return entity.get().getParameters();
    }
}
