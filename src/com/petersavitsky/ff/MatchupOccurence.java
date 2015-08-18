package com.petersavitsky.ff;

import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchupOccurence {
	
	private final Matchup matchup;
	private AtomicInteger numberOfOccurences = new AtomicInteger(1);
	
	public MatchupOccurence(Matchup matchup) {
		this.matchup = matchup;
	}
	
	public Matchup getMatchup() {
		return matchup;
	}
	
	public void incrementNumberOfOccurences() {
		numberOfOccurences.incrementAndGet();
	}
	
	public void decrementNumberOfOccurences() {
		numberOfOccurences.decrementAndGet();
	}
	
	public int getNumberOfOccurences() {
		return numberOfOccurences.get();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matchup == null) ? 0 : matchup.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchupOccurence other = (MatchupOccurence) obj;
		if (matchup == null) {
			if (other.matchup != null)
				return false;
		} else if (!matchup.equals(other.matchup))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MatchupOccurence [matchup=" + matchup + ", numberOfOccurences=" + numberOfOccurences + "]";
	}
	
	public static class MatchupOccurenceComparator implements Comparator<MatchupOccurence> {

		@Override
		public int compare(MatchupOccurence o1, MatchupOccurence o2) {
			int difference = o1.getNumberOfOccurences() - o2.getNumberOfOccurences();
			if (difference == 0) {
				// 50/50 shot at ordering if 2 elements are the same
				Random random = new Random();
				return 1 - (random.nextInt(2) * 2);
			} else if (difference > 0) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}
}
