// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ui.components.fields;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.Expandable;
import com.intellij.ui.ScreenUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Function;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static com.intellij.openapi.keymap.KeymapUtil.createTooltipText;
import static java.awt.event.InputEvent.CTRL_MASK;
import static java.beans.EventHandler.create;
import static java.util.Collections.singletonList;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 * @author Sergey Malenkov
 */
public class ExpandableTextField extends ExtendableTextField implements Expandable {
  private static final int MINIMAL_WIDTH = 50;
  private final Function<String, String> parser;
  private final Function<String, String> joiner;
  private JBPopup popup;
  private String title;

  /**
   * Creates an expandable text field with the default line parser/joiner,
   * that uses a whitespaces to split a string to several lines.
   */
  public ExpandableTextField() {
    this(ParametersListUtil.DEFAULT_LINE_PARSER, ParametersListUtil.DEFAULT_LINE_JOINER);
  }

  /**
   * Creates an expandable text field with the specified line parser/joiner.
   *
   * @see ParametersListUtil
   */
  public ExpandableTextField(@NotNull Function<String, List<String>> parser, @NotNull Function<List<String>, String> joiner) {
    this.parser = text -> StringUtil.join(parser.fun(text), "\n");
    this.joiner = text -> joiner.fun(Arrays.asList(StringUtil.splitByLines(text)));
    addAncestorListener(create(AncestorListener.class, this, "collapse"));
    addComponentListener(create(ComponentListener.class, this, "collapse"));
    putClientProperty("monospaced", true);
    setExtensions(createExtensions());
  }

  protected List<ExtendableTextComponent.Extension> createExtensions() {
    return singletonList(new ExtendableTextComponent.Extension() {
      @Override
      public Icon getIcon(boolean hovered) {
        return hovered ? AllIcons.General.ExpandComponentHover : AllIcons.General.ExpandComponent;
      }

      @Override
      public Runnable getActionOnClick() {
        return ExpandableTextField.this::expand;
      }

      @Override
      public String getTooltip() {
        return createTooltipText("Expand", "ExpandExpandableComponent");
      }
    });
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!enabled) collapse();
    super.setEnabled(enabled);
  }

  @Override
  public void collapse() {
    if (popup != null) popup.cancel();
  }

  @Override
  public boolean isExpanded() {
    return popup != null;
  }

  @Override
  public void expand() {
    if (popup != null || !isEnabled()) return;

    Font font = getFont();
    FontMetrics metrics = font == null ? null : getFontMetrics(font);
    int height = metrics == null ? 16 : metrics.getHeight();
    Dimension size = new Dimension(height * 32, height * 16);

    JTextArea area = new JTextArea(parser.fun(getText()));
    area.setEditable(isEditable());
    area.setBackground(getBackground());
    area.setForeground(getForeground());
    area.setFont(font);
    area.setWrapStyleWord(true);
    area.setLineWrap(true);
    area.putClientProperty(Expandable.class, this);
    copyCaretPosition(this, area);
    UIUtil.addUndoRedoActions(area);

    JBScrollPane pane = new JBScrollPane(area);
    pane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
    pane.getVerticalScrollBar().setBackground(area.getBackground());
    pane.getVerticalScrollBar().add(JBScrollBar.LEADING, new JLabel(AllIcons.General.CollapseComponent) {{
      setToolTipText(createTooltipText("Collapse", "CollapseExpandableComponent"));
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      setBorder(JBUI.Borders.empty(5, 0, 5, 5));
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent event) {
          setIcon(AllIcons.General.CollapseComponentHover);
        }

        @Override
        public void mouseExited(MouseEvent event) {
          setIcon(AllIcons.General.CollapseComponent);
        }

        @Override
        public void mousePressed(MouseEvent event) {
          collapse();
        }
      });
    }});

    Insets insets = getInsets();
    Insets margins = getMargin();

    JBInsets.addTo(size, insets);
    JBInsets.addTo(size, margins);
    JBInsets.addTo(size, pane.getInsets());
    if (size.width - MINIMAL_WIDTH < getWidth()) size.width = getWidth();

    Point location = new Point(0, 0);
    SwingUtilities.convertPointToScreen(location, this);
    Rectangle screen = ScreenUtil.getScreenRectangle(this);
    int bottom = screen.y - location.y + screen.height;
    if (bottom < size.height) {
      int top = location.y - screen.y + getHeight();
      if (top < bottom) {
        size.height = bottom;
      }
      else {
        if (size.height > top) size.height = top;
        location.y -= size.height - getHeight();
      }
    }
    pane.setPreferredSize(size);
    pane.setViewportBorder(BorderFactory.createEmptyBorder(insets.top + margins.top,
                                                           insets.left + margins.left,
                                                           insets.bottom + margins.bottom,
                                                           insets.right + margins.right));

    popup = JBPopupFactory.getInstance()
      .createComponentPopupBuilder(pane, area)
      .setMayBeParent(true) // this creates a popup as a dialog with alwaysOnTop=false
      .setFocusable(true)
      .setRequestFocus(true)
      .setTitle(title)
      .setLocateByContent(true)
      .setCancelOnWindowDeactivation(false)
      .setKeyboardActions(singletonList(Pair.create(event -> {
        collapse();
        Window window = UIUtil.getWindow(this);
        if (window != null) {
          window.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), CTRL_MASK, KeyEvent.VK_ENTER, '\r'));
        }
      }, getKeyStroke(KeyEvent.VK_ENTER, CTRL_MASK))))
      .setCancelCallback(() -> {
        try {
          if (isEditable()) {
            setText(joiner.fun(area.getText()));
            copyCaretPosition(area, this);
          }
          popup = null;
          return true;
        }
        catch (Exception ignore) {
          return false;
        }
      }).createPopup();
    popup.show(new RelativePoint(location));
  }

  private static void copyCaretPosition(JTextComponent source, JTextComponent destination) {
    try {
      destination.setCaretPosition(source.getCaretPosition());
    }
    catch (Exception ignored) {
    }
  }
}
