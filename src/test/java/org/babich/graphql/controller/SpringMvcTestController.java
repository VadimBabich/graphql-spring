/**
 * @author Vadim Babich
 */

package org.babich.graphql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.babich.graphql.GraphQLContextBuilder;
import org.babich.graphql.GraphQLRootObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.babich.graphql.GraphQLHttpServletUtil.*;

@Controller
@RequestMapping("test")
public class SpringMvcTestController {

    @Autowired
    GraphQL graphQL;

    @Autowired
    private GraphQLContextBuilder contextBuilder;

    @Autowired
    private GraphQLRootObjectBuilder graphQLRootObjectBuilder;

    @Autowired
    private ObjectMapper objectMapper;


    @GetMapping(value = "/projection/{queryName}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> projectionPoint(@PathVariable("queryName") String queryName
            , @RequestParam MultiValueMap<String, String> params) {

        Map<String, Object> variables = getVariables(params);

        contextBuilder.projection(true);

        graphQLRootObjectBuilder.build();

        ExecutionResult executionResult = execute(graphQL
                , queryName
                , null
                , variables
                , contextBuilder
                , graphQLRootObjectBuilder);

        return getResultAsResponseEntity(executionResult, objectMapper);
    }

    @PostMapping(value = "/"
            , produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> openQueryPoint(@RequestBody() Payload payload) {

        Map<String, Object> variables = payload.getVariables();

        contextBuilder.projection(false);

        ExecutionResult executionResult = execute(graphQL
                , payload.getQuery()
                , payload.getOperation()
                , variables
                , contextBuilder
                , graphQLRootObjectBuilder);

        return getResultAsResponseEntity(executionResult, objectMapper);
    }
}

