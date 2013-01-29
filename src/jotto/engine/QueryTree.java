package jotto.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.Vector;
import java.util.zip.DataFormatException;

public class QueryTree implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -647157552718128746L;
	private final Node root_;
	private Node current_;
	private final Random rand_ = new Random(System.currentTimeMillis());


	public QueryTree(Node n) {
		root_ = n;
	}


	public void start() {
		current_ = root_;
	}


	public void traverse(int n) {
		if (current_.getLink(n) != null) {
			current_ = current_.getLink(n);
		}
	}

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
	 *            file to open
	 * @throws IOException
	 *             on read error
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
	 *            file name
	 * @throws IOException
	 *             on write error
	 */
	public void saveToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
	}


	/**
	 * Query the system for possible words.
	 * 
	 * @return a Vector<String> of possible anagrams. Returns null if options
	 *         are narrowed only to one set of anagrams.
	 */
	public Vector<String> getWords() {
		return current_.getWords();
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

class Node implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2694492917093744338L;
	private Vector<String> guess_;
	private Node[] links_ = new Node[6];
	private Vector<String> words_;

	
	protected static Node nodeFromGuesses(Vector<String> vs) {
		Node retValue = new Node();
		retValue.guess_ = vs;
		return retValue;
	}



	protected static Node nodeFromAnagrams(Vector<String> vs) {
		Node retValue = new Node();
		retValue.words_ = vs;
		return retValue;
	}


	protected Vector<String> getWords() {
		return words_;
	}


	protected Vector<String> getGuess() {
		return guess_;
	}


	protected void setGuess(Vector<String> vs) {
		guess_ = vs;
	}


	protected Node getLink(int n) {
		return links_[n];
	}


	protected void setLink(int i, Node n) {
		links_[i] = n;
	}
}