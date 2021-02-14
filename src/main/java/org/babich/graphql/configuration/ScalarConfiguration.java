/**
 * @author Vadim Babich
 */

package org.babich.graphql.configuration;

import graphql.scalars.ExtendedScalars;
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
    @ConditionalOnMissingBean(name = "localDateTimeScalarType")
    public GraphQLScalarType localDateTimeScalarType() {
        return GraphQLScalarType.newScalar()
                .name("OffsetDateTime")
                .description("java.time.OffsetDateTime Scalar")
                .coercing(new OffsetDateTimeCoercing(dateTimePattern))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "localDateScalarType")
    public GraphQLScalarType localDateScalarType() {
        return GraphQLScalarType.newScalar()
                .name("LocalDate")
                .description("java.time.LocalDate Scalar")
                .coercing(new LocalDateCoercing(datePattern))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "dateScalarType")
    public GraphQLScalarType dateScalarType() {
        return GraphQLScalarType.newScalar()
                .name("Date")
                .description("java.util.Date Scalar")
                .coercing(new DateCoercing(dateTimePattern))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "bigDecimalScalarType")
    public GraphQLScalarType bigDecimalScalarType() {
        return ExtendedScalars.GraphQLBigDecimal;
    }

    @Bean
    @ConditionalOnMissingBean(name = "bigIntegerScalarType")
    public GraphQLScalarType bigIntegerScalarType() {
        return ExtendedScalars.GraphQLBigInteger;
    }

    @Bean
    @ConditionalOnMissingBean(name = "byteScalarType")
    public GraphQLScalarType byteScalarType() {
        return ExtendedScalars.GraphQLByte;
    }

    @Bean
    @ConditionalOnMissingBean(name = "longScalarType")
    public GraphQLScalarType longScalarType() {
        return ExtendedScalars.GraphQLLong;
    }

    @Bean
    @ConditionalOnMissingBean(name = "charScalarType")
    public GraphQLScalarType charScalarType() {
        return ExtendedScalars.GraphQLChar;
    }


}
