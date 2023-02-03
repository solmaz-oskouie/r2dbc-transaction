package home.example.r2dbctransaction.configuration;

import com.sepantasolutions.utils.logs.LogUtil;
import com.sepantasolutions.utils.logs.Logger;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

@Configuration
public class FlywayConfiguration {

    private final Environment env;

    private final Logger logger = LogUtil.getLogger();

    public FlywayConfiguration(final Environment env) {
        this.env = env;
    }

    @Bean(initMethod = "migrate")
    public Flyway instantiateFlyway() {
        var flyway =
                new Flyway(Flyway.configure().baselineOnMigrate(false).dataSource(
                        env.getRequiredProperty("spring.flyway.url"),
                        env.getRequiredProperty("spring.flyway.user"),
                        env.getRequiredProperty("spring.flyway.password")
                ).locations(env.getRequiredProperty("spring.flyway.locations")));

        logger.general().info("FLYWAY LOCATIONS IS ", Map.of("path", flyway.getConfiguration().getLocations()[0].getPath()));
        return flyway;
    }

}
