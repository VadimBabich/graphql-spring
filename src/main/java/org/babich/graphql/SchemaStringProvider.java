/**
 * @author Vadim Babich
 */

package org.babich.graphql;


import java.io.IOException;

/**
 * Finding a graphQL schema in the project.
 */
public interface SchemaStringProvider {

    String schemaStrings() throws IOException;

}

