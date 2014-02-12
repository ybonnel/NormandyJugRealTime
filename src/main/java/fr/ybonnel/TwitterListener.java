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

import fr.ybonnel.simpleweb4j.handlers.eventsource.EndOfStreamException;
import fr.ybonnel.simpleweb4j.handlers.eventsource.ReactiveHandler;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.HashSet;
import java.util.Set;

public class TwitterListener extends StatusAdapter {

    private String filter;

    public TwitterListener(String filter) {
        this.filter = filter;
    }

    public TwitterListener startConsumeTwitter() {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(this);
        twitterStream.filter(new FilterQuery().track(new String[]{filter}));
        return this;
    }

    private Set<ReactiveHandler<String>> handlers = new HashSet<>();

    public void addHandler(ReactiveHandler<String> handler) {
        handlers.add(handler);
    }

    public void removeHandler(ReactiveHandler<String> handler) {
        handlers.remove(handler);
    }

    @Override
    public void onStatus(Status status) {
        new HashSet<>(handlers).stream().forEach(handler -> sendTweetToHandler(handler, status.getText()));
    }

    private void sendTweetToHandler(ReactiveHandler<String> handler, String text) {
        try {
            handler.next(text);
        }
        catch (EndOfStreamException e) {
            removeHandler(handler);
        }
    }
}
