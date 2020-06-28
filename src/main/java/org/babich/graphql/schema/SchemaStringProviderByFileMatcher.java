/**
 * @author Vadim Babich
 */

package org.babich.graphql.schema;

import org.apache.commons.io.IOUtils;
import org.babich.graphql.SchemaStringProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Finding a graphQL schema in the project by file path matcher.
 */
public class SchemaStringProviderByFileMatcher implements SchemaStringProvider {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private ApplicationContext applicationContext;

    private final String locationPattern;

    public SchemaStringProviderByFileMatcher(String locationPattern) {
        this.locationPattern = locationPattern;
    }


    @Override
    public String schemaStrings() throws IOException {

        Resource[] resources = applicationContext.getResources("classpath*:" + locationPattern);

        validate(resources);

        Resource schema = resources[0];
        log.info("GraphQL schema {} is applied.", schema.getDescription());

        return readSchema(schema);
    }


    private static String readSchema(Resource resource) {

        StringWriter writer = new StringWriter();
        try (InputStream inputStream = resource.getInputStream()) {
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read graphql schema from resource " + resource, e);
        }
        return writer.toString();
    }

    private static String prettyPrintResourcesAsList(Resource[] resources) {

        return Arrays.stream(resources)
                .map(Resource::getDescription)
                .collect(Collectors.joining(",\n\t", "\t{", "}"));
    }

    protected void validate(Resource[] resources) {

        if (resources.length == 0) {

            throw new IllegalStateException("No graphql schema files found on classpath with location pattern '"
                    + locationPattern
                    + "'.  Please add a graphql schema to the classpath or add a SchemaParser bean to your application context.");
        }

        if (resources.length > 1) {

            log.warn("More then one graphql schema files found on classpath with location pattern '{}'. \n {} "
                            + "\nPlease change matcher pattern {graphql.schemaLocationPattern}."
                    , locationPattern
                    , prettyPrintResourcesAsList(resources));
        }
    }

}
