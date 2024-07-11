package group.flyfish.fluent.chain.common;

import group.flyfish.fluent.entity.SQLEntity;

/**
 * 可执行的sql
 *
 * @author wangyu
 */
public interface ExecutableSql {

    /**
     * 执行并获取更新条数
     *
     * @return 更新条数
     */
    int execute();

    /**
     * 转换为SQL实体
     *
     * @return 结果
     */
    SQLEntity toEntity();
}
