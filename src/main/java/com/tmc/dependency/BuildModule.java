package com.tmc.dependency;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Customer;
import com.tmc.model.Employee;
import com.tmc.model.Timesheet;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.manager.CacheManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Module
public class BuildModule {

    @Provides
    @Singleton
    public DynamoDBMapper provideDynamoDbMapper(AmazonDynamoDB client) {
        return new DynamoDBMapper(client);
    }

    @Provides
    @Singleton
    public AmazonDynamoDB provideAmazonDynamoDb() {
        return AmazonDynamoDBClient.builder().withCredentials(new ProfileCredentialsProvider("Personal")).withRegion(Regions.US_EAST_1).build();
    }

    @Provides
    @Singleton
    public DynamoDB provideDynamoDb(AmazonDynamoDB client) {
        return new DynamoDB(client);
    }

    @Provides
    @Singleton
    public LoadingCache<String, Timesheet> provideTimesheetCache(DynamoDbDao dao) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getTimesheet));
    }
    @Provides
    @Singleton
    public LoadingCache<String, Employee> provideEmployeeCache(DynamoDbDao dao) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getEmployee));
    }
    @Provides
    @Singleton
    public LoadingCache<String, Customer> provideCustomerCache(DynamoDbDao dao) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getCustomer));
    }
    @Provides
    @Singleton
    public LoadingCache<String, Company> provideCompanyCache(DynamoDbDao dao) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getCompany));
    }

    @Provides
    @Singleton
    public CacheManager provideCacheManager(DynamoDbDao dao, LoadingCache<String, Timesheet> timesheetCache, LoadingCache<String, Customer> customerCache, LoadingCache<String, Employee> employeeCache, LoadingCache<String, Company> companyCache) {
        return new CacheManager(dao, timesheetCache, customerCache, employeeCache, companyCache);
    }
}
