package com.lkc.yupao.algorithm;

import com.lkc.yupao.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author lkc
 * @version 1.0
 */
public class AlgorithmUtilsTest {
    @Test
    void test01() {
        String s1 = "javac++php";
        String s2 = "phpjavac++";
        int score1 = AlgorithmUtils.minDistance(s1, s2);
        System.out.println(score1);
    }

    @Test
    void test02() {
        List<String> list1 = Arrays.asList("php", "c++", "c++");
        List<String> list2 = Arrays.asList("php", "java", "c++");
        int score1 = AlgorithmUtils.minDistance(list1, list2);
        System.out.println(score1);
    }
}
