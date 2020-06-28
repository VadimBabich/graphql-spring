/**
 * @author Vadim Babich
 */

package org.babich.graphql.configuration;

import graphql.schema.GraphQLScalarType;
import org.babich.graphql.scalars.DateCoercing;
import org.babich.graphql.scalars.LocalDateCoercing;
import org.babich.graphql.scalars.OffsetDateTimeCoercing;
import org.babich.graphql.scalars.OffsetTimeCoercing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScalarConfiguration {

    @Value("${graphql.dateTimePattern:yyyy-MM-dd'T'HH:mm:ss.SSSZ}")
    private String dateTimePattern;

    @Value("${graphql.datePattern:yyyy-MM-dd}")
    private String datePattern;

    @Value("${graphql.timePattern:HH:mm:ss.SSSZ}")
    private String timePattern;


    @Bean
    @ConditionalOnMissingBean(name = "localTimeScalarType")
    public GraphQLScalarType localTimeScalarType() {
        return GraphQLScalarType.newScalar()
                .name("OffsetTime")
                .description("java.time.OffsetTime Scalar")
                .coercing(new OffsetTimeCoercing(timePattern))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "localDataTimeScalarType")
    public GraphQLScalarType localDataTimeScalarType() {
        return GraphQLScalarType.newScalar()
                .name("OffsetDateTime")
                .description("java.time.OffsetDateTime Scalar")
                .coercing(new OffsetDateTimeCoercing(dateTimePattern))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "localDataScalarType")
    public GraphQLScalarType localDataScalarType() {
        return GraphQLScalarType.newScalar()
                .name("LocalDate")
                .description("java.time.LocalDate Scalar")
                .coercing(new LocalDateCoercing(datePattern))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataScalarType")
    public GraphQLScalarType dataScalarType() {
        return GraphQLScalarType.newScalar()
                .name("Date")
                .description("java.util.Date Scalar")
                .coercing(new DateCoercing(dateTimePattern))
                .build();
    }
}
