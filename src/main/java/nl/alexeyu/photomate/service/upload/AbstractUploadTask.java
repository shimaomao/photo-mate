package nl.alexeyu.photomate.service.upload;

import java.util.Collection;

import nl.alexeyu.photomate.api.LocalPhoto;
import nl.alexeyu.photomate.model.PhotoStock;
import nl.alexeyu.photomate.service.PrioritizedTask;

public abstract class AbstractUploadTask implements PrioritizedTask, Runnable {

	protected final PhotoStock photoStock;
	protected final LocalPhoto photo;
	protected final int attemptsLeft;
	protected final Collection<UploadPhotoListener> uploadPhotoListeners;

	public AbstractUploadTask(PhotoStock photoStock, LocalPhoto photo, int attemptsLeft, 
			Collection<UploadPhotoListener> uploadPhotoListeners) {
		this.photoStock = photoStock;
		this.photo = photo;
		this.attemptsLeft = attemptsLeft;
		this.uploadPhotoListeners = uploadPhotoListeners;
	}

	@Override
	public TaskPriority getPriority() {
		return TaskPriority.LOW;
	}

	protected final void notifyProgress(long bytes) {
		for (UploadPhotoListener l : uploadPhotoListeners) {
			l.onProgress(photoStock, photo, bytes);
		}
	}

	protected final void notifyError(Exception ex) {
		for (UploadPhotoListener l : uploadPhotoListeners) {
			l.onError(photoStock, photo, ex, attemptsLeft);
		}
	}
	
	protected final void notifySuccess() {
		for (UploadPhotoListener l : uploadPhotoListeners) {
			l.onSuccess(photoStock, photo);
		}
	}

}