/**
 * @author Vadim Babich
 */

package org.babich.graphql.test.utils.fetchers;

import graphql.relay.Connection;
import graphql.relay.SimpleListConnection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.babich.graphql.test.utils.RootObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelayCursorConnectionsFetcher implements DataFetcher<Connection<RootObject>> {

    private static final List<RootObject> onceGeneratedData = generateRandomData(100);

    private static final String prefix = RelayCursorConnectionsFetcher.class.getSimpleName();

    @Override
    public Connection<RootObject> get(DataFetchingEnvironment environment) {

        String containsCharacter = environment.getArgument("filter");

        Predicate<RootObject> predicate = rootObject ->
                rootObject.getStringField().contains(containsCharacter);

        return new SimpleListConnection<>(onceGeneratedData.stream()
                .filter(predicate)
                .collect(Collectors.toList()), prefix).get(environment);
    }




    private static List<RootObject> generateRandomData(int limit) {

        ThreadLocalRandom localRandom = ThreadLocalRandom.current();

        Supplier<LocalDate> localDateSupplier = () -> {

            int year = localRandom.nextInt(1, 1999);
            int month = localRandom.nextInt(1, 12);
            int day = localRandom.nextInt(1, 28);
            return LocalDate.of(year, month, day);
        };

        Supplier<RootObject> rootObjectSupplier = () ->
                new RootObject(UUID.randomUUID().toString().replace("-", "")
                , BigDecimal.valueOf(localRandom.nextDouble())
                , localDateSupplier.get()
                , new Date());

        return Stream.generate(rootObjectSupplier)
                .limit(limit)
                .collect(Collectors.toList());
    }

}
