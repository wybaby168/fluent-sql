package group.flyfish.fluent.utils.sql;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.query.ConcatCandidate;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 可连接的片段
 *
 * @author wangyu
 */
public abstract class ConcatSegment<T extends ConcatSegment<T>> {

    // 处理链集合
    protected final List<SQLSegment> segments = new ArrayList<>();

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

    @RequiredArgsConstructor
    private static class StaticSegment implements SQLSegment {

        private final String content;

        /**
         * @return 得到sql片段
         */
        @Override
        public String get() {
            return content;
        }
    }
}
