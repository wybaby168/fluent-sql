package group.flyfish.fluent.query;

import group.flyfish.fluent.chain.SQLSegment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JoinCandidate implements SQLSegment {

    INNER_JOIN("内连接", "INNER JOIN"),
    LEFT_JOIN("左连接", "LEFT JOIN"),
    RIGHT_JOIN("右连接", "RIGHT JOIN");

    private final String name;

    private final String content;

    /**
     * @return 得到sql片段
     */
    @Override
    public String get() {
        return content;
    }
}
