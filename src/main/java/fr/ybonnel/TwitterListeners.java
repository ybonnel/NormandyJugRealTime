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
import fr.ybonnel.simpleweb4j.handlers.eventsource.ReactiveHandler;

import java.util.HashMap;
import java.util.Map;

public class TwitterListeners {

    private Map<String, TwitterListener> listenersByKeyWord = new HashMap<>();

    public void addHandlerForKeyWord(ReactiveHandler<TweetPhoto> handler, String keyWord) {
        if (!listenersByKeyWord.containsKey(keyWord)) {
            listenersByKeyWord.put(keyWord, new TwitterListener(keyWord, listenersByKeyWord::remove));
        }
        listenersByKeyWord.get(keyWord).addHandler(handler);
    }

}
