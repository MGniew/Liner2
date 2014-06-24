package g419.liner2.api.chunker.factory;



import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 9/3/13
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkerManager {

    private HashMap<String, Chunker> chunkers = new HashMap<String, Chunker>();
    public LinerOptions opts;

    public ChunkerManager(LinerOptions opts){
        this.opts = opts;
    }

    public void addChunker(String name, Chunker chunker) {
        if(chunkers.containsKey(name)){
            throw new Error(String.format("Chunker name '%s' duplicated", name));
        }
        chunkers.put(name, chunker);
    }

    public void addChunker(String name, String description){
        try {
            addChunker(name, ChunkerFactory.createChunker(description, this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Chunker getChunkerByName(String name) {
        return chunkers.get(name);
    }

}