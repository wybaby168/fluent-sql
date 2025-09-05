package group.flyfish.fluent.chain;

import group.flyfish.fluent.chain.common.PreSqlChain;
import group.flyfish.fluent.update.Update;

/**
 * SQL操作
 */
public interface SQLOperations {

    /**
     * 查询起手（表达式/聚合）
     * 只能和使用字段起手的写法二选一
     *
     * @param segments 表达式/聚合
     * @return 预查询链
     */
    PreSqlChain select(SQLSegment... segments);

    /**
     * 更新起手
     *
     * @param clazz 具体表
     * @param <T>   泛型
     * @return 链式调用
     */
    <T> Update update(Class<T> clazz);
}
