package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.FullDictionaryChunker;
import g419.corpus.ConsolePrinter;
import org.ini4j.Ini;


public class ChunkerFactoryItemDictFullCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullCompile() {
		super("dict-full-compile");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        ConsolePrinter.log("--> Full Dictionary Chunker compile");

        String dictFile = description.get("dict");
        String modelFile = description.get("store");

        FullDictionaryChunker chunker = new FullDictionaryChunker();
        ConsolePrinter.log("--> Compiling dictionary from file=" + dictFile);
        chunker.loadDictionary(dictFile);
        ConsolePrinter.log("--> Saving chunker to file=" + modelFile);
        chunker.serialize(modelFile);

        return chunker;
	}

}