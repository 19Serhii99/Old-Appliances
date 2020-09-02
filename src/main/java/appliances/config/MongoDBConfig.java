package appliances.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoDBConfig {

	@Bean
	public MongoClient getMongoClient() {
		return MongoClients.create("mongodb://localhost:27017/appliances");
	}
	
	@Bean
	public MongoTemplate getMongoTemplate() {
		return new MongoTemplate(getMongoClient(), "appliances");
	}
	
	@Bean 
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}