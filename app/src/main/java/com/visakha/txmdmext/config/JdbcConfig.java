package com.visakha.txmdmext.config;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.RelationalManagedTypes;
import org.springframework.data.relational.core.mapping.NamingStrategy;

/**
 * Spring Data JDBC for the business entity packages under model (each entity: its own package,
 * entity records + CrudRepository interfaces). Identifiers are NOT quoted, so the plain names in
 * the entities' @Table / @Column match Oracle's unquoted (upper case) table and column names.
 * Spring Data JDBC never creates or changes tables: the app runs no DDL.
 */
@Configuration
@EnableJdbcRepositories(basePackages = JdbcConfig.MODEL_PACKAGE)
public class JdbcConfig extends AbstractJdbcConfiguration {

  static final String MODEL_PACKAGE = "com.visakha.txmdmext.model";

  @Override
  protected Collection<String> getMappingBasePackages() {
    return List.of(MODEL_PACKAGE);
  }

  @Bean
  @Override
  public JdbcMappingContext jdbcMappingContext(
      Optional<NamingStrategy> namingStrategy,
      JdbcCustomConversions customConversions,
      RelationalManagedTypes jdbcManagedTypes) {
    JdbcMappingContext context =
        super.jdbcMappingContext(namingStrategy, customConversions, jdbcManagedTypes);
    context.setForceQuote(false);
    return context;
  }
}
