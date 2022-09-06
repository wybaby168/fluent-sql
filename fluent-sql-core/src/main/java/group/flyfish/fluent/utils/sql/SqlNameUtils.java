package group.flyfish.fluent.utils.sql;

import java.security.InvalidParameterException;
import java.util.Locale;

public class SqlNameUtils {

    /**
     * 方法转为属性
     *
     * @param name 方法名称
     * @return 最终属性
     */
    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else {
            if (!name.startsWith("get") && !name.startsWith("set")) {
                throw new InvalidParameterException("不是正确的getter或setter");
            }
            name = name.substring(3);
        }
        if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
     * 例如：hello_world->helloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String camelName(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母小写
            //update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
            //update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
            return name.substring(0, 1).toLowerCase() + name.substring(1).toLowerCase();
            //update-end--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 将驼峰命名转化成下划线
     *
     * @param para
     * @return
     */
    public static String camelToUnderline(String para) {
        if (para.length() < 3) {
            return para.toLowerCase();
        }
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;//定位
        //从第三个字符开始 避免命名不规范
        for (int i = 2; i < para.length(); i++) {
            if (Character.isUpperCase(para.charAt(i))) {
                sb.insert(i + temp, "_");
                temp += 1;
            }
        }
        return sb.toString().toLowerCase();
    }

    public static String wrap(String identifier) {
        if (identifier.startsWith("`")) {
            return identifier;
        }
        return String.join("", "`", identifier, "`");
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R cast(T t) {
        return (R) t;
    }
}
