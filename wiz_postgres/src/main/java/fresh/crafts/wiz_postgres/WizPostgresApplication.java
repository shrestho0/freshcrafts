package fresh.crafts.wiz_postgres;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@SpringBootApplication
public class WizPostgresApplication {

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(WizPostgresApplication.class, args);
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl(env.getProperty("freshCrafts.SPRING_DATASOURCE_URL"));
		dataSource.setUsername(env.getProperty("freshCrafts.SPRING_DATASOURCE_USERNAME"));
		dataSource.setPassword(env.getProperty("freshCrafts.SPRING_DATASOURCE_PASSWORD"));
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

}
