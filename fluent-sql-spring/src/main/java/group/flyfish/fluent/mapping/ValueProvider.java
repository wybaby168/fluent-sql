package group.flyfish.fluent.mapping;

/**
 * 值提供者
 *
 * @author wangyu
 */
interface ValueProvider {

    /**
     * 通过具体类型获取值
     *
     * @param type 类型
     * @return 结果
     */
    Object get(Class<?> type) throws Exception;
}
