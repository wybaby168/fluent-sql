package group.flyfish.fluent.query;

import group.flyfish.fluent.utils.text.TemplateCompiler;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查询条件候选模板
 *
 * @author wangyu
 */
@Getter
enum ConditionCandidate {

    EQ("字段等于值", "{column} = ?"),
    NE("字段不等于值", "{column} != ?"),
    GT("字段大于值", "{column} > ?"),
    GTE("字段大于等于值", "{column} >= ?"),
    LT("字段小于值", "{column} < ?"),
    LTE("字段小于等于值", "{column} <= ?"),
    LIKE("字段模糊匹配值", "{column} LIKE CONCAT('%', ?, '%')"),
    LIKE_LEFT("字段匹配左半部分值", "{column} LIKE CONCAT(?, '%')"),
    LIKE_RIGHT("字段匹配右半部分值", "{column} LIKE CONCAT('%', ?)"),
    IN("字段在值列表内", "{column} IN ({?})", ConditionCandidate::multiple),
    NIN("字段不在值列表内", "{column} NOT IN ({?))", ConditionCandidate::multiple),
    NOT_NULL("字段不为空", "{column} IS NOT NULL"),
    IS_NULL("字段为空", "{column} IS NULL"),
    BETWEEN("字段介于列表下标0和1的值之间", "{column} BETWEEN ? and ?"),
    DATE_GTE("日期字段大于值", "{column} > ?"),
    DATE_LTE("日期字段小于值", "{column} < ?");

    private final String name;

    private final TemplateCompiler.DynamicValue template;

    private final Function<Object, ?> mapper;

    ConditionCandidate(String name, String template) {
        this(name, template, null);
    }

    ConditionCandidate(String name, String template, Function<Object, ?> mapper) {
        this.name = name;
        this.template = TemplateCompiler.compile(template);
        this.mapper = mapper;
    }

    private static String multiple(Object value) {
        return value instanceof Collection ?
                ((Collection<?>) value).stream().map(v -> "?").collect(Collectors.joining(", ")) : "?";
    }

    /**
     * 编译并取得值
     *
     * @param field 字段
     * @param value 值
     * @return 结果
     */
    public String compile(String field, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("column", field);
        params.put("value", value);
        params.put("?", null != mapper ? mapper.apply(value) : "?");
        return template.apply(params);
    }
}
