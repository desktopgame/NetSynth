/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import jp.desktopgame.prc.Phrase;

/**
 *
 * @author desktopgame
 */
public class PhraseList implements List<Phrase> {

    private List<Phrase> phrases;

    public PhraseList() {
        this.phrases = new ArrayList<>();
    }

    @Override
    public int size() {
        return phrases.size();
    }

    @Override
    public boolean isEmpty() {
        return phrases.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return phrases.contains(o);
    }

    @Override
    public Iterator<Phrase> iterator() {
        return phrases.iterator();
    }

    @Override
    public Object[] toArray() {
        return phrases.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return phrases.toArray(a);
    }

    @Override
    public boolean add(Phrase e) {
        return phrases.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return phrases.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return phrases.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Phrase> c) {
        return phrases.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Phrase> c) {
        return phrases.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return phrases.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return phrases.retainAll(c);
    }

    @Override
    public void clear() {
        phrases.clear();
    }

    @Override
    public Phrase get(int index) {
        return phrases.get(index);
    }

    @Override
    public Phrase set(int index, Phrase element) {
        return phrases.set(index, element);
    }

    @Override
    public void add(int index, Phrase element) {
        phrases.add(index, element);
    }

    @Override
    public Phrase remove(int index) {
        return phrases.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return phrases.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return phrases.lastIndexOf(o);
    }

    @Override
    public ListIterator<Phrase> listIterator() {
        return phrases.listIterator();
    }

    @Override
    public ListIterator<Phrase> listIterator(int index) {
        return phrases.listIterator(index);
    }

    @Override
    public List<Phrase> subList(int fromIndex, int toIndex) {
        return phrases.subList(fromIndex, toIndex);
    }

}
