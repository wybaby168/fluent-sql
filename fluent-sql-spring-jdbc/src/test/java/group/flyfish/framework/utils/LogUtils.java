package group.flyfish.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import group.flyfish.fluent.utils.data.ObjectMappers;

import java.text.MessageFormat;

public class LogUtils {

    public static void print(String message, Object... args) {
        System.out.println(MessageFormat.format(message, args));
    }

    public static void printResult(Object value) {
        if (null == value) {
            print("执行结果为null");
        } else {
            try {
                print("执行结果为{0}", ObjectMappers.shared().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                print("执行结果为{0}", value);
            }
        }
    }
}
