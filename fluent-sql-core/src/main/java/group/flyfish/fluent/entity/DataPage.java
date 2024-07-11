package group.flyfish.fluent.entity;

import lombok.Data;

import java.util.List;

/**
 * 数据分页
 *
 * @author wangyu
 */
@Data
public class DataPage<T> {

    private List<T> list;

    private long total;

    private int size = 10;

    private int page = 1;
}
