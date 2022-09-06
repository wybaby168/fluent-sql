package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.chain.common.ExecutableSql;

import java.util.List;

/**
 * order做完后支持的操作
 *
 * @author wangyu
 */
public interface AfterOrderSqlChain extends ExecutableSql {

    /**
     * 执行并获取结果
     *
     * @param clazz 结果类
     * @param <T>   泛型
     * @return 单一结果值
     */
    <T> T one(Class<T> clazz);

    /**
     * 执行并获取多条结果
     *
     * @param clazz 结果类
     * @param <T>   结果泛型
     * @return 结果列表
     */
    <T> List<T> list(Class<T> clazz);

}
