package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queueu1 = new LinkedBlockingDeque<>();

        subscribeToBlockingStack(queueu1)
                .subscribe(element -> System.out.println("received: " + element),
                        error -> System.out.println(error),
                        () -> System.out.println("COMPLETE!!!!"));

        fillQueue(queueu1)
                .subscribe(element -> System.out.println("pushed: " + element),
                        error -> System.out.println("error from publisher: " + error),
                        () -> System.out.println("COMPLETE PUBLISHING!!!!"));

        queueu1.add(10);
        queueu1.add(100);
        Thread.sleep(100000);
    }

    private static Flux<Integer> fillQueue(BlockingQueue<Integer> queueu1) {
        return Flux.interval(Duration.ofSeconds(1))
                .map(Long::intValue)
                .doOnNext(index -> queueu1.add(index))
                .takeUntil(amountOfPushes -> amountOfPushes < 2);
    }

    private static Flux<Tuple2<Long, Integer>> subscribeToBlockingStack(BlockingQueue<Integer> queueu1) {

        return Flux.create((FluxSink<Integer> sink) -> {
            while (true) {
                try {
                    Integer element = queueu1.take();
                    sink.next(element);
                } catch (InterruptedException e) {
                    sink.complete();
                }
            }
        }).index().subscribeOn(Schedulers.single());
    }

}
