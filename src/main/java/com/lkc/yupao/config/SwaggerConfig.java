package com.lkc.yupao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 类说明 :自定义swagger配置信息
 */
@Configuration
@EnableSwagger2WebMvc
@Profile({"dev","test"})//限定配置只在部分环境开启；如果在dev运行代码，则swagger在"dev","test"下可用
public class SwaggerConfig {

    @Bean
    public Docket creatApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select() //选择哪些路径和api会生成document
                .apis(RequestHandlerSelectors.basePackage("com.lkc.yupao.controller"))//controller路径
                //.apis(RequestHandlerSelectors.any())   //对所有api进行监控
                .paths(PathSelectors.any())  //对所有路径进行监控  线上环境不要把接口暴露出去
                .build();
    }

	/**
	 * 接口文档的一些基本信息  链式调用
	 * @return
	 */
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("伙伴匹配系统")//文档主标题
                .description("接口文档")//文档描述
                .version("1.0.0")//API的版本
                .termsOfServiceUrl("###")
                .license("LICENSE")
                .licenseUrl("###")
                .build();
    }
}