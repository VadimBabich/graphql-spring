/**
 * @author Vadim Babich
 */

package org.babich.graphql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpringMvcTestController.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringMvcTest.Config.class
        , CacheConfiguration.class
        , ServletConfiguration.class
        , ScalarConfiguration.class})

@TestPropertySource(properties = {
        "graphql.schemaLocationPattern=**/*configuration-test-schema.graphqls",
})
public class SpringMvcTest {

    @Autowired
    MockMvc mockMvc;

    static final String expectedStringValue = UUID.randomUUID().toString();
    static final BigDecimal expectedDecimalValue = new BigDecimal("10.01");
    static final String expectedLocalDateValue = "2020-06-27";

    @Test
    public void projectionTest() throws Exception {
        mockMvc.perform(get("/test/projection/getAllFields"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.stringField").value(expectedStringValue))
                .andExpect(jsonPath("$.data.decimalField").value(expectedDecimalValue))
                .andExpect(jsonPath("$.data.localDateField").value(expectedLocalDateValue))

                .andDo(print());
    }

    @Test
    public void projectionWithFetcherTest() throws Exception {
        mockMvc.perform(get("/test/projection/getAllFieldsWithDates")
                .param("countDates", "5"))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.stringField").value(expectedStringValue))
                .andExpect(jsonPath("$.data.decimalField").value(expectedDecimalValue))
                .andExpect(jsonPath("$.data.localDateField").value(expectedLocalDateValue))

                .andExpect(jsonPath("$.data.getRandomLocalDates.length()").value(5))

                .andDo(print());
    }

    @Test
    public void executeQueryTest() throws Exception {

        String payload = "{ \"query\": \"query{stringField, decimalField, localDateField, dateField}\"}";

        mockMvc.perform(post("/test/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.stringField").value(expectedStringValue))
                .andExpect(jsonPath("$.data.decimalField").value(expectedDecimalValue))
                .andExpect(jsonPath("$.data.localDateField").value(expectedLocalDateValue))

                .andDo(print());
    }

    @Test
    public void executeQueryWithFetcherTest() throws Exception {

        String payload = "{ \"query\": \"query($countDates: Int){stringField, decimalField, localDateField, dateField" +
                ", getRandomLocalDates(count: $countDates)}\"" +
                ", \"variables\": { \"countDates\": \"5\"}" +
                " }";

        mockMvc.perform(post("/test/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.data.stringField").value(expectedStringValue))
                .andExpect(jsonPath("$.data.decimalField").value(expectedDecimalValue))
                .andExpect(jsonPath("$.data.localDateField").value(expectedLocalDateValue))

                .andExpect(jsonPath("$.data.getRandomLocalDates.length()").value(5))

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
            return () -> new RootObject(expectedStringValue
                    , new BigDecimal("10.01")
                    , LocalDate.of(2020, 6, 27)
                    , new Date());
        }

        @Bean
        public ObjectMapper getObjectMapper() {
            return new ObjectMapper();
        }
    }

}
