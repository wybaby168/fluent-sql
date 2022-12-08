package group.flyfish.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 测试器
 *
 * @author wangyu
 */
public interface TestCase<T> {

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    void initialize() throws Exception;

    /**
     * 执行并获取结果
     *
     * @return 结果
     */
    T test();

    /**
     * 测试用例名称
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Name {

        String value();
    }
}
