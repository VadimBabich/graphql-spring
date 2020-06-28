/**
 * @author Vadim Babich
 */

package org.babich.graphql;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.GraphQLError;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.language.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.List;
import java.util.function.Function;

/**
 * Class that allows to caching of preparsed graphql queries
 */
public class GraphQLQueryCache implements PreparsedDocumentProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Cache cache;

    private final Function<ExecutionInput, ExecutionInput> queryResolver;


    public GraphQLQueryCache(Cache cache, Function<ExecutionInput, ExecutionInput> queryResolver) {
        this.cache = cache;
        this.queryResolver = queryResolver;
    }

    public GraphQLQueryCache() {
        this(new ConcurrentMapCache("preparsed-graphql-queries", false), Function.identity());
    }


    private PreparsedDocumentEntry errorResult(Exception e) {
        return new PreparsedDocumentEntry(new GraphQLError() {
            @Override
            public String getMessage() {
                return e.getMessage();
            }

            @Override
            public List<SourceLocation> getLocations() {
                return null;
            }

            @Override
            public ErrorClassification getErrorType() {
                return ErrorType.ValidationError;
            }
        });
    }

    @Override
    public PreparsedDocumentEntry getDocument(ExecutionInput executionInput
            , Function<ExecutionInput, PreparsedDocumentEntry> computeFunction) {

        String query = executionInput.getQuery();

        try {

            PreparsedDocumentEntry entry = cache.get(query, PreparsedDocumentEntry.class);

            if(null == entry){
                cache.putIfAbsent(query, entry = computeFunction.compose(queryResolver).apply(executionInput));
            }

            return entry;

        } catch (Exception e) {

            logger.warn("GraphQl gets error resolving query name {}.", query, e.getCause());

            return errorResult(e);
        }
    }

}
