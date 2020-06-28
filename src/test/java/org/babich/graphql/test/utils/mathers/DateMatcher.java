/**
 * @author Vadim Babich
 */

package org.babich.graphql.test.utils.mathers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateMatcher extends BaseMatcher<Date> {

    private final ZoneId zoneOffset;
    final int year, month, day, hour, minute, sec, ms;

    public DateMatcher(int year, int month, int day, int hour, int minute, int sec, int ms) {
        this(ZoneId.systemDefault(), year, month, day, hour, minute, sec, ms);
    }

    public DateMatcher(ZoneId zoneOffset, int year, int month, int day, int hour, int minute, int sec, int ms) {
        this.zoneOffset = zoneOffset;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.sec = sec;
        this.ms = ms;
    }

    public static Matcher<Date> equalTo(ZoneId zoneOffset, int year, int month, int day, int hour, int minute, int sec, int ms) {
        return new DateMatcher(zoneOffset, year, month, day, hour, minute, sec, ms);
    }

    @Override
    public boolean matches(Object item) {
        Date date = (Date) item;

        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), zoneOffset);

        return localDateTime.getYear() == year
                && localDateTime.getMonthValue() == month
                && localDateTime.getDayOfMonth() == day
                && localDateTime.getHour() == hour
                && localDateTime.getMinute() == minute
                && localDateTime.getSecond() == sec
                && localDateTime.getNano() == ms * 1_000_000;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Date not equals ")
                .appendText(String.join(".", String.valueOf(year), String.valueOf(month), String.valueOf(day)))
                .appendText(" ").appendText(String.join(":", String.valueOf(hour), String.valueOf(minute), String.valueOf(sec)))
                .appendText(" ").appendText(String.valueOf(ms)).appendText("ms")
                .appendText(" Z").appendText(zoneOffset.getId());
    }

}
