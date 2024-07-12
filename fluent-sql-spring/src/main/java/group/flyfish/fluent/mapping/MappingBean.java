package group.flyfish.fluent.mapping;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 映射中的bean
 */
@Setter
@Slf4j
class MappingBean<T> {

    private final T instance;

    private final MappingDescriptor<T> descriptor;

    private final BeanWrapper bw;

    private boolean logged;

    private Supplier<ConversionService> conversionService = DefaultConversionService::getSharedInstance;

    private UnaryOperator<String> propertyTransformer = UnaryOperator.identity();

    private BiFunction<String, Object, Object> transformer = (column, value) -> value;

    /**
     * 创建实例
     *
     * @return 结果
     */
    static <T> MappingBean<T> create(MappingDescriptor<T> descriptor) {
        T instance = BeanUtils.instantiateClass(descriptor.getMappedClass());
        return new MappingBean<>(instance, descriptor);
    }

    private MappingBean(T instance, MappingDescriptor<T> descriptor) {
        this.instance = instance;
        this.descriptor = descriptor;
        this.bw = new BeanWrapperImpl(instance);
        initBeanWrapper(bw);
    }

    /**
     * 初始化bean wrapper
     *
     * @param bw bean包装器
     */
    protected void initBeanWrapper(BeanWrapper bw) {
        ConversionService cs = conversionService.get();
        if (cs != null) {
            bw.setConversionService(cs);
        }
    }

    /**
     * 设置bean的值
     *
     * @param column 属性名
     * @param value  值
     */
    void setValue(String column, ValueProvider value) throws Exception {
        String findName = getProperty(column);
        if (StringUtils.hasText(findName)) {
            PropertyDescriptor pd = descriptor.getMappedFields().get(findName);
            if (null == pd) return;
            String name = pd.getName();
            if (!logged) {
                logged = true;
                if (log.isDebugEnabled()) {
                    log.debug("Mapping column '{}' to property '{}' of type '{}'", column, name,
                            ClassUtils.getQualifiedName(pd.getPropertyType()));
                }
            }
            // 尝试获取值
            Object mappedValue = transformer.apply(name, value.get(pd.getPropertyType()));
            // 尝试设置
            bw.setPropertyValue(name, mappedValue);
        }
    }

    /**
     * 获取真正的列名
     *
     * @param column 列
     * @return 结果
     */
    String getProperty(String column) {
        return propertyTransformer.apply(column);
    }

    /**
     * 获取设置后的实体
     *
     * @return 结果
     */
    T get() {
        return instance;
    }
}
