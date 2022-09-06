package group.flyfish.fluent.chain;

import group.flyfish.fluent.chain.common.PreSqlChain;
import group.flyfish.fluent.update.Update;
import group.flyfish.fluent.utils.sql.SFunction;

/**
 * SQL操作
 */
public interface SQLOperations {

    /**
     * 查询起手
     *
     * @param fields 字段列表，不传代表所有字段
     * @param <T>    实体泛型
     * @return 预查询链
     */
    @SuppressWarnings("unchecked")
    <T> PreSqlChain select(SFunction<T, ?>... fields);

    /**
     * 更新起手
     *
     * @param clazz 具体表
     * @param <T>   泛型
     * @return 链式调用
     */
    <T> Update update(Class<T> clazz);
}
