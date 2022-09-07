package group.flyfish.framework.entity;


import group.flyfish.fluent.binding.JSONInject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * 租户订阅表
 *
 * @author wangyu
 */
@Table(name = "saas_order")
@Getter
@Setter
public class SaasOrder extends Po {

    @Property("订阅备注")
    private String comment;

    @Property("套餐id")
    @ManyToOne
    @Column(name = "plan_id")
    private String planId;

    @Property("租户id")
    @ManyToOne
    @Column(name = "tenant_id")
    private String tenantId;

    @Property("配额配置JSON")
    @Column(name = "quota_config")
    @JSONInject
    private SaasQuota quotaConfig;

    @Property("订阅类型 （LIMITED:有限期订阅,UNLIMITED:无限期订阅）")
    @Column(name = "order_type")
    private Type orderType;

    @Property("订阅时间")
    @Column(name = "order_time")
    private Date orderTime;

    @Property("过期时间")
    @Column(name = "expire_time")
    private Date expireTime;

    @Property("订阅状态 （VALID:有效,INVALID:无效,ERROR:异常）")
    @Column(name = "status")
    private Status status;

    @Getter
    @AllArgsConstructor
    public enum Status {

        VALID("有效"),
        INVALID("无效"),
        ERROR("异常");

        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public enum Type {

        LIMITED("有限期订阅"),
        UNLIMITED("无限期订阅");

        private final String name;
    }

}
