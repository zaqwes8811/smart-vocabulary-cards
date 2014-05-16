package business.mapreduce;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dal_gae_kinds.ContentItem;
import dal_gae_kinds.ContentPage;
import dal_gae_kinds.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static dal_gae_kinds.OfyService.ofy;

public class MRContentPageBuilder {
  public ContentPage build(String name, ArrayList<ContentItem> contentItems) {
    // TODO: BAD! В страницу собрана обработка
    Multimap<String, ContentItem> wordHistogram = HashMultimap.create();
    CountReducer reducer = new CountReducer(wordHistogram);
    CounterMapper mapper = new CounterMapper(reducer);

    // Split
    mapper.map(contentItems);  // TODO: implicit, but be so

    // WARNING: Порядок важен!
    // Persist content items
    ofy().save().entities(contentItems).now();

    // Persist words
    ArrayList<Word> words = new ArrayList<Word>();
    for (String word: wordHistogram.keySet()) {
      Collection<ContentItem> value = wordHistogram.get(word);
      words.add(Word.create(word, value));
    }

    // Sort words by frequency and assign idx
    Collections.sort(words, Word.createFreqComparator());
    Collections.reverse(words);

    for (int i = 0; i < words.size(); i++)
      words.get(i).setSortedIdx(i);

    ofy().save().entities(words).now();

    // Слова сортированы
    return new ContentPage(name, contentItems, words);
  }
}
