/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides
 * methods to create rules and other "lightweight" indicators, using a
 * (hopefully) natural-looking and expressive series of method calls.
 * 
 * <p>
 * Methods like plus(), minus() and sqrt() correspond directly to methods in the
 * {@code Num} interface. These methods create "lightweight" (not cached)
 * indicators to add, subtract, etc. Many methods are overloaded to accept
 * either {@code Indicator<Num>} or {@code Number} arguments.
 * 
 * <p>
 * Methods like sma() and ema() simply create the corresponding indicator
 * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first
 * argument. These methods usually instantiate cached objects.
 * 
 * <p>
 * Another set of methods, like crossedOver() and isGreaterThan() create Rule
 * objects. These are also overloaded to accept both {@code Indicator<Num>} and
 * {@code Number} arguments.
 */
public class NumericIndicator implements Indicator<Num>, Comparable<NumericIndicator>, List<Num>  {

    /**
     * Creates a fluent NumericIndicator wrapped around a "regular" indicator.
     *
     * @param delegate an indicator
     *
     * @return a fluent NumericIndicator wrapped around the argument
     */
    public static NumericIndicator of(Indicator<Num> delegate) {
        return new NumericIndicator(delegate);
    }

    /**
     * Creates a fluent version of the ClosePriceIndicator.
     *
     * @return a NumericIndicator wrapped around a ClosePriceIndicator
     */
    public static NumericIndicator closePrice(BarSeries bs) {
        return of(new ClosePriceIndicator(bs));
    }

    /**
     * Creates a fluent version of the VolumeIndicator.
     *
     * @return a NumericIndicator wrapped around a VolumeIndicator
     */
    public static NumericIndicator volume(BarSeries bs) {
        return of(new VolumeIndicator(bs));
    }

    protected final Indicator<Num> delegate;

    protected NumericIndicator(Indicator<Num> delegate) {
        this.delegate = delegate;
    }

    public Indicator<Num> delegate() {
        return delegate;
    }

    /**
     * @param other the other indicator
     * @return {@code this + other}, rounded as necessary
     */
    public NumericIndicator plus(Indicator<Num> other) {
        return NumericIndicator.of(BinaryOperation.sum(this, other));
    }

    /**
     * @param n the other number
     * @return {@code this + n}, rounded as necessary
     */
    public NumericIndicator plus(Number n) {
        return plus(createConstant(n));
    }

    /**
     * @param other the other indicator
     * @return {@code this - other}, rounded as necessary
     */
    public NumericIndicator minus(Indicator<Num> other) {
        return NumericIndicator.of(BinaryOperation.difference(this, other));
    }

    /**
     * @param n the other number
     * @return {@code this - n}, rounded as necessary
     */
    public NumericIndicator minus(Number n) {
        return minus(createConstant(n));
    }

    /**
     * @param other the other indicator
     * @return {@code this * other}, rounded as necessary
     */
    public NumericIndicator multipliedBy(Indicator<Num> other) {
        return NumericIndicator.of(BinaryOperation.product(this, other));
    }

    public NumericIndicator multiply(Indicator<Num> other) {
        return multipliedBy(other);
    }

    /**
     * @param n the other number
     * @return {@code this * n}, rounded as necessary
     */
    public NumericIndicator multipliedBy(Number n) {
        return multipliedBy(createConstant(n));
    }

    public NumericIndicator multiply(Number n) {
        return multipliedBy(n);
    }

    /**
     * @param other the other indicator
     * @return {@code this / other}, rounded as necessary
     */
    public NumericIndicator dividedBy(Indicator<Num> other) {
        return NumericIndicator.of(BinaryOperation.quotient(this, other));
    }

    public NumericIndicator div(Indicator<Num> other) {
        return dividedBy(other);
    }

    /**
     * @param n the other number
     * @return {@code this / n}, rounded as necessary
     */
    public NumericIndicator dividedBy(Number n) {
        return dividedBy(createConstant(n));
    }

    public NumericIndicator div(Number n) {
        return dividedBy(n);
    }

    /**
     * @param other the other indicator
     * @return the smaller of {@code this} and {@code other}; if they are equal,
     *         {@code this} is returned.
     */
    public NumericIndicator min(Indicator<Num> other) {
        return NumericIndicator.of(BinaryOperation.min(this, other));
    }

    /**
     * @param n the other number
     * @return the smaller of {@code this} and {@code n}; if they are equal,
     *         {@code this} is returned.
     */
    public NumericIndicator min(Number n) {
        return min(createConstant(n));
    }

    /**
     * @param other the other indicator
     * @return the greater of {@code this} and {@code other}; if they are equal,
     *         {@code this} is returned.
     */
    public NumericIndicator max(Indicator<Num> other) {
        return NumericIndicator.of(BinaryOperation.max(this, other));
    }

    /**
     * @param n the other number
     * @return the greater of {@code this} and {@code n}; if they are equal,
     *         {@code this} is returned.
     */
    public NumericIndicator max(Number n) {
        return max(createConstant(n));
    }

    /**
     * Returns an Indicator whose values are the absolute values of {@code this}.
     * 
     * @return {@code abs(this)}
     */
    public NumericIndicator abs() {
        return NumericIndicator.of(UnaryOperation.abs(this));
    }

    /**
     * Returns an Indicator whose values are √(this).
     * 
     * @return {@code √(this)}
     */
    public NumericIndicator sqrt() {
        return NumericIndicator.of(UnaryOperation.sqrt(this));
    }

