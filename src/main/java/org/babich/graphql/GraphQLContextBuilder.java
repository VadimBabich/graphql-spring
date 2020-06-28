/**
 * @author Vadim Babich
 */

package org.babich.graphql;


import graphql.GraphQLContext;

import javax.servlet.http.HttpServletRequest;
import java.util.StringJoiner;

/**
 *  Builder of {@link GraphQLContext}.
 *  httpServletRequest - might be used in fetchers to obtain authentication information.
 *  isProjection - is a flag for using projection instead of a real qraphQL query.
 */
public interface GraphQLContextBuilder {

    String isProjection = "isProjection";
    String httpServletRequest = "httpServletRequest";

    GraphQLContextBuilder projection(boolean projection);

    GraphQLContextBuilder httpServletRequest(HttpServletRequest httpServletRequest);

    GraphQLContext build();


    /**
     * Default implementation of {@link GraphQLContextBuilder} based on {@link GraphQLContext.Builder}.
     */
    class ServletGraphQLContextBuilder implements GraphQLContextBuilder {

        private boolean projection;

        private HttpServletRequest request;


        @Override
        public GraphQLContextBuilder projection(boolean projection) {
            this.projection = projection;
            return this;
        }

        @Override
        public GraphQLContextBuilder httpServletRequest(HttpServletRequest httpServletRequest) {
            this.request = httpServletRequest;
            return this;
        }

        @Override
        public GraphQLContext build() {
            GraphQLContext.Builder context = GraphQLContext.newContext().of(isProjection, projection);

            if (null != request) {
                context.of(httpServletRequest, request);
            }

            return context.build();
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ServletGraphQLContextBuilder.class.getSimpleName() + "[", "]")
                    .add("projection=" + projection)
                    .add("request=" + request)
                    .toString();
        }
    }

}