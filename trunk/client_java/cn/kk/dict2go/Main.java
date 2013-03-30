package cn.kk.dict2go;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.html.HTMLDocument;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import cn.kk.dict2go.lib.ClientHelper;
import cn.kk.dict2go.lib.Context;
import cn.kk.dict2go.lib.Language;
import cn.kk.dict2go.lib.SearchEngine;
import cn.kk.dict2go.lib.SearchResultListener;
import cn.kk.dict2go.lib.SearchTask;
import cn.kk.dict2go.lib.SearchType;
import cn.kk.dict2go.lib.Translation;
import cn.kk.dict2go.lib.TranslationComparator;

public class Main {

  private static final String  TXT_NO_RESULTS   = "<html>抱歉，没有找到与“<font color=darkred>%s</font>”相关的翻译。<br><br>建议：<ul><li>简化输入词</li><li>尝试其他相关词</li><li>在语言选项里选择更多的语言</li></ul></html>";

  private static final String  TXT_RESULTS      = "<html><head><base href='" + Configuration.getBasePath() + "'></head><body align=left>%s</body></html>";

  private static final String  TXT_HINT         = "<html><head><base href='" + Configuration.getBasePath()
                                                    + "'></head><body><img src='up.png'>&nbsp;&nbsp;请输入要翻译的词汇&nbsp;&nbsp;<img src='up.png'></body></html>";

  private final Clipboard      clipboard        = Toolkit.getDefaultToolkit().getSystemClipboard();
  JFrame                       frmMain;

  JPanel                       pnlMain;

  JComboBox<String>            itmInput;

  JButton                      btnOptions;

  JTextField                   itmInputEditor;

  long                         lastInputTime    = -1;

  String                       lastInputText    = null;

  String                       searchText       = null;

  String                       autocompleteText = null;

  JTextPane                    itmResult;

  HTMLDocument                 itmResultDoc;

  final Color                  COLOR_NORMAL     = Color.WHITE;

  final Color                  COLOR_SEARCHING  = new Color(255, 250, 244);

  JLabel                       lblWait;

  final static ImageIcon       iconWait         = new ImageIcon(Main.class.getResource("/wait.gif"));

  final static ImageIcon       iconOptions      = new ImageIcon(Main.class.getResource("/options.png"));

  final static ImageIcon       iconWindow       = new ImageIcon(Main.class.getResource("/icon.png"));

  Vector<String>               vInputHistories;

  DefaultComboBoxModel<String> cbmInputHistories;

  JComboBox<String>            cbAutocomplete;

  DefaultComboBoxModel<String> cbmAutocomplete;

  Context                      searchContext;
  int                          resultCount      = -1;
  int                          resultDisplayed  = -1;

  /**
   * Launch the application.
   * 
   * @throws UnsupportedLookAndFeelException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  public static void main(final String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          final Main window = new Main();
          window.frmMain.setVisible(true);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  void enable() {
    this.itmInput.setEnabled(true);
    this.itmResult.setEnabled(true);
    this.btnOptions.setEnabled(true);
  }

  /**
   * Create the application.
   */
  public Main() {

    this.initialize();
    SearchEngine.getExecutor().execute(new Runnable() {

      @Override
      public void run() {
        SearchEngine.initialize(1, Configuration.getSelectedLanguageIds(), Configuration.getWaitCheckResult());
        Main.this.searchContext = SearchEngine.borrowContext();
        SearchEngine.setLanguages(Main.this.searchContext, Configuration.getSelectedLanguageIds());
        Main.this.load();
        Main.this.enable();
      }

    });
  }

  final static List<String> toSuggestions(List<Translation> trls) {
    final List<String> suggestions = new ArrayList<>(trls.size());
    for (Translation t : trls) {
      suggestions.add(t.srcVal);
    }
    return suggestions;
  }

