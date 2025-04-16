package group.flyfish.fluent.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.flyfish.fluent.binding.Alias;
import group.flyfish.fluent.binding.JSONInject;
import group.flyfish.fluent.utils.data.ObjectMappers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 特定类型的映射解释器
 *
 * @param <T> 泛型
 */
@Getter
@Slf4j
class MappingDescriptor<T> {

    private final ObjectMapper objectMapper = ObjectMappers.shared();

    /**
     * The class we are mapping to.
     */
    private Class<T> mappedClass;
    /**
     * Map of the fields we provide mapping for.
     */
    private final Map<String, PropertyDescriptor> mappedFields;

    /**
     * Map of the fields which need json convert
     */
    private final Map<String, Class<?>> jsonFields;

    static <T> MappingDescriptor<T> of(Class<T> mappedClass) {
        return new MappingDescriptor<>(mappedClass);
    }

    private MappingDescriptor(Class<T> mappedClass) {
        Assert.state(mappedClass != null, "Mapped class was not specified");
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<>();
        this.jsonFields = new HashMap<>();
        initialize();
    }

    private void initialize() {
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

    MappingBean<T> create() {
        MappingBean<T> bean = MappingBean.create(this);
        bean.setTransformer(this::convertPropertyIfNeed);
        bean.setPropertyTransformer(this::lowerCaseName);
        return bean;
    }

    boolean isPrimitive() {
        return ClassUtils.isPrimitiveOrWrapper(mappedClass);
    }

    /**
     * Remove the specified property from the mapped fields.
     *
     * @param propertyName the property name (as used by property descriptors)
     * @since 5.3.9
     */
    private void suppressProperty(String propertyName) {
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
    private String lowerCaseName(String name) {
        return StringUtils.delete(name, " ").toLowerCase(Locale.US);
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
    private String underscoreName(String name) {
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
     * Set the class that each row should be mapped to.
     */
    void setMappedClass(Class<T> mappedClass) {
        if (this.mappedClass == null) {
            this.mappedClass = mappedClass;
            initialize();
        } else {
            if (this.mappedClass != mappedClass) {
                throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
                        mappedClass + " since it is already providing mapping for " + this.mappedClass);
            }
        }
    }

    private Object convertPropertyIfNeed(String name, Object value) {
        if (jsonFields.containsKey(name)) {
            value = convert(value, jsonFields.get(name));
        }
        return value;
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
