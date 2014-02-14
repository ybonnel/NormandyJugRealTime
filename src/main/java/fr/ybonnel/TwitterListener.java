/*
 * Copyright 2013- Yan Bonnel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ybonnel;

import fr.ybonnel.model.TweetPhoto;
import fr.ybonnel.simpleweb4j.handlers.eventsource.EndOfStreamException;
import fr.ybonnel.simpleweb4j.handlers.eventsource.ReactiveHandler;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TwitterListener extends StatusAdapter {

    private final String filter;
    private final Consumer<String> onShutdown;
    private final TwitterStream twitterStream;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ConcurrentLinkedQueue<TweetPhoto> tweetToSend = new ConcurrentLinkedQueue<>();

    public TwitterListener(String filter, Consumer<String> onShutdown) {
        this.filter = filter;
        this.onShutdown = onShutdown;
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(this);
        twitterStream.filter(new FilterQuery().track(new String[]{filter}));
        this.executor.scheduleAtFixedRate(() -> {
            TweetPhoto tweet = tweetToSend.poll();
            if (tweet != null) {
                new HashSet<>(handlers).stream().forEach(handler -> sendTweetToHandler(handler, tweet));
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private Set<ReactiveHandler<TweetPhoto>> handlers = new HashSet<>();

    public void addHandler(ReactiveHandler<TweetPhoto> handler) {
        handlers.add(handler);
    }

    public void removeHandler(ReactiveHandler<TweetPhoto> handler) {
        handlers.remove(handler);
        if (handlers.isEmpty()) {
            executor.shutdownNow();
            onShutdown.accept(filter);
            twitterStream.shutdown();
        }
    }

    @Override
    public void onStatus(Status status) {
        tweetToSend.addAll(TweetPhoto.fromStatus(status).collect(Collectors.toList()));
    }

    private void sendTweetToHandler(ReactiveHandler<TweetPhoto> handler, TweetPhoto tweet) {
        try {
            handler.next(tweet);
        }
        catch (EndOfStreamException e) {
            removeHandler(handler);
        }
    }
}