    /**
     * Returns an Indicator whose values are {@code this * this}.
     * 
     * @return {@code this * this}
     */
    public NumericIndicator squared() {
        // TODO: implement pow(n); a few others
        return this.multipliedBy(this);
    }

    /**
     * @param barCount the time frame
     * @return the {@link SMAIndicator} of {@code this}
     */
    public NumericIndicator sma(int barCount) {
        return NumericIndicator.of(new SMAIndicator(this, barCount));
    }

    /**
     * @param barCount the time frame
     * @return the {@link EMAIndicator} of {@code this}
     */
    public NumericIndicator ema(int barCount) {
        return NumericIndicator.of(new EMAIndicator(this, barCount));
    }

    /**
     * @param barCount the time frame
     * @return the {@link StandardDeviationIndicator} of {@code this}
     */
    public NumericIndicator stddev(int barCount) {
        return NumericIndicator.of(new StandardDeviationIndicator(this, barCount));
    }

    /**
     * @param barCount the time frame
     * @return the {@link HighestValueIndicator} of {@code this}
     */
    public NumericIndicator highest(int barCount) {
        return NumericIndicator.of(new HighestValueIndicator(this, barCount));
    }

    /**
     * @param barCount the time frame
     * @return the {@link LowestValueIndicator} of {@code this}
     */
    public NumericIndicator lowest(int barCount) {
        return NumericIndicator.of(new LowestValueIndicator(this, barCount));
    }

    /**
     * @param barCount the time frame
     * @return the {@link PreviousValueIndicator} of {@code this}
     */
    public NumericIndicator previous(int barCount) {
        return NumericIndicator.of(new PreviousValueIndicator(this, barCount));
    }

    /**
     * @return the {@link PreviousValueIndicator} of {@code this} with
     *         {@code barCount=1}
     */
    public Indicator<Num> previous() {
        return previous(1);
    }

    /**
     * @param other the other indicator
     * @return the {@link CrossedUpIndicatorRule} of {@code this} and {@code other}
     */
    public Rule crossedOver(Indicator<Num> other) {
        return new CrossedUpIndicatorRule(this, other);
    }

    /**
     * @param n the other number
     * @return the {@link CrossedUpIndicatorRule} of {@code this} and {@code n}
     */
    public Rule crossedOver(Number n) {
        return crossedOver(createConstant(n));
    }

    /**
     * @param other the other indicator
     * @return the {@link CrossedDownIndicatorRule} of {@code this} and
     *         {@code other}
     */
    public Rule crossedUnder(Indicator<Num> other) {
        return new CrossedDownIndicatorRule(this, other);
    }

    /**
     * @param n the other number
     * @return the {@link CrossedDownIndicatorRule} of {@code this} and {@code n}
     */
    public Rule crossedUnder(Number n) {
        return crossedUnder(createConstant(n));
    }

    /**
     * @param other the other indicator
     * @return the {@link OverIndicatorRule} of {@code this} and {@code other}
     */
    public Rule isGreaterThan(Indicator<Num> other) {
        return new OverIndicatorRule(this, other);
    }

    /**
     * @param n the other number
     * @return the {@link OverIndicatorRule} of {@code this} and {@code n}
     */
    public Rule isGreaterThan(Number n) {
        return isGreaterThan(createConstant(n));
    }

    /**
     * @param other the other indicator
     * @return the {@link UnderIndicatorRule} of {@code this} and {@code other}
     */
    public Rule isLessThan(Indicator<Num> other) {
        return new UnderIndicatorRule(this, other);
    }

    /**
     * @param n the other number
     * @return the {@link UnderIndicatorRule} of {@code this} and {@code n}
     */
    public Rule isLessThan(Number n) {
        return isLessThan(createConstant(n));
    }

    private Indicator<Num> createConstant(Number n) {
        return new ConstantIndicator<>(getBarSeries(), numOf(n));
    }

    @Override
    public Num getValue(int index) {
        return delegate.getValue(index);
    }

    @Override
    public int getUnstableBars() {
        return 0;
    }

    @Override
    public BarSeries getBarSeries() {
        return delegate.getBarSeries();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public Num get(int index) {
        return delegate.getValue(index);
    }

    @Override
    public Num set(int index, Num element) {
        return null;
    }

    @Override
    public void add(int index, Num element) {

    }

    @Override
    public Num remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<Num> listIterator() {
        return null;
    }

    @Override
    public ListIterator<Num> listIterator(int index) {
        return null;
    }

    @Override
    public List<Num> subList(int fromIndex, int toIndex) {
        return List.of();
    }

    @Override
    public Spliterator<Num> spliterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<Num> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<Num> parallelStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        return delegate.getBarSeries().getBarCount();
    }

    @Override
    public boolean isEmpty() {
        return delegate.getBarSeries().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Num) {
            Num num = (Num) o;
            for (int i = 0; i < size(); i++) {
                if (get(i).equals(num)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<Num> iterator() {
        return delegate.stream().iterator();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(Num num) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends Num> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends Num> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeIf(Predicate<? super Num> filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replaceAll(UnaryOperator<Num> operator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sort(Comparator<? super Num> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(NumericIndicator other) {
        Num thisValue = this.getValue(this.getBarSeries().getEndIndex());
        Num otherValue = other.getValue(other.getBarSeries().getEndIndex());
        return thisValue.compareTo(otherValue);
    }

    // Optionally, you might want to override the equals and hashCode methods as well:
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumericIndicator that = (NumericIndicator) obj;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return this.getValue(this.getBarSeries().getEndIndex()).hashCode();
    }
}
