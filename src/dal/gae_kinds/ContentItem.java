package dal.gae_kinds;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by zaqwes on 5/9/14.
 */
@Entity
public class ContentItem {
  @Id
  Long id;

  // value <= 500 symbols
  // TODO: 500 чего именно?
  String item;
}
