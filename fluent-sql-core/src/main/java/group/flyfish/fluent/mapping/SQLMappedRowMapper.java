package group.flyfish.fluent.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.flyfish.fluent.binding.Alias;
import group.flyfish.fluent.binding.JSONInject;
import group.flyfish.fluent.utils.data.ObjectMappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.*;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 基于SQL映射的行映射器
 *
 * @param <T> 响应实体泛型
 * @author wangyu
 */
@Slf4j
public class SQLMappedRowMapper<T> implements RowMapper<T> {

    private final ObjectMapper objectMapper = ObjectMappers.shared();

    /**
     * The class we are mapping to.
     */
    @Nullable
    private Class<T> mappedClass;
    /**
     * ConversionService for binding JDBC values to bean properties.
     */
    @Nullable
    private ConversionService conversionService = DefaultConversionService.getSharedInstance();
    /**
     * Map of the fields we provide mapping for.
     */
    @Nullable
    private Map<String, PropertyDescriptor> mappedFields;
    /**
     * Map of the fields which need json convert
     */
    private Map<String, Class<?>> jsonFields;

    /**
     * Create a new {@code BeanPropertyRowMapper}, accepting unpopulated
     * properties in the target bean.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public SQLMappedRowMapper(Class<T> mappedClass) {
        initialize(mappedClass);
    }

    /**
     * Static factory method to create a new {@code BeanPropertyRowMapper}.
     *
     * @param mappedClass the class that each row should be mapped to
     * @see #newInstance(Class, ConversionService)
     */
    public static <T> SQLMappedRowMapper<T> newInstance(Class<T> mappedClass) {
        return new SQLMappedRowMapper<>(mappedClass);
    }

    /**
     * Static factory method to create a new {@code BeanPropertyRowMapper}.
     *
     * @param mappedClass       the class that each row should be mapped to
     * @param conversionService the {@link ConversionService} for binding
     *                          JDBC values to bean properties, or {@code null} for none
     * @see #newInstance(Class)
     * @see #setConversionService
     * @since 5.2.3
     */
    public static <T> SQLMappedRowMapper<T> newInstance(
            Class<T> mappedClass, @Nullable ConversionService conversionService) {

        SQLMappedRowMapper<T> rowMapper = newInstance(mappedClass);
        rowMapper.setConversionService(conversionService);
        return rowMapper;
    }

    /**
     * Get the class that we are mapping to.
     */
    @Nullable
    public final Class<T> getMappedClass() {
        return this.mappedClass;
    }

