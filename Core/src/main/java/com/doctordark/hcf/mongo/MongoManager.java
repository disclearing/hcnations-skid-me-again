/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.mongo;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.mongo.codec.DeathbanCodec;
import com.doctordark.hcf.mongo.codec.DeathbanCodecProvider;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.Collections;
import java.util.List;

public class MongoManager{

    private MongoClient mongoClient;
    @Getter private MongoDatabase database;

    public MongoManager(HCF plugin){
        List<ServerAddress> seeds = Collections.singletonList(new ServerAddress(plugin.getConfiguration().getMongoAddress(), plugin.getConfiguration().getMongoPort()));
        List<MongoCredential> credentials = Collections.singletonList(new MongoClientURI("mongodb://" + plugin.getConfiguration().getMongoUsername() + ":" +
                        plugin.getConfiguration().getMongoPassword() +
                        "@" + plugin.getConfiguration().getMongoAddress() +
                        ":" + plugin.getConfiguration().getMongoPort() +
                        "/" + plugin.getConfiguration().getMongoDatabase()).getCredentials());

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new DeathbanCodec()), CodecRegistries.fromProviders(new DeathbanCodecProvider()), MongoClient.getDefaultCodecRegistry());
        MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry).build();

        if(plugin.getConfiguration().isMongoDataAuth()){
            mongoClient = new MongoClient(seeds, credentials, options);
        }else{
            mongoClient = new MongoClient(seeds, options);
        }
//        mongoClient = new MongoClient(
//                new MongoClientURI("mongodb://" + plugin.getConfiguration().getMongoUsername() +
//                        ":" +
//                        plugin.getConfiguration().getMongoPassword() +
//                        "@" + plugin.getConfiguration().getMongoAddress() +
//                        ":" + plugin.getConfiguration().getMongoPort() +
//                        "/" + plugin.getConfiguration().getMongoDatabase())
//        );
        database = mongoClient.getDatabase(plugin.getConfiguration().getMongoDatabase());
    }

    public void close(){
        mongoClient.close();
    }
}
