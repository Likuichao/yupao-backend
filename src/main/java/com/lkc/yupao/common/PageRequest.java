package com.lkc.yupao.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 * @author lkc
 * @version 1.0
 */
@Data
public class PageRequest implements Serializable {


    private static final long serialVersionUID = -3358384555017657497L;

    /**
     * 页面大小
     */
    protected int pageSize = 10;

    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
