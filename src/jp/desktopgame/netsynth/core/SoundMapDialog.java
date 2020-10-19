/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JScrollPane;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.DefaultMutableTreeNode;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.sound.KeyInfo;
import jp.desktopgame.netsynth.sound.SoundDatabase;
import jp.desktopgame.netsynth.sound.SoundEffect;
import jp.desktopgame.prc.Keyboard;
import jp.desktopgame.prc.PianoRoll;

/**
 *
 * @author desktopgame
 */
public class SoundMapDialog extends SoundDatabaseDialog {

    private PianoRoll editor;
    private JLayer<PianoRoll> editorLayer;
    private SoundDatabase database;
    private List<SoundIndex> indexList;

    public SoundMapDialog() {
        super();
        lazyInit();
        setTitle("サウンドのマッピング");
    }

    private void lazyInit() {
        if (indexList == null) {
            this.indexList = new ArrayList<>();
        }
    }

    @Override
    protected void onSelectSoundDatabase(SoundDatabase sdb) {
        this.database = sdb;
        lazyInit();
        indexList.clear();
        boolean dirty = false;
        for (SoundEffect se : sdb.getEffects()) {
            SoundSampleSetting ss = GlobalSetting.Context.getGlobalSetting().getSampleSetting(se.getFile().getPath());
            int i = ss.keyHeight;
            int newI = i == -1 ? getIndexForKey(se) : i;
            SoundIndex si = new SoundIndex(se, newI);
            indexList.add(si);
            if (i == -1) {
                dirty = true;
                ss.keyHeight = newI;
            }
        }
        if (dirty) {
            try {
                GlobalSetting.Context.getInstance().save();
            } catch (IOException ex) {
                logException(ex);
            }
        }
        editorLayer.repaint();
    }

    private int getIndexForKey(SoundEffect se) {
        List<String> kList = Arrays.asList(Keyboard.KEY_STRING_TABLE);
        KeyInfo info = se.getKeyInfo();
        int kc = editor.getModel().getKeyCount();
        int baseIndex = Math.max(0, info.index) * 12;
        int key = kList.indexOf(info.key);
        int y = baseIndex + key;
        boolean invalid = key == -1 || info.index == -1;
        int yIndex = (kc - y - 1);
        int i = database.getEffects().indexOf(se);
        if (invalid) {
            yIndex = i;
        }
        return yIndex;
    }

    @Override
    protected void onUnselectSoundDatabase() {
        this.database = null;
        editorLayer.repaint();
    }

    @Override
    protected void onSelectNode(DefaultMutableTreeNode node) {
        SoundDatabase sdb = (SoundDatabase) node.getUserObject();
        onSelectSoundDatabase(sdb);
    }

    @Override
    protected Component createRightComponent() {
        this.editor = new PianoRoll();
        this.editorLayer = new JLayer<>(editor, new PianoRollLayerUI());
        return new JScrollPane(editorLayer);
    }

    private class SoundIndex {

        public SoundEffect effect;
        public int index;

        public SoundIndex(SoundEffect effect, int index) {
            this.effect = effect;
            this.index = index;
        }
    }

    private class PianoRollLayerUI extends LayerUI<PianoRoll> implements MouseListener, MouseMotionListener {

        private JComponent layerSelf;
        private JComponent comp;
        private SoundIndex dragSE;
        private int startX, endX, startY, endY;

        @Override
        public void installUI(JComponent c) {
            super.installUI(c); //To change body of generated methods, choose Tools | Templates.
            this.layerSelf = c;
            this.comp = ((JLayer<PianoRoll>) c).getView();
            comp.addMouseListener(this);
            comp.addMouseMotionListener(this);
        }

        @Override
        public void uninstallUI(JComponent c) {
            super.uninstallUI(c); //To change body of generated methods, choose Tools | Templates.
            comp.removeMouseListener(this);
            comp.removeMouseMotionListener(this);
            this.comp = null;
            this.layerSelf = null;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c); //To change body of generated methods, choose Tools | Templates.
            Graphics2D g2 = (Graphics2D) g;
            if (database == null) {
                return;
            }
            Color col = g2.getColor();
            for (SoundIndex se : indexList) {
                String name = getText(se);
                Rectangle rect = getRect(se);
                g2.setColor(Color.ORANGE);
                if (name.contains("!!")) {
                    g2.setColor(Color.RED);
                }
                g2.fill(rect);
                g2.setColor(Color.white);
                g2.draw(rect);
                g2.setColor(Color.black);
                g2.drawString(name, rect.x, rect.y + (editor.getBeatHeight() / 2));
            }
            if (dragSE != null) {
                Rectangle rect = getRect(dragSE);
                rect.y = endY;
                g2.setColor(Color.CYAN);
                g2.fill(rect);
                g2.setColor(Color.black);
                g2.drawString(dragSE.effect.getName(), rect.x, rect.y + (editor.getBeatHeight() / 2));
            }
            g2.setColor(col);
        }

        private String getText(SoundIndex se) {
            String name = se.effect.getName();
            long l = indexList.stream().filter((e) -> e.index == se.index).count();
            if (l >= 2) {
                //g2.setColor(Color.RED);
                name = "!! " + name + " !!";
            }
            return name;
        }

        private Rectangle getRect(SoundIndex se) {
            int beatHeight = editor.getBeatHeight();
            Rectangle rect = new Rectangle();
            rect.x = 0;
            rect.y = se.index * beatHeight;
            rect.width = comp.getGraphics().getFontMetrics().stringWidth(getText(se));
            rect.height = beatHeight;
            return rect;
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
        }

        @Override
        public void mousePressed(MouseEvent arg0) {
            Optional<SoundIndex> seOpt = indexList.stream().filter((e) -> getRect(e).contains(arg0.getPoint())).findFirst();
            if (!seOpt.isPresent()) {
                return;
            }
            dragSE = seOpt.get();
            startX = arg0.getX();
            startY = arg0.getY();
            try {
                dragSE.effect.playLoop(TimeUnit.SECONDS.toMicros(1), 0, 1);
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                logException(ex);
            }
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {
            if (dragSE != null) {
                dragSE.index = arg0.getY() / editor.getBeatHeight();
                dragSE = null;
                layerSelf.repaint();
            }
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
        }

        private boolean checkRangeY(Rectangle rect, int yPos) {
            return yPos >= rect.y && yPos < rect.y + rect.height;
        }

        @Override
        public void mouseDragged(MouseEvent arg0) {
            if (dragSE == null) {
                return;
            }
            endX = arg0.getX();
            endY = arg0.getY();
            Optional<SoundIndex> seOpt = indexList
                    .stream()
                    .filter((e) -> checkRangeY(getRect(e), endY))
                    .filter((e) -> e != dragSE)
                    .findFirst();
            if (seOpt.isPresent()) {
                SoundIndex swapT = seOpt.get();
                int temp = swapT.index;
                swapT.index = dragSE.index;
                dragSE.index = temp;
            }
            layerSelf.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
            Optional<SoundIndex> seOpt = indexList.stream().filter((e) -> getRect(e).contains(arg0.getPoint())).findFirst();
            if (seOpt.isPresent()) {
                comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
}
