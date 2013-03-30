package cn.kk.dict2go;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;

import org.eclipse.wb.swing.FocusTraversalOnArray;

public class LanguagesChooser extends JDialog implements ActionListener {
  private static final long              serialVersionUID = 2719011369334453598L;

  int                                    idxDropTarget    = -1;

  private final ListTransferHandler      transferHandler;

  private final DefaultListModel<String> lstModelAvailable;

  private final DefaultListModel<String> lstModelSelected;

  final JList<String>                    lstAvailable;

  final JList<String>                    lstSelected;

  private JPanel                         pnlButtons;

  private JButton                        btnSave;

  private JButton                        btnSort;

  private boolean                        changed;

  /**
   * Launch the application.
   */
  public static void main(final String[] args) {
    try {
      final String[] available = new String[] { "1 [1]", "2 [2]", "3 [3]", "4 [4]", "5 [5]", "6 [6]" };
      final LanguagesChooser dialog = new LanguagesChooser(null, Arrays.asList(available), null);
      dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public LanguagesChooser(final JFrame parent, final List<String> available, final List<String> selected) {
    super(parent, "语言设置", ModalityType.APPLICATION_MODAL);
    this.setIconImage(Main.iconOptions.getImage());
    final IconListRenderer cellRenderer = new IconListRenderer(new HashMap<String, Icon>());
    this.transferHandler = new ListTransferHandler();
    this.lstModelAvailable = new DefaultListModel<>();
    this.lstModelSelected = new DefaultListModel<>();
    this.lstAvailable = new JList<>(this.lstModelAvailable);
    this.lstAvailable.setCellRenderer(cellRenderer);
    this.lstAvailable.setTransferHandler(this.transferHandler);
    this.lstAvailable.setDragEnabled(true);
    this.lstSelected = new JList<>(this.lstModelSelected);
    this.lstSelected.setCellRenderer(cellRenderer);
    this.lstSelected.setTransferHandler(this.transferHandler);
    this.lstSelected.setDragEnabled(true);

    if (available != null) {
      for (final String s : available) {
        this.lstModelAvailable.addElement(s);
      }
    }

    if (selected != null) {
      for (final String s : selected) {
        this.lstModelSelected.addElement(s);
      }
    }

    this.setBounds(100, 100, 450, 300);
    {
      this.pnlButtons = new JPanel();
      this.pnlButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));

      {
        this.btnSort = new JButton("按词汇数量排列");
        this.btnSort.setHorizontalAlignment(SwingConstants.LEFT);
        this.btnSort.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if ("按词汇数量排列".equals(LanguagesChooser.this.btnSort.getText())) {
              LanguagesChooser.this.btnSort.setText("按语言代码排列");
            } else {
              LanguagesChooser.this.btnSort.setText("按词汇数量排列");
            }
            LanguagesChooser.this.sortAvailable();
          }

        });
        this.pnlButtons.add(this.btnSort);
      }
      {
        this.btnSave = new JButton("保存");
        this.btnSave.addActionListener(this);
        this.pnlButtons.add(this.btnSave);
        // getRootPane().setDefaultButton(btnSave);
      }
    }
    this.sortAvailable();
    final JPanel panel = new JPanel();
    final GroupLayout groupLayout = new GroupLayout(this.getContentPane());
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
        groupLayout.createSequentialGroup().addGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(this.pnlButtons, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, Short.MAX_VALUE))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
        groupLayout.createSequentialGroup().addComponent(panel, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
            .addComponent(this.pnlButtons, GroupLayout.DEFAULT_SIZE, 40, 40)));
    this.pnlButtons.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.btnSort, this.btnSave }));
    panel.setLayout(new GridLayout(0, 2, 0, 0));
    {
      final JPanel pnlAvailable = new JPanel();
      panel.add(pnlAvailable);
      final JLabel lblAvailable = new JLabel("可供选择语言：");
      final GroupLayout gl_pnlAvailable = new GroupLayout(pnlAvailable);
      final JScrollPane spAvailable = new JScrollPane(this.lstAvailable);
      gl_pnlAvailable.setHorizontalGroup(gl_pnlAvailable.createParallelGroup(Alignment.LEADING).addGroup(
          gl_pnlAvailable.createSequentialGroup().addGroup(
              gl_pnlAvailable.createParallelGroup(Alignment.LEADING).addComponent(lblAvailable, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, Short.MAX_VALUE)
                  .addComponent(spAvailable, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, Short.MAX_VALUE))));
      gl_pnlAvailable.setVerticalGroup(gl_pnlAvailable.createParallelGroup(Alignment.LEADING).addGroup(
          gl_pnlAvailable.createSequentialGroup()
              .addComponent(lblAvailable, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
              .addComponent(spAvailable, GroupLayout.PREFERRED_SIZE, 20, Short.MAX_VALUE)));
      pnlAvailable.setLayout(gl_pnlAvailable);
      pnlAvailable.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.lstAvailable }));
    }
    {
      final JPanel pnlSelected = new JPanel();
      panel.add(pnlSelected);

      final JLabel lblSelected = new JLabel("用户语言排序：");
      final GroupLayout gl_pnlSelected = new GroupLayout(pnlSelected);
      final JScrollPane spSelected = new JScrollPane(this.lstSelected);
      gl_pnlSelected.setHorizontalGroup(gl_pnlSelected.createParallelGroup(Alignment.LEADING).addGroup(
          gl_pnlSelected.createSequentialGroup().addGroup(
              gl_pnlSelected.createParallelGroup(Alignment.LEADING).addComponent(lblSelected, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, Short.MAX_VALUE)
                  .addComponent(spSelected, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, Short.MAX_VALUE))));
      gl_pnlSelected.setVerticalGroup(gl_pnlSelected.createParallelGroup(Alignment.LEADING).addGroup(
          gl_pnlSelected.createSequentialGroup().addComponent(lblSelected, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
              .addComponent(spSelected, GroupLayout.PREFERRED_SIZE, 20, Short.MAX_VALUE)));
      pnlSelected.setLayout(gl_pnlSelected);
      pnlSelected.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.lstSelected }));
    }
    panel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.lstAvailable, this.lstSelected }));
    this.getContentPane().setLayout(groupLayout);
    this.getContentPane().setFocusTraversalPolicy(
        new FocusTraversalOnArray(new Component[] { this.btnSort, this.btnSave, this.lstAvailable, this.lstSelected }));

    this.lstAvailable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          LanguagesChooser.this.selectFromAvailable();
        }
      }
    });
    this.lstAvailable.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_ENTER) || (e.getKeyCode() == KeyEvent.VK_RIGHT)) {
          LanguagesChooser.this.selectFromAvailable();
        }
      }
    });

    this.lstSelected.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          LanguagesChooser.this.removeSelected();
        }
      }
    });

    this.lstSelected.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_LEFT)) {
          LanguagesChooser.this.removeSelected();
        } else if (e.isAltDown()) {
          if (e.getKeyCode() == KeyEvent.VK_UP) {
            final int[] indices = LanguagesChooser.this.lstSelected.getSelectedIndices();
            if (indices.length > 0) {
              final int idx = indices[0];
              if (idx != 0) {
                LanguagesChooser.this.idxDropTarget = idx - 1;
              }
            }
            LanguagesChooser.this.changeOrder();
          } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            final int[] indices = LanguagesChooser.this.lstSelected.getSelectedIndices();
            if (indices.length > 0) {
              final int idx = indices[indices.length - 1];
              LanguagesChooser.this.idxDropTarget = idx + 1;
            }
            LanguagesChooser.this.changeOrder();
          }
        }
      }
    });
    if (parent != null) {
      this.setLocationRelativeTo(parent);
    }
    this.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.btnSort, this.btnSave, this.lstAvailable, this.lstSelected }));
  }

  void sortAvailable() {
    final List<String> current = new ArrayList<>(LanguagesChooser.this.lstModelAvailable.getSize());
    for (int i = 0; i < LanguagesChooser.this.lstModelAvailable.getSize(); i++) {
      current.add(LanguagesChooser.this.lstModelAvailable.getElementAt(i));
    }
    if ("按词汇数量排列".equals(this.btnSort.getText())) {
      Collections.sort(current);
      LanguagesChooser.this.lstModelAvailable.clear();
      for (final String s : current) {
        LanguagesChooser.this.lstModelAvailable.addElement(s);
      }
    } else {
      Collections.sort(current, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          final int idx1 = o1.lastIndexOf('(') + 1;
          final int idx2 = o2.lastIndexOf('(') + 1;
          final String c1 = o1.substring(idx1, o1.length() - 1);
          final String c2 = o2.substring(idx2, o2.length() - 1);
          return Integer.parseInt(c2) - Integer.parseInt(c1);
        }
      });
      LanguagesChooser.this.lstModelAvailable.clear();
      for (final String s : current) {
        LanguagesChooser.this.lstModelAvailable.addElement(s);
      }
    }
  }

  void changeOrder() {
    final int[] selected = this.lstSelected.getSelectedIndices();
    if (selected.length > 0) {
      final List<String> values = LanguagesChooser.getValues(this.lstModelSelected, selected);
      if (this.idxDropTarget == -1) {
        this.idxDropTarget = this.lstModelSelected.size();
      } else {
        this.idxDropTarget++;
      }
      if (this.idxDropTarget > this.lstModelSelected.size()) {
        this.idxDropTarget = this.lstModelSelected.size();
      }
      for (int i = selected.length - 1; i >= 0; i--) {
        final int s = selected[i];
        this.lstModelSelected.remove(s);
        if (this.idxDropTarget >= s) {
          this.idxDropTarget--;
        }
      }
      // paste
      for (int i = 0; i < selected.length; i++) {
        final String v = values.get(i);
        selected[i] = this.idxDropTarget;
        this.lstModelSelected.add(this.idxDropTarget++, v);
      }
      this.lstSelected.setSelectedIndices(selected);
      this.idxDropTarget = -1;
    }
  }

  void removeSelected() {
    final int[] selected = this.lstSelected.getSelectedIndices();
    if (selected.length > 0) {
      final List<String> values = LanguagesChooser.getValues(this.lstModelSelected, selected);
      for (int i = selected.length - 1; i >= 0; i--) {
        this.lstModelSelected.remove(selected[i]);
      }
      // paste
      for (String v : values) {
        this.lstModelAvailable.addElement(v);
      }
      this.sortAvailable();
      this.lstSelected.clearSelection();
      this.idxDropTarget = -1;
    }
  }

  void selectFromAvailable() {
    final int[] selected = this.lstAvailable.getSelectedIndices();
    if (selected.length > 0) {
      final List<String> values = LanguagesChooser.getValues(this.lstModelAvailable, selected);
      for (String v : values) {
        this.lstModelAvailable.removeElement(v);
      }
      // paste
      if (this.idxDropTarget == -1) {
        this.idxDropTarget = this.lstModelSelected.size();
      }
      if (this.idxDropTarget > this.lstModelSelected.size()) {
        this.idxDropTarget = this.lstModelSelected.size();
      }
      for (int i = 0; i < selected.length; i++) {
        final String v = values.get(i);
        selected[i] = this.idxDropTarget;
        this.lstModelSelected.add(this.idxDropTarget++, v);
      }
      this.lstSelected.setSelectedIndices(selected);
      this.lstAvailable.clearSelection();
      this.idxDropTarget = -1;
    }
  }

  private static List<String> getValues(final AbstractListModel<String> lstModel, final int[] selected) {
    final List<String> values = new ArrayList<>(selected.length);
    for (final int i : selected) {
      values.add(String.valueOf(lstModel.getElementAt(i)));
    }
    return values;
  }

  class ListTransferHandler extends TransferHandler {
    private static final long serialVersionUID = -8115786584973100926L;

    private JComponent        target           = null;

    private JComponent        source           = null;

    @Override
    protected synchronized void exportDone(final JComponent src, final Transferable data, final int action) {
      if (LanguagesChooser.this.lstAvailable == this.source) {
        if (LanguagesChooser.this.lstSelected == this.target) {
          LanguagesChooser.this.selectFromAvailable();
        }
      } else if (LanguagesChooser.this.lstSelected == this.source) {
        if (LanguagesChooser.this.lstAvailable == this.target) {
          LanguagesChooser.this.removeSelected();
        } else {
          LanguagesChooser.this.changeOrder();
        }
      }
    }

    @Override
    public int getSourceActions(final JComponent c) {
      return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean canImport(final JComponent tgt, final DataFlavor[] flavors) {
      this.target = tgt;
      LanguagesChooser.this.idxDropTarget = ((JList<?>) tgt).getSelectedIndex();
      return !((this.target == LanguagesChooser.this.lstAvailable) && (this.source == LanguagesChooser.this.lstAvailable));
    }

    @Override
    protected Transferable createTransferable(final JComponent src) {
      this.source = src;
      return new Transferable() {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
          return null;
        }

        @Override
        public boolean isDataFlavorSupported(final DataFlavor flavor) {
          return true;
        }

        @Override
        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
          return Boolean.TRUE;
        }
      };
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    if (this.btnSave == e.getSource()) {
      this.changed = true;
    } else {
      this.changed = false;
    }
    this.setVisible(false);
  }

  public boolean isChanged() {
    return this.changed;
  }

  public List<String> getSelected() {
    return Collections.list(this.lstModelSelected.elements());
  }

}
