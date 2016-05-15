package biz.redsoft.ncore.client.vaadin.widgets.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.HashMap;

/**
 * @author Korshunov Pavel
 */
public class PaintEditorTool implements ToolMouseHandler {

    protected boolean drawing;
    protected int mouseX;
    protected int mouseY;
    protected int brushSize = 20; //TODO del

    protected Canvas canvas;
    protected Context2d context;
    private HandlerRegistration hrDown;
    private HandlerRegistration hrUp;
    private HandlerRegistration hrMove;


    PaintEditorTool(HashMap<String, Object> params) {
            canvas = LayerManager.getInstance().getCurrentCanvas();//TODO sityaciya esli na sloe ne risovat i pomenyat instryment
            context = canvas.getContext2d();
            addingHandlers(canvas);
    }

    private void addingHandlers(final Canvas canvas) {
        hrDown = canvas.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                int button = event.getNativeEvent().getButton();
                if (NativeEvent.BUTTON_LEFT == button)
                    mouseDownAction(event);
            }
        });

        hrUp = canvas.addMouseUpHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                int button = event.getNativeEvent().getButton();
                if (NativeEvent.BUTTON_LEFT == button) {
                    mouseUpAction(event);
                }
            }
        });

        hrMove = canvas.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                int button = event.getNativeEvent().getButton();
                if (NativeEvent.BUTTON_LEFT == button)
                    mouseMoveAction(event);
            }
        });
    }

    @Override
    public void mouseDownAction(MouseDownEvent event) {
    }

    @Override
    public void mouseUpAction(MouseUpEvent event) {
        delListeners();
        canvas = LayerManager.getInstance().addNewDrawingLayer();
        context = canvas.getContext2d();
        addingHandlers(canvas);
    }

    @Override
    public void mouseMoveAction(MouseMoveEvent event) {
    }

    public void delListeners(){
        if(hrDown!=null){
            hrDown.removeHandler();
        }
        if(hrUp!=null){
            hrUp.removeHandler();
        }
        if(hrMove!=null){
            hrMove.removeHandler();
        }
    }
}
