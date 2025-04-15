package group.flyfish.fluent.entity;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 数据分页
 *
 * @author wangyu
 */
@Data
public class DataPage<T> {

    private List<T> list = Collections.emptyList();

    private long total = 0;

    private int size = 10;

    private int page = 1;

    public static <T> DataPage<T> of(int page, int size) {
        DataPage<T> result = new DataPage<>();
        result.page = page;
        result.size = size;
        return result;
    }
}
