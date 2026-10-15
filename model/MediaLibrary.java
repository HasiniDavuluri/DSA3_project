package mediamine.model;

import mediamine.randomized.ReservoirSampling;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Central in-memory data store for the MediaMine platform: manages media
 * records, search-query history, and continuous viewing-event data, as
 * described in the platform's high-level data model.
 */
public class MediaLibrary {

    public static final int VIEW_BUCKETS = 8; // 8 rotating "hour" buckets (power of 2, for Blelloch scan)

    private final List<MediaItem> items = new ArrayList<>();
    private final List<String> searchHistory = new ArrayList<>();
    private final long[] hourlyViewBuckets = new long[VIEW_BUCKETS];
    private final ReservoirSampling<String> viewEventSampler = new ReservoirSampling<>(5);
    private int nextId = 1;

    public MediaItem addMedia(String title, String type, String content, String category) {
        String id = "M" + (nextId++);
        MediaItem item = new MediaItem(id, title, type, content, category);
        items.add(item);
        return item;
    }

    public List<MediaItem> getAllItems() {
        return items;
    }

    public Optional<MediaItem> findById(String id) {
        return items.stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst();
    }

    public void recordSearch(String query) {
        searchHistory.add(query);
    }

    public List<String> getSearchHistory() {
        return searchHistory;
    }

    /** Every distinct category/tag/title-word currently in the library -- used as a
     *  "known vocabulary" dictionary for search-query correction. */
    public String[] buildVocabulary() {
        Set<String> vocab = new LinkedHashSet<>();
        for (MediaItem item : items) {
            vocab.add(item.getCategory().toLowerCase());
            for (String word : item.getTitle().toLowerCase().split("\\W+")) {
                if (word.length() > 3) vocab.add(word);
            }
        }
        return vocab.toArray(new String[0]);
    }

    /** Records a viewing event for real-time analytics: updates the item's view
     *  count, buckets it into a rotating hour-slot, and feeds the streaming sampler. */
    public void recordView(String itemId, int hour) {
        findById(itemId).ifPresent(MediaItem::addView);
        hourlyViewBuckets[Math.floorMod(hour, VIEW_BUCKETS)]++;
        viewEventSampler.add(itemId + "@hour" + hour);
    }

    public long[] getHourlyViewBuckets() {
        return hourlyViewBuckets.clone();
    }

    public ReservoirSampling<String> getViewEventSampler() {
        return viewEventSampler;
    }

    public int size() {
        return items.size();
    }
}
