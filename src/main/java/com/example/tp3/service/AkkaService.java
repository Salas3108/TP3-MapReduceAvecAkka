package com.example.tp3.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.example.tp3.actor.Mapper;
import com.example.tp3.actor.Reducer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@Service
public class AkkaService {
    private ActorSystem system;
    private List<ActorRef> mappers = new ArrayList<>();
    private List<ActorRef> reducers = new ArrayList<>();

    //Initialise les acteurs
    public void initializeActors() {
        if (system == null) {
            system = ActorSystem.create("MapReduceSystem");

            // Crée les Reducers
            reducers.clear();
            for (int i = 0; i < 2; i++) {
                ActorRef reducer = system.actorOf(Props.create(Reducer.class), "reducer" + i);
                reducers.add(reducer);
                System.out.println("Reducer créé : " + reducer.path().name());
            }

            // Crée les Mappers
            mappers.clear();
            for (int i = 0; i < 3; i++) {
                // Convertit la liste de reducers en tableau
                ActorRef[] reducersArray = reducers.toArray(new ActorRef[0]);

                // Crée le Mapper avec le tableau d'ActorRef
                ActorRef mapper = system.actorOf(
                    Props.create(Mapper.class, (Object) reducersArray), // Passe le tableau comme un seul argument
                    "mapper" + i
                );
                mappers.add(mapper);
                System.out.println("Mapper créé : " + mapper.path().name());
            }
        }
    }

     //Traiter le contenu d'un fichier en envoyant chaque ligne à un Mapper.
   
    public void processFile(String content) {
        if (system == null) {
            throw new IllegalStateException("Le système Akka n'a pas été initialisé.");
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue; // Ignore les lignes vides
            }

            // Trouve le Mapper responsable de cette ligne
            int mapperIndex = Math.abs(line.hashCode() % mappers.size());
            ActorRef mapper = mappers.get(mapperIndex);

            // Envoie la ligne au Mapper
            mapper.tell(line, ActorRef.noSender());
        }
    }

     //Récupère le nombre d'occurrences d'un mot en interrogeant le Reducer approprié.
    
    public int getWordCount(String word) {
        if (system == null) {
            throw new IllegalStateException("Le système Akka n'a pas été initialisé.");
        }

        if (word == null || word.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot ne peut pas être vide ou nul.");
        }

        // Trouve le Reducer responsable de ce mot
        int reducerIndex = Math.abs(word.hashCode() % reducers.size());
        ActorRef reducer = reducers.get(reducerIndex);

        // Crée un Future pour attendre la réponse du Reducer
        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(reducer, new Reducer.GetCount(word), timeout);

        try {
            // Attend la réponse du Reducer
            return (int) Await.result(future, timeout.duration());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du compteur pour le mot '" + word + "' : " + e.getMessage());
            return 0;
        }
    }

    //Arrête le système Akka.

    public void shutdown() {
        if (system != null) {
            system.terminate();
            system = null;
            mappers.clear();
            reducers.clear();
        }
    }

}