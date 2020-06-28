/**
 * @author Vadim Babich
 */

package org.babich.graphql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.babich.graphql.GraphQLRootObjectBuilder;
import org.babich.graphql.configuration.CacheConfiguration;
import org.babich.graphql.configuration.ScalarConfiguration;
import org.babich.graphql.configuration.ServletConfiguration;
import org.babich.graphql.test.utils.RootObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(SpringMvcTestController.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RelayCursorConnectionsTest.Config.class
        , CacheConfiguration.class
        , ServletConfiguration.class
        , ScalarConfiguration.class})

@TestPropertySource(properties = {
        "graphql.schemaLocationPattern=**/*relay-cursor-connections-test-schema.graphqls",
})
public class RelayCursorConnectionsTest {

    @Autowired
    MockMvc mockMvc;


    @Test
    public void projectionTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/projection/getPaginatedCollectionByFilter")
                .param("containsCharacter", "a")
                .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.getRandomTestObjects.edges.length()").value(2))
                .andExpect(jsonPath("$.data.getRandomTestObjects.edges[*].cursor").exists())
                .andExpect(jsonPath("$.data.getRandomTestObjects.edges[*].node").isNotEmpty())


                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.hasPreviousPage").value(false))
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.hasNextPage").value(true))
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.startCursor").exists())
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.endCursor").exists())

                //.andDo(print())
                .andReturn();

        String nextPage = JsonPath.read(result.getResponse().getContentAsString()
                , "$.data.getRandomTestObjects.pageInfo.endCursor");

        mockMvc.perform(get("/test/projection/getPaginatedCollectionByFilter")
                .param("containsCharacter", "a")
                .param("pageSize", "2")
                .param("nextPage", nextPage))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.hasPreviousPage").value(true))
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.hasNextPage").value(true))

                .andDo(print());
    }

    @Test
    public void executeQueryTest() throws Exception {

        String payload = "{ \"query\": \"query($pageSize: Int, $containsCharacter: String)" +
                " { rootTestObject {stringField, decimalField, localDateField, dateField} " +
                ", getRandomTestObjects(first: $pageSize, filter: $containsCharacter) " +
                "{ edges { node {stringField, decimalField, localDateField}, cursor }" +
                ", pageInfo{hasPreviousPage, hasNextPage, startCursor, endCursor} } } \"" +
                ", \"variables\": { \"containsCharacter\": \"a\", \"pageSize\": 2 }" +
                " }";

        mockMvc.perform(post("/test/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.getRandomTestObjects.edges.length()").value(2))
                .andExpect(jsonPath("$.data.getRandomTestObjects.edges[*].cursor").exists())
                .andExpect(jsonPath("$.data.getRandomTestObjects.edges[*].node").isNotEmpty())


                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.hasPreviousPage").value(false))
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.hasNextPage").value(true))
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.startCursor").exists())
                .andExpect(jsonPath("$.data.getRandomTestObjects.pageInfo.endCursor").exists())

                .andDo(print());
    }

    @Configuration
    @ImportResource(value = {"classpath*:graphql/projection-test-context.xml"})
    public static class Config {

        @Bean
        public SpringMvcTestController graphQlController() {
            return new SpringMvcTestController();
        }

        @Bean
        public GraphQLRootObjectBuilder graphQLRootObjectBuilder() {

            Supplier<RootObject> rootObjectSupplier =
                    new RootObjectSupplier(
                            new RootObject(UUID.randomUUID().toString()
                                    , new BigDecimal("10.01")
                                    , LocalDate.of(2020, 6, 27)
                                    , new Date()));

            return () -> rootObjectSupplier;
        }

        @Bean
        public ObjectMapper getObjectMapper() {
            return new ObjectMapper();
        }

        static public class RootObjectSupplier implements Supplier<RootObject> {

            public RootObjectSupplier(RootObject rootObject) {
                this.rootObject = rootObject;
            }

            private RootObject rootObject;

            public RootObject get() {
                return rootObject;
            }
        }
    }
}
