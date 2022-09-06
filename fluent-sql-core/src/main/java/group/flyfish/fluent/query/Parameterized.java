package group.flyfish.fluent.query;

import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * 表示带参数的类
 *
 * @author wangyu
 */
public interface Parameterized {

    /**
     * 获取当前对象包含的参数
     * 返回空集合，代表不需要参数
     * 返回null，代表参数为空，不处理该条件或查询
     *
     * @return 结果
     */
    @Nullable
    Collection<Object> getParameters();

    /**
     * 是否为空，参数为null则为空
     *
     * @return 结果
     */
    default boolean isEmpty() {
        return null == getParameters();
    }
}
