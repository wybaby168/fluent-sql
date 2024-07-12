package group.flyfish.fluent.chain.execution;

/**
 * 绑定状态下的代理
 *
 * @author wangyu
 */
public interface BoundProxy<T> {

    /**
     * 阻塞的数据库操作
     *
     * @return 结果
     */
    BoundEntitySpec<T> block();

    /**
     * 异步数据库逻辑
     *
     * @return 结果
     */
    ReactiveBoundEntitySpec<T> reactive();

}
