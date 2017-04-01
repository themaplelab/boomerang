package boomerang;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.FieldGraph;
import boomerang.accessgraph.WrappedSootField;
import heros.solver.Pair;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NullConstant;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class AliasResults extends ForwardingMultimap<AccessGraph, AccessGraph> {
	private Multimap< AccessGraph, AccessGraph> delegate;
	private boolean timedout;


	/**
	 * Creates an empty result map.
	 */
	public AliasResults() {
		this.delegate = HashMultimap.create();
	}

	@Override
	protected Multimap<AccessGraph, AccessGraph> delegate() {
		return this.delegate;
	}

	/**
	 * Appends the fields to the each of the access graph in the collection
	 * supplied.
	 * 
	 * @param in
	 *            Set of access graph to append the fields to.
	 * @param fields
	 *            Fields to be appended.
	 * @param context
	 *            The general context (needed for type checking.)
	 * @return
	 */
	public static Set<AccessGraph> appendFields(Collection<AccessGraph> in, WrappedSootField[] fields,
			BoomerangContext context) {
		Set<AccessGraph> out = new HashSet<>();
		for (AccessGraph v : in) {
			if (!v.canAppend(fields[0])) {
				continue;
			}
			;
			AccessGraph newValue = v.appendFields(fields);

			if (context.isValidAccessPath(newValue)) {
				out.add(newValue);
			}
		}
		return out;
	}

	/**
	 * Appends a single field to the each of the access graph in the collection
	 * supplied.
	 * 
	 * @param in
	 *            Set of access graph to append the fields to.
	 * @param lastField
	 *            Fields to be appended.
	 * @param context
	 *            The general context (needed for type checking.)
	 * @return
	 */
	public static Set<AccessGraph> appendField(Collection<AccessGraph> results, WrappedSootField lastField,
			BoomerangContext context) {
		return appendFields(results, new WrappedSootField[] { lastField }, context);
	}

	/**
	 * Returns the may alias set of access graph, that is just all values of the
	 * map.
	 * 
	 * @return The set of aliasing access graphs
	 */
	public Collection<AccessGraph> mayAliasSet() {
		return new HashSet<>(this.values());
	}

	/**
	 * Appends another field graph the each of the access graph in the
	 * collection supplied.
	 * 
	 * @param in
	 *            Set of access graph to append the fields to.
	 * @param graph
	 *            {@link FieldGraph} to be appended.
	 * @param context
	 *            The general context (needed for type checking.)
	 * @return
	 */
	public static Set<AccessGraph> appendFields(Set<AccessGraph> in, AccessGraph graph, BoomerangContext context) {
		if (graph == null || graph.getFieldGraph() == null)
			return new HashSet<>(in);
		Set<AccessGraph> out = new HashSet<>();
		for (AccessGraph v : in) {
			boolean canAppend = false;
			for (WrappedSootField f : graph.getFirstField()) {
				canAppend = canAppend || v.canAppend(f);
			}
			if (!canAppend) {
				continue;
			}
			;
			AccessGraph newValue = v.appendGraph(graph.getFieldGraph());

			if (context.isValidAccessPath(newValue)) {
				out.add(newValue);
			}
		}
		return out;
	}

//	public String withMethodOfAllocationSite(IInfoflowCFG cfg) {
//		StringBuilder sb = new StringBuilder();
//		sb.append("{");
//		for (Pair<Unit, AccessGraph> k : keySet()) {
//			sb.append(k.getO1() + " in " + cfg.getMethodOf(k.getO1()));
//			sb.append("=");
//			sb.append(get(k));
//		}
//		sb.append("}");
//		return "AliasResults: " + sb.toString();
//	}
	public AliasResults withoutNullAllocationSites(){
		AliasResults res = new AliasResults();
		for(AccessGraph key : keySet()){
			if(!key.hasNullAllocationSite())
				res.putAll(key, get(key));
		}
		if(timedout)
			res.setTimedout();
		return res;
	}
	
	public void setTimedout() {
		timedout = true;
	}

	public boolean queryTimedout(){
		return timedout;
	}
}
