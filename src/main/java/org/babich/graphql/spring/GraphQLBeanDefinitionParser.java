/**
 * @author Vadim Babich
 */
package org.babich.graphql.spring;

import org.babich.graphql.spring.factory.GraphQLQueryFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;

/**
 * Loads data fetchers the TypeRuntimeWiring object from an XML file.
 */
public class GraphQLBeanDefinitionParser implements BeanDefinitionParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        if (isIgnore(element)) {
            return null;
        }

        String typeNme = element.getAttribute("type-name");

        List<Element> elements = DomUtils.getChildElementsByTagName(element, "field");
        if (elements.isEmpty()) {
            parserContext.getReaderContext()
                    .error("At least one sub-element ('field') must be defined", element);
        }

        elements.forEach(item -> registerField(typeNme, item, parserContext));

        return null;
    }

    private void registerField(String typeName, Element fieldElement, ParserContext parserContext) {

        String fieldName = fieldElement.getAttribute("field-name");
        if (StringUtils.isEmpty(fieldName)) {
            parserContext.getReaderContext().error("Attribute ('field-name') must be defined", fieldElement);
        }

        String fetcherClassName = fieldElement.getAttribute("fetcher-class");
        String fetcherReference = fieldElement.getAttribute("fetcher-ref");
        if (attributesNotDefined(fetcherClassName, fetcherReference)) {
            parserContext.getReaderContext()
                    .error("Attribute ('fetcher-class' or 'fetcher-ref') must be defined", fieldElement);
        }

        String fetcherBean = StringUtils.isEmpty(fetcherReference) ? registerFetcher(fetcherClassName, parserContext)
                : fetcherReference;

        registerComponent(parserContext,
                build(GraphQLQueryFactoryBean.class)
                        .addPropertyValue("typeName", typeName)
                        .addPropertyValue("fieldName", fieldName)
                        .addPropertyReference("dataFetcher", fetcherBean)
        );

        logger.debug("New resolver has been registered for type {} in field {} through fetcher {}."
                , typeName, fieldName, fetcherBean);
    }


    private String registerFetcher(String fetcherClassName, ParserContext parserContext) {
        return registerComponent(parserContext, build(fetcherClassName));
    }

    private boolean isIgnore(Element element) {
        return !"type".equals(element.getLocalName());
    }

    private static BeanDefinitionBuilder build(Class<?> beanClass) {
        return BeanDefinitionBuilder.rootBeanDefinition(beanClass).setRole(ROLE_APPLICATION);
    }

    private static BeanDefinitionBuilder build(String beanClassName) {
        return BeanDefinitionBuilder.rootBeanDefinition(beanClassName).setRole(ROLE_APPLICATION);
    }

    private static String registerComponent(ParserContext parserContext, BeanDefinitionBuilder beanDefBuilder) {
        final BeanDefinition beanDef = beanDefBuilder.getBeanDefinition();
        final String beanName = parserContext.getReaderContext().registerWithGeneratedName(beanDef);
        parserContext.registerComponent(new BeanComponentDefinition(beanDef, beanName));
        return beanName;
    }

    private static boolean attributesNotDefined(String... attributes) {
        for (String attribute : attributes) {
            if (StringUtils.isEmpty(attribute)) {
                return false;
            }
        }
        return true;
    }

}
