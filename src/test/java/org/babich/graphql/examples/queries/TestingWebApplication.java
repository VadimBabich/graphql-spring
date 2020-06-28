/**
 * @author Vadim Babich
 */
package org.babich.graphql.examples.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.babich.graphql.GraphQLRootObjectBuilder;
import org.babich.graphql.controller.SpringMvcTestController;
import org.babich.graphql.test.utils.RootObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * request GET http://localhost:8080/test/projection/{queryName}
 * <p></p>
 * queryName located in {@code src/test/resources/graphql/projection-test-context.xml}
 * <p></p>
 * example:
 * <pre>
 *      request
 *
 *      GET http://localhost:8080/test/projection/getAllFields
 *      Accept: application/json
 *      Cache-Control: no-cache
 *
 *
 *      response
 *
 *      HTTP/1.1 200
 *      Content-Type: application/json;charset=UTF-8
 *      Content-Length: 157
 *          {
 *              "data": {
 *              "stringField": "05807ab1-8bc7-42ba-b451-a883cdf00709",
 *              "decimalField": 10.01,
 *              "localDateField": "2020-06-27",
 *              "dateField": "2020-06-27T15:15:12.267+0300"
 *          }
 * }
 * </pre>
 */
@SpringBootApplication(scanBasePackageClasses = {TestingWebApplication.class})
@ContextConfiguration(classes = {TestingWebApplication.Config.class})
public class TestingWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingWebApplication.class, args);
    }

    @Configuration
    @PropertySource("classpath:test-application.properties")
    @ImportResource(value = {"classpath*:graphql/projection-test-context.xml"})
    public static class Config {

        @Bean
        public SpringMvcTestController graphQlController() {
            return new SpringMvcTestController();
        }


        @Bean
        GraphQLRootObjectBuilder graphQLRootObjectBuilder() {
            return () -> new RootObject(UUID.randomUUID().toString()
                    , new BigDecimal("10.01")
                    , LocalDate.now()
                    , new Date());
        }

        @Bean
        public ObjectMapper getObjectMapper() {
            return new ObjectMapper();
        }

    }

}
