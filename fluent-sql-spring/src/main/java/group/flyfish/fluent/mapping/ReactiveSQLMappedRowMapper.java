package group.flyfish.fluent.mapping;

import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.function.BiFunction;

import static group.flyfish.fluent.utils.sql.SqlNameUtils.cast;

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
        if (descriptor.isPrimitive()) {
            for (ColumnMetadata metadata : rowMetadata.getColumnMetadatas()) {
                String column = metadata.getName();
                return cast(R2dbcUtils.getRowValue(row, column, descriptor.getMappedClass()));
            }
            return null;
        }

        MappingBean<T> bean = descriptor.create();

        for (ColumnMetadata metadata : rowMetadata.getColumnMetadatas()) {
            String column = metadata.getName();
            try {
                bean.setValue(column, type -> R2dbcUtils.getRowValue(row, column, type));
            } catch (Exception ex) {
                throw new DataRetrievalFailureException(
                        "Unable to map column '" + column + "' to property '" + bean.getProperty(column) + "'", ex);
            }
        }

        return bean.get();
    }
}
