package fresh.crafts.wiz_mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@SpringBootApplication
public class WizMongoApplication {

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(WizMongoApplication.class, args);
	}

	@Bean
	public MongoClient mongoClient() {
		// connection string from these in env
		// spring.data.mongodb.host=localhost
		// spring.data.mongodb.port=17017
		// spring.data.mongodb.username=root
		// spring.data.mongodb.password=password
		// spring.data.mongodb.authentication-database=admin

		String host = env.getProperty("spring.data.mongodb.host");
		int port = Integer.parseInt(env.getProperty("spring.data.mongodb.port"));
		String username = env.getProperty("spring.data.mongodb.username");
		String password = env.getProperty("spring.data.mongodb.password");
		String authenticationDatabase = env.getProperty("spring.data.mongodb.authentication-database");

		ConnectionString connectionString = new ConnectionString("mongodb://" + username + ":" + password + "@" + host
				+ ":" + port + "/" + "?authSource=" + authenticationDatabase);

		return MongoClients.create(connectionString);

	}

}
