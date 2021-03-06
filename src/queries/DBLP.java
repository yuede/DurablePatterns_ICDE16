package queries;

import java.util.BitSet;

import system.Config;
import system.index.TimePathIndex;
import system.loader.LoaderDBLP;

/**
 * DBLP query
 * @author ksemer
 */
public class DBLP extends Query {

	/**
	 * Run method for DBLP dataset
	 * @throws Exception
	 */
	public void run() throws Exception {
		// settings for DBLP dataset
		Config.sizeOfNodes = 1063835;
		Config.sizeOfLabels = 4;
		Config.PATH_DATASET = "../files/DBLP_Graph";
		Config.PATH_LABELS = "../files/DBLP_Authors_Attr";
		Config.ISDIRECTED = false;
		Config.QUERY_SIZE = 6;
		Config.MAXIMUM_INTERVAL = 57;

		iQ = new BitSet(Config.MAXIMUM_INTERVAL);
		iQ.set(0, Config.MAXIMUM_INTERVAL, true);
				
		lvg = new LoaderDBLP().loadDataset();
		
		if (Config.SHOW_MEMORY)
			getMemory("LVG");
		
		if (!Config.TILA_ENABLED) {
			lvg.getTiLa().clear();			
			
			if (Config.SHOW_MEMORY)
				getMemory("without TiLa");
		}
		
		if (Config.TIPLA_ENABLED) {
			TiPLa = new TimePathIndex(Config.TIPLA_MAX_DEPTH).createPathIndex(lvg);
			
			if (Config.SHOW_MEMORY)
				getMemory("TiPLa");		
		}
		
		if (Config.RUN_CLIQUES)
			runClique();
		
		if (Config.RUN_STAR)
			runStar();

		if (Config.RUN_RANDOM)
			runRandom();
	}
}