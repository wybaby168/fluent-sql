package group.flyfish.fluent.utils.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模板编译器
 * 基于匹配模式，将字符串替换为表达式，通过sb返回结果
 */
public class TemplateCompiler {

    // 暂存静态文本
    private final StringBuilder sb = new StringBuilder();
    // 负责存储结果
    private final List<Object> parts = new ArrayList<>();
    // 栈。负责存储标记
    private int start = -1;

    private TemplateCompiler(String code) {
        parse(code);
    }

    /**
     * 静态编译，编译为Function
     *
     * @param template 模板
     * @return 编译结果
     */
    public static DynamicValue compile(String template) {
        return new TemplateCompiler(template).getCompiled();
    }

    /**
     * 动态编译，直接获得结果
     *
     * @return 结果
     */
    public static String explain(String template, Map<String, Object> args) {
        return new TemplateCompiler(template).getCompiled().apply(args);
    }

    private void parse(String code) {
        int length = code.length();
        for (int i = 0; i < length; i++) {
            char c = code.charAt(i);
            // 在栈作用中
            if (start != -1) {
                if (c == '}') {
                    parts.add(resolveExpression(code.substring(start, i)));
                    start = -1;
                }
            } else {
                // 匹配到开始标记
                if (c == '{') {
                    start = i + 1;
                    // 文本缓存非空，拼接之前的文本
                    if (sb.length() != 0) {
                        parts.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        // 拼接完成，释放sb资源。
        if (sb.length() != 0) {
            parts.add(sb.toString());
            sb.delete(0, sb.length());
        }
    }

    /**
     * 获取编译后的内容
     *
     * @return 结果
     */
    public DynamicValue getCompiled() {
        return context -> parts.stream().map(obj -> {
            if (obj instanceof DynamicValue) {
                return ((DynamicValue) obj).apply(context);
            }
            return String.valueOf(obj);
        }).collect(Collectors.joining());
    }

    private DynamicValue resolveExpression(String key) {
        return context -> String.valueOf(context.get(key));
    }

    @FunctionalInterface
    public interface DynamicValue extends Function<Map<String, Object>, String> {

    }
}