  void load() {

    SearchEngine.addListener(new SearchResultListener(SearchType.LIST) {
      @Override
      public void onResultChanged(final SearchTask task) {
        if (Main.this.adjusting) {
          if ((Main.this.resultCount == -1) && !task.isCancelled() && task.isComplete()) {
            final List<Translation> trls = task.getResult().getTranslations();
            List<String> suggestions = Main.toSuggestions(trls);
            Main.this.finishAutocomplete(suggestions);
            Main.this.adjusting = false;
          }
          if (task.isCancelled()) {
            Main.this.adjusting = false;
          }
        }
      }
    });
    SearchEngine.addListener(new SearchResultListener(SearchType.LIST) {
      @Override
      public void onResultChanged(final SearchTask task) {
        final List<Translation> trls = task.getResult().getTranslations();
        if ((Main.this.resultCount == -1) && task.isComplete()) {
          Main.this.resultCount = trls.size();
          Collections.sort(trls, new TranslationComparator(Main.this.searchContext, Configuration.getSelectedLanguageIds()));
          Main.this.resultDisplayed = 0;
          Main.this.setResults(trls);
        } else if (task.isDeepComplete()) {
          Collections.sort(trls, new TranslationComparator(Main.this.searchContext, Configuration.getSelectedLanguageIds()));
          Main.this.resultDisplayed = 0;
          Main.this.setResults(trls);
          Main.this.displayNormalLook();
        } else {
          // Main.this.appendResults(trls);
        }
      }
    });

    this.setupAutoComplete();
    if (Configuration.getWaitStartAutocomplete() > 0) {
      SearchEngine.getTimer().schedule(new TimerTask() {
        @Override
        public void run() {
          if ((Main.this.lastInputTime != -1) && ((System.currentTimeMillis() - Main.this.lastInputTime) > Configuration.getWaitStartAutocomplete())) {
            Main.this.startSearch();
          }
          if (Main.this.cbAutocomplete.isPopupVisible() && (Main.this.cbAutocomplete.getSelectedIndex() == -1)) {
            // TODO config
            if (((System.currentTimeMillis() - Main.this.lastInputTime) > 5000) || !Main.this.itmInputEditor.isFocusOwner()) {
              Main.this.cbAutocomplete.setPopupVisible(false);
            }
          }
        }
      }, Configuration.getWaitStartAutocomplete(), Configuration.getWaitStartAutocomplete());
      SearchEngine.getTimer().schedule(new TimerTask() {
        @Override
        public void run() {
          final Transferable t = Main.this.clipboard.getContents(null);
          try {
            if ((t != null) && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
              final String text = (String) t.getTransferData(DataFlavor.stringFlavor);
              if (ClientHelper.isNotEmptyOrNull(text) && (text.length() < 40) && (text.length() > 2) && !text.equals(Main.this.lastClipBoardInput)) {
                Main.this.itmInputEditor.setText(text);
                Main.this.lastClipBoardInput = text;
                Main.this.startSearch();
              }
            }
          } catch (Exception e) {
          }
        }
      }, 500, 500);

    }
  }

  private String              lastClipBoardInput = null;

  private final StringBuilder resultBuilder      = new StringBuilder(1000);

