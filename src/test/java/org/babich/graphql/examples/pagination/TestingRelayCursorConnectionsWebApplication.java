/**
 * @author Vadim Babich
 */

package org.babich.graphql.examples.pagination;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.babich.graphql.GraphQLRootObjectBuilder;
import org.babich.graphql.controller.RelayCursorConnectionsTest;
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
import java.util.function.Supplier;


/**
 * request GET http://localhost:8080/test/projection/{queryName}?pageSize={num}&nextPage={cursor}&containsCharacter={str}
 * <p></p>
 * queryName located in {@code src/test/resources/graphql/projection-test-context.xml}
 * <p></p>
 * example:
 * <pre>
 *
 * GET http://localhost:8080/test/projection/getPaginatedCollectionByFilter?containsCharacter=a&pageSize=2
 *
 * HTTP/1.1 200
 * Content-Type: application/json;charset=UTF-8
 * Content-Length: 731
 *
 * {
 *   "data": {
 *     "rootTestObject": {
 *       "stringField": "11f70348-db64-486c-b650-90af41526784",
 *       "decimalField": 10.01,
 *       "localDateField": "2020-06-27",
 *       "dateField": "2020-06-28T12:47:37.305+0300"
 *     },
 *     "getRandomTestObjects": {
 *       "edges": [
 *         {
 *           "node": {
 *             "stringField": "708769596da6428eaf466c8fa1607e46",
 *             "decimalField": 0.4353377906316077,
 *             "localDateField": "1412-04-15"
 *           },
 *           "cursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIw"
 *         },
 *         {
 *           "node": {
 *             "stringField": "4335f25ec9d744b3a6cfe152720ae5d5",
 *             "decimalField": 0.8450920759952562,
 *             "localDateField": "0075-10-11"
 *           },
 *           "cursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIx"
 *         }
 *       ],
 *       "pageInfo": {
 *         "hasPreviousPage": false,
 *         "hasNextPage": true,
 *         "startCursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIw",
 *         "endCursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIx"
 *       }
 *     }
 *   }
 * }
 *
 * Response code: 200; Time: 254ms; Content length: 731 bytes
 * </pre>
 */
@SpringBootApplication(scanBasePackageClasses = {TestingRelayCursorConnectionsWebApplication.class})
@ContextConfiguration(classes = {TestingRelayCursorConnectionsWebApplication.Config.class})
public class TestingRelayCursorConnectionsWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingRelayCursorConnectionsWebApplication.class, args);
    }

    @Configuration
    @PropertySource("classpath:relay-cursor-connections-test-application.properties")
    @ImportResource(value = {"classpath*:graphql/projection-test-context.xml"})
    public static class Config {

        @Bean
        public SpringMvcTestController graphQlController() {
            return new SpringMvcTestController();
        }

        @Bean
        public ObjectMapper getObjectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public GraphQLRootObjectBuilder graphQLRootObjectBuilder() {

            Supplier<RootObject> rootObjectSupplier =
                    new RelayCursorConnectionsTest.Config.RootObjectSupplier(
                            new RootObject(UUID.randomUUID().toString()
                                    , new BigDecimal("10.01")
                                    , LocalDate.of(2020, 6, 27)
                                    , new Date()));

            return () -> rootObjectSupplier;
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
