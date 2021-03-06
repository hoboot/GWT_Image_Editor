package biz.redsoft.ncore.client.vaadin.widgets.client.ui;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import java.util.HashMap;

/**
 * @author Korshunov Pavel
 */
public class RedMarkerTool extends PaintEditorTool implements ToolMouseHandler {

    RedMarkerTool(HashMap<String, Object> params) {
        super(params);
    }

    @Override
    public void mouseDownAction(MouseDownEvent event) {
        drawing = true;
        brushSize = 40;
        context.setLineWidth(brushSize);
        context.setStrokeStyle("Red");
        context.beginPath();
        context.moveTo(mouseX, mouseY);
    }

    @Override
    public void mouseUpAction(MouseUpEvent event) {
        drawing = false;
        super.mouseUpAction(event);
    }

    @Override
    public void mouseMoveAction(MouseMoveEvent event) {
        mouseX = event.getRelativeX(canvas.getElement());
        mouseY = event.getRelativeY(canvas.getElement());
        if (drawing) {
            context.lineTo(mouseX, mouseY);
            context.stroke();
        }
    }
}
