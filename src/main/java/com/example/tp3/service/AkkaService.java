package com.example.tp3.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.example.tp3.actor.Mapper;
import com.example.tp3.actor.Reducer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AkkaService {
    private ActorSystem system;
    private List<ActorRef> mappers = new ArrayList<>();
    private List<ActorRef> reducers = new ArrayList<>();
    private Map<String, Integer> wordCounts = new ConcurrentHashMap<>();

    public void initializeActors() {
        // Réinitialiser les acteurs
        if (system != null) {
            system.terminate();
        }
        system = ActorSystem.create("MapReduceSystem");

        // Réinitialiser les compteurs
        wordCounts.clear();

        // Création des Reducers
        for (int i = 0; i < 2; i++) {
            reducers.add(system.actorOf(Props.create(Reducer.class), "reducer" + i));
        }

        // Création des Mappers
        for (int i = 0; i < 3; i++) {
            mappers.add(system.actorOf(
                Props.create(Mapper.class, (Object) reducers.toArray(new ActorRef[0])), // Conversion en Object
                "mapper" + i
            ));
        }
    }

    public void processFile(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            ActorRef mapper = mappers.get(line.hashCode() % mappers.size());
            mapper.tell(line, ActorRef.noSender());
        }
    }

    public int getWordCount(String word) {
        return wordCounts.getOrDefault(word, 0);
    }
}