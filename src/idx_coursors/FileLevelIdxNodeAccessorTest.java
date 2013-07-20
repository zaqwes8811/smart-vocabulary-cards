package idx_coursors;

import com.google.common.base.Optional;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: кей
 * Date: 20.07.13
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
public class FileLevelIdxNodeAccessorTest {
  @Test(expected=NodeNoFound.class)
  public void testNoExistNode() throws NodeNoFound, NodeAlreadyExist {
     String pathToNode = "z:/NoExist";
     FileLevelIdxNodeAccessor accessor = FileLevelIdxNodeAccessor.create(pathToNode);
  }

  @Test
  public void testThrowCtr() throws NodeAlreadyExist {
    String pathToNode = "zd:/";

    // Если несколько блоков try-catch, то чтобы можно было видеть объекты ссыкли нужно создать
    //   вне блоков try. Тогда все-таки нужно использовать Optional. Если внутри блока, то тоже наверное
    //   Проблема в том, что пророй конструкторы могут генерировать исключения.
    Optional<FileLevelIdxNodeAccessor> accessor = Optional.absent();
    try {
      accessor = Optional.of(FileLevelIdxNodeAccessor.create(pathToNode));
    } catch (NodeNoFound e) {
      assertEquals(accessor, Optional.absent());
    }
  }
}
