package jotto.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.zip.DataFormatException;

public class JottoCore {

	private HashMap<String, Vector<String>> allWords_ = new HashMap<String, Vector<String>>();
	private QueryTree qt_;


	private JottoCore() {

	}


	/**
	 * Constructor.
	 * 
	 * @throws FileNotFoundException
	 *             when words resource not found
	 * @throws DataFormatException
	 */
	public JottoCore(File file) throws FileNotFoundException,
			DataFormatException {
		Scanner sc = new Scanner(file);
		while (sc.hasNext()) {
			String word = sc.next().trim().toLowerCase();
			if (word.length() != 5)
				continue;
			String letters = sortLetters(word);
			if (allWords_.containsKey(letters)) {
				allWords_.get(letters).add(word);
			} else {
				Vector<String> words = new Vector<String>();
				words.add(word);
				allWords_.put(letters, words);
			}
		}
		sc.close();
		if (allWords_.size() == 0) {
			throw new DataFormatException();
		}
		buildQueryTree();
	}


	/*
	 * Determine word that gives highest information gain
	 */
	private String findBestWord(Set<String> orderedLetters) {
		String bestWord = "";
		double bestEntropy = 0;
		for (String w : allWords_.keySet()) {
			int[] count = new int[6];
			// HashMap<Integer, HashSet<String>> wordMatchesByNumberOfLetters =
			// new HashMap<Integer, HashSet<String>>();
			int N = 0;
			for (String word : orderedLetters) {
				int match = numMatchingLetters(w, word);
				// if (wordMatchesByNumberOfLetters.containsKey(match)) {
				// wordMatchesByNumberOfLetters.get(match).add(word);
				int numWords = allWords_.get(word).size();
				count[match] += numWords;
				N += numWords;
				// }
			}
			double entropy = getEntropy(count, N);
			if (entropy > bestEntropy) {
				bestWord = w;
				bestEntropy = entropy;
			}

		}

		return bestWord;
	}


	/**
	 * Prints a path to the target
	 * 
	 * @param target
	 *            target word
	 * @return a Vector of Strings that match the target word
	 */
	public Vector<String> solve(String target) {
		String sortedTarget = sortLetters(target);
		if (!allWords_.containsKey(sortedTarget)
				|| !allWords_.get(sortedTarget).contains(target)) {
			System.out.println("Sorry I don't know this word...");
			System.exit(0);
		}
		Set<String> letterSet = allWords_.keySet();
		while (letterSet.size() > 1) {
			String w = findBestWord(letterSet);
			int match = numMatchingLetters(sortedTarget, w);
			System.out.println(allWords_.get(w).get(0) + ": " + match);
			HashSet<String> workingSet = new HashSet<String>();
			for (String s : letterSet) {
				if (numMatchingLetters(s, w) == match) {
					workingSet.add(s);
				}
			}
			letterSet = workingSet;
		}
		if (letterSet.size() == 0) {
			return null;
		} else {
			Iterator<String> it = letterSet.iterator();
			return allWords_.get(it.next());
		}
	}


	/**
	 * Interactive command line guessing
	 * 
	 * @return Vector of Strings of anagrams for the possible word
	 */
	public Vector<String> solve() {
		Scanner sc = new Scanner(System.in);
		Set<String> letterSet = allWords_.keySet();
		while (letterSet.size() > 1) {
			String w = findBestWord(letterSet);
			System.out.print("How many letters does your word match in \""
					+ allWords_.get(w).get(0) + "\": ");
			int match = sc.nextInt();
			HashSet<String> workingSet = new HashSet<String>();
			for (String s : letterSet) {
				if (numMatchingLetters(s, w) == match) {
					workingSet.add(s);
				}
			}
			letterSet = workingSet;
		}
		sc.close();
		if (letterSet.size() == 0) {
			return null;
		} else {
			Iterator<String> it = letterSet.iterator();
			return allWords_.get(it.next());
		}

	}


