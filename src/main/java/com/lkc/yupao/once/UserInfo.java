package com.lkc.yupao.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author lkc
 * @version 1.0
 * 星球表格用户信息
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserInfo {
    /**
     * 用名字去匹配，这里需要注意，如果名字重复，会导致只有一个字段读取到数据
     */
    @ExcelProperty("成员编号")
    private String  planetCode;

    @ExcelProperty("用户昵称")
    private String username;

}
