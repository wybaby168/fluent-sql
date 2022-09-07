package group.flyfish.framework.vo;


import group.flyfish.fluent.binding.Alias;
import group.flyfish.fluent.binding.JSONInject;
import group.flyfish.framework.entity.SaasOrder;
import group.flyfish.framework.entity.SaasQuota;
import group.flyfish.framework.entity.SaasTenant;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * 租户上下文
 *
 * @author wangyu
 * 负责租户生命周期管理，缓存读写，驻留在请求上下文中
 */
@Data
public class TenantContext implements Serializable {

    private static final long serialVersionUID = -6912908497223962047L;

    // 租户id
    private String id;

    // 租户名称
    private String name;

    // 租户标识符
    private String identifier;

    // 租户数据源配置
    @JSONInject
    private SaasTenant.DataSourceConfig datasource;

    // 租户存储配置
    @JSONInject
    private SaasTenant.StorageConfig storage;

    // 租户状态
    private SaasTenant.Status status;

    // 租户启用状态
    private Boolean enable;

    // 配额信息
    @Alias("quota_config")
    @JSONInject
    private SaasQuota quota;

    // 订阅类型 （LIMITED:有限期订阅,UNLIMITED:无限期订阅）
    @Column(name = "order_type")
    private SaasOrder.Type orderType;

    // 订阅时间
    @Column(name = "order_time")
    private Date orderTime;

    // 过期时间
    @Column(name = "expire_time")
    private Date expireTime;
}
