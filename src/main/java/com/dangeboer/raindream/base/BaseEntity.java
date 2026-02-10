package com.dangeboer.raindream.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
// 实现 Serializable 即这个类可以被序列化，方便缓存、网络传输等
public class BaseEntity implements Serializable {
    // 标识此类，防止类结构变动导致反序列化失败
    @Serial
    private static final long serialVersionUID = 1L;

    // 主键id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 创建时间
    // LocalDateTime 是带时区的，以服务器所在时区为准
    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充
    @JsonInclude(value = JsonInclude.Include.NON_NULL) // 如果字段是null就不返回给前端
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都自动填充
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
