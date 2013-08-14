package jotto.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

/**
 * Implements decision tree for the Jotto game. See {@link Node}.
 * 
 * @author Zhe Lu
 * 
 */
public class QueryTree implements Serializable {

  private static final long serialVersionUID = -647157552718128746L;
  private final Node root_;
  private Node current_;
  private final Random rand_ = new Random(System.currentTimeMillis());


  /**
   * Create a new query tree with its root at node n
   * 
   * @param n
   *          the root node
   */
  public QueryTree(Node n) {
    root_ = n;
  }


  /**
   * Reinitialize the tree at its root node.
   */
  public void start() {
    current_ = root_;
  }


  /**
   * Follow the link to the node matching n letters.
   * 
   * @param n
   *          the number of matching letters of the guess word.
   */
  public void traverse(int n) {
    if (current_.getLink(n) != null) {
      current_ = current_.getLink(n);
    }
  }


  /**
   * Query whether the links are active.
   * 
   * @return a boolean array of length 6. Each element's index indicates whether
   *         there is a Node corresponding to a match of that many letters.
   */
  public boolean[] getActiveLinks() {
    boolean[] retValue = new boolean[6];
    for (int i = 0; i < 6; i++) {
      retValue[i] = (current_.getLink(i) != null);
    }
    return retValue;
  }


  /**
   * Returns a query tree opened from a file.
   * 
   * @param file
   *          file to open
   * @throws IOException
   *           on read error
   * @throws DataFormatException
   */
  public static QueryTree readFromFile(File file) throws IOException,
      DataFormatException {
    FileInputStream fis = new FileInputStream(file);
    ObjectInputStream ois = new ObjectInputStream(fis);
    QueryTree qt = null;
    try {
      qt = (QueryTree) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new DataFormatException();
    } finally {
      ois.close();
    }
    return qt;
  }


  /**
   * Write this QueryTree to file.
   * 
   * @param file
   *          file name
   * @throws IOException
   *           on write error
   */
  public void saveToFile(File file) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(this);
    oos.close();
  }

  
  /*
   * Create a subtree for each link using the current set of possibilities.
   */
  static Node recursivelyBuildTree(Set<String> possibilities, Map<String, List<String>> allWords) {
    if (possibilities == null) {
      return null;
    } else if (possibilities.size() == 1) {
      // get the single element
      Iterator<String> it = possibilities.iterator();
      return Node.nodeFromAnagrams(allWords.get(it.next()));
    }
    String guess = JottoCore.findBestWord(possibilities, allWords);
    Node n = Node.nodeFromGuesses(allWords.get(guess));
    HashMap<Integer, Set<String>> bins = new HashMap<Integer, Set<String>>();
    for (String w : possibilities) {
      int match = JottoCore.numMatchingLetters(guess, w);
      if (!bins.containsKey(match)) {
        bins.put(match, new HashSet<String>());
      }
      bins.get(match).add(w);
    }
    for (int i = 0; i < 6; i++) {
      n.setLink(i, recursivelyBuildTree(bins.get(i), allWords));
    }
    return n;
  }

  /**
   * Query the system for possible words.
   * 
   * @return a List<String> of possible anagrams. Returns null if options are
   *         narrowed only to one set of anagrams.
   */
  public List<String> getWords() {
    return current_.getAnagrams();
  }


  /**
   * Query the system for the next guess
   * 
   * @return the next word to guess
   */
  public String getGuessWord() {
    if (current_.getGuess() == null) {
      return null;
    }
    int n = rand_.nextInt(current_.getGuess().size());
    return current_.getGuess().get(n);
  }


  /**
   * @param args
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static void main(String[] args) throws IOException,
      ClassNotFoundException {
    // FileInputStream fos = new FileInputStream(new File("test"));
    // ObjectInputStream oos = new ObjectInputStream(fos);
    // QueryTree test = (QueryTree) oos.readObject();
    // oos.close();
  }
}

/**
 * Represents nodes of the decision tree. Interior nodes have a list of words
 * for what to guess. Leaf nodes have a list of anagrams of proposed solution
 * words.
 * 
 * @author Zhe Lu
 * 
 */
class Node implements Serializable {

  private static final long serialVersionUID = 2694492917093744338L;
  private List<String> guess_;
  private Node[] links_ = new Node[6];
  private List<String> words_;


  /**
   * Create an internal node from a list of guess
   * 
   * @param guesses
   * @return
   */
  protected static Node nodeFromGuesses(List<String> guesses) {
    Node retValue = new Node();
    retValue.guess_ = guesses;
    return retValue;
  }


  /**
   * Create a leaf node from a list of anagrams
   * 
   * @param anagrams
   * @return
   */
  protected static Node nodeFromAnagrams(List<String> anagrams) {
    Node retValue = new Node();
    retValue.words_ = anagrams;
    return retValue;
  }


  /**
   * Get the anagrams associated with this node
   * 
   * @return a list of anagrams representing the guesses for the current game
   */
  protected List<String> getAnagrams() {
    return words_;
  }


  /**
   * Get the guesses for the current node
   * 
   * @return a list of guess words we can use
   */
  protected List<String> getGuess() {
    return guess_;
  }


  /**
   * Set the guesses for the current node
   * 
   * @param guesses
   *          a list of guesses
   */
  protected void setGuess(List<String> guesses) {
    guess_ = guesses;
  }


  /**
   * Get the node corresponding to a match of n letters in the guess word
   * 
   * @param n
   *          the number of letters matching in the guess word
   * @return the next node in the tree traversal
   */
  protected Node getLink(int n) {
    return links_[n];
  }


  /**
   * Set the node corresponding to a match of n letters in the guess word
   * 
   * @param n
   *          the number of matches
   * @param node
   *          the next node
   */
  protected void setLink(int n, Node node) {
    links_[n] = node;
  }
}