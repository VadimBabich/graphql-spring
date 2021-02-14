/**
 * @author Vadim Babich
 */

package org.babich.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.babich.graphql.configuration.ScalarConfiguration;
import org.babich.graphql.test.utils.mathers.DateMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZoneId;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ScalarConfiguration.class})
public class DateCoercingTest {
    //yyyy-MM-dd'T'HH:mm:ss.SSSZ

    @Autowired
    private GraphQLScalarType dateScalarType;

    @Test
    public void parseValue() {
        Coercing coercing = dateScalarType.getCoercing();
        ZoneId zoneOffset = ZoneId.of("+0400");

        Date date = (Date) coercing.parseValue("1985-04-12T23:20:50.369+0400");

        Assert.assertThat(date, DateMatcher.equalTo(zoneOffset
                , 1985, 4, 12, 23, 20, 50, 369));
    }

}