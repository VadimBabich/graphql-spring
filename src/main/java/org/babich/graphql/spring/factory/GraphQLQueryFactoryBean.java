/**
 * @author Vadim Babich
 */
package org.babich.graphql.spring.factory;

import graphql.schema.DataFetcher;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.beans.factory.FactoryBean;

import java.util.StringJoiner;

public class GraphQLQueryFactoryBean implements FactoryBean<TypeRuntimeWiring> {

    private String typeName;
    private String fieldName;
    private DataFetcher dataFetcher;

    public GraphQLQueryFactoryBean() {
    }

    @Override
    public TypeRuntimeWiring getObject() {

        return TypeRuntimeWiring
                .newTypeWiring(typeName)
                .dataFetcher(fieldName, dataFetcher)
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return TypeRuntimeWiring.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", GraphQLQueryFactoryBean.class.getSimpleName() + "[", "]")
                .add("typeName='" + typeName + "'")
                .add("fieldName='" + fieldName + "'")
                .add("dataFetcher=" + dataFetcher)
                .toString();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public DataFetcher getDataFetcher() {
        return dataFetcher;
    }

    public void setDataFetcher(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }
}
