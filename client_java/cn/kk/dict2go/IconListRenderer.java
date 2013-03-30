package cn.kk.dict2go;

import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

public class IconListRenderer extends DefaultListCellRenderer {
  private static final long       serialVersionUID = 2991642248694484116L;

  private final Map<String, Icon> icons;

  public IconListRenderer(final Map<String, Icon> icons) {
    this.icons = icons;
  }

  @Override
  public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
    final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    final String val = String.valueOf(value);
    final int idx = val.indexOf(':');
    if (idx != -1) {
      final String lng = val.substring(0, idx).trim().toLowerCase();
      final Icon icon = this.icons.get(lng);
      if (icon != null) {
        label.setIcon(icon);
      }
    }
    return label;
  }
}
