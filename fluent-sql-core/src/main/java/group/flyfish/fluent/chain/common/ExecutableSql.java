package group.flyfish.fluent.chain.common;

import group.flyfish.fluent.chain.execution.BoundProxy;

/**
 * 可执行的sql
 *
 * @author wangyu
 */
public interface ExecutableSql {

    /**
     * 进入下一步，以主表作为输出结果
     *
     * @param <T> 泛型
     * @return 绑定操作
     */
    <T> BoundProxy<T> next();
}
