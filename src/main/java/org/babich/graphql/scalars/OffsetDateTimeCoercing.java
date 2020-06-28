/**
 * @author Vadim Babich
 */

package org.babich.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

public class OffsetDateTimeCoercing implements Coercing<OffsetDateTime, String> {

    private final DateTimeFormatter dateFormatter;


    public OffsetDateTimeCoercing(String pattern) {
        dateFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {

        OffsetDateTime offsetDateTime = getOffsetDateTime(dataFetcherResult);

        try {
            return dateFormatter.format(offsetDateTime);
        } catch (DateTimeException e) {

            throw new CoercingSerializeException(String
                    .format("Unable to turn TemporalAccessor into OffsetDateTime because of : {%s}."
                            , e.getMessage()));
        }
    }

    @Override
    public OffsetDateTime parseValue(Object input) throws CoercingParseValueException {
        return getOffsetDateTime(input);
    }

    @Override
    public OffsetDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            return parseOffsetDateTime(((StringValue) input).getValue(), CoercingParseLiteralException::new);
        }

        throw new CoercingParseLiteralException(String
                .format("Expected AST type 'StringValue' but was {%s}", input.getClass().getCanonicalName()));
    }

    private OffsetDateTime getOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime) {
            return (OffsetDateTime) value;
        }

        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toOffsetDateTime();
        }

        if (value instanceof String) {
            return parseOffsetDateTime(value.toString(), CoercingSerializeException::new);
        }

        throw new CoercingSerializeException(String
                .format("Expected something we can convert to 'java.time.OffsetDateTime' but was {%s}."
                        , value.getClass().getCanonicalName()));
    }

    private OffsetDateTime parseOffsetDateTime(String dateTimeString
            , Function<String
            , RuntimeException> exceptionMaker) {

        try {
            return OffsetDateTime.parse(dateTimeString, dateFormatter);
        } catch (DateTimeParseException e) {

            throw exceptionMaker.apply(String.format("Invalid RFC3339 value : {%s}. because of : {%s}"
                    , dateTimeString
                    , e.getMessage()));
        }
    }
}
