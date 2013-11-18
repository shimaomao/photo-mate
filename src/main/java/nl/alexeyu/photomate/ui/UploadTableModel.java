package nl.alexeyu.photomate.ui;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.table.AbstractTableModel;

import nl.alexeyu.photomate.api.EditablePhoto;
import nl.alexeyu.photomate.model.Photo;
import nl.alexeyu.photomate.model.PhotoStock;

public class UploadTableModel extends AbstractTableModel {

	private final List<EditablePhoto> photos;

	private final List<PhotoStock> photoStocks;

	private final Map<String, Object> statuses = new ConcurrentHashMap<>();

	public UploadTableModel(List<EditablePhoto> photos, List<PhotoStock> photoStocks) {
		this.photos = photos;
		this.photoStocks = photoStocks;
	}
	
    public void setStatus(PhotoStock photoStock, Photo photo, Object status) {
		String key = getKey(photoStock, photo);
		statuses.put(key, status);
	}

	private String getKey(PhotoStock photoStock, Photo photo) {
		return photoStock.getName() + "-" + photo.getName();
	}

	public int getRowCount() {
		return photos.size();
	}

	public int getColumnCount() {
		return photoStocks.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		String key = getKey(photoStocks.get(columnIndex), photos.get(rowIndex));
		return statuses.get(key);
	}
	
	public PhotoStock getPhotoStock(int index) {
	    return photoStocks.get(index);
	}

}
