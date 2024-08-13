package org.ta4j.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.ta4j.core.num.DecimalNum;

public class FixedDateBarSeriesTest {

  private final ZonedDateTime baseDate = ZonedDateTime.parse("2010-01-01T00:00:00Z");
  private final ZonedDateTime currentDate = ZonedDateTime.parse("2024-08-13T00:00:00Z");
  private final Duration timePeriod = Duration.ofMinutes(5);

  private Bar createMockBar(ZonedDateTime endTime, double price) {
    return new BaseBar(Duration.ofMinutes(5), endTime, price, price, price, price, price, price, 0, DecimalNum::valueOf);
  }

  @Test
  public void testGetBar() {
    ZonedDateTime endTime1 = baseDate.plus(timePeriod);
    ZonedDateTime endTime2 = baseDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = baseDate.plus(timePeriod.multipliedBy(3));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    assertEquals(bar1, series.getBar(0));  // 0번째 인덱스에서 첫 번째 바 반환(음수 index는 첫번째 바로 설정)
    assertEquals(bar1, series.getBar(1));  // 1번째 인덱스에서도 첫 번째 바 반환
    assertEquals(bar2, series.getBar(2));  // 2번째 인덱스에서 두 번째 바 반환
    assertEquals(bar3, series.getBar(3));  // 3번째 인덱스에서 세 번째 바 반환
  }

  @Test
  public void testGetBarCount() {
    ZonedDateTime endTime1 = baseDate.plus(timePeriod);
    ZonedDateTime endTime2 = baseDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = baseDate.plus(timePeriod.multipliedBy(3));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    assertEquals(4, series.getBarCount());  // 3개의 Bar가 있지만, 전체 범위에서 4개의 인덱스를 가진다.
  }

  @Test
  public void testGetBeginIndex() {
    ZonedDateTime endTime1 = baseDate.plus(timePeriod);
    ZonedDateTime endTime2 = baseDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = baseDate.plus(timePeriod.multipliedBy(3));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    assertEquals(1, series.getBeginIndex());  // 시작 인덱스는 항상 0
  }

  @Test
  public void testGetEndIndex() {
    ZonedDateTime endTime1 = baseDate.plus(timePeriod);
    ZonedDateTime endTime2 = baseDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = baseDate.plus(timePeriod.multipliedBy(3));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    assertEquals(3, series.getEndIndex());  // 마지막 바의 인덱스는 3
  }

  @Test
  public void testGetSubSeries() {
    ZonedDateTime endTime1 = baseDate.plus(timePeriod);
    ZonedDateTime endTime2 = baseDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = baseDate.plus(timePeriod.multipliedBy(3));
    ZonedDateTime endTime4 = baseDate.plus(timePeriod.multipliedBy(4));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);
    Bar bar4 = createMockBar(endTime4, 4);

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3, bar4);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    assertEquals(5, series.getBarCount());
    BarSeries subSeries = series.getSubSeries(2, 4);
    assertEquals(2, subSeries.getBarCount());
    assertEquals(bar2, subSeries.getBar(0));
    assertEquals(bar3, subSeries.getBar(1));

    // 0번 인덱스은 실제로 없고, 1번 인덱스가 첫 번째 바를 나타낸다.
    subSeries = series.getSubSeries(0, 2);
    assertEquals(1, subSeries.getBarCount());
    assertEquals(bar1, subSeries.getBar(0));

    subSeries = series.getSubSeries(0, 4);
    assertEquals(3, subSeries.getBarCount());
    assertEquals(bar1, subSeries.getBar(0));
    assertEquals(bar2, subSeries.getBar(1));
    assertEquals(bar3, subSeries.getBar(2));

    // 테스트 3: 끝 인덱스가 시리즈 범위 밖일 때 자동 조정
    subSeries = series.getSubSeries(2, 5);
    assertEquals(3, subSeries.getBarCount());
    assertEquals(bar2, subSeries.getBar(0));
    assertEquals(bar3, subSeries.getBar(1));

    // 테스트 4: 잘못된 인덱스 (endIndex <= startIndex)
    assertThrows(IllegalArgumentException.class, () -> {
      series.getSubSeries(2, 2);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      series.getSubSeries(3, 1);
    });
  }

  @Test
  public void testGetIndex() {
    ZonedDateTime endTime1 = baseDate.plus(timePeriod);
    ZonedDateTime endTime2 = baseDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = baseDate.plus(timePeriod.multipliedBy(3));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    assertEquals(1, series.getIndex(endTime1));
    assertEquals(2, series.getIndex(endTime2));
    assertEquals(3, series.getIndex(endTime3));
  }

  @Test
  public void testFindBarByCurrentDate() {
    ZonedDateTime endTime1 = currentDate.plus(timePeriod);
    ZonedDateTime endTime2 = currentDate.plus(timePeriod.multipliedBy(2));
    ZonedDateTime endTime3 = currentDate.plus(timePeriod.multipliedBy(3));
    ZonedDateTime endTimeCurrent = currentDate.plus(timePeriod.multipliedBy(4));

    Bar bar1 = createMockBar(endTime1, 1);
    Bar bar2 = createMockBar(endTime2, 2);
    Bar bar3 = createMockBar(endTime3, 3);
    Bar barCurrent = createMockBar(endTimeCurrent, 100);  // 현재 시간을 기준으로 바 생성

    List<Bar> bars = Arrays.asList(bar1, bar2, bar3, barCurrent);
    FixedDateBarSeries series = new FixedDateBarSeries("Test Series", baseDate, bars);

    int expectedIndex = series.getIndex(endTimeCurrent);
    assertEquals(barCurrent, series.getBar(expectedIndex));
    assertEquals(series.getEndIndex(), expectedIndex);  // currentDate가 리스트에 있는 마지막 바를 나타내야 한다.
  }
}
