package com.application.config;

import com.application.dynamicdatasource.routing.DbType;
import com.application.dynamicdatasource.routing.RoutingDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@MapperScan(basePackages = "com.application.mapper", sqlSessionTemplateRef = "sqlSessionTemplate")
public class DataSourceConfig {


    @Bean(name = "readSource")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    @Primary
    public DataSource readSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "writeSource")
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean("routingDataSource")
    @Qualifier("routingDataSource")
    public RoutingDataSource routingDataSource(){
        RoutingDataSource routingDataSource=  new   RoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DbType.READ,readSource());
        targetDataSources.put(DbType.WRITE,writeSource());
        routingDataSource.setDefaultTargetDataSource(targetDataSources.get(DbType.READ));
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet();
        return  routingDataSource;
    }


    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory dbSqlSessionFactory(@Qualifier("routingDataSource") RoutingDataSource dataSource
                                                 ) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean=new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }


    @Bean(name = "transactionManager")
    @Primary
    public DataSourceTransactionManager dbTransactionManager(@Qualifier("routingDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate dbSqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }




}
