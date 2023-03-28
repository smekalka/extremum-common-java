package common.dao.mongo;

import config.DescriptorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@Import(DescriptorConfiguration.class)
public class MongoCommonDaoConfiguration {
}
