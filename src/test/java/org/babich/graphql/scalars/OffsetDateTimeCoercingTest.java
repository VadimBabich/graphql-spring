/**
 * @author Vadim Babich
 */

package org.babich.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.babich.graphql.configuration.ScalarConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ScalarConfiguration.class})
public class OffsetDateTimeCoercingTest {
    //yyyy-MM-dd'T'HH:mm:ss.SSSZ

    @Autowired
    private GraphQLScalarType localDateTimeScalarType;

    @Test
    public void parseValue() {
        Coercing coercing = localDateTimeScalarType.getCoercing();
        ZoneId zoneOffset = ZoneId.of("+0400");

        OffsetDateTime date = (OffsetDateTime) coercing.parseValue("1985-04-12T23:20:50.369+0400");

        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.of(1985
                , 4
                , 12
                , 23
                , 20
                , 50
                , 369 * 1_000_000
                , ZoneOffset.of(zoneOffset.getId()));

        Assert.assertEquals(expectedOffsetDateTime, date);
    }
}