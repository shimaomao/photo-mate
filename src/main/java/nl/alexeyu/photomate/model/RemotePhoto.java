package nl.alexeyu.photomate.model;

public class RemotePhoto extends AbstractPhoto {
    
    private final String url;
    
    private final String thumbnailUrl;
    
    RemotePhoto(String url, String thumbnailUrl) {
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        getThumbnail();
    }

    @Override
    public String getName() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getUrl() {
        return url;
    }

}