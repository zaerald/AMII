package io.unthrottled.amii.config.ui;

import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.GuiUtils;
import io.unthrottled.amii.assets.CharacterEntity;
import io.unthrottled.amii.assets.VisualAssetEntity;
import io.unthrottled.amii.assets.VisualContentManager;
import io.unthrottled.amii.assets.VisualMemeContent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static io.unthrottled.amii.assets.VisualAssetDefinitionServiceKt.getAssetsByCharacterId;
import static java.util.stream.Collectors.joining;

public final class PreferredCharacterPanel {
  private final PreferredCharacterTree myPreferredCharacterTree;
  private JPanel myPanel;
  private JPanel myTreePanel;

  private JPanel myContainerPanel;
  private JPanel myEmptyPreviewPanel;
  private JButton myShowPreviewButton;
  private JScrollPane myScrollPane;
  private JTextPane myPreviewTextPane;

  public PreferredCharacterPanel(
    Predicate<CharacterEntity> selectionPredicate
  ) {
    myPreferredCharacterTree = new PreferredCharacterTree(selectionPredicate);
    myTreePanel.setLayout(new BorderLayout());
    myTreePanel.add(myPreferredCharacterTree.getComponent(), BorderLayout.CENTER);

    myPreferredCharacterTree.addTreeSelectionListener(e -> {
      Object node = e.getPath().getLastPathComponent();
      if (node instanceof CheckedTreeNode) {
        Object userObject = ((CheckedTreeNode) node).getUserObject();
        if (userObject instanceof CharacterEntity) {
          showPreviewPanel();

          String characterId = ((CharacterEntity) userObject).getId();
          Collection<VisualAssetEntity> assetsByCharacterId = getAssetsByCharacterId(characterId);

          if (assetsByCharacterId.isEmpty()) {
            showEmptyPreviewPanel();
          } else {
            myPreviewTextPane.setText(generateCharacterPreview(assetsByCharacterId));
            myPreviewTextPane.setCaretPosition(0);
          }
        } else {
          showEmptyPreviewPanel();
        }
      }
    });

    GuiUtils.replaceJSplitPaneWithIDEASplitter(myPanel);
  }

  @NotNull
  private String generateCharacterPreview(Collection<VisualAssetEntity> assetsByCharacterId) {
    return assetsByCharacterId.stream()
      .filter(it -> !it.getRepresentation().getPath().isEmpty())
      .map(it ->
        "<div style=\"text-align: center\">" +
          "<img src=\"" +
          VisualContentManager.INSTANCE
            .resolveAsset(it.getRepresentation())
            .map(VisualMemeContent::getFilePath)
            .orElse(URI.create("")) +
          "\">" +
          "</div>" +
          "<br><br>"
      ).collect(joining());
  }

  private void showEmptyPreviewPanel() {
    myEmptyPreviewPanel.setVisible(true);
    myScrollPane.setVisible(false);
  }

  private void showPreviewPanel() {
    myEmptyPreviewPanel.setVisible(false);
    myScrollPane.setVisible(true);
  }

  public void reset() {
    myPreferredCharacterTree.reset();
  }

  public List<CharacterEntity> getSelected() {
    return myPreferredCharacterTree.getSelected();
  }

  public JPanel getComponent() {
    return myPanel;
  }

  public boolean isModified() {
    return myPreferredCharacterTree.isModified();
  }

  public void dispose() {
    myPreferredCharacterTree.dispose();
  }

  public Runnable showOption(final String option) {
    return () -> {
      myPreferredCharacterTree.filter(myPreferredCharacterTree.filterModel(option, true));
      myPreferredCharacterTree.setFilter(option);
    };
  }
}
