package us.freshbeans.manage_own_dbs;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@SpringBootApplication
public class ManageOwnDbsApplication {

	    // Autowire the Environment object for accessing environment variables
    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(ManageOwnDbsApplication.class, args);
    }

	@Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(env.getProperty("env.data.SPRING_DATASOURCE_URL"));
        dataSource.setUsername(env.getProperty("env.data.SPRING_DATASOURCE_USERNAME"));
        dataSource.setPassword(env.getProperty("env.data.SPRING_DATASOURCE_PASSWORD"));
        return dataSource;
    }

	@Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
