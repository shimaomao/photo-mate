package nl.alexeyu.photomate.ui;

import static java.util.Arrays.asList;
import static nl.alexeyu.photomate.model.PhotoProperty.CAPTION;
import static nl.alexeyu.photomate.model.PhotoProperty.DESCRIPTION;
import static nl.alexeyu.photomate.model.PhotoProperty.KEYWORDS;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import nl.alexeyu.photomate.api.editable.EditablePhoto;

public class EditablePhotoMetaDataPanel extends AbstractPhotoMetaDataPanel<EditablePhoto> {

    private static final String NEW_KEYWORD_PROPERTY = "newKeyword";

    private HintedTextField keywordToAddField;

    public EditablePhotoMetaDataPanel() {
        captionEditor.addPropertyChangeListener(CAPTION.propertyName(), this);
        descriptionEditor.addPropertyChangeListener(DESCRIPTION.propertyName(), this);

        keywordList.addKeyListener(new KeywordRemover());
        keywordList.addPropertyChangeListener(KEYWORDS.propertyName(), this);

        keywordToAddField = new HintedTextField("Keyword to add", NEW_KEYWORD_PROPERTY, false);
        add(keywordToAddField, BorderLayout.SOUTH);
        keywordToAddField.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (NEW_KEYWORD_PROPERTY.equals(e.getPropertyName())) {
            addKeywords(asList(e.getNewValue().toString()));
            keywordToAddField.setText("");
        } else {
            super.propertyChange(e);
        }
    }

    private void removeKeywords(List<String> keywords) {
        if (keywords.size() > 0) {
            Collection<String> currentKeywords = photo.keywords();
            List<String> reducedKeywords = currentKeywords.stream().filter(k -> !keywords.contains(k))
                    .collect(Collectors.toList());
            firePropertyChange(KEYWORDS.propertyName(), currentKeywords, reducedKeywords);
        }
    }

    private void addKeywords(List<String> keywords) {
        Collection<String> currentKeywords = photo.keywords();
        Collection<String> extendedKeywords = new LinkedHashSet<>(currentKeywords);
        extendedKeywords.addAll(keywords);
        firePropertyChange(KEYWORDS.propertyName(), currentKeywords, extendedKeywords);
    }

    public DropTarget getDropTarget() {
        return new DropTarget(keywordList, new KeywordDropTarget());
    }

    private final class KeywordRemover extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                removeKeywords(keywordList.getSelectedValuesList());
            }
        }
    }

    private final class KeywordDropTarget extends DropTargetAdapter {
        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                DataFlavor dataFlavor = new DataFlavor("text/plain; class=java.lang.String");
                String draggedValue = dtde.getTransferable().getTransferData(dataFlavor).toString();
                String[] keywords = draggedValue.split(System.getProperty("line.separator"));
                addKeywords(asList(keywords));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
