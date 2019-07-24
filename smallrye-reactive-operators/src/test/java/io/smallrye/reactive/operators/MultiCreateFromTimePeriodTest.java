package io.smallrye.reactive.operators;

import io.smallrye.reactive.Multi;
import io.smallrye.reactive.subscription.BackPressureFailure;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class MultiCreateFromTimePeriodTest {


    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @After
    public void cleanup() {
        executor.shutdown();
    }

    @Test
    public void testIntervalOfAFewMillis() {
        MultiAssertSubscriber<Long> ts = MultiAssertSubscriber.create(Long.MAX_VALUE);

        // Add a fake result with the beginning time
        ts.results().add(System.currentTimeMillis());

        Multi.createFrom().ticks()
                .startingAfter(Duration.ofMillis(100)).onExecutor(executor).every(Duration.ofMillis(100))
                .onResult().mapToResult(l -> System.currentTimeMillis())
                .subscribe().withSubscriber(ts);

        await().until(() -> ts.results().size() == 10);
        ts.cancel();

        ts
                .assertHasNotCompleted()
                .assertHasNotFailed();

        List<Long> list = ts.results();
        for (int i = 0; i < list.size() - 1; i++) {
            long delta = list.get(i + 1) - list.get(i);
            assertThat(delta).isBetween(50L, 200L);
        }
    }

    @Test
    public void testWithInfraExecutorAndNoDelay() {
        MultiAssertSubscriber<Long> ts = MultiAssertSubscriber.create(Long.MAX_VALUE);

        // Add a fake result with the beginning time
        ts.results().add(System.currentTimeMillis());

        Multi.createFrom().ticks()
                .every(Duration.ofMillis(100))
                .onResult().mapToResult(l -> System.currentTimeMillis())
                .subscribe().withSubscriber(ts);

        await().until(() -> ts.results().size() == 10);
        ts.cancel();

        ts
                .assertHasNotCompleted()
                .assertHasNotFailed();

        List<Long> list = ts.results();
        for (int i = 0; i < list.size() - 1; i++) {
            long delta = list.get(i + 1) - list.get(i);
            assertThat(delta).isBetween(50L, 200L);
        }
    }

    @Test(timeout = 1000)
    public void testBackPressureOverflow() {
        MultiAssertSubscriber<Long> ts = MultiAssertSubscriber.create();

        ts.results().add(System.currentTimeMillis());

        Multi.createFrom().ticks()
                .startingAfter(Duration.ofMillis(50)).onExecutor(executor).every(Duration.ofMillis(50))
                .onResult().mapToResult(l -> System.currentTimeMillis())
                .subscribe().withSubscriber(ts);

        ts
                .request(2) // request only 2
                .await()   // wait until failure
                .assertHasFailedWith(BackPressureFailure.class, "lack of requests");
    }


}
