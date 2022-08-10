package com.oye.ref.config.dataSource;

import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Description
 * @Date 2020/7/23
 * @Created by ZhonghaoMa
 */
@Configuration
@MapperScan(basePackages = "com.oye.ref.dao")
public class DataSourceConfigurer {

    private Logger LOGGER = LoggerFactory.getLogger(DataSourceConfigurer.class);

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Value("${spring.datasource.master.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public YamlPropertiesFactoryBean configProperties() throws Exception {
        YamlPropertiesFactoryBean propertiesFactoryBean = new YamlPropertiesFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        propertiesFactoryBean.setResources(resolver.getResources("classpath*:application.yml"));
        return propertiesFactoryBean;
    }

    @Bean
    @Primary
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaxActive(30);
        dataSource.setInitialSize(10);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        return dataSource;
    }

    @Bean
    @Primary
    public DataSourceTransactionManager dataSourceTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource());
        return dataSourceTransactionManager;
    }

    @Bean
    @Primary
    public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:com/gitee/sunchenbin/mybatis/actable/mapping/*/*.xml"));
        sqlSessionFactoryBean.setTypeAliasesPackage("com.example.entity.*");
        return sqlSessionFactoryBean;
    }

}
