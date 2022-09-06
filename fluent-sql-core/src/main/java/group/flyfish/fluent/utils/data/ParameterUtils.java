package group.flyfish.fluent.utils.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.BeanUtils;

import java.security.InvalidParameterException;

/**
 * 参数工具类
 *
 * @author wangyu
 */
public final class ParameterUtils {

    public static Object convert(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof Enum) {
            return String.valueOf(value);
        }
        if (BeanUtils.isSimpleProperty(value.getClass())) {
            return value;
        }
        try {
            return ObjectMappers.shared().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new InvalidParameterException("不是一个json数据，或者是未识别的类！");
        }
    }
}
