package com.tmc.dependency;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

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
        AmazonDynamoDB client = AmazonDynamoDBClient.builder()
                .withCredentials(new ProfileCredentialsProvider("Personal"))
                .withRegion(Regions.US_EAST_1)
                .build();

        return client;
    }
}
