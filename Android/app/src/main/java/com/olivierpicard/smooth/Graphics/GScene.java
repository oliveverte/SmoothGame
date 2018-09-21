package com.olivierpicard.smooth.Graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Scene côté vue, qui va servir à afficher
 * et gérer les items de la scene
 */
public abstract class GScene extends GNode implements Runnable {
    public enum TouchType {DOWN, UP, MOVE}
    public class TouchEvent {
        public boolean isEdited = false;
        private TouchType touchType = null;
        private GPoint position = GPoint.zero();
        public void edit(TouchType t, GPoint pos) {isEdited = true; touchType = t; position = pos; }
    }

    protected int backgroundColor = Color.BLACK;
    private List<GNode> elementsToAdd;
    private List<GNode> elementsToRemove;
    private GSize baseSize;
    private GSize scale;

    public volatile boolean enable = false;
    public TouchEvent touchEvent;
    private GSize size;
    private long time_from_lastFrame = 0;
    public boolean showFPS = true;
    public boolean showNodeCounter = true;


    abstract public void didInitialized();
    abstract public void start();
    abstract public void update(long currentTime);

    protected void touchDown(GPoint pos) { }
    protected void touchUp(GPoint pos) { }
    protected void touchMove(GPoint pos) { }


    public void init(GSize baseSceneSize) {
        touchEvent = new TouchEvent();
        elementsToAdd = new ArrayList<>();
        elementsToRemove = new ArrayList<>();
        this.baseSize = baseSceneSize;
        final float xScale = GTools.screenMetrics.widthPixels / this.baseSize.width;
        final float yScale = GTools.screenMetrics.heightPixels / this.baseSize.height;
        this.scale = new GSize(xScale, yScale);
        this.size = new GSize(GTools.screenMetrics.widthPixels / this.scale.width,
                GTools.screenMetrics.heightPixels/ this.scale.height);

        GSceneViewController.surfaceHolder.setFixedSize(
                (int)this.size.width,
                (int)this.size.height
        );
    }


    public void run() {
        didInitialized();
        while(this.enable) {
            refreshSceneNodes();
            processTouch();
            update(System.currentTimeMillis());
            Canvas canvas = GSceneViewController.surfaceHolder.lockCanvas();
            if (canvas != null) {
                synchronized (GSceneViewController.surfaceHolder) {
                    canvas.drawColor(backgroundColor, PorterDuff.Mode.CLEAR);
                    render(canvas);
                }
                GSceneViewController.surfaceHolder.unlockCanvasAndPost(canvas);
            }
            try {
                Thread.sleep(16);
            } catch (Exception e) {}
        }
    }



    private void render(Canvas canvas) {
        Map<Integer, List<IGRenderAllowable>> renderElements = new LinkedHashMap<>();
        processRenderOrder(renderElements);
        SortedSet<Integer> orderedRenderElement = new TreeSet<>(renderElements.keySet());
        for(Integer id : orderedRenderElement) {
            for(IGRenderAllowable node : renderElements.get(id)) {
                node.render(canvas);
            }
        }

        Paint p = new Paint();
        p.setTextSize(12);
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextAlign(Paint.Align.LEFT);
        if(this.showFPS)
            canvas.drawText("FPS : " + (int)(1 / ((float)(System.currentTimeMillis() - this.time_from_lastFrame) / 1000)),
                    0, (int) this.size.height - 20, p);
        if(this.showNodeCounter)
            canvas.drawText("Nodes : " + children.size(),
                    0, (int)this.size.height - 5, p);

        this.time_from_lastFrame = System.currentTimeMillis();
    }


    private void refreshSceneNodes() {
        for(GNode node : elementsToRemove) {

            this.children.remove(node);
            node.setScene(null);
        }
        this.children.addAll(elementsToAdd);

        // On peut utilisé clear, mais il me semble
        // que le garbageCollector fera mieux son travail
        // que clear()
        elementsToAdd = new ArrayList<>();
        elementsToRemove = new ArrayList<>();
    }

    private void processTouch() {
        if(!this.touchEvent.isEdited) return;
        this.touchEvent.isEdited = false;
        final GPoint pos = this.touchEvent.position;
        switch (this.touchEvent.touchType) {
            case DOWN:
                touchDown(pos);
                break;
            case UP:
                touchUp(pos);
                break;
            case MOVE:
                touchMove(pos);
                break;
        }
    }

    /**
     * Organise tous les noeuds de la scène selon leur position en z
     * @param map : Key: position en Z  -  Value: Tous les élements à cette position
     */
    private void processRenderOrder(Map<Integer, List<IGRenderAllowable>> map) {
        for(GNode node : this.children) {
            if(!(node instanceof IGRenderAllowable)) continue;
            IGRenderAllowable renderAllowable = (IGRenderAllowable)node;
            List<IGRenderAllowable> list = map.get(renderAllowable.getRelativeRender().zPosition);
            if(list == null) list = new ArrayList<>();
            list.add(renderAllowable);
            map.put(renderAllowable.getRelativeRender().zPosition, list);
        }
    }



    /**
     * Ajoute un noeud ansi que ses enfants
     * de la scène lors de la prochaine frame
     * @param node noeud à ajouter
     */
    @Override
    public final void addChild(GNode node) {
        if(node.getScene() != this) {
            node.setScene(this);
            if(node.parent == null) return;
        }
        this.elementsToAdd.add(node);
    }

    /**
     * Supprime un noeud ansi que ses enfants
     * de la scène lors de la prochaine frame
     * @param node noeud à supprimer
     */
    @Override
    public final void removeChild(GNode node) {
        this.elementsToRemove.add(node);
        this.elementsToRemove.addAll(node.getDeepChildren());
    }

    /**
     * Supprime une liste de noeuds ansi que tous leurs enfants
     * de la scène lors de la prochaine frame
     * @param nodes liste de noeuds à supprimer
     */
    @Override
    public final void removeChildren(List<GNode> nodes) {
        for (GNode node: nodes) {
            this.elementsToRemove.add(node);
            this.elementsToRemove.addAll(node.getDeepChildren());
        }
    }

    public GSize getSize() {
        return size;
    }
}
