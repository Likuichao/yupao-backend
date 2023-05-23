package com.lkc.yupao.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 * @author lkc
 * @version 1.0
 */
@Data
public class DeleteRequest implements Serializable {


    private static final long serialVersionUID = -7818498651627706660L;
    /**
     * 队伍名
     */
    private long id;

}
