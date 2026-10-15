package mediamine.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single media record managed by the MediaMine platform:
 * a news article, video, or transcript, with associated captions and
 * metadata, matching the platform's high-level data model.
 */
public class MediaItem {
    private final String id;
    private final String title;
    private final String type;         // "Article", "Video", "Transcript"
    private final String content;      // article text / transcript / caption body
    private final String category;
    private final List<String> captions = new ArrayList<>();
    private final Map<String, String> metadata = new LinkedHashMap<>();
    private long viewCount;

    public MediaItem(String id, String title, String type, String content, String category) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.content = content;
        this.category = category;
        this.viewCount = 0;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public long getViewCount() { return viewCount; }
    public void addView() { viewCount++; }

    public void addCaption(String caption) { captions.add(caption); }
    public List<String> getCaptions() { return captions; }

    public void putMetadata(String key, String value) { metadata.put(key, value); }
    public Map<String, String> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" | type=%s | category=%s | views=%d | captions=%d",
                id, title, type, category, viewCount, captions.size());
    }

    /** Full detail view, including captions and metadata, for console display. */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this).append("\n");
        sb.append("  Content: ").append(truncate(content, 100)).append("\n");
        if (!captions.isEmpty()) {
            sb.append("  Captions: ").append(captions).append("\n");
        }
        if (!metadata.isEmpty()) {
            sb.append("  Metadata: ").append(metadata).append("\n");
        }
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
