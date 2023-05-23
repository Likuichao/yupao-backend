package com.lkc.yupao.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lkc
 * @version 1.0
 */
public class ImportUserInfo {

    public static void main(String[] args) {
        String fileName ="E:\\java_project_partner\\yupao-backend\\src\\main\\java\\com\\lkc\\yupao\\easyexcel\\UserInfo.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserInfo> userInfoList =
                EasyExcel.read(fileName).head(UserInfo.class).sheet().doReadSync();
        System.out.println("用户昵称总数: "+userInfoList.size());
        //过滤器-只要用户名不为空； 通过用户名分组，作为map的key
        Map<String, List<UserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(UserInfo::getUsername));
        //listMap.entrySet()获取map的k-v关系；Map.Entry是entrySet()的元素类型
        for (Map.Entry<String, List<UserInfo>> stringListEntry : listMap.entrySet()) {
            if(stringListEntry.getValue().size()>1){
                System.out.println("重复username = "+stringListEntry.getKey());
            }
        }
        System.out.println("不重复用户昵称:"+listMap.keySet().size());
    }
}
