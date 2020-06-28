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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.function.Function;

public class DateCoercing implements Coercing<Date, String> {

    private final DateTimeFormatter dateFormatter;


    public DateCoercing(String pattern) {
        dateFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
        Date date = getDate(dataFetcherResult);

        try {
            return dateFormatter
                    .format(OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        } catch (DateTimeException e) {

            throw new CoercingSerializeException(String
                    .format("Unable to turn Date into full date because of : {%s}"
                            , e.getMessage()));
        }
    }

    @Override
    public Date parseValue(Object input) throws CoercingParseValueException {
        return getDate(input);
    }

    @Override
    public Date parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            return parseDate(((StringValue) input).getValue(), CoercingParseLiteralException::new);
        }

        throw new CoercingParseLiteralException(String
                .format("Expected AST type 'StringValue' but was {%s}", input.getClass().getCanonicalName()));
    }


    private Date getDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof String) {
            return parseDate(value.toString(), CoercingSerializeException::new);
        }

        throw new CoercingSerializeException(
                String.format("Expected a 'String' or 'java.util.Date' but was {%s}"
                        , value.getClass().getCanonicalName()));
    }

    public Date parseDate(String dateString, Function<String, RuntimeException> exceptionMaker) {
        try {

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, dateFormatter);

            return Date.from(offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toInstant());

        } catch (DateTimeParseException e) {

            throw exceptionMaker.apply(String.format("Invalid RFC3339 full date value : {%s}. because of : {%s}"
                    , dateString
                    , e.getMessage()));
        }
    }
}
