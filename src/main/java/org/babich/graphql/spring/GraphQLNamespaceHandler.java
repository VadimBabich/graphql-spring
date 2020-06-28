/**
 * @author Vadim Babich
 */
package org.babich.graphql.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Simple namespace handler for graphql namespace.
 */
public class GraphQLNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("type", new GraphQLBeanDefinitionParser());
    }

}