    /**
     * Set the class that each row should be mapped to.
     */
    public void setMappedClass(Class<T> mappedClass) {
        if (this.mappedClass == null) {
            initialize(mappedClass);
        } else {
            if (this.mappedClass != mappedClass) {
                throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
                        mappedClass + " since it is already providing mapping for " + this.mappedClass);
            }
        }
    }

    /**
     * Return a {@link ConversionService} for binding JDBC values to bean properties,
     * or {@code null} if none.
     *
     * @since 4.3
     */
    @Nullable
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    /**
     * Set a {@link ConversionService} for binding JDBC values to bean properties,
     * or {@code null} for none.
     * <p>Default is a {@link DefaultConversionService}, as of Spring 4.3. This
     * provides support for {@code java.time} conversion and other special types.
     *
     * @see #initBeanWrapper(BeanWrapper)
     * @since 4.3
     */
    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Initialize the mapping meta-data for the given class.
     *
     * @param mappedClass the mapped class
     */
    protected void initialize(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<>();
        this.jsonFields = new HashMap<>();

        Map<String, MergedAnnotations> fieldAnnotations = new HashMap<>();
        ReflectionUtils.doWithFields(mappedClass, field -> fieldAnnotations.put(field.getName(), MergedAnnotations.from(field)));

        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
            if (pd.getWriteMethod() != null) {
                MergedAnnotations annotations = fieldAnnotations.get(pd.getName());
                String lowerCaseName;
                if (annotations.isPresent(Alias.class)) {
                    String rawName = annotations.get(Alias.class).synthesize().value();
                    lowerCaseName = lowerCaseName(rawName.replace("_", ""));
                } else {
                    lowerCaseName = lowerCaseName(pd.getName());
                }
                this.mappedFields.put(lowerCaseName, pd);
                String underscoreName = underscoreName(pd.getName());
                if (!lowerCaseName.equals(underscoreName)) {
                    this.mappedFields.put(underscoreName, pd);
                }
                // 添加json字段
                if (annotations.isPresent(JSONInject.class)) {
                    this.jsonFields.put(pd.getName(), pd.getPropertyType());
                }
            }
        }
    }

    /**
     * Remove the specified property from the mapped fields.
     *
     * @param propertyName the property name (as used by property descriptors)
     * @since 5.3.9
     */
    protected void suppressProperty(String propertyName) {
        if (this.mappedFields != null) {
            this.mappedFields.remove(lowerCaseName(propertyName));
            this.mappedFields.remove(underscoreName(propertyName));
        }
    }

    /**
     * Convert the given name to lower case.
     * By default, conversions will happen within the US locale.
     *
     * @param name the original name
     * @return the converted name
     * @since 4.2
     */
    protected String lowerCaseName(String name) {
        return name.toLowerCase(Locale.US);
    }

    /**
     * Convert a name in camelCase to an underscored name in lower case.
     * Any upper case letters are converted to lower case with a preceding underscore.
     *
     * @param name the original name
     * @return the converted name
     * @see #lowerCaseName
     * @since 4.2
     */
    protected String underscoreName(String name) {
        if (!StringUtils.hasLength(name)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(name.charAt(0)));
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Extract the values for all columns in the current row.
     * <p>Utilizes public setters and result set meta-data.
     *
     * @see java.sql.ResultSetMetaData
     */
    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        BeanWrapperImpl bw = new BeanWrapperImpl();
        initBeanWrapper(bw);

        T mappedObject = constructMappedInstance(rs, bw);
        bw.setBeanInstance(mappedObject);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            String field = lowerCaseName(StringUtils.delete(column, " "));
            PropertyDescriptor pd = (this.mappedFields != null ? this.mappedFields.get(field) : null);
            if (pd != null) {
                try {
                    Object value = getColumnValue(rs, index, pd);
                    if (rowNumber == 0 && log.isDebugEnabled()) {
                        log.debug("Mapping column '" + column + "' to property '" + pd.getName() +
                                "' of type '" + ClassUtils.getQualifiedName(pd.getPropertyType()) + "'");
                    }
                    if (jsonFields.containsKey(pd.getName())) {
                        value = convert(value, jsonFields.get(pd.getName()));
                    }
                    bw.setPropertyValue(pd.getName(), value);
                } catch (NotWritablePropertyException ex) {
                    throw new DataRetrievalFailureException(
                            "Unable to map column '" + column + "' to property '" + pd.getName() + "'", ex);
                }
            }
        }

        return mappedObject;
    }

    /**
     * Construct an instance of the mapped class for the current row.
     *
     * @param rs the ResultSet to map (pre-initialized for the current row)
     * @param tc a TypeConverter with this RowMapper's conversion service
     * @return a corresponding instance of the mapped class
     * @throws SQLException if an SQLException is encountered
     * @since 5.3
     */
    protected T constructMappedInstance(ResultSet rs, TypeConverter tc) throws SQLException {
        Assert.state(this.mappedClass != null, "Mapped class was not specified");
        return BeanUtils.instantiateClass(this.mappedClass);
    }

    /**
     * Initialize the given BeanWrapper to be used for row mapping.
     * To be called for each row.
     * <p>The default implementation applies the configured {@link ConversionService},
     * if any. Can be overridden in subclasses.
     *
     * @param bw the BeanWrapper to initialize
     * @see #getConversionService()
     * @see BeanWrapper#setConversionService
     */
    protected void initBeanWrapper(BeanWrapper bw) {
        ConversionService cs = getConversionService();
        if (cs != null) {
            bw.setConversionService(cs);
        }
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>The default implementation delegates to
     * {@link #getColumnValue(ResultSet, int, Class)}.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @param pd    the bean property that each result object is expected to match
     * @return the Object value
     * @throws SQLException in case of extraction failure
     * @see #getColumnValue(ResultSet, int, Class)
     */
    @Nullable
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>The default implementation calls
     * {@link JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)}.
     * Subclasses may override this to check specific value types upfront,
     * or to post-process values return from {@code getResultSetValue}.
     *
     * @param rs        is the ResultSet holding the data
     * @param index     is the column index
     * @param paramType the target parameter type
     * @return the Object value
     * @throws SQLException in case of extraction failure
     * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)
     * @since 5.3
     */
    @Nullable
    protected Object getColumnValue(ResultSet rs, int index, Class<?> paramType) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, paramType);
    }

    /**
     * 转换json对象
     *
     * @param type  目标类型
     * @param value 值
     * @return 结果
     */
    private Object convert(Object value, Class<?> type) {
        if (value instanceof String) {
            try {
                return objectMapper.readValue((String) value, type);
            } catch (JsonProcessingException e) {
                log.error("转换json为对象时出错！{}", e.getMessage());
                return null;
            }
        }
        return value;
    }
}
