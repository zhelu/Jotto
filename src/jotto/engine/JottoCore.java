package jotto.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * This class implements the core logic of the jotto game. See html.html in
 * resources.
 * 
 * @author Zhe Lu
 * 
 */
public class JottoCore {

  private Map<String, List<String>> allWords_ = new HashMap<String, List<String>>();
  private QueryTree qt_;


  /*
   * Only used internally.
   */
  private JottoCore(QueryTree qt) {
    qt_ = qt;
  }


  /**
   * Constructor.
   * 
   * @throws FileNotFoundException
   *           when words resource not found
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
        List<String> words = new ArrayList<String>();
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
    return findBestWord(orderedLetters, allWords_);
  }


  /*
   * Determine word that gives highest information gain
   */
  static String findBestWord(Set<String> orderedLetters,
      Map<String, List<String>> allWords) {
    String bestWord = "";
    double bestEntropy = 0;
    for (String w : allWords.keySet()) {
      int[] count = new int[6];
      int N = 0;
      for (String word : orderedLetters) {
        int match = numMatchingLetters(w, word);
        int numWords = allWords.get(word).size();
        count[match] += numWords;
        N += numWords;
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
   *          target word
   * @return a List of Strings that match the target word
   */
  public List<String> solve(String target) {
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
      Set<String> workingSet = new HashSet<String>();
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
   * @return List of Strings of anagrams for the possible word
   */
  public List<String> solve() {
    Scanner sc = new Scanner(System.in);
    Set<String> letterSet = allWords_.keySet();
    while (letterSet.size() > 1) {
      String w = findBestWord(letterSet);
      System.out.print("How many letters does your word match in \""
          + allWords_.get(w).get(0) + "\": ");
      int match = sc.nextInt();
      Set<String> workingSet = new HashSet<String>();
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
   *          number of matching letters
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
   * Return array of booleans indicating where the ith link is active (i.e., not
   * null)
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
   * @return List of Strings of anagrams, or null if not narrowed to a single
   *         set of anagrams
   */
  public List<String> getAnagrams() {
    if (qt_ == null) {
      throw new NullPointerException("no query tree has been initialized");
    }
    return qt_.getWords();

  }


  /**
   * Use a query tree from a file.
   * 
   * @param file
   *          file containing QueryTree
   * @return JottoCore object with QueryTree
   * @throws IOException
   *           on file access error
   * @throws DataFormatException
   *           if file is not a saved QueryTree
   */
  public static JottoCore useQueryTreeFromFile(File file) throws IOException,
      DataFormatException {
    QueryTree qt = QueryTree.readFromFile(file);
    return new JottoCore(qt);
  }


  /*
   * Builds a query tree using the list of words. The root node contains an
   * array of links to subtrees where the index of the link is the number of
   * matching letters for all words in the subtree to the current guess word.
   */
  private void buildQueryTree() {
    long startTime = System.currentTimeMillis();
    Set<String> possibilities = allWords_.keySet();
    qt_ = new QueryTree(QueryTree.recursivelyBuildTree(possibilities, allWords_));
    qt_.start();
    System.out.println("Building tree took "
        + (System.currentTimeMillis() - startTime) + " ms");
  }


  /**
   * Save the query tree to file
   * 
   * @param file
   *          filename to save into
   * @throws IOException
   *           on file write error
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
  static int numMatchingLetters(String a, String b) {
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
  private static String sortLetters(String s) {
    char[] chars = s.toCharArray();
    Arrays.sort(chars);
    return new String(chars);
  }

}