	/*
	 * Get the entropy of a set of N items divided into bins each with count[i]
	 */
	private static double getEntropy(int[] counts, int N) {
		double entropy = 0;
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] == 0) {
				continue;
			}
			double p = (double) counts[i] / N;
			entropy -= p * Math.log(p);
		}
		return entropy;
	}


	/**
	 * Restart the game.
	 */
	public void restart() {
		qt_.start();
	}


	/**
	 * Input the number of matching letters for the current guess word.
	 * 
	 * @param n
	 *            number of matching letters
	 */
	public void numMatches(int n) {
		if (n < 0 || n > 5) {
			throw new IllegalArgumentException(
					"The number of matches must be between 0 and 5, inclusive.");
		}
		if (qt_ == null) {
			throw new NullPointerException("no query tree has been initialized");
		}
		qt_.traverse(n);
	}


	/**
	 * Get the best guess word.
	 * 
	 * @return best guess word or null if no more guesses are needed
	 */
	public String getGuess() {
		if (qt_ == null) {
			throw new NullPointerException("no query tree has been initialized");
		}
		return qt_.getGuessWord();
	}


	/**
	 * Return array of booleans indicating where the ith link is active (i.e.,
	 * not null)
	 * 
	 * @return array of booleans indicating link availability
	 */
	public boolean[] linksAvailableArray() {
		if (qt_ == null) {
			throw new NullPointerException("no query tree has been initialized");
		}
		return qt_.getActiveLinks();
	}


	/**
	 * Get all words associated with the current set of guesses. Returns null if
	 * not narrowed to a single set of anagrams.
	 * 
	 * @return Vector of Strings of anagrams, or null if not narrowed to a
	 *         single set of anagrams
	 */
	public Vector<String> getAnagrams() {
		if (qt_ == null) {
			throw new NullPointerException("no query tree has been initialized");
		}
		return qt_.getWords();

	}


	/**
	 * Use a query tree from a file.
	 * 
	 * @param file
	 *            file containing QueryTree
	 * @return JottoCore object with QueryTree
	 * @throws IOException
	 *             on file access error
	 * @throws DataFormatException
	 *             if file is not a saved QueryTree
	 */
	public static JottoCore useQueryTreeFromFile(File file) throws IOException,
			DataFormatException {
		JottoCore retValue = new JottoCore();
		retValue.qt_ = QueryTree.readFromFile(file);
		return retValue;
	}


	/*
	 * Builds a query tree using the list of words. The root node contains an
	 * array of links to subtrees where the index of the link is the number of
	 * matching letters for all words in the subtree to the current guess word.
	 */
	private void buildQueryTree() {
		long startTime = System.currentTimeMillis();
		Set<String> possibilities = allWords_.keySet();
		qt_ = new QueryTree(recursivelyBuildTree(possibilities));
		qt_.start();
		System.out.println("Building tree took "
				+ (System.currentTimeMillis() - startTime) + " ms");
	}


	/*
	 * Create a subtree for each link.
	 */
	private Node recursivelyBuildTree(Set<String> possibilities) {
		if (possibilities == null) {
			return null;
		} else if (possibilities.size() == 1) {
			Iterator<String> it = possibilities.iterator();
			return Node.nodeFromAnagrams(allWords_.get(it.next()));
		}
		String guess = findBestWord(possibilities);
		Node n = Node.nodeFromGuesses(allWords_.get(guess));
		HashMap<Integer, HashSet<String>> bins = new HashMap<Integer, HashSet<String>>();
		for (String w : possibilities) {
			int match = numMatchingLetters(guess, w);
			if (!bins.containsKey(match)) {
				bins.put(match, new HashSet<String>());
			}
			bins.get(match).add(w);
		}
		for (int i = 0; i < 6; i++) {
			n.setLink(i, recursivelyBuildTree(bins.get(i)));
		}
		return n;
	}


	/**
	 * Save the query tree to file
	 * 
	 * @param file
	 *            filename to save into
	 * @throws IOException
	 *             on file write error
	 */
	public void saveQueryTree(File file) throws IOException {
		if (qt_ == null) {
			throw new NullPointerException("no query tree has been initialized");
		}
		qt_.saveToFile(file);
	}


	/*
	 * Matching letters in both strings. a and b must have letters sorted in
	 * ascending alphabetical order. See sortLetters() method.
	 */
	private static int numMatchingLetters(String a, String b) {
		int i = 0;
		int j = 0;
		int match = 0;
		while (j < 5 && i < 5) {
			if (a.charAt(i) < b.charAt(j)) {
				i++;
			} else if (a.charAt(i) > b.charAt(j)) {
				j++;
			} else {
				match++;
				i++;
				j++;
			}
		}
		return match;
	}


	/*
	 * Given a string, return the string with the letters sorted in ascending
	 * alphabetical order.
	 */
	private String sortLetters(String s) {
		char[] chars = s.toCharArray();
		Arrays.sort(chars);
		return new String(chars);
	}


	/**
	 * @param args
	 * @throws IOException
	 * @throws DataFormatException
	 */
	public static void main(String[] args) throws IOException,
			DataFormatException {
		// unit tests
		JottoCore myJotto = new JottoCore(new File(
				"src/jotto/resources/words.txt"));
		myJotto.saveQueryTree(new File("test"));
		System.out.println(myJotto.getGuess());
		myJotto.numMatches(5);
		for (String s : myJotto.getAnagrams()) {
			System.out.println(s);
		}
		// Vector<String> answers = myJotto.solve();
		// for (String s : answers) {
		// System.out.println(s);
		// }
	}

}
