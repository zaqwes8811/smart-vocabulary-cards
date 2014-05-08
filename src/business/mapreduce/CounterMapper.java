package business.mapreduce;

import dal.gae_kinds.ContentItem;
import org.checkthread.annotations.NotThreadSafe;

import java.util.List;

/**
 https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html

 Fake MapReduce - try make similar
 */
@NotThreadSafe
public class CounterMapper {
  private final CountReducer reducer_;
  public CounterMapper(CountReducer reducer) {
    reducer_ = reducer;
  }

  private void emit(String key, ContentItem value) {
    reducer_.reduce(key, value);
  }

  public void map(List<ContentItem> contentItems) {
    for (ContentItem item : contentItems) {
      String key = item.getItem();
      emit(key, item);
    }
  }
}