  protected void setResults(final List<Translation> trls) {
    final int size = trls.size();
    Main.this.resultBuilder.setLength(0);
    if (Main.this.resultDisplayed == -1) {
      Main.this.resultDisplayed = 0;
    }
    if (Main.this.resultDisplayed < size) {
      this.resultBuilder.append("<html><head><base href='").append(Configuration.getBasePath()).append("'></head><body align=left>");
      for (int i = Main.this.resultDisplayed; i < size; i++) {
        final Translation trl = trls.get(i);
        if (this.searchContext.isValid(trl.srcVal) || this.searchContext.isValid(trl.tgtVal)) {
          Main.this.resultBuilder.append("<ul><li>").append(Language.get(trl.srcLng));
          Main.this.resultBuilder.append("</li><li>").append(trl.srcVal);
          Main.this.resultBuilder.append("</li><li>").append(Language.get(trl.tgtLng));
          Main.this.resultBuilder.append("</li><li>").append(trl.tgtVal).append("</li></ul>");
        }
      }
      this.resultBuilder.append("</body></html>");
      Main.this.resultDisplayed = size;
      final String text = Main.this.resultBuilder.toString();
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          synchronized (Main.this.itmResult) {
            Main.this.itmResult.setText(text);
            Main.this.itmResult.setCaretPosition(0);
          }
        }
      });
    }
  }

  protected void displayResults(final SearchTask task) {
    final String input = task.getInput();
    if (input.equals(this.lastInputText)) {
      System.out.println("display result: " + task.getResult());
      if (task.getResult().getTranslations().isEmpty()) {
        this.itmResult.setText(String.format(Main.TXT_NO_RESULTS, input));
      } else {
        this.itmResult.setText("<html>" + task.getResult() + "</html>");
      }

    }
  }

  void startSearch() {
    final String txtInput = this.itmInputEditor.getText();
    if (!txtInput.equals(this.lastInputText) && !txtInput.isEmpty()) {
      this.lastInputTime = -1;
      this.lastInputText = txtInput;
      this.startAutocomplete();
      this.displaySearchingLook();
      this.itmInput.hidePopup();
      this.itmInputEditor.setText("");
      if (!this.vInputHistories.contains(txtInput)) {
        this.vInputHistories.add(txtInput);
        // repaint popup list and set caret to the end
        this.cbmInputHistories.setSelectedItem(txtInput);
        this.itmInputEditor.setCaretPosition(this.itmInputEditor.getText().length());
        if (this.vInputHistories.size() > Configuration.getMaxInputHistories()) {
          this.vInputHistories.remove(0);
        }
      }
      this.resultCount = -1;
      this.resultDisplayed = -1;
      SearchEngine.startSearchTask(SearchType.DEEP_SEARCH, this.searchContext, txtInput);
      System.out.println("startSearchTimer: " + txtInput);
    }
  }

  void startAutocomplete() {
    this.adjusting = true;
    this.cbAutocomplete.setPopupVisible(false);
    this.cbmAutocomplete.removeAllElements();
  }

  void cancelSearch() {
    this.displayNormalLook();
  }

  void displaySearchingLook() {
    this.itmInputEditor.setBackground(this.COLOR_SEARCHING);
    this.itmResult.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    this.lblWait.setVisible(true);
    // this.pnlMain.repaint();
  }

  void displayNormalLook() {
    Main.this.lastInputText = null;
    this.searchText = null;
    this.autocompleteText = null;
    Main.this.itmInputEditor.setBackground(Main.this.COLOR_NORMAL);
    Main.this.itmResult.setCursor(Cursor.getDefaultCursor());
    Main.this.lblWait.setVisible(false);
    // Main.this.pnlMain.repaint();

  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    this.vInputHistories = new Vector<>(Configuration.getMaxInputHistories());
    this.cbmInputHistories = new DefaultComboBoxModel<>(this.vInputHistories);
    this.itmInput = new JComboBox<>(this.cbmInputHistories);
    this.itmInput.setEnabled(false);
    this.itmInput.setEditable(true);
    this.itmInput.setBorder(new EmptyBorder(2, 2, 2, 2));
    this.itmInput.setBackground(this.COLOR_NORMAL);
    this.itmInputEditor = (JTextField) this.itmInput.getEditor().getEditorComponent();
    this.itmInputEditor.setFont(this.itmInputEditor.getFont().deriveFont(20f));
    this.itmInputEditor.setBorder(new EmptyBorder(0, 6, 0, 6));
    // itmInputEditor.setBorder();
    this.itmInput.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        Main.this.startSearch();
      }
    });
    this.itmInputEditor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        final String txtInput = Main.this.itmInputEditor.getText();
        if ((txtInput.length() == 0) || (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
          Main.this.cancelSearch();
        } else if (e.getKeyCode() != KeyEvent.VK_ENTER) {
          Main.this.lastInputTime = System.currentTimeMillis();
        } else {
          Main.this.startSearch();
        }
      }
    });

    this.frmMain = new JFrame("中外离线电子词典 (v201212)");
    this.frmMain.setAlwaysOnTop(true);
    this.frmMain.setLocationByPlatform(true);
    this.frmMain.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        if (!Main.this.frmMain.isUndecorated()) {
          SearchEngine.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
              if (!Main.this.frmMain.isFocused() && !Main.this.frmMain.isUndecorated()) {
                Main.this.frmMain.dispose();
                Main.this.frmMain.setUndecorated(true);
                Main.this.frmMain.setVisible(true);
              }
            }
          }, 100);
        }
      }
    });
    final long eventMask = AWTEvent.MOUSE_EVENT_MASK;
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      @Override
      public void eventDispatched(AWTEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
          if (Main.this.frmMain.isUndecorated()) {
            SearchEngine.getTimer().schedule(new TimerTask() {
              @Override
              public void run() {
                if (Main.this.frmMain.isFocused() && Main.this.frmMain.isUndecorated()) {
                  Main.this.frmMain.dispose();
                  Main.this.frmMain.setUndecorated(false);
                  Main.this.frmMain.setVisible(true);
                }
              }
            }, 100);
          }
        }
      }
    }, eventMask);
    this.frmMain.setBounds(100, 100, 450, 300);
    this.frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.frmMain.setIconImage(Main.iconWindow.getImage());
    this.frmMain.getContentPane().setLayout(new BorderLayout(0, 0));

    this.pnlMain = new JPanel();
    this.pnlMain.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    this.frmMain.getContentPane().add(this.pnlMain, BorderLayout.NORTH);
    this.pnlMain.setLayout(new BoxLayout(this.pnlMain, BoxLayout.X_AXIS));

    this.lblWait = new JLabel(Main.iconWait, SwingConstants.CENTER);
    this.lblWait.setBackground(this.COLOR_SEARCHING);
    this.lblWait.setVisible(false);
    this.pnlMain.add(this.lblWait);

    this.pnlMain.add(this.itmInput);

    this.btnOptions = new JButton(Main.iconOptions);
    this.btnOptions.setEnabled(false);
    this.btnOptions.setBorder(new EmptyBorder(4, 4, 4, 4));
    this.btnOptions.setBackground(this.COLOR_NORMAL);
    this.btnOptions.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        Main.this.showOptions();
      }

    });
    this.pnlMain.add(this.btnOptions);
    this.pnlMain.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.itmInput, this.btnOptions }));

    this.itmResult = new JTextPane();
    this.itmResult.setEnabled(false);
    this.itmResult.setEditable(false);
    this.itmResult.setContentType("text/html");
    this.itmResult.setText(Main.TXT_HINT);
    this.itmResultDoc = (HTMLDocument) this.itmResult.getStyledDocument();
    final JScrollPane spResult = new JScrollPane(this.itmResult);
    this.frmMain.getContentPane().add(spResult, BorderLayout.CENTER);

    this.frmMain.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.itmInput, this.btnOptions, this.itmResult }));
    this.displayNormalLook();
  }

  boolean adjusting;

  void setupAutoComplete() {
    this.cbmAutocomplete = new DefaultComboBoxModel<>();
    this.cbAutocomplete = new JComboBox<String>(this.cbmAutocomplete) {
      private static final long serialVersionUID = 1L;

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 0);
      }
    };
    this.adjusting = false;
    this.cbAutocomplete.setSelectedItem(null);
    this.cbAutocomplete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (!Main.this.adjusting) {
          if ((Main.this.cbAutocomplete.getSelectedItem() != null) && (InputEvent.BUTTON1_MASK == (e.getModifiers() & InputEvent.BUTTON1_MASK))) {
            Main.this.itmInputEditor.setText(Main.this.cbAutocomplete.getSelectedItem().toString());
          }
        }
      }
    });

    this.itmInputEditor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        Main.this.adjusting = true;
        final int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE) {
          if (Main.this.cbAutocomplete.isPopupVisible()) {
            e.setKeyCode(KeyEvent.VK_ENTER);
          }
        }
        if ((keyCode == KeyEvent.VK_ENTER) || (keyCode == KeyEvent.VK_UP) || (keyCode == KeyEvent.VK_DOWN)) {
          e.setSource(Main.this.cbAutocomplete);
          Main.this.cbAutocomplete.dispatchEvent(e);
          if ((keyCode == KeyEvent.VK_ENTER) && (Main.this.cbAutocomplete.getSelectedIndex() != -1)) {
            Main.this.itmInputEditor.setText(Main.this.cbAutocomplete.getSelectedItem().toString());
            Main.this.cbAutocomplete.setPopupVisible(false);
          }
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
          Main.this.cbAutocomplete.setPopupVisible(false);
        }
        Main.this.adjusting = false;
      }
    });
    this.itmInputEditor.setLayout(new BorderLayout());
    this.itmInputEditor.add(this.cbAutocomplete, BorderLayout.SOUTH);
  }

  void showOptions() {
    Main.this.cancelSearch();
    final LanguagesChooser dlg = new LanguagesChooser(Main.this.frmMain, Configuration.toDisplayNames(Configuration.getAvailableLanguages()),
        Configuration.toDisplayNames(Configuration.getSelectedLanguages()));
    dlg.setVisible(true);
    if (dlg.isChanged()) {
      // System.out.println("changed: " + dlg.getSelected());
      Configuration.setSelectedLanguages(Configuration.toIsoNames(dlg.getSelected()));
      Configuration.save();
      SearchEngine.setLanguages(Main.this.searchContext, Configuration.getSelectedLanguageIds());
    }
    if (Main.this.itmInputEditor.getText().length() > 0) {
      Main.this.startSearch();
    }
  }

  void finishAutocomplete(final List<String> suggestions) {
    if (!suggestions.isEmpty()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          for (final String s : suggestions) {
            Main.this.cbmAutocomplete.addElement(s);
          }
          Main.this.cbAutocomplete.setSelectedItem(null);
          Main.this.cbAutocomplete.setPopupVisible(true);
        }
      });
    } else {
      this.cbAutocomplete.setPopupVisible(false);
    }
  }
}
