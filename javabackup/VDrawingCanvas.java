package biz.redsoft.ncore.client.vaadin.widgets.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VButton;

import java.util.HashMap;
import java.util.List;

/**
 * @author Korshunov Pavel
 */
public class VDrawingCanvas extends Composite implements Paintable {

    public static final String TAGNAME = "drawing-canvas";
    public static final String CLASSNAME = "v-" + TAGNAME;

    private HorizontalPanel toolbar;
    private AbsolutePanel absolutePanel;

    /**
     * Component identifier in UIDL communications.
     */
    String paintableId;

    /**
     * Reference to the server connection object.
     */
    ApplicationConnection client;

    private Canvas backgroundCanvas;
    private Canvas mainCanvas;
    private HashMap<String, Object> toolSettings;

    private int width;
    private int height;

    private int brushSize = 20;

    private PaintEditorTool tool;

    public VDrawingCanvas() {
        VerticalPanel vpanel = new VerticalPanel();
        toolbar = new HorizontalPanel();
        absolutePanel = new AbsolutePanel();

        vpanel.getElement().setClassName(CLASSNAME);
        toolbarInitializing();

        vpanel.add(toolbar);
        vpanel.add(absolutePanel);
        DOM.setStyleAttribute(absolutePanel.getElement(), "border", "2px solid black");

        backgroundCanvas = Canvas.createIfSupported();
        backgroundCanvas.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        backgroundCanvas.getElement().setId(String.valueOf(0));
        backgroundCanvas.getElement().getStyle().setZIndex(0);

        LayerManager.getInstance().setPanel(absolutePanel);
        LayerManager.getInstance().addLayout(backgroundCanvas);
        initWidget(vpanel);
    }

    private void toolbarInitializing() {
        final ListBox toolChanger = new ListBox(false);
        toolChanger.addItem("Маркер", "marker");
        toolChanger.addItem("Прямоугольник", "rect");
        toolChanger.addItem("Прямоугольник с заливкой", "filled_rect");
        toolChanger.addItem("Текст", "text");
        toolChanger.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String s = toolChanger.getValue(toolChanger.getSelectedIndex());
                tool.delListeners();
                if (s.equals("marker")) {
                    tool = new MarkerTool(toolSettings);
                } else {
                    tool = new RedMarkerTool(toolSettings);
                }
            }
        });

        final VButton undoButton = new VButton();
        undoButton.setText("Отмена хуена");
        undoButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                undoAction();
            }
        });

        final VButton redoButton = new VButton();
        redoButton.setText("Вновь хуевь");
        redoButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                redoAction();
            }
        });

        final VButton saveButton = new VButton();
        saveButton.setText("Сохранить");
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveCanvas();
            }
        });

        final VTextEditor lineSizeButton = new VTextEditor();
        lineSizeButton.setText("Размер");
        lineSizeButton.setValue(String.valueOf(brushSize));
        lineSizeButton.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                brushSize = Integer.parseInt(lineSizeButton.getValue());//TODO need change params
                //if (mainContext != null)
                //  mainContext.setLineWidth(brushSize); //TODO ter nax ne nado
            }
        });

        toolbar.add(lineSizeButton);
        toolbar.add(toolChanger);
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.add(saveButton);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true))
            return;
        this.client = client;
        paintableId = uidl.getId();
        String backgroundImage = uidl.getStringAttribute("background");

        final Image img = new Image(backgroundImage);
        absolutePanel.add(img);
        img.addLoadHandler(new LoadHandler() {
            public void onLoad(LoadEvent event) {
                drawBackground(img);
                absolutePanel.remove(img);
            }
        });
    }

    private void drawBackground(Image img) {
        width = img.getWidth();
        height = img.getHeight();

        absolutePanel.setSize(width + "px", height + "px");


        backgroundCanvas.setCoordinateSpaceWidth(width);
        backgroundCanvas.setCoordinateSpaceHeight(height);
        backgroundCanvas.getContext2d().drawImage(ImageElement.as(img.getElement()), 0, 0, width, height);

        mainCanvas = Canvas.createIfSupported();
        mainCanvas.setCoordinateSpaceWidth(width);
        mainCanvas.setCoordinateSpaceHeight(height);
        mainCanvas.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        mainCanvas.getElement().setId(String.valueOf(1));
        mainCanvas.getElement().getStyle().setZIndex(1);
        LayerManager.getInstance().addLayout(mainCanvas);

        tool = new MarkerTool(toolSettings);
    }

    public void saveCanvas() {
        List<Canvas> ls = LayerManager.getInstance().getAllActiveLayers();
        if (!ls.isEmpty()) {
            for (Canvas c : ls) {
                mainCanvas.getContext2d().drawImage(c.getCanvasElement(), 0, 0, width, height);
            }
            if (this.client != null && this.paintableId != null)
                client.updateVariable(paintableId, "encodedimage", mainCanvas.toDataUrl("image/png"), true);
            Window.open(mainCanvas.toDataUrl("image/png"), "_blank", "");
        }
    }

    public void undoAction() {
        LayerManager.getInstance().hideLayout();
    }

    private void redoAction() {
        LayerManager.getInstance().redoLayout();
    }
}