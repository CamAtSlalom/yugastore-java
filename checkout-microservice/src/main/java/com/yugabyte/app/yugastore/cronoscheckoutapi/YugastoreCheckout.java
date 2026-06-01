package com.yugabyte.app.yugastore.cronoscheckoutapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
		CassandraAutoConfiguration.class,
		CassandraDataAutoConfiguration.class,
		CassandraReactiveDataAutoConfiguration.class,
		CassandraReactiveRepositoriesAutoConfiguration.class,
		CassandraRepositoriesAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients
public class YugastoreCheckout {

	public static void main(String[] args) {
		SpringApplication.run(YugastoreCheckout.class, args);
	}

}
