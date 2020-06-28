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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

public class LocalDateCoercing implements Coercing<LocalDate, String> {

    private final DateTimeFormatter dateFormatter;


    public LocalDateCoercing(String pattern) {
        dateFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {

        TemporalAccessor temporalAccessor = getTemporalAccessor(dataFetcherResult);

        try {
            return dateFormatter.format(temporalAccessor);
        } catch (DateTimeException e) {

            throw new CoercingSerializeException(String
                    .format("Unable to turn TemporalAccessor into full date because of : {%s}"
                            , e.getMessage()));
        }
    }

    @Override
    public LocalDate parseValue(Object input) throws CoercingParseValueException {

        TemporalAccessor temporalAccessor = getTemporalAccessor(input);

        try {
            return LocalDate.from(temporalAccessor);
        } catch (DateTimeException e) {

            throw new CoercingSerializeException(String
                    .format("Unable to turn TemporalAccessor into full date because of : {%s}"
                            , e.getMessage()));
        }
    }

    @Override
    public LocalDate parseLiteral(Object input) throws CoercingParseLiteralException {

        if (input instanceof StringValue) {
            return parseLocalDate(((StringValue) input).getValue(), CoercingParseLiteralException::new);
        }

        throw new CoercingParseLiteralException(String
                .format("Expected AST type 'StringValue' but was {%s}", input.getClass().getCanonicalName()));
    }

    private TemporalAccessor getTemporalAccessor(Object value) {

        if (value instanceof TemporalAccessor) {
            return (TemporalAccessor) value;
        }

        if (value instanceof String) {
            return parseLocalDate(value.toString(), CoercingSerializeException::new);
        }

        throw new CoercingSerializeException(
                String.format("Expected a 'String' or 'java.time.temporal.TemporalAccessor' but was {%s}"
                        , value.getClass().getCanonicalName()));
    }

    private LocalDate parseLocalDate(String dateString, Function<String, RuntimeException> exceptionMaker) {
        try {

            TemporalAccessor temporalAccessor = dateFormatter.parse(dateString);
            return LocalDate.from(temporalAccessor);

        } catch (DateTimeParseException e) {

            throw exceptionMaker.apply(String.format("Invalid RFC3339 full date value : {%s}. because of : {%s}"
                    , dateString
                    , e.getMessage()));
        }
    }
}
