package org.ta4j.core;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

public class FixedDateBarSeries implements BarSeries {

  private final ZonedDateTime baseDate;
  private final List<Bar> bars;
  private final String name;
  private final Num num;
  private final Duration timePeriod;

  public FixedDateBarSeries(String name, ZonedDateTime baseDate, List<Bar> bars) {
    this.name = name;
    this.baseDate = baseDate;
    this.bars = Collections.unmodifiableList(bars);

    if (bars.isEmpty()) {
      this.num = DecimalNum.ZERO;  // bars가 비어 있을 경우 DecimalNum.ZERO로 설정
      this.timePeriod = Duration.ZERO;
    } else {
      this.num = bars.get(0).getClosePrice();  // 첫 번째 Bar의 closePrice로 num 타입 설정
      this.timePeriod = bars.get(0).getTimePeriod();  // 첫 번째 Bar의 TimePeriod로 설정
      validateBarsTimePeriod();
    }
  }

  private void validateBarsTimePeriod() {
    // 모든 Bar들이 같은 TimePeriod로 저장되었는지 검사
    for (int i = 1; i < bars.size(); i++) {
      Duration actualTimePeriod = Duration.between(bars.get(i - 1).getEndTime(), bars.get(i).getEndTime());
      if (!actualTimePeriod.equals(timePeriod)) {
        throw new IllegalArgumentException("All bars must have the same time period.");
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Num num() {
    return num;  // 결정된 num 타입 반환
  }

  @Override
  public Bar getBar(int i) {
    if (i < 0) {
      throw new IndexOutOfBoundsException(buildOutOfBoundsMessage(i));
    }

    int beginIndex = getBeginIndex();
    int adjustedIndex = i - beginIndex;

    if (adjustedIndex < 0) {
      // 첫 번째 Bar 이전의 인덱스는 첫 번째 Bar를 반환
      adjustedIndex = 0;
    } else if (adjustedIndex >= bars.size()) {
      throw new IndexOutOfBoundsException(buildOutOfBoundsMessage(i));
    }

    return bars.get(adjustedIndex);
  }

  private String buildOutOfBoundsMessage(int index) {
    return String.format("Index %d is out of bounds for BarSeries `%s` with %d bars", index, name, bars.size());
  }

  @Override
  public int getBarCount() {
    return bars.size();
  }

  @Override
  public List<Bar> getBarData() {
    return bars;
  }

  @Override
  public int getBeginIndex() {
    // 기준 시간과 첫 번째 바의 종료 시간 사이의 차이를 바 개수로 계산
    ZonedDateTime firstBarEndTime = bars.get(0).getEndTime();
    long beginIndex = Duration.between(baseDate, firstBarEndTime).dividedBy(timePeriod);
    return (int) beginIndex;
  }

  @Override
  public int getEndIndex() {
    return getBeginIndex() + bars.size() - 1;
  }

  @Override
  public int getMaximumBarCount() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void setMaximumBarCount(int maximumBarCount) {
    throw new UnsupportedOperationException("setMaximumBarCount() not implemented");
  }

  @Override
  public int getRemovedBarsCount() {
    return 0; // Bar가 삭제되지 않음
  }

  @Override
  public void addBar(Bar bar, boolean replace) {
    throw new UnsupportedOperationException("addBar() not implemented");
  }

  @Override
  public void addBar(Duration timePeriod, ZonedDateTime endTime) {
    throw new UnsupportedOperationException("addBar(timePeriod, endTime) not implemented");
  }

  @Override
  public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {
    throw new UnsupportedOperationException("addBar() not implemented");
  }

  @Override
  public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) {
    throw new UnsupportedOperationException("addBar() not implemented");
  }

  @Override
  public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {
    throw new UnsupportedOperationException("addBar() not implemented");
  }

  @Override
  public BarSeries getSubSeries(int startIndex, int endIndex) {
    if (endIndex <= startIndex || startIndex < 0) {
      throw new IllegalArgumentException("Invalid start or end index.");
    }

    // 인덱스가 전체 범위를 벗어나지 않도록 조정
    startIndex = Math.max(startIndex, getBeginIndex());
    endIndex = Math.min(endIndex, getEndIndex() + 1);

    // 시작과 끝 인덱스 사이의 바 리스트를 서브 리스트로 추출
    List<Bar> subBars = bars.subList(startIndex - getBeginIndex(), endIndex - getBeginIndex());
    return new FixedDateBarSeries(name + "_sub", baseDate.plus(timePeriod.multipliedBy(startIndex)), subBars);
  }

  public int getIndex(ZonedDateTime dateTime) {
    long index = Duration.between(baseDate, dateTime).dividedBy(timePeriod);
    return (int) index;
  }

  @Override
  public void addTrade(Num tradeVolume, Num tradePrice) {
    throw new UnsupportedOperationException("addTrade() not implemented");
  }

  @Override
  public void addPrice(Num price) {
    throw new UnsupportedOperationException("addPrice() not implemented");
  }
}
