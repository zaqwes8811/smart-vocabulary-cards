package business.math;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.jcip.annotations.Immutable;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// About:
//   Класс способен генерировать последовательности любого дискретного распределения
//   Возвращает индекс массива исходного распределения.
//
// На вход подается...
//
// TODO(zaqwes): ImmutableLists для триплета избыточны, лучше сделать через Tuples - Triplet
//
@Immutable
public class GeneratorAnyDistribution {
  private final Integer COUNT_POINTS_;
  private final Integer MAX_VALUE_;
  private final ImmutableList<ImmutableList<Integer>> CODE_BOOK;

  // Любой список с числами
  // @throws: RandomGeneratorException
  public static GeneratorAnyDistribution create(ArrayList<Integer> distribution) {
    if (distribution.isEmpty())
      throw new RandomGeneratorException("In list must be no empty.");
    return new GeneratorAnyDistribution(distribution);
  }

  public Integer getPosition() {
    // Используется рекурсивная реализация на базе бинарного поиска.
    // На модели она показала наилучшую масштабирумость и скорость работы.
    Integer INTERVAL_POS = 1;
    Integer IDX_POS = 2;
    Float value = new Random().nextFloat()* MAX_VALUE_;
    ImmutableList<Integer> result =  split(CODE_BOOK, COUNT_POINTS_, value).getValue1().get();
    return result.get(IDX_POS);
  }

  private Triplet<List<Integer>, Integer, Integer> makeFx(List<Integer> distribution) {
    List<Integer> Fx = new ArrayList<Integer>();
    Integer Fxi = 0;
    Integer count = 0;
    for (final Integer frequency: distribution) {
      Fxi += frequency;
      Fx.add(Fxi);
      count++;
    }
    return Triplet.with(Fx, count, Fxi);
  }

  private List<ImmutableList<Integer>> makeRanges(List<Integer> Fx) {
    List<ImmutableList<Integer>> ranges = new ArrayList<ImmutableList<Integer>>();
    ranges.add(ImmutableList.of(0, Fx.get(0), 0));
    for (Integer i = 0; i < COUNT_POINTS_ -1; i++) {
      ranges.add(ImmutableList.of(Fx.get(i), Fx.get(i+1), i+1));
    }
    return ranges;
  }

  private GeneratorAnyDistribution(ArrayList<Integer> distribution) {
    Triplet<List<Integer>, Integer, Integer> resultMakeFx = makeFx(distribution);
    List<Integer> Fx = resultMakeFx.getValue0();
    COUNT_POINTS_ = resultMakeFx.getValue1();
    MAX_VALUE_ = resultMakeFx.getValue2();
    CODE_BOOK = ImmutableList.copyOf(makeRanges(Fx));
  }

  private Pair<Boolean, Optional<ImmutableList<Integer>>> split(
    ImmutableList<ImmutableList<Integer>> ranges, Integer n, Float value) {
    Boolean contain = isContain(ranges, n, value);
    if (!contain) {
      Optional<ImmutableList<Integer>> none = Optional.absent();
      return Pair.with(false, none);
    }
    if (n == 1) return Pair.with(contain, Optional.of(ranges.get(0)));
    else {
      Integer oneSize = n/2;
      Integer twoSize = n-oneSize;
      Pair<Boolean, Optional<ImmutableList<Integer>>> resultSubTree =
          split(ranges.subList(0, oneSize), oneSize, value);
      if (!resultSubTree.getValue0()) {
        resultSubTree = split(ranges.subList(oneSize, n), twoSize, value);
      }
      return resultSubTree;
    }
  }

  private Boolean isContain(ImmutableList<ImmutableList<Integer>> ranges, Integer n, Float value) {
      return ranges.get(0).get(0) < value && value <= ranges.get(n-1).get(1);
  }
}
