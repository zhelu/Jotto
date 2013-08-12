package jotto.gui;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import jotto.engine.JottoCore;

/**
 * Implements the GUI for playing Jotto.
 * 
 * @author Zhe Lu
 * 
 */
public class Jotto {

  private JFrame frmJotto;
  private final JFileChooser fileChooser_ = new JFileChooser();
  private JottoCore jottoCore_;
  private JButton[] buttonMatches_ = new JButton[6];
  private String lastGuess_;
  private List<String> anagrams_;
  private Iterator<String> it_;
  private JLabel labelInfo_;
  private int guesses_ = 1;
  private JButton buttonYes_;
  private JButton buttonNo_;
  private JButton buttonStart_;
  private Help about_;


  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          Jotto window = new Jotto();
          window.frmJotto.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  /**
   * Create the application.
   */
  public Jotto() {
    initialize();
  }


  /*
   * Go to the next word.
   */
  private void update() {
    if (jottoCore_.getGuess() != null) {
      lastGuess_ = jottoCore_.getGuess();
      labelInfo_.setText("<HTML>I'm going to guess \""
          + lastGuess_
          + ".\" How many letters match?</HTML>");
      boolean[] linkOK = jottoCore_.linksAvailableArray();
      for (int i = 0; i < 6; i++) {
        buttonMatches_[i].setEnabled(linkOK[i]);
      }
    } else {
      if (jottoCore_.getAnagrams() == null
          || (jottoCore_.getAnagrams() != null && it_ != null
          && !it_.hasNext())) {
        labelInfo_.setText("Sorry. I don't know your word...");
        for (int i = 0; i < 6; i++) {
          buttonMatches_[i].setEnabled(false);
        }
        buttonYes_.setEnabled(false);
        buttonNo_.setEnabled(false);
      } else if (jottoCore_.getAnagrams().size() == 1) {
        lastGuess_ = jottoCore_.getAnagrams().get(0);
        labelInfo_.setText("<HTML>I think your word is \""
            + lastGuess_ + ".\" It took "
            + guesses_ + " guesses.</HTML>");
        for (int i = 0; i < 6; i++) {
          buttonMatches_[i].setEnabled(false);
        }
        buttonYes_.setEnabled(false);
        buttonNo_.setEnabled(false);
      } else {
        if (it_ == null) {
          anagrams_ = jottoCore_.getAnagrams();
          Collections.shuffle(anagrams_);
          anagrams_.remove(lastGuess_);
          it_ = anagrams_.iterator();
        }
        String w = it_.next();
        lastGuess_ = w;
        labelInfo_
            .setText("<HTML>I'm going to guess \""
                + w
                + ".\" Is it your word?</HTML>");
        for (int i = 0; i < 6; i++) {
          buttonMatches_[i].setEnabled(false);
        }
        buttonNo_.setEnabled(true);
      }
    }
  }


  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frmJotto = new JFrame();
    frmJotto.setTitle("Jotto");
    frmJotto.setBounds(100, 100, 333, 286);
    frmJotto.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmJotto.getContentPane().setLayout(null);
    try {
      fileChooser_.setCurrentDirectory(new File(getClass()
          .getClassLoader()
          .getResource("jotto/resources").toURI()));
    } catch (URISyntaxException e2) {
      fileChooser_.setCurrentDirectory(new File("."));
    }

    // initialize main text of application
    final JLabel lblInfo = new JLabel(
        "Please load a list of words or a Jotto data file.");
    lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
    lblInfo.setBounds(34, 66, 248, 49);
    frmJotto.getContentPane().add(lblInfo);
    labelInfo_ = lblInfo;

