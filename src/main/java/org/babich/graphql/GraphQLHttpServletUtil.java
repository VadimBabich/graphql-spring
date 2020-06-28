/**
 * @author Vadim Babich
 */

package org.babich.graphql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class GraphQLHttpServletUtil {


    //output format constants
    public static final String data = "data";
    public static final String errors = "errors";
    public static final String extensions = "extensions";

    //input format constants
    public static final String query = "query";
    public static final String variables = "variables";
    public static final String operation = "operation";

    //input document constants
    public static final String archived = "archived";

    /**
     * Wrapping result of graphql-query in json format
     * @param executionResult result
     * @param objectMapper json mapper
     * @return resopnse as json
     */
    public static Response getResult(ExecutionResult executionResult, ObjectMapper objectMapper) {
        if (areErrorsPresent(executionResult)) {
            return Response.status(INTERNAL_SERVER_ERROR)
                    .entity(serializeResultAsJson(executionResult, objectMapper))
                    .build();
        }

        return Response.ok(serializeResultAsJson(executionResult, objectMapper)).build();
    }

    /**
     * Wrapping result of graphql-query in json format
     * @param executionResult result
     * @param objectMapper json mapper
     * @return ResponseEntity<String> body is json string
     */
    public static ResponseEntity<String> getResultAsResponseEntity(ExecutionResult executionResult, ObjectMapper objectMapper) {
        if (areErrorsPresent(executionResult)) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .body(serializeResultAsJson(executionResult, objectMapper));
        }

        return ResponseEntity.ok(serializeResultAsJson(executionResult, objectMapper));
    }

    /**
     * Normalizing of query parameters to the format of graphql query variables
     */
    public static Map<String, Object> getVariables(MultivaluedMap<String, String> queryParameters) {

        return queryParameters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, normalizeValue));
    }

    public static Map<String, Object> getVariables(MultiValueMap<String, String> queryParameters) {

        return queryParameters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, normalizeValue));
    }

    static final Function<Map.Entry<String, List<String>>, Object> normalizeValue = item ->
    {
        Collection<String> values = item.getValue();
        if (1 == values.size()) {
            return values.iterator().next();
        }
        return values;
    };

    /**
     *
     * @param variables
     * @return
     */
    public static boolean isArchived(Map<String, Object> variables){
        if(null == variables){
            return false;
        }

        return Boolean.TRUE.equals(variables.getOrDefault(archived, false));
    }

    /**
     *
     * @param query
     * @param operationName
     * @param variables
     * @param contextBuilder
     * @param graphQLRootObjectBuilder
     * @return
     */
    public static ExecutionResult execute(GraphQL graphQL
            , String query
            , String operationName
            , Map<String, Object> variables
            , GraphQLContextBuilder contextBuilder
            , GraphQLRootObjectBuilder graphQLRootObjectBuilder) {

        Map<String, Object> vars = null == variables ? Collections.emptyMap()
                : new HashMap<>(variables);

        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(vars)
                .root(graphQLRootObjectBuilder.build())
                .context(contextBuilder.build())
                .build();

        return graphQL.execute(input);
    }

    private static String serializeResultAsJson(ExecutionResult executionResult, ObjectMapper objectMapper) {

        Map<String, Object> asMap = createResultFromExecutionResult(executionResult);

        try {

            return objectMapper.writeValueAsString(asMap);

        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Object> createResultFromExecutionResult(ExecutionResult executionResult) {
        final Map<String, Object> result = new LinkedHashMap<>();

        result.put(data, executionResult.getData());

        if (areErrorsPresent(executionResult)) {
            result.put(errors, executionResult.getErrors());
        }

        if (null != executionResult.getExtensions()) {
            result.put(extensions, executionResult.getExtensions());
        }

        return result;
    }

    private static boolean areErrorsPresent(ExecutionResult executionResult) {
        return 0 != executionResult.getErrors().size();
    }

    /**
     * Payload of open query graphql end-point.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Payload {

        private final String query;

        private final Map<String, Object> variables;

        private final String operation;

        @JsonCreator
        public Payload(@JsonProperty(GraphQLHttpServletUtil.query) String query
                , @JsonProperty(GraphQLHttpServletUtil.variables) Map<String, Object> variables
                , @JsonProperty(GraphQLHttpServletUtil.operation) String operation) {

            this.query = query;
            this.variables = variables;
            this.operation = operation;
        }

        public String getQuery() {
            return query;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public String getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Payload.class.getSimpleName() + "[", "]")
                    .add("query='" + query + "'")
                    .add("variables=" + variables)
                    .add("operation='" + operation + "'")
                    .toString();
        }
    }

}
