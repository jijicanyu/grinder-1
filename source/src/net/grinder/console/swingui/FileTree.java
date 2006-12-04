// Copyright (C) 2004, 2005 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.grinder.console.common.ConsoleException;
import net.grinder.console.common.ErrorHandler;
import net.grinder.console.common.Resources;
import net.grinder.console.editor.Buffer;
import net.grinder.console.editor.EditorModel;
import net.grinder.console.swingui.FileTreeModel.FileNode;


/**
 * Panel containing buffer list and file tree.
 *
 * <p>
 * Listens to the Editor Model, and updates a BufferTreeModel and FileTreeModel
 * appropriately.
 * </p>
 *
 * @author Philip Aston
 * @version $Revision$
 */
final class FileTree {

  private final Resources m_resources;
  private final ErrorHandler m_errorHandler;
  private final EditorModel m_editorModel;
  private final BufferTreeModel m_bufferTreeModel;
  private final FileTreeModel m_fileTreeModel;

  private final JTree m_tree;
  private final OpenAction m_openAction;
  private final SetScriptAction m_setScriptAction;
  private final JScrollPane m_scrollPane;

  public FileTree(Resources resources,
                  ErrorHandler errorHandler,
                  EditorModel editorModel,
                  BufferTreeModel bufferTreeModel,
                  FileTreeModel fileTreeModel) {

    m_resources = resources;
    m_errorHandler = errorHandler;
    m_editorModel = editorModel;

    m_bufferTreeModel = bufferTreeModel;
    m_fileTreeModel = fileTreeModel;

    final CompositeTreeModel compositeTreeModel = new CompositeTreeModel();

    compositeTreeModel.addTreeModel(m_bufferTreeModel, false);
    compositeTreeModel.addTreeModel(m_fileTreeModel, true);

    m_tree = new JTree(compositeTreeModel) {
        /**
         * A new CustomTreeCellRenderer needs to be set whenever the
         * L&F changes because its superclass constructor reads the
         * resources.
         */
        public void updateUI() {
          super.updateUI();

          // Unfortunately updateUI is called from the JTree
          // constructor and we can't use the nested
          // CustomTreeCellRenderer until its enclosing class has been
          // fully initialised. We hack to prevent this with the
          // following conditional.
          if (!isRootVisible()) {
            setCellRenderer(new CustomTreeCellRenderer());
          }
        }
      };

    m_tree.setRootVisible(false);
    m_tree.setShowsRootHandles(true);

    m_tree.setCellRenderer(new CustomTreeCellRenderer());
    m_tree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION);

