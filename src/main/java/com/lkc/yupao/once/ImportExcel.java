package com.lkc.yupao.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author lkc
 * @version 1.0
 */
public class ImportExcel {

    static String fileName ="E:\\java_project_partner\\yupao-backend\\src\\main\\java\\com\\lkc\\yupao\\easyexcel\\UserInfo.xlsx";
    /**
     * 读取数据
     * @param args
     */
    public static void main(String[] args) {
//        readByListener();
        synchronousRead();
    }

    /**
     * 监听器读取,，通过 new UserInfoListener()监听
     */
    public static void readByListener(){
        // 写法1：JDK8+ ,不用额外写一个UserInfoListener
        // since: 3.0.0-beta
        EasyExcel.read(fileName, UserInfo.class, new UserInfoListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 一次性读取全部数据，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead() {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserInfo> totalDtaList = EasyExcel.read(fileName).head(UserInfo.class).sheet().doReadSync();
        for (UserInfo userInfo : totalDtaList) {
            System.out.println(userInfo);
        }
    }

}
