/**
 * @author Vadim Babich
 */

package org.babich.graphql.test.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.StringJoiner;

public class RootObject implements Serializable {

    private final String stringField;
    private final BigDecimal decimalField;
    private final LocalDate localDateField;
    private final Date dateField;

    public RootObject(String stringField, BigDecimal decimalField, LocalDate localDateField, Date dateField) {
        this.stringField = stringField;
        this.decimalField = decimalField;
        this.localDateField = localDateField;
        this.dateField = dateField;
    }

    public String getStringField() {
        return stringField;
    }

    public BigDecimal getDecimalField() {
        return decimalField;
    }

    public LocalDate getLocalDateField() {
        return localDateField;
    }

    public Date getDateField() {
        return dateField;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RootObject.class.getSimpleName() + "[", "]")
                .add("stringField='" + stringField + "'")
                .add("decimalField=" + decimalField)
                .add("localDateField=" + localDateField)
                .add("dateField=" + dateField)
                .toString();
    }
}
