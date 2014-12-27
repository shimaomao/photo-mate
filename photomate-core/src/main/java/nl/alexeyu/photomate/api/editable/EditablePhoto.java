package nl.alexeyu.photomate.api.editable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;

import nl.alexeyu.photomate.api.LocalPhoto;
import nl.alexeyu.photomate.model.PhotoMetaData;

public final class EditablePhoto extends LocalPhoto {
    
    public static final String PREVIEW_PROPERTY = "preview";

    private final AtomicReference<ImageIcon> preview = new AtomicReference<>();

    public EditablePhoto(Path path) {
        super(path);
    }

    public boolean isReadyToUpload() {
        if (!thumbnail().isPresent() || !metaData().isPresent()) {
        	return false;
        }
        PhotoMetaData m = metaData().get();
		return !m.description().isEmpty()
				&& !m.caption().isEmpty()
                && m.keywords().size() > 0
                && m.keywords().size() <= 50;
    }

    public Optional<ImageIcon> preview() {
        return Optional.ofNullable(preview.get());
    }

	@Override
	public void addThumbnail(ImageIcon thumbnail) {
		if (thumbnail().isPresent()) {
	        preview.set(thumbnail);
	        firePropertyChange(PREVIEW_PROPERTY, null, preview);
		} else {
			super.addThumbnail(thumbnail);
		}
	}

}