    addTreeMouseListener(new MouseAdapter() {
      private boolean m_handledOnPress;

      public void mousePressed(MouseEvent e) {
        m_handledOnPress = false;

        if (!e.isConsumed() && SwingUtilities.isLeftMouseButton(e)) {
          final TreePath path =
            m_tree.getPathForLocation(e.getX(), e.getY());

          if (path == null) {
            return;
          }

          final Object selectedNode = path.getLastPathComponent();

          if (selectedNode instanceof Node) {
            final Node node = (Node)selectedNode;
            final int clickCount = e.getClickCount();

            final boolean hasBuffer = node.getBuffer() != null;

            if (clickCount == 2 ||
                clickCount == 1 && hasBuffer) {
              m_openAction.invoke(node);
              m_handledOnPress = true;
              e.consume();
            }

            if (clickCount == 2 && hasBuffer &&
                m_setScriptAction.isEnabled()) {
              m_setScriptAction.invoke();
              m_handledOnPress = true;
              e.consume();
            }
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (m_handledOnPress) {
          // Prevent downstream event handlers from overriding our good work.
          e.consume();
        }
      }
    });

    m_tree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          updateActionState();
        }
      });

    m_openAction = new OpenAction();
    m_setScriptAction = new SetScriptAction();

    // J2SE 1.4 drops the mapping from "ENTER" -> "toggle"
    // (expand/collapse) that J2SE 1.3 has. I like this mapping, so
    // we combine the "toggle" action with our OpenFileAction and let
    // TeeAction figure out which to call based on what's enabled.
    final InputMap inputMap = m_tree.getInputMap();

    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "activateNode");
    inputMap.put(KeyStroke.getKeyStroke("SPACE"), "activateNode");

    final ActionMap actionMap = m_tree.getActionMap();
    actionMap.put("activateNode",
                  new TeeAction(actionMap.get("toggle"), m_openAction));

    m_scrollPane = new JScrollPane(m_tree);

    m_editorModel.addListener(new EditorModelListener());

    updateActionState();
  }

  public void addTreeMouseListener(MouseListener adapter) {
    m_tree.addMouseListener(adapter);
  }

  private class EditorModelListener extends EditorModel.AbstractListener {

    public void bufferAdded(Buffer buffer) {
      // When a file is opened, the new buffer causes the view to
      // scroll down by one row. This feels wrong, so we compensate.
      final int rowHeight = m_tree.getRowBounds(0).height;
      final JScrollBar verticalScrollBar = m_scrollPane.getVerticalScrollBar();
      verticalScrollBar.setValue(verticalScrollBar.getValue() + rowHeight);
    }

    public void bufferStateChanged(Buffer buffer) {
      final File file = buffer.getFile();

      if (file != null) {
        final FileTreeModel.FileNode oldFileNode =
          m_fileTreeModel.findFileNode(buffer);

        // Find a node, if its in our directory structure. This
        // may cause parts of the tree to be refreshed.
        final FileTreeModel.Node node = m_fileTreeModel.findNode(file);

        if (oldFileNode == null || !oldFileNode.equals(node)) {
          // Buffer's associated file has changed.

          if (oldFileNode != null) {
            oldFileNode.setBuffer(null);
          }

          if (node instanceof FileTreeModel.FileNode) {
            final FileTreeModel.FileNode fileNode =
              (FileTreeModel.FileNode)node;

            fileNode.setBuffer(buffer);
            m_tree.scrollPathToVisible(treePathForFileNode(fileNode));
          }
        }
      }

      final FileTreeModel.Node fileNode = m_fileTreeModel.findFileNode(buffer);

      if (fileNode != null) {
        m_fileTreeModel.valueForPathChanged(fileNode.getPath(), fileNode);
      }

      m_bufferTreeModel.bufferChanged(buffer);

      updateActionState();
    }

    public void bufferRemoved(Buffer buffer) {
      final FileTreeModel.FileNode fileNode =
        m_fileTreeModel.findFileNode(buffer);

      if (fileNode != null) {
        fileNode.setBuffer(null);
        m_fileTreeModel.valueForPathChanged(fileNode.getPath(), fileNode);
      }
    }
  }

  public JComponent getComponent() {
    return m_scrollPane;
  }

  public CustomAction getOpenFileAction() {
    return m_openAction;
  }

  public CustomAction getSetScriptAction() {
    return m_setScriptAction;
  }

  /**
   * Action for opening the currently selected file in the tree.
   */
  private final class OpenAction extends CustomAction {
    public OpenAction() {
      super(m_resources, "open-file");
    }

    public void actionPerformed(ActionEvent event) {
      invoke(m_tree.getLastSelectedPathComponent());
    }

    public void invoke(Object selectedNode) {
      if (selectedNode instanceof BufferTreeModel.BufferNode) {
        m_editorModel.selectBuffer(
          ((BufferTreeModel.BufferNode)selectedNode).getBuffer());
      }
      else if (selectedNode instanceof FileTreeModel.FileNode) {
        final FileNode fileNode = (FileTreeModel.FileNode)selectedNode;

        try {
          fileNode.setBuffer(
            m_editorModel.selectBufferForFile(fileNode.getFile()));

          // The above line can add the buffer to the editor model which
          // causes the BufferTreeModel to fire a top level structure
          // change, which in turn causes the selection to clear. We
          // reselect the original node so our actions are enabled
          // correctly.
          m_tree.setSelectionPath(treePathForFileNode(fileNode));
        }
        catch (ConsoleException e) {
          m_errorHandler.handleException(
            e, m_resources.getString("fileError.title"));
        }
      }
    }
  }

  private final class SetScriptAction extends CustomAction {
    public SetScriptAction() {
      super(m_resources, "set-script");
    }

    public void actionPerformed(ActionEvent event) {
      invoke();
    }

    public void invoke() {
      final Object selectedNode = m_tree.getLastSelectedPathComponent();

      if (selectedNode instanceof Node) {
        final Node node = (Node)selectedNode;

        if (node.getFile().isFile()) {
          m_editorModel.setMarkedScript(node.getFile());
          m_bufferTreeModel.valueForPathChanged(node.getPath(), node);
          updateActionState();
        }
      }
    }
  }

  private void updateActionState() {
    if (m_tree.isEnabled()) {
      final Object selectedNode = m_tree.getLastSelectedPathComponent();
      if (selectedNode instanceof Node) {
        final Node node = (Node)selectedNode;

        final Buffer buffer = node.getBuffer();
        final File file = node.getFile();

        m_openAction.setEnabled(
          node.canOpen() &&
          (buffer == null ||
           !buffer.equals(m_editorModel.getSelectedBuffer())));

        m_setScriptAction.setEnabled(
          m_editorModel.isPythonFile(file) &&
          !file.equals(m_editorModel.getMarkedScript()));

        return;
      }
    }

    m_openAction.setEnabled(false);
    m_setScriptAction.setEnabled(false);
  }

  /**
   * Custom cell renderer.
   */
  private final class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
    private final DefaultTreeCellRenderer m_defaultRenderer =
      new DefaultTreeCellRenderer();

    private final Font m_boldFont;
    private final Font m_boldItalicFont;
    private final ImageIcon m_markedScriptIcon =
      m_resources.getImageIcon("script.markedscript.image");
    private final ImageIcon m_pythonIcon =
      m_resources.getImageIcon("script.pythonfile.image");

    private boolean m_active;

    CustomTreeCellRenderer() {
      m_boldFont = new JLabel().getFont().deriveFont(Font.BOLD);
      m_boldItalicFont = m_boldFont.deriveFont(Font.BOLD | Font.ITALIC);
    }

    public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {

      if (value instanceof Node) {
        final Node node = (Node)value;

        final File file = node.getFile();

        if (file != null && !file.isFile()) {
          return m_defaultRenderer.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus);
        }

        final Icon icon;

        if (file != null && file.equals(m_editorModel.getMarkedScript())) {
          icon = m_markedScriptIcon;
        }
        else if (m_editorModel.isPythonFile(file)) {
          icon = m_pythonIcon;
        }
        else {
          icon = m_defaultRenderer.getLeafIcon();
        }

        setLeafIcon(icon);

        final Buffer buffer = node.getBuffer();

        setTextNonSelectionColor(
          buffer == null && m_editorModel.isBoringFile(file) ?
          Colours.INACTIVE_TEXT : m_defaultRenderer.getTextNonSelectionColor());

        if (buffer != null) {
          // File has an open buffer.
          setFont(buffer.isDirty() ? m_boldItalicFont : m_boldFont);
          m_active = buffer.equals(m_editorModel.getSelectedBuffer());
        }
        else {
          setFont(m_defaultRenderer.getFont());
          m_active = false;
        }

        return super.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
      }
      else {
        return m_defaultRenderer.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
      }
    }

    /**
     * Our parent overrides validate() and revalidate() for speed.
     * This means it never resizes. Go with this, but be a few pixels
     * wider to allow text to be italicised.
     */
    public Dimension getPreferredSize() {
      final Dimension result = super.getPreferredSize();

      return result != null ?
        new Dimension(result.width + 3, result.height) : null;
    }

    public void paint(Graphics g) {

      final Color backgroundColour;

      if (m_active) {
        backgroundColour = Colours.FAINT_YELLOW;
        setTextSelectionColor(Colours.BLACK);
        setTextNonSelectionColor(Colours.BLACK);
      }
      else if (selected) {
        backgroundColour = getBackgroundSelectionColor();
        setTextSelectionColor(m_defaultRenderer.getTextSelectionColor());
      }
      else {
        backgroundColour = getBackgroundNonSelectionColor();
        setTextNonSelectionColor(m_defaultRenderer.getTextNonSelectionColor());
      }

      if (backgroundColour != null) {
        g.setColor(backgroundColour);
        g.fillRect(0, 0, getWidth() - 1, getHeight());
      }

      // Sigh. The whole reason we override paint is that the
      // DefaultTreeCellRenderer version is crap. We can't call
      // super.super.paint() so we work hard to make the
      // DefaultTreeCellRenderer version ineffectual.

      final boolean oldHasFocus = hasFocus;
      final boolean oldSelected = selected;
      final Color oldBackgroundNonSelectionColour =
        getBackgroundNonSelectionColor();

      try {
        hasFocus = false;
        selected = false;
        setBackgroundNonSelectionColor(backgroundColour);

        super.paint(g);
      }
      finally {
        hasFocus = oldHasFocus;
        selected = oldSelected;
        setBackgroundNonSelectionColor(oldBackgroundNonSelectionColour);
      }

      // Now draw our border.
      final Color borderColour;

      if (m_active) {
        borderColour = getTextNonSelectionColor();
      }
      else if (hasFocus) {
        borderColour = getBorderSelectionColor();
      }
      else {
        borderColour = null;
      }

      if (borderColour != null) {
        g.setColor(borderColour);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
      }
    }
  }

  /**
   * Hard to see how this could be easily incorporated into
   * CompositeTreeModel without having the child models know about the
   * composite model.
   */
  private TreePath treePathForFileNode(FileTreeModel.FileNode fileNode) {
    final Object[] original = fileNode.getPath().getPath();
    final Object[] result = new Object[original.length + 1];
    System.arraycopy(original, 0, result, 1, original.length);

    result[0] = m_tree.getModel().getRoot();

    return new TreePath(result);
  }

  /**
   * Allows us to treat FileNodes and BufferNodes polymorphically.
   */
  interface Node {

    /**
     * @return <code>null</code> if the node has no associated buffer.
     */
    Buffer getBuffer();

    /**
     * @return <code>null</code> if the node has no associated file.
     */
    File getFile();

    TreePath getPath();

    boolean canOpen();
  }
}