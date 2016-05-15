package biz.redsoft.ncore.client.vaadin.widgets.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Korshunov Pavel
 */
public class LayerManager {

    private static final int FIRST_SECOND_AND_LAST_EMPTY_LAYERS = 3;
    private static final int LAST_EMPTY_LAYER = 2;
    private static final int FIRST_TWO_LAYERS = 2;
    private static LayerManager instance;
    private List<Canvas> canvasLayouts;
    private HashMap<Integer, Boolean> layerVisibility;
    private AbsolutePanel panel;
    private int stackSlider = 0;

    private LayerManager() {
        canvasLayouts = new ArrayList<Canvas>();
        layerVisibility = new HashMap<Integer, Boolean>();
    }

    public static synchronized LayerManager getInstance() {
        if (instance == null)
            instance = new LayerManager();
        return instance;
    }

    public void setPanel(AbsolutePanel panel) {
        if (this.panel == null)
            this.panel = panel;
    }

    public void addLayout(Canvas canvas) {
        panel.add(canvas);
        canvasLayouts.add(canvas);
        layerVisibility.put(Integer.valueOf(canvas.getElement().getId()), true);
    }

    public void hideLayout() {
        if (!hasVisibleLayers())
            return;
        for (int i = canvasLayouts.size() - LAST_EMPTY_LAYER; i > 1; i--) { //The latest index is current empty layer. Don't touch it.
            if (layerVisibility.get(i)) {
                //panel.remove(canvasLayouts.get(i));
                canvasLayouts.get(i).setVisible(false);
                layerVisibility.put(i, false);
                stackSlider = i;
                return;
            }
        }
    }

    private boolean hasVisibleLayers() {
        if (canvasLayouts.size() <= FIRST_SECOND_AND_LAST_EMPTY_LAYERS)
            return false;
        for (int i = canvasLayouts.size() - 1; i > FIRST_TWO_LAYERS; i--) {
            if (layerVisibility.get(i))
                return true;
        }
        return false;
    }

    public Canvas addNewDrawingLayer() {
        stackSlider = 0;
        Canvas newCanvas = Canvas.createIfSupported();
        newCanvas.setCoordinateSpaceWidth(getMainCanvas().getCoordinateSpaceWidth());
        newCanvas.setCoordinateSpaceHeight(getMainCanvas().getCoordinateSpaceHeight());
        newCanvas.getElement().setId(String.valueOf(getNewId()));
        newCanvas.getElement().getStyle().setZIndex(getNewId());
        newCanvas.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        addLayout(newCanvas);
        return newCanvas;
    }

    private Canvas getMainCanvas() {
        return canvasLayouts.get(1);
    }

    public Canvas getCurrentCanvas() {
        return canvasLayouts.size() > FIRST_TWO_LAYERS ? canvasLayouts.get(canvasLayouts.size() - 1) : addNewDrawingLayer();
    }

    private int getNewId() {
        return layerVisibility.size();
    }

    public List<Canvas> getAllActiveLayers() {
        List<Canvas> finalLayers = new ArrayList<Canvas>();
        for (int i = FIRST_TWO_LAYERS; i <= canvasLayouts.size() - LAST_EMPTY_LAYER; i++) {
            if (layerVisibility.get(i))
                finalLayers.add(canvasLayouts.get(i));
        }
        return finalLayers;
    }

    public void redoLayout() {
        if (stackSlider != 0) {
            canvasLayouts.get(stackSlider).setVisible(true);
            layerVisibility.put(stackSlider, true);
            if (layerVisibility.get(++stackSlider))
                stackSlider = 0;
        }
    }
}