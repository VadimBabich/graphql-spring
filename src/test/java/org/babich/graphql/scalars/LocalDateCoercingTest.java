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

import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ScalarConfiguration.class})
public class LocalDateCoercingTest {
    //yyyy-MM-dd

    @Autowired
    private GraphQLScalarType localDateScalarType;

    @Test
    public void parseValue() {
        Coercing coercing = localDateScalarType.getCoercing();
        LocalDate date = (LocalDate) coercing.parseValue("1985-04-12");

        Assert.assertEquals(LocalDate.of(1985, 04, 12), date);
    }
}