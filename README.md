## Integrating GraphQL Java implementation with Spring Framework

This is a Java library for integrating [GraphQl-Java](https://github.com/graphql-java/graphql-java) and [Spring](https://github.com/spring-projects). It provides an approach to using stored queries and managed fetchers through Spring XML-configuration. Also, take advantage of the Spring-boot configuration for the most important aspects of the GraphQl engine.
### How to start 
 
* #### Executing stored queries for graphql
     1. Define the Java class of the root object
     
             public class RootObject implements Serializable {
             
                 private String stringField;
                 private BigDecimal decimalField;
                 private LocalDate localDateField;
                 private Date dateField;
             }
         
     1. Define a schema that describes the root object
     
              scalar BigDecimal
              scalar LocalDate
              scalar Date
              
              schema {
                  query: Query
              }
              
              type Query{
              
                  stringField: String
                  decimalField: BigDecimal
                  localDateField: LocalDate
                  dateField: Date
              }
     
     1. Define the schema path in the `application.properties` or `application.yml` file.
     
            graphql.schemaLocationPattern=**/*schema.graphqls     
          
     1. Define stored queries in the Spring xml-configuration `application.xml`.
     
             <util:map id="projectionQueries" key-type="java.lang.String" value-type="java.lang.String">
         
                 <entry key="getAllFields"
                        value="query{stringField, decimalField, localDateField, dateField} "/>
                        
             </util:map>
           
     1. Create and define a root object provider is an implementation of the `GraphQLRootObjectBuilder` interface.
     
             @Bean
             GraphQLRootObjectBuilder graphQLRootObjectBuilder() {
                  return () -> new RootObject(UUID.randomUUID().toString()
                         , new BigDecimal("10.01")
                         , LocalDate.now()
                         , new Date());
             }
              
     1. Creating Spring-mvc controller or JAX-RS servlet
     
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
                
             }
         
     1. Define the controller in an application configuration
     
            @Bean
            public SpringMvcTestController graphQlController() {
                 return new SpringMvcTestController();
            }
    
     1. When the application is running, the request below can be executed:
    
            request test/projection/{nameStoredQuery}
         
               GET http://localhost:8080/test/projection/getAllFields
               Accept: application/json
               Cache-Control: no-cache
         
         
            response 
         
               HTTP/1.1 200
               Content-Type: application/json;charset=UTF-8
               Content-Length: 157
                   {
                       "data": {
                       "stringField": "05807ab1-8bc7-42ba-b451-a883cdf00709",
                       "decimalField": 10.01,
                       "localDateField": "2020-06-27",
                       "dateField": "2020-06-27T15:15:12.267+0300"
                   }
      
     >An example of the implementation of all the above steps can be found in `org.babich.graphql.examples.queries.TestingWebApplication`.

 * #### Pagination and connections
 
     >Implements the [Relay Cursor Connections](https://relay.dev/graphql/connections.htm) specification
     
     1. Define a schema that describes connections
     
            directive @connection(for: String!) on FIELD_DEFINITION
            
            scalar BigDecimal
            scalar LocalDate
            scalar Date
            
            schema {
                query: Query
            }
            
            type Query{
            
                getRandomTestObjects(first: Int, after: String, last: Int, before: String, filter: String): RootObjectConnection @connection(for: "RootObject")
            }
            
            type RootObject{
            
                stringField: String
                decimalField: BigDecimal
                localDateField: LocalDate
                dateField: Date
            }
     
     1. Define stored queries in the Spring xml-configuration - `application.xml`.

             <util:map id="projectionQueries" key-type="java.lang.String" value-type="java.lang.String">
         
                 <entry key="getAllFields"
                        value="query{stringField, decimalField, localDateField, dateField} "/>

                 <entry key="getPaginatedCollectionByFilter"
                         value="query($pageSize: Int, $nextPage: String, $containsCharacter: String)
                             { getRandomTestObjects(first: $pageSize, after: $nextPage, filter: $containsCharacter)
                                 { edges { node {stringField, decimalField, localDateField}, cursor }
                                  , pageInfo{hasPreviousPage, hasNextPage, startCursor, endCursor} } } "/>
         
             </util:map>
             
             <ql:type type-name="Query">

                  <!--  Fetcher definition for `getRandomTestObjects` field  -->
                  <ql:field field-name="getRandomTestObjects"
                               fetcher-class="org.babich.graphql.test.utils.fetchers.RelayCursorConnectionsFetcher"/>
             
             </ql:type>
        
     1. Implement the Fetcher for the field `query.getRandomTestObjects`.

             public class RelayCursorConnectionsFetcher implements DataFetcher<Connection<RootObject>> {
             
                 private static final List<RootObject> onceGeneratedData = generateRandomData(100);
             
                 private static final String prefix = RelayCursorConnectionsFetcher.class.getSimpleName();
             
                 @Override
                 public Connection<RootObject> get(DataFetchingEnvironment environment) {
             
                     String containsCharacter = environment.getArgument("filter");
             
                     Predicate<RootObject> predicate = rootObject ->
                             rootObject.getStringField().contains(containsCharacter);
             
                     return new SimpleListConnection<>(onceGeneratedData.stream()
                             .filter(predicate)
                             .collect(Collectors.toList()), prefix).get(environment);
                 }
             }
     1. When the application is running, the request below can be executed:
           
             request test/projection/{nameStoredQuery}?pageSize={num}&nextPage={cursor}&containsCharacter={str}

                GET http://localhost:8080/test/projection/getPaginatedCollectionByFilter?containsCharacter=a&pageSize=2
                Accept: application/json
                Cache-Control: no-cache
             
             
             response
                 
                HTTP/1.1 200 
                Content-Type: application/json;charset=UTF-8
                Content-Length: 564
                
                {
                  "data": {
                    "getRandomTestObjects": {
                      "edges": [
                        {
                          "node": {
                            "stringField": "2b89b7c4236a4789b3e692205d94dd3c",
                            "decimalField": 0.15514964887734872,
                            "localDateField": "1309-02-22"
                          },
                          "cursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIw"
                        },
                        {
                          "node": {
                            "stringField": "48cc77af98f24f8fa78d8835799f3869",
                            "decimalField": 0.98891017086623,
                            "localDateField": "0987-08-08"
                          },
                          "cursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIx"
                        }
                      ],
                      "pageInfo": {
                        "hasPreviousPage": false,
                        "hasNextPage": true,
                        "startCursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIw",
                        "endCursor": "UmVsYXlDdXJzb3JDb25uZWN0aW9uc0ZldGNoZXIx"
                      }
                    }
                  }
                }