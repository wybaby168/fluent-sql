package group.flyfish.fluent.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BoundSQLEntity<T> implements Supplier<String> {

    @NonNull
    private final Supplier<SQLEntity> entity;

    // 结果类型
    @NonNull
    @Getter
    private final Class<T> resultType;

    public static <T> BoundSQLEntity<T> of(Supplier<SQLEntity> entity, Class<T> resultType) {
        return new BoundSQLEntity<>(entity, resultType);
    }

    @Override
    public String get() {
        return entity.get().getSql();
    }

    public String getSql() {
        return entity.get().getSql();
    }

    public Object[] getParameters() {
        return entity.get().getParameters();
    }
}
