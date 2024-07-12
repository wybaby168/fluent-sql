package group.flyfish.fluent.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 基于SQL映射的行映射器
 *
 * @param <T> 响应实体泛型
 * @author wangyu
 */
@Slf4j
public class SQLMappedRowMapper<T> implements RowMapper<T> {

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
    public SQLMappedRowMapper(Class<T> mappedClass) {
        this.descriptor = MappingDescriptor.of(mappedClass);
    }

    /**
     * Static factory method to create a new {@code BeanPropertyRowMapper}.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public static <T> SQLMappedRowMapper<T> newInstance(Class<T> mappedClass) {
        return new SQLMappedRowMapper<>(mappedClass);
    }

    /**
     * Extract the values for all columns in the current row.
     * <p>Utilizes public setters and result set meta-data.
     *
     * @see java.sql.ResultSetMetaData
     */
    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        MappingBean<T> bean = descriptor.create();

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String column = JdbcUtils.lookupColumnName(rsmd, i);
            try {
                final int index = i;
                bean.setValue(column, type -> getColumnValue(rs, index, type));
            } catch (Exception ex) {
                throw new DataRetrievalFailureException(
                        "Unable to map column '" + column + "' to property '" + bean.getProperty(column) + "'", ex);
            }
        }

        return bean.get();
    }

    @Nullable
    protected Object getColumnValue(ResultSet rs, int index, Class<?> paramType) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, paramType);
    }

}
