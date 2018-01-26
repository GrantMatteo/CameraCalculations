package org.usfirst.frc.team1086.CameraCalculator.Example;

import org.usfirst.frc.team1086.CameraCalculator.Sighting;
import org.usfirst.frc.team1086.CameraCalculator.VisionTarget;
import java.util.ArrayList;
import java.util.HashMap;

public class GearTarget extends VisionTarget {
	int pixelDistanceThreshold = 20;

	public GearTarget() {
		super("Gear Lift");
	}

	@Override
	public ArrayList<Sighting> validateSightings(ArrayList<Sighting> polys) {
		DisjointUnionSets sets = new DisjointUnionSets(polys.size());
		sets.makeSet();
		for (int i = 0; i < polys.size(); i++)
			for (int j = i + 1; j < polys.size(); j++)
				if (polys.get(i).distanceTo(polys.get(j)) < pixelDistanceThreshold)
					sets.union(i, j);
		HashMap<Integer, Sighting> parentNodes = new HashMap();
		for (int i = 0; i < polys.size(); i++) {
			int parent = sets.find(i);
			parentNodes.putIfAbsent(parent, polys.get(parent));
			parentNodes.get(parent).addSighting(polys.get(i));
		}
		return new ArrayList(parentNodes.values());
	}

	@Override
	public boolean estimationIsGood(Sighting s) {
		return Math.abs(120 - s.distance.getAsDouble()) < 120 && Math.abs(s.angle.getAsDouble()) < 35;
	}
}

class DisjointUnionSets {
	int[] rank, parent;
	int n;

	public DisjointUnionSets(int n) {
		rank = new int[n];
		parent = new int[n];
		this.n = n;
		makeSet();
	}

	void makeSet() {
		for (int i = 0; i < n; i++) {
			parent[i] = i;
		}
	}

	public int find(int x) {
		if (parent[x] != x)
			parent[x] = find(parent[x]);
		return parent[x];
	}

	public void union(int x, int y) {
		int xRoot = find(x), yRoot = find(y);
		if (xRoot == yRoot)
			return;
		if (rank[xRoot] < rank[yRoot])
			parent[xRoot] = yRoot;
		else if (rank[yRoot] < rank[xRoot])
			parent[yRoot] = xRoot;
		else {
			parent[yRoot] = xRoot;
			rank[xRoot] = rank[xRoot] + 1;
		}
	}
}