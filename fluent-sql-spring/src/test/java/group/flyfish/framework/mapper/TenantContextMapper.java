package group.flyfish.framework.mapper;

import group.flyfish.framework.vo.TenantContext;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 测试用的mapper
 *
 * @author wangyu
 */
@Mapper
public interface TenantContextMapper {

    /**
     * 查询列表
     *
     * @param enable 启用与否
     * @return 结果
     */
    List<TenantContext> selectList(Boolean enable);
}