    // initialize buttons for number of matching letters
    final JButton button_0 = new JButton("0");
    button_0.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        guesses_++;
        jottoCore_.numMatches(0);
        update();
      }
    });
    button_0.setEnabled(false);
    button_0.setBounds(82, 126, 44, 23);
    frmJotto.getContentPane().add(button_0);
    buttonMatches_[0] = button_0;

    final JButton button_1 = new JButton("1");
    button_1.setEnabled(false);
    button_1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        guesses_++;
        jottoCore_.numMatches(1);
        update();
      }
    });
    button_1.setBounds(136, 126, 44, 23);
    frmJotto.getContentPane().add(button_1);
    buttonMatches_[1] = button_1;

    final JButton button_2 = new JButton("2");
    button_2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        guesses_++;
        jottoCore_.numMatches(2);
        update();
      }
    });
    button_2.setEnabled(false);
    button_2.setBounds(190, 126, 44, 23);
    frmJotto.getContentPane().add(button_2);
    buttonMatches_[2] = button_2;

    final JButton button_3 = new JButton("3");
    button_3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        guesses_++;
        jottoCore_.numMatches(3);
        update();
      }
    });
    button_3.setEnabled(false);
    button_3.setBounds(82, 160, 44, 23);
    frmJotto.getContentPane().add(button_3);
    buttonMatches_[3] = button_3;

    final JButton button_4 = new JButton("4");
    button_4.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        guesses_++;
        jottoCore_.numMatches(4);
        update();
      }
    });
    button_4.setEnabled(false);
    button_4.setBounds(136, 160, 44, 23);
    frmJotto.getContentPane().add(button_4);
    buttonMatches_[4] = button_4;

    final JButton button_5 = new JButton("5");
    button_5.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        guesses_++;
        jottoCore_.numMatches(5);
        update();
      }
    });
    button_5.setEnabled(false);
    button_5.setBounds(190, 160, 44, 23);
    frmJotto.getContentPane().add(button_5);
    buttonMatches_[5] = button_5;

    // initialize yes/no buttons
    final JButton btnYes = new JButton("That's it!");
    btnYes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        labelInfo_.setText("<HTML>Your word is \""
            + lastGuess_
            + ".\" It took " + guesses_ + " guesses.</HTML>");
        for (int i = 0; i < 6; i++) {
          buttonMatches_[i].setEnabled(false);
          buttonYes_.setEnabled(false);
          buttonNo_.setEnabled(false);
        }
      }
    });
    btnYes.setEnabled(false);
    btnYes.setBounds(82, 192, 83, 23);
    frmJotto.getContentPane().add(btnYes);
    buttonYes_ = btnYes;

    final JButton btnNo = new JButton("No");
    btnNo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        guesses_++;
        update();
      }
    });
    btnNo.setEnabled(false);
    btnNo.setBounds(175, 193, 59, 23);
    frmJotto.getContentPane().add(btnNo);
    buttonNo_ = btnNo;

    // initialize menus and menu items
    final JMenuBar menuBar = new JMenuBar();
    frmJotto.setJMenuBar(menuBar);

    final JMenu mnJotto = new JMenu("Jotto");
    menuBar.add(mnJotto);

    final JMenuItem mntmLoadWordList = new JMenuItem("Load word list...");
    mntmLoadWordList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        lblInfo.setText("Loading ...");
        int returnVal = fileChooser_.showOpenDialog(frmJotto);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          // load word list

          try {
            jottoCore_ = new JottoCore(fileChooser_
                .getSelectedFile());
            int retValue = JOptionPane
                .showConfirmDialog(
                    frmJotto,
                    "Do you want to save a Jotto file for faster loading next time?",
                    "Save?",
                    JOptionPane.YES_NO_OPTION);
            if (retValue == JOptionPane.YES_OPTION) {
              // save tree
              returnVal = fileChooser_.showSaveDialog(frmJotto);
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                  jottoCore_.saveQueryTree(fileChooser_
                      .getSelectedFile());
                } catch (IOException e) {
                  JOptionPane
                      .showMessageDialog(
                          frmJotto,
                          "Unable to save to selected file.",
                          "Error",
                          JOptionPane.ERROR_MESSAGE);
                }
              }
            }
            buttonStart_.setEnabled(true);
            lblInfo.setText("Press Start!");
          } catch (FileNotFoundException | DataFormatException e) {
            JOptionPane
                .showMessageDialog(
                    frmJotto,
                    "The selected file does not contain a usable list of five letter words.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            lblInfo
                .setText("Please load a list of words or a Jotto data file.");
          }
        } else {
          lblInfo.setText("Please load a list of words or a Jotto data file.");
        }
      }
    });
    mnJotto.add(mntmLoadWordList);

    final JButton btnStart = new JButton("Start!");
    btnStart.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        jottoCore_.restart();
        for (int i = 0; i < 6; i++) {
          buttonMatches_[i].setEnabled(false);
        }
        btnNo.setEnabled(false);
        btnYes.setEnabled(true);
        guesses_ = 1;
        it_ = null;
        update();
      }
    });
    btnStart.setEnabled(false);
    btnStart.setBounds(115, 11, 89, 44);
    frmJotto.getContentPane().add(btnStart);
    buttonStart_ = btnStart;

    final JMenuItem mntmLoadJottoFile = new JMenuItem("Load Jotto file...");
    mntmLoadJottoFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileChooser_.showOpenDialog(frmJotto);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            lblInfo.setText("Loading ...");
            jottoCore_ = JottoCore
                .useQueryTreeFromFile(fileChooser_
                    .getSelectedFile());
            lblInfo.setText("Press Start!");
            btnStart.setEnabled(true);
          } catch (DataFormatException | IOException e1) {
            JOptionPane
                .showMessageDialog(
                    frmJotto,
                    "The selected file is not a valid Jotto file built by this program.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            lblInfo
                .setText("Please load a list of words or a Jotto data file.");
          }
        }
      }
    });
    mnJotto.add(mntmLoadJottoFile);

    final JSeparator separator = new JSeparator();
    mnJotto.add(separator);

    final JMenuItem mntmExit = new JMenuItem("Exit");
    mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
        InputEvent.ALT_MASK));
    mntmExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        System.exit(0);
      }
    });
    mnJotto.add(mntmExit);

    JMenu mnHelp = new JMenu("Help");
    menuBar.add(mnHelp);

    final JMenuItem mntmHowToPlay = new JMenuItem("How to play");
    mntmHowToPlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        about_.setVisible(true);
      }
    });
    mntmHowToPlay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    mnHelp.add(mntmHowToPlay);

    final JSeparator separator_1 = new JSeparator();
    mnHelp.add(separator_1);

    final JMenuItem mntmAbout = new JMenuItem("About");
    mntmAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(frmJotto,
            "Free Jotto\n(c) Zhe Lu 2013\nGLPv3", "About",
            JOptionPane.INFORMATION_MESSAGE);
      }
    });
    mnHelp.add(mntmAbout);

    try {
      about_ = new Help();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

  }
}
