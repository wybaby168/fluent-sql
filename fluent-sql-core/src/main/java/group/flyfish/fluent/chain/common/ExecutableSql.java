package group.flyfish.fluent.chain.common;

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
}
