package com.example.tp3.actor;

import akka.actor.UntypedActor;
import java.util.HashMap;
import java.util.Map;

public class Reducer extends UntypedActor {
    private final Map<String, Integer> wordCounts = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            String word = (String) message;
            System.out.println("Reducer reçoit le mot : " + word);
            wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            System.out.println("Compteur pour '" + word + "' : " + wordCounts.get(word));
        } else if (message instanceof GetCount) {
            GetCount getCount = (GetCount) message;
            getSender().tell(wordCounts.getOrDefault(getCount.word, 0), getSelf());
        } else {
            unhandled(message);
        }
    }

    public static class GetCount {
        public final String word;

        public GetCount(String word) {
            this.word = word;
        }
    }
}