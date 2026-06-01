package com.yugabyte.app.yugastore.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.net.InetSocketAddress;

// Bypasses CqlSessionFactoryBean (which calls session.refreshSchema() unconditionally)
// and builds CqlSession directly with schema metadata disabled to avoid a
// compatibility error in java-driver-core 4.6.0-yb-10 when parsing
// YugabyteDB system tables that contain the internal uint32 type.
@Configuration
@EnableAutoConfiguration
@Profile(value = "local")
class YugabyteYCQLConfig {

  @Configuration
  @EnableCassandraRepositories(basePackages = { "com.yugabyte.app.yugastore.repo" })
  class CassandraConfig {

    @Value("${cronos.yugabyte.hostname:localhost}")
    private String cassandraHost;

    @Value("${cronos.yugabyte.port:9042}")
    private int cassandraPort;

    @Value("${cronos.yugabyte.keyspace:cronos}")
    private String keyspace;

    @Bean(destroyMethod = "close")
    public CqlSession cqlSession() {
      return CqlSession.builder()
          .addContactPoint(new InetSocketAddress(cassandraHost, cassandraPort))
          .withKeyspace(keyspace)
          .withConfigLoader(DriverConfigLoader.programmaticBuilder()
              .withBoolean(DefaultDriverOption.METADATA_SCHEMA_ENABLED, false)
              .build())
          .build();
    }

    @Bean
    public CassandraMappingContext cassandraMapping() {
      return new CassandraMappingContext();
    }

    @Bean
    public MappingCassandraConverter cassandraConverter() {
      MappingCassandraConverter converter = new MappingCassandraConverter(cassandraMapping());
      converter.afterPropertiesSet();
      return converter;
    }

    @Bean
    public CassandraTemplate cassandraTemplate(CqlSession session) {
      return new CassandraTemplate(session, cassandraConverter());
    }
  }
}

