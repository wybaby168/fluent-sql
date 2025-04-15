package group.flyfish.fluent.utils.sql;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.query.ConcatCandidate;
import group.flyfish.fluent.utils.context.AliasComposite;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 可连接的片段
 *
 * @author wangyu
 */
public abstract class ConcatSegment<T extends ConcatSegment<T>> implements SQLSegment {

    // 处理链集合
    protected final List<SQLSegment> segments = new ArrayList<>();


    protected final List<Consumer<Map<String, Object>>> consumers = new ArrayList<>();

    // 上下文
    private final Map<String, Object> ctx = new HashMap<>();

    @Override
    public String get() {
        try {
            // 提前消费上下文
            consumeCtx();
            // 开始拼接所有片段
            return segments.stream().map(SQLSegment::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));
        } finally {
            // 清理上下文
            ctx.clear();
            // 构建完成后，清空缓存对象
            AliasComposite.flush();
        }
    }

    /**
     * 设置上下文，本质上会添加一个空的片段用于生效上下文
     *
     * @param consumer 消费者
     * @return 结果
     */
    public T ctxPut(Consumer<Map<String, Object>> consumer) {
        consumers.add(consumer);
        return self();
    }

    /**
     * 获取上下文的值
     *
     * @param key 键
     * @return 具体的值
     */
    @SuppressWarnings("unchecked")
    public <R> R ctx(String key) {
        return (R) ctx.get(key);
    }

    /**
     * 拼接sql片段
     *
     * @param segment 片段
     * @return 链式调用
     */
    public T concat(SQLSegment segment) {
        if (segments.isEmpty() && segment instanceof ConcatCandidate) {
            return self();
        }
        this.segments.add(segment);
        return self();
    }

    /**
     * 拼接静态sql片段
     *
     * @param content 内容
     * @return 结果
     */
    public T concat(String content) {
        this.segments.add(new StaticSegment(content));
        return self();
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /**
     * 消费上下文
     */
    private void consumeCtx() {
        consumers.forEach(consumer -> consumer.accept(ctx));
    }

    private record StaticSegment(String content) implements SQLSegment {

        /**
         * @return 得到sql片段
         */
        @Override
        public String get() {
            return content;
        }
    }
}
