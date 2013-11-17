package nl.alexeyu.photomate.api;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;

public class EditablePhoto extends LocalPhoto {
    
    public static final String PREVIEW_PROPERTY = "preview";

    private final AtomicReference<ImageIcon> preview = new AtomicReference<>();

    public EditablePhoto(File file) {
        super(file);
    }

    public boolean isReadyToUpload() {
        return getThumbnail() != null && getMetaData() != null && getMetaData().isComplete();
    }

    public ImageIcon getPreview() {
        return preview.get();
    }

    public void setPreview(ImageIcon preview) {
        if (!this.preview.compareAndSet(null, preview)) {
            throw new IllegalStateException("Attempt to set preview 2nd time");
        }
        firePropertyChanged(PREVIEW_PROPERTY, null, preview);
    }

}
