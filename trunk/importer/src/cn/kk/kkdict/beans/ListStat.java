/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.beans;

import java.util.List;

public class ListStat implements Comparable<ListStat> {

    @Override
    public String toString() {
        return "[counter=" + counter + ", " + (values != null ? "values=" + values : "") + "]";
    }

    public void add(ListStat s) {
        this.counter += s.counter;
        this.values.addAll(s.values);
    }

    private int counter;
    private List<String> values;

    public ListStat(int counter, String firstVal) {
        this.counter = counter;
        this.values = new FormattedArrayList<String>();
        this.values.add(firstVal);
    }

    public ListStat(int counter, List<String> values) {
        this.counter = counter;
        this.values = values;
    }

    @Override
    public int compareTo(ListStat o) {
        // changed to descending order for performance optimization
        return o.counter - this.counter;
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @param counter
     *            the counter to set
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
