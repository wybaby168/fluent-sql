package group.flyfish.fluent.mapping;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.function.BiFunction;

/**
 * 异步支持的sql映射，支持json映射
 *
 * @param <T> 泛型
 */
public class ReactiveSQLMappedRowMapper<T> implements BiFunction<Row, RowMetadata, T> {

    /**
     * the actual logic for conversation
     */
    private final MappingDescriptor<T> descriptor;

    /**
     * Create a new {@code BeanPropertyRowMapper}, accepting unpopulated
     * properties in the target bean.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    private ReactiveSQLMappedRowMapper(Class<T> mappedClass) {
        this.descriptor = MappingDescriptor.of(mappedClass);
    }

    /**
     * Static factory method to create a new {@code BeanPropertyRowMapper}.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public static <T> ReactiveSQLMappedRowMapper<T> newInstance(Class<T> mappedClass) {
        return new ReactiveSQLMappedRowMapper<>(mappedClass);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param row         the first function argument
     * @param rowMetadata the second function argument
     * @return the function result
     */
    @Override
    public T apply(Row row, RowMetadata rowMetadata) {
        MappingBean<T> bean = descriptor.create();

        for (String column : rowMetadata.getColumnNames()) {
            try {
                bean.setValue(column, type -> row.get(column, type));
            } catch (Exception ex) {
                throw new DataRetrievalFailureException(
                        "Unable to map column '" + column + "' to property '" + bean.getProperty(column) + "'", ex);
            }
        }

        return bean.get();
    }
}
