/**
 * @author Vadim Babich
 */

package org.babich.graphql.configuration;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tracing is a GraphQL extension for performance tracing.
 * Tracing works by including data in the extensions field of the GraphQL response.
 * That way, you have access to performance traces alongside the data returned by your query.
 */
@Configuration
public class InstrumentationConfiguration {

    @Value("${graphql.maxQueryComplexity:0}")
    private Integer maxQueryComplexity;

    @Value("${graphql.maxQueryDepth:0}")
    private Integer maxQueryDepth;


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "graphql.tracing-enabled", havingValue = "true")
    public TracingInstrumentation tracingInstrumentation() {
        return new TracingInstrumentation();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression(value = "#{${graphql.maxQueryComplexity:0} > 0}")
    public MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation() {
        return new MaxQueryComplexityInstrumentation(maxQueryComplexity);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression(value = "#{${graphql.maxQueryDepth:0} > 0}")
    public MaxQueryDepthInstrumentation maxQueryDepthInstrumentation() {
        return new MaxQueryDepthInstrumentation(maxQueryDepth);
    }

}
