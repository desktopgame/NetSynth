/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Action;

/**
 * 使い回し可能なアクションの一覧を保存するクラスです.
 *
 * @author desktopgame
 * @param <T>
 */
public class ActionTable<T> {

    private T context;
    private List<Class<? extends Action>> classList;
    private Map<String, Action> simpleNameMap;
    private Map<String, Action> nameMap;

    public ActionTable(T context) {
        this.context = context;
        this.classList = new ArrayList<>();
        this.simpleNameMap = new HashMap<>();
        this.nameMap = new HashMap<>();
    }

    /**
     * Tを引数にとるアクションの実装クラスを登録します.
     *
     * @param cl
     */
    public void register(Class<? extends Action> cl) {
        if (!classList.contains(cl)) {
            classList.add(cl);
        }
    }

    /**
     * 指定のクラス名のアクションを返します. まだ生成されていなければ生成します。
     *
     * @param name
     * @return
     */
    public Action get(String name) {
        if (simpleNameMap.containsKey(name)) {
            return simpleNameMap.get(name);
        }
        if (nameMap.containsKey(name)) {
            return nameMap.get(name);
        }
        createAction(classList.stream().filter((e) -> e.getSimpleName().equals(name) || e.getName().equals(name)).findFirst().get());
        return get(name);
    }

    /**
     * 全てのアクションを取得します.
     *
     * @return
     */
    public List<Action> getAll() {
        return classList.stream().map((e) -> get(e.getSimpleName())).collect(Collectors.toList());
    }

    /* package private */ void createActions() {
        for (Class<? extends Action> cl : classList) {
            createAction(cl);
        }
    }

    private void createAction(Class<? extends Action> cl) {
        try {
            Constructor ctor = cl.getConstructor(new Class<?>[]{context.getClass()});
            Object action = Objects.requireNonNull(ctor.newInstance(context));
            simpleNameMap.put(cl.getSimpleName(), (Action) action);
            nameMap.put(cl.getName(), (Action) action);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ActionTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
