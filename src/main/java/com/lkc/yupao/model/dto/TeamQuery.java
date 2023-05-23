package com.lkc.yupao.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lkc.yupao.common.PageRequest;
import lombok.Data;

import java.util.List;

/**
 * 队伍查询封装类
 * @author lkc
 * @version 1.0
 * 为什么需要请求参数包装类？
 *  1.请求参数名称和实体类不一样
 *  2.有一些参数用不到，如果要自动生成接口文档，会增加理解理解成本
 *  3.对多个实体类映射到同一个对象
 * 为什么需要包装类？
 *  1.可能有些字段要隐藏，不能返回给前端
 *  2.或者有些字段有些方法是不关心的
 */
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * id列表
     */
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开  1-私有 2-加密
     */
    private Integer status;
}
