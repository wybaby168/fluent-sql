package group.flyfish.framework.entity;

import group.flyfish.fluent.binding.JSONInject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * saas计划套餐
 *
 * @author wangyu
 */
@Table(name = "saas_plan")
@Getter
@Setter
public class SaasPlan extends Po {

    @Property("套餐名称")
    @Column(name = "name")
    private String name;

    @Property("套餐描述")
    @Column(name = "description")
    private String description;

    @Property("配额配置JSON")
    @Column(name = "quota_config")
    @JSONInject
    private SaasQuota quotaConfig;

    @Property("启用状态(未启用0,已启用1)")
    @Column(name = "enable")
    private Boolean enable;

    @Property("套餐状态 （NORMAL:上架中,DISABLED:已下架）")
    @Column(name = "status")
    private String status;
}
