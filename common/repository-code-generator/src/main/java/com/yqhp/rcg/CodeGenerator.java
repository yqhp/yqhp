package com.yqhp.rcg;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * @author jiangyitao
 */
public class CodeGenerator {

    public static void main(String[] args) {
        AutoGenerator generator = new AutoGenerator();
        generator.setDataSource(dataSourceConfig(
                "jdbc:mysql://" + System.getenv("CONSOLE_MYSQL_ADDR") + "/console?characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8",
                "com.mysql.cj.jdbc.Driver",
                System.getenv("CONSOLE_MYSQL_USERNAME"),
                System.getenv("CONSOLE_MYSQL_PWD")
        ));
        generator.setGlobalConfig(globalConfig("/console/console-repository/src/main/java"));
        generator.setPackageInfo(packageConfig("com.yqhp.console.repository"));
        String[] tables = {"device"};
        generator.setStrategy(strategyConfig(tables));
        generator.setTemplate(templateConfig());
        generator.execute();
    }

    private static TemplateConfig templateConfig() {
        TemplateConfig config = new TemplateConfig();
        config.setController(null);
        config.setService(null);
        config.setServiceImpl(null);
//        config.setXml(null);
        return config;
    }

    private static StrategyConfig strategyConfig(String[] tables) {
        StrategyConfig config = new StrategyConfig();
        config.setNaming(NamingStrategy.underline_to_camel);
        config.setColumnNaming(NamingStrategy.underline_to_camel);
//        strategyConfig.setSuperControllerClass("");
//        strategyConfig.setSuperEntityClass("");
//         strategy.setTablePrefix("t_"); // 表名前缀
        config.setEntityLombokModel(true); // 使用lombok
        config.setInclude(tables);
        return config;
    }

    private static PackageConfig packageConfig(String parent) {
        PackageConfig config = new PackageConfig();
        config.setParent(parent);
        config.setParent(parent);
        return config;
    }

    private static GlobalConfig globalConfig(String outputDir) {
        GlobalConfig config = new GlobalConfig();
        config.setOutputDir(System.getProperty("user.dir") + outputDir);
        config.setAuthor("mybatis-plus generator");
        config.setOpen(false);
        config.setFileOverride(true); // 是否覆盖上一次生成的文件
        config.setControllerName(null);
        config.setServiceName(null);
        config.setBaseResultMap(true); // 生成resultMap
        return config;
    }

    private static DataSourceConfig dataSourceConfig(String url, String driverName, String username, String password) {
        DataSourceConfig config = new DataSourceConfig();
        config.setUrl(url);
        config.setDriverName(driverName);
        config.setUsername(username);
        config.setPassword(password);
        return config;
    }
}
