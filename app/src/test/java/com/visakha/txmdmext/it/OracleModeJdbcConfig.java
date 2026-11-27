package com.visakha.txmdmext.it;

import com.visakha.txmdmext.config.JdbcConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcOracleDialect;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

/**
 * The production JdbcConfig, but with the Oracle dialect forced: H2 in Oracle mode would otherwise
 * be detected as H2 (LIMIT instead of FETCH FIRST), so the tests render the same SQL as Oracle.
 */
@Configuration
@EnableJdbcRepositories(basePackages = "com.visakha.txmdmext.model")
class OracleModeJdbcConfig extends JdbcConfig {

  @Bean
  @Override
  public JdbcDialect jdbcDialect(NamedParameterJdbcOperations operations) {
    return JdbcOracleDialect.INSTANCE;
  }
}
