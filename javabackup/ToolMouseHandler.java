package biz.redsoft.ncore.client.vaadin.widgets.client.ui;

import com.google.gwt.event.dom.client.*;

/**
 * @author Korshunov Pavel
 */
public interface ToolMouseHandler {
    void mouseDownAction(MouseDownEvent event);

    void mouseUpAction(MouseUpEvent event);

    void mouseMoveAction(MouseMoveEvent event);
}
