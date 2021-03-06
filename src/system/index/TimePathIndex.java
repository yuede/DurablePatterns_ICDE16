package system.index;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graph.version.Edge;
import graph.version.LVGraph;
import graph.version.Node;
import system.Config;

/**
 * TiPLa
 * @author ksemertz
 */
public class TimePathIndex {
	//====================================================================
	private Map<Integer, Map<String, Set<Node>>> timePathIndexWT;
	private int max_depth;
	private Set<Node> set;
	//====================================================================

	/**
	 * Constructor
	 * @param max_depth
	 * @throws Exception
	 */
	public TimePathIndex(int max_depth) {
		this.max_depth = max_depth;
		this.timePathIndexWT = new HashMap<>();
		
		for (int i = 0; i < Config.MAXIMUM_INTERVAL; i++)
			this.timePathIndexWT.put(i, new HashMap<>());
	}

	/**
	 * Create path index
	 * Return for each time instant 
	 * all combinations of paths -> nodes with that combination
	 * @param g
	 * @return
	 * @throws IOException 
	 */
	public Map<Integer, Map<String, Set<Node>>> createPathIndex(LVGraph g) throws IOException {
		System.out.println("Time Path Index is running");
		long time = System.currentTimeMillis();

		for (int depth = max_depth; depth >= 1; depth--) {
			for (Node n : g.getNodes()) {
				traversePath(n, depth);
			}
		}
		
		if (Config.ISDIRECTED) {
		
			Set<Node> set;
	
			// to contain also the paths of size 0, node label itself
			for (Node n : g.getNodes()) {

				for (int label = 0; label < Config.sizeOfLabels; label++) {
//				for (int label = 0; label < Config.sizeOfLabels; label+=7) {
					// if label exists
					if (n.getLabel(label) != null) {
						for (Iterator<Integer> it = n.getLabel(label).stream().iterator(); it.hasNext();) {
							int t = it.next();
							
							if ((set = timePathIndexWT.get(t).get("" + label)) == null) {
								set = new HashSet<>();
								timePathIndexWT.get(t).put("" + label, set);
							}
							set.add(n);
						}
					}
				}
			}
		}
		
		System.out.println("TiPLa time: " + (System.currentTimeMillis() - time) / 1000 + " (sec)");

		return this.timePathIndexWT;
	}
	
	/**
	 * Traverse paths in depth
	 * @param n
	 * @param max_depth
	 */
	private void traversePath(Node n, int max_depth) {
		
		Deque<n_info> toBeVisited = new ArrayDeque<>();
		List<Node> path;
		BitSet l;
		n_info info = new n_info(n, null, new BitSet(), 0);
		toBeVisited.add(info);
		
		while (!toBeVisited.isEmpty()) {
			info = toBeVisited.poll();
			
			// if we are in the last node defined by the depth
			if (info.depth == max_depth) {
				
				if (!info.lifespan.isEmpty()) {
					
					BitSet life = null;
					path = new ArrayList<>();
					
					while (true) {
						if (info.father == null) {
							path.add(info.n);
							break;
						}
						
						if (life == null)
							life = info.lifespan;
						
						// add the node to the path and update father
						path.add(info.n);
						info = info.father;
					}
					
					Collections.reverse(path);
					
					// call recursive
					rec_labelComp(path, path.get(0), life, "", 0);
				}
				continue;
			}
			
			// for all neighbors
			for (Edge e : info.n.getAdjacency()) {
				
				// if we are in src node we take as lifetime the edge's lifetime
				if (info.father == null)
					toBeVisited.add(new n_info(e.getTarget(), info, (BitSet) e.getLifetime().clone(), info.depth + 1));
				else if (!info.father.n.equals(e.getTarget())) {
					// else we and the edge's lifespan with the info.lifespan
					l = (BitSet) info.lifespan.clone();
					l.and(e.getLifetime());
					toBeVisited.add(new n_info(e.getTarget(), info, l, info.depth + 1));
				}
			}
		}
	}

	/**
	 * Recursive function
	 * @param path
	 * @param src
	 * @param life
	 * @param label
	 * @param depth
	 */
	private void rec_labelComp(List<Node> path, Node src, BitSet life, String label, int depth) {
		Node n = path.get(depth);
		BitSet lifetime;
		
		for (int l = 0; l < Config.sizeOfLabels; l++) {
//		for (int l = 0; l < Config.sizeOfLabels; l+=7) {
			// if nodes has label l
			if (n.getLabel(l) == null)
				continue;

			lifetime = (BitSet) life.clone();
			lifetime.and(n.getLabel(l));

			if (!lifetime.isEmpty()) {
				
				if (depth+1 < path.size())
					rec_labelComp(path, src, lifetime, label + "|" + l, depth + 1);
				else {
					// i is the next label in path
					// we use integers to denote labels
					String Path = label + "|" + l;

					for (Iterator<Integer> it = lifetime.stream().iterator(); it.hasNext();) {
						int t = it.next();
						
						if ((set = timePathIndexWT.get(t).get(Path)) == null) {
							set = new HashSet<>();
							timePathIndexWT.get(t).put(Path, set);
						}
						
						set.add(src);
					}
				}
			}
		}
	}

	class n_info {
		Node n;
		n_info father;
		BitSet lifespan;
		int depth;
		
		public n_info(Node n, n_info father, BitSet lifespan, int depth) {
			this.n = n;
			this.father = father;
			this.lifespan = lifespan;
			this.depth = depth;
		}
	}
}
