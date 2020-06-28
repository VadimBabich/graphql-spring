/**
 * @author Vadim Babich
 */

package org.babich.graphql.schema;

import graphql.GraphQLError;
import graphql.language.*;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The directive '@connection' is extending the schema for the specified type as below:
 * <p>schema</p>
 * <pre>
 *
 * 	directive @connection(for: String!) on FIELD_DEFINITION
 *
 *
 *      type PageInfo {
 *                 hasPreviousPage: Boolean!
 *                 hasNextPage: Boolean!
 *                 startCursor: String
 *                 endCursor: String
 *             }
 *
 *      type {TypeName}Connection{
 *                 edges: [{TypeName}ConnectionEdge]
 *                 pageInfo: PageInfo
 *             }
 *
 *      type {TypeName}ConnectionEdge {
 *                 cursor: String
 *                 node: TelecomServiceAsset
 *             }
 *
 *      type Query{
 *
 *                 gettingSomeCollectionOf(first: Int, after: String, last: Int, before: String, filter: String): {TypeName}Connection @connection(for: "{TypeName}")
 *             				. . .
 *            }
 * </pre>
 */
public class DirectiveExtensionSchemaParser extends SchemaParser {

    @Override
    public TypeDefinitionRegistry buildRegistry(Document document) {
        List<GraphQLError> errors = new ArrayList<>();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();

        Consumer<ObjectTypeDefinition> objectTypeDefinitionConsumer =
                registrationOfSDLDefinitions(document, typeRegistry, errors);

        List<DirectiveWithField> directiveWithFields = findDirectiveWithField(document);

        createdRelayCursorTypeDefinitions(directiveWithFields).forEach(objectTypeDefinitionConsumer);

        if (errors.isEmpty()) {
            return typeRegistry;
        }

        throw new SchemaProblem(errors);
    }


    @SuppressWarnings("rawtypes")
    private Consumer<ObjectTypeDefinition> registrationOfSDLDefinitions(Document document
            , TypeDefinitionRegistry typeRegistry
            , List<GraphQLError> errors) {

        Consumer<SDLDefinition> registrationExistedDefinitions = item -> typeRegistry.add(item).ifPresent(errors::add);

        Predicate<SDLDefinition> onlyTypeDefinition = item -> TypeDefinition.class.isAssignableFrom(item.getClass());

        Stream<SDLDefinition> documentDefinitionStream = document.getDefinitionsOfType(SDLDefinition.class)
                .stream()
                .peek(registrationExistedDefinitions);

        Map<String, TypeDefinition> definitionMap = documentDefinitionStream
                .filter(onlyTypeDefinition)
                .map(TypeDefinition.class::cast)
                .collect(Collectors.toMap(TypeDefinition::getName, Function.identity()));

        return item -> {
            if (definitionMap.containsKey(item.getName())) {
                return;
            }

            definitionMap.put(item.getName(), item);
            typeRegistry.add(item).ifPresent(errors::add);
        };
    }

    private List<DirectiveWithField> findDirectiveWithField(Document document) {

        List<ObjectTypeDefinition> definitions = document.getDefinitionsOfType(ObjectTypeDefinition.class);

        Function<ObjectTypeDefinition, Stream<FieldDefinition>> toFieldDefinition = (definition) ->
                definition.getFieldDefinitions().stream();

        Function<FieldDefinition, Stream<DirectiveWithField>> toDirectives = (definition) ->
                definition.getDirectives().stream()
                        .map(toDirectivesWithField(definition));

        return definitions.stream()
                .flatMap(toFieldDefinition)
                .flatMap(toDirectives)
                .collect(Collectors.toList());
    }

    private Function<Directive, DirectiveWithField> toDirectivesWithField(FieldDefinition fieldDefinition) {

        return item -> new DirectiveWithField(fieldDefinition
                , item.getName()
                , item.getArguments()
                , item.getSourceLocation()
                , item.getComments()
                , item.getIgnoredChars()
                , item.getAdditionalData());
    }

    private Stream<ObjectTypeDefinition> createdRelayCursorTypeDefinitions(List<DirectiveWithField> directives) {
        return directives.stream()
                .filter(RelayCursorConnectionsSchemaDefinitionHelper.isConnection)
                .flatMap(RelayCursorConnectionsSchemaDefinitionHelper::createDefinitions);
    }

    private Stream<ObjectTypeDefinition> createdFetchDefinitions(List<DirectiveWithField> directives) {
        return directives.stream()
                .filter(RelayCursorConnectionsSchemaDefinitionHelper.isConnection)
                .flatMap(RelayCursorConnectionsSchemaDefinitionHelper::createDefinitions);
    }

    static class RelayCursorConnectionsSchemaDefinitionHelper {

        static final Predicate<DirectiveWithField> isConnection = (directive) ->
                "connection".equals(directive.getName());

        static private Stream<ObjectTypeDefinition> createDefinitions(DirectiveWithField directive) {
            String type = directive.getTypeName();

            return Stream.<ObjectTypeDefinition>builder()
                    .add(createConnectionDefinition(type))
                    .add(createEdgeDefinition(type, forTypeName(directive)))
                    .add(createPageInfo())
                    .build();
        }

        static private String forTypeName(Directive directive) {
            graphql.language.Value<?> value = directive.getArgument("for").getValue();
            if (value instanceof StringValue) {
                return ((StringValue) value).getValue();
            }

            throw new IllegalArgumentException("Unsupported value type " + value.getClass());
        }

        static private ObjectTypeDefinition createConnectionDefinition(String type) {
            FieldDefinition edgesDefinition =
                    new FieldDefinition("edges", new ListType(new TypeName(type + "Edge")));

            FieldDefinition pageInfoDefinition =
                    new FieldDefinition("pageInfo", new TypeName("PageInfo"));

            return ObjectTypeDefinition.newObjectTypeDefinition()
                    .name(type)
                    .fieldDefinition(edgesDefinition)
                    .fieldDefinition(pageInfoDefinition)
                    .build();
        }

        static private ObjectTypeDefinition createEdgeDefinition(String connectionType, String nodeType) {
            FieldDefinition cursorDefinition = new FieldDefinition("cursor", new TypeName("String"));
            FieldDefinition nodeDefinition = new FieldDefinition("node", new TypeName(nodeType));

            return ObjectTypeDefinition.newObjectTypeDefinition()
                    .name(connectionType + "Edge")
                    .fieldDefinition(cursorDefinition)
                    .fieldDefinition(nodeDefinition)
                    .build();
        }

        static private ObjectTypeDefinition createPageInfo() {
            FieldDefinition hasPreviousPageDefinition =
                    new FieldDefinition("hasPreviousPage", new NonNullType(new TypeName("Boolean")));

            FieldDefinition hasNextPageDefinition =
                    new FieldDefinition("hasNextPage", new NonNullType(new TypeName("Boolean")));

            FieldDefinition startCursorDefinition =
                    new FieldDefinition("startCursor", new NonNullType(new TypeName("String")));

            FieldDefinition endCursorDefinition =
                    new FieldDefinition("endCursor", new NonNullType(new TypeName("String")));

            return ObjectTypeDefinition.newObjectTypeDefinition()
                    .name("PageInfo")
                    .fieldDefinition(hasPreviousPageDefinition)
                    .fieldDefinition(hasNextPageDefinition)
                    .fieldDefinition(startCursorDefinition)
                    .fieldDefinition(endCursorDefinition)
                    .build();
        }
    }

    static class DirectiveWithField extends Directive {

        final FieldDefinition field;

        protected DirectiveWithField(FieldDefinition field
                , String name
                , List<Argument> arguments
                , SourceLocation sourceLocation
                , List<Comment> comments
                , IgnoredChars ignoredChars
                , Map<String, String> additionalData) {
            super(name, arguments, sourceLocation, comments, ignoredChars, additionalData);
            this.field = field;
        }

        String getTypeName() {

            Type<?> type = field.getType();
            if (type instanceof NonNullType) {
                TypeName typeName = (TypeName) ((NonNullType) type).getType();
                return typeName.getName();
            }

            TypeName typeName = (TypeName) type;
            return typeName.getName();
        }

    }
}
