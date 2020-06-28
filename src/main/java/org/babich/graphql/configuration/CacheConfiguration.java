/**
 * @author Vadim Babich
 */

package org.babich.graphql.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {


    @Value("${graphql.cache.preparsed-queries.size:1000}")
    private int preparsedGraphqlQueriesCacheMaximumSize;


    @Bean
    @ConditionalOnMissingBean(name = "preparsedGraphqlQueriesCache")
    Cache preparsedGraphqlQueriesCache() {

        return new CaffeineCache("preparsed-graphql-queries", Caffeine.newBuilder()
                .maximumSize(preparsedGraphqlQueriesCacheMaximumSize)
                .build(), false);
    }

}
