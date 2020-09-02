package appliances.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class JdbcConfig {
	
	@Bean
	public DataSource getDataSource() {
		final DataSourceBuilder<?> builder = DataSourceBuilder.create();
		
		builder.driverClassName("com.mysql.cj.jdbc.Driver");
		builder.url("jdbc:mysql://localhost:3306/appliances?serverTimezone=UTC&useLegacyDatetimeCode=false");
		builder.username("root");
		builder.password("1Rhfcfdxbr1");
		
		return builder.build();
	}
	
	@Bean
	public JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(getDataSource());
	}
}