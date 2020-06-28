/**
 * @author Vadim Babich
 */

package org.babich.graphql.configuration;

import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import org.babich.graphql.GraphQLContextBuilder;
import org.babich.graphql.GraphQLQueryCache;
import org.babich.graphql.SchemaStringProvider;
import org.babich.graphql.schema.DirectiveExtensionSchemaParser;
import org.babich.graphql.schema.SchemaStringProviderByFileMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Configuration of graphql-java query execution, schema and servlet.
 */

@Configuration
@ConditionalOnProperty(value = "graphql.enabled", havingValue = "true", matchIfMissing = true)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class ServletConfiguration {

    @Value("${graphql.schemaLocationPattern: **/*.graphqls}")
    private String schemaLocationPattern;


    @Autowired(required = false)
    private List<Instrumentation> instrumentations;

    @Autowired(required = false)
    private List<GraphQLScalarType> graphQLScalarTypes;

    @Autowired(required = false)
    private List<TypeRuntimeWiring> typeRuntimeWirings;

    @Autowired(required = false)
    private PreparsedDocumentProvider preparsedDocumentProvider;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Qualifier("documentProjectionQueries")
    @Autowired(required = false)
    private Map<String, String> documentProjectionQueries;


    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean
    GraphQLContextBuilder graphQLContextBuilder() {
        return new GraphQLContextBuilder.ServletGraphQLContextBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    SchemaStringProvider schemaStringProvider() {
        return new SchemaStringProviderByFileMatcher(schemaLocationPattern);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchema graphQLSchema(SchemaStringProvider schemaStringProvider) throws IOException {
        return loadSchema(schemaStringProvider);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @ConditionalOnMissingBean
    PreparsedDocumentProvider preparsedDocumentProvider(Cache preparsedGraphqlQueriesCache) {
        return buildPreparsedDocumentProvider(preparsedGraphqlQueriesCache);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQL graphQL(GraphQLSchema graphQLSchema) {

        GraphQL.Builder builder = GraphQL.newGraphQL(graphQLSchema);

        if (null != preparsedDocumentProvider) {
            builder.preparsedDocumentProvider(preparsedDocumentProvider);
        }

        if (null != instrumentations) {
            builder.instrumentation(new ChainedInstrumentation(instrumentations));
        }

        return builder.build();
    }

    private GraphQLSchema loadSchema(SchemaStringProvider schemaStringProvider) throws IOException {

        TypeDefinitionRegistry registry = new DirectiveExtensionSchemaParser()
                .parse(schemaStringProvider.schemaStrings());

        RuntimeWiring runtimeWiring = buildRuntimeWiring();

        return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
    }

    private RuntimeWiring buildRuntimeWiring() {

        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

        if (null != graphQLScalarTypes) {
            graphQLScalarTypes.forEach(builder::scalar);
        }

        if (null != typeRuntimeWirings) {
            typeRuntimeWirings.forEach(builder::type);
        }

        return builder.build();
    }

    private PreparsedDocumentProvider buildPreparsedDocumentProvider(Cache cache) {

        return new GraphQLQueryCache(cache, executionInput -> {

            if (isNotProjection((GraphQLContext) executionInput.getContext())) {
                return executionInput;
            }

            String query;
            if (null == (query = documentProjectionQueries.get(executionInput.getQuery()))) {
                throw new IllegalArgumentException("Query name {" + executionInput.getQuery() + "} is not found.");
            }

            return executionInput.transform(builder -> builder.query(query));
        });
    }

    static boolean isNotProjection(GraphQLContext context) {
        if (null == context) {
            return true;
        }
        return !context.getOrDefault(GraphQLContextBuilder.isProjection, false);
    }
}
