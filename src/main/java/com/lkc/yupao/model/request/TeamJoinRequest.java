package com.lkc.yupao.model.request;

import lombok.Data;

/**
 *
 * @author lkc
 * @version 1.0
 */
@Data
public class TeamJoinRequest {
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
