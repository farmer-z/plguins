package com.farmer.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @Description
 * @Author: zhang
 * @Date: 2021-11-14 22:27
 */
@Configuration
@PropertySource("classpath:application.properties")
@MapperScan(basePackages = "com.farmer.db.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
public class DataSourceConfig {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String location = "classpath*:com/farmer/db/mapping/*.xml";

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("serviceDataSource") DataSource spaceServiceDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(spaceServiceDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(location));
        return sessionFactory.getObject();
    }


    @Bean
    public  ServletRegistrationBean<? extends Servlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> registrationBean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        registrationBean.addInitParameter("allow", "127.0.0.1");
        //(存在共同时，deny优先于allow)
        registrationBean.addInitParameter("deny", "192.168.1.1");
        registrationBean.addInitParameter("loginUsername", "admin");
        registrationBean.addInitParameter("loginPassword", "admin");
        //禁用HTML页面上的“Reset All”功能
        registrationBean.addInitParameter("resetEnable", "false");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<? extends Filter> sapceWebStatViewFilter() {
        FilterRegistrationBean<WebStatFilter> registrationBean = new FilterRegistrationBean<>(new WebStatFilter());
        registrationBean.addInitParameter("urlPatterns", "/*");
        registrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*");

        return registrationBean;

    }

    @Bean(name = "serviceDataSource")
    public DataSource serviceDataSource(@Value("${spring.datasource.url}") String url,
                                             @Value("${spring.datasource.driver-class-name}") String driver,
                                             @Value("${spring.datasource.username}") String userName,
                                             @Value("${spring.datasource.password}") String password,
                                             @Value("${spring.datasource.maxActive}") int maxActive,
                                             @Value("${spring.datasource.filters}") String filters,
                                             @Value("${spring.datasource.initialSize}")
                                                     int initialSize,
                                             @Value("${spring.datasource.minIdle}")
                                                     int minIdle,
                                             @Value("${spring.datasource.maxWait}")
                                                     int maxWait,
                                             @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
                                                     int timeBetweenEvictionRunsMillis,
                                             @Value("${spring.datasource.minEvictableIdleTimeMillis}")
                                                     int minEvictableIdleTimeMillis,
                                             @Value("${spring.datasource.validationQuery}")
                                                     String validationQuery,
                                             @Value("${spring.datasource.testWhileIdle}")
                                                     boolean testWhileIdle,
                                             @Value("${spring.datasource.testOnBorrow}")
                                                     boolean testOnBorrow,
                                             @Value("${spring.datasource.testOnReturn}")
                                                     boolean testOnReturn,
                                             @Value("${spring.datasource.poolPreparedStatements}")
                                                     boolean poolPreparedStatements,
                                             @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")
                                                     int maxPoolPreparedStatementPerConnectionSize,
                                             @Value("${spring.datasource.connectionProperties}")
                                                     String connectionProperties,
                                             @Value("${spring.datasource.useGlobalDataSourceStat}")
                                                     boolean useGlobalDataSourceStat

    ) {
        DruidDataSource dataSource = new DruidDataSource();
        /*数据源主要配置*/
        dataSource.setUrl(url);
        dataSource.setDriverClassName(driver);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);

        /*数据源补充配置*/
        dataSource.setMaxActive(maxActive);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxWait(maxWait);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setUseUnfairLock(true);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setPoolPreparedStatements(poolPreparedStatements);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        dataSource.setConnectionProperties(connectionProperties);
        dataSource.setUseGlobalDataSourceStat(useGlobalDataSourceStat);

        try {
            dataSource.setFilters(filters);
            logger.info("Druid数据源初始化设置成功......");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Druid数据源filters设置失败......");
        }
        return dataSource;

    }

}
