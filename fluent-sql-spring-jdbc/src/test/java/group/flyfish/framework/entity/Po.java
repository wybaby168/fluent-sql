package group.flyfish.framework.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * 所有实体类继承
 */
@Getter
@Setter
public abstract class Po implements Serializable {

    @Id
    @Property("主键")
    private String id;

    @Column(name = "create_by")
    @Property("创建者")
    private String createBy;

    @Column(name = "create_time")
    @Property("创建时间")
    private Date createTime;

    @Column(name = "update_by")
    @Property("更新者")
    private String updateBy;

    @Column(name = "update_time")
    @Property("更新时间")
    private Date updateTime;

    @Column(name = "is_delete")
    @Property("逻辑删除标志")
    private Boolean delete;
}
