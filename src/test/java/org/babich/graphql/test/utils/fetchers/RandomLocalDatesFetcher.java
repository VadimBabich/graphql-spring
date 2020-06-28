/**
 * @author Vadim Babich
 */

package org.babich.graphql.test.utils.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomLocalDatesFetcher implements DataFetcher<List<LocalDate>> {

    @Override
    public List<LocalDate> get(DataFetchingEnvironment environment) {

        Integer count = environment.getArgument("count");

        if (null == count || count == 0) {
            return null;
        }

        Supplier<LocalDate> localDateSupplier = () -> {
            ThreadLocalRandom localRandom = ThreadLocalRandom.current();
            int year = localRandom.nextInt(1, 1999);
            int month = localRandom.nextInt(1, 12);
            int day = localRandom.nextInt(1, 28);
            return LocalDate.of(year, month, day);
        };

        return Stream.generate(localDateSupplier)
                .limit(count)
                .collect(Collectors.toList());
    }
}
