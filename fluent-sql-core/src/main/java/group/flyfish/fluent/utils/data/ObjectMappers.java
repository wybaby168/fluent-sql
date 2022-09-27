package group.flyfish.fluent.utils.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * 项目唯一的jackson工具
 *
 * @author wangyu
 */
public final class ObjectMappers {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private ObjectMappers() {

    }

    public static ObjectMapper shared() {
        return objectMapper;
    }
}
