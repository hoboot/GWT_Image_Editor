if (window.addEventListener) {
    window.addEventListener('load', function() {
        var canvas, context, mainCanvas, mainContext, tool;
        var tools = {};
        var defaultTool = 'pencil';
        var lineWidth = "5";
        var textSize = "12";
        var colorPickerButton = document.getElementById('colorPicker');
        var fillColorPickerButton = document.getElementById('fillColorPicker');
        var cancelButton = document.getElementById('cancelButton');

        function init() {
            mainCanvas = document.getElementById('imageViewFrontground');
            mainContext = mainCanvas.getContext('2d');
            colorPickerButton = document.getElementById('colorPicker');
            fillColorPickerButton = document.getElementById('fillColorPicker');
            cancelButton = document.getElementById('cancelButton');

            mainCanvas.width = 0;
            mainCanvas.height = 0;
            colorPickerButton.style.borderColor = "#000000";
	        fillColorPickerButton.style.borderColor = "#000000";
	        cancelButton.disabled = true;

            var container = mainCanvas.parentNode;
            canvas = document.createElement('canvas');
            canvas.id = 'imageTemp';
            canvas.width = mainCanvas.width;
            canvas.height = mainCanvas.height;
            container.appendChild(canvas);
            context = canvas.getContext('2d');

            var radiusButton = document.getElementById('radius');
            lineWidth = radiusButton.value;
            context.lineWidth = lineWidth;
            mainContext.lineWidth = lineWidth;
            radiusButton.addEventListener('input', function() {
                lineWidth = radiusButton.value;
                context.lineWidth = lineWidth;
                mainContext.lineWidth = lineWidth;
            }, false);

            var textSizeButton = document.getElementById('textSize');
            textSize = textSizeButton.value;
            context.textSize = textSize;
            textSizeButton.addEventListener('input', function() {
                textSize = textSizeButton.value;
                context.textSize = textSize;
            }, false);

            var toolSelector = document.getElementById('dtool');
            if (tools[defaultTool]) {
                tool = new tools[defaultTool]();
                toolSelector.value = defaultTool;
            }
            toolSelector.addEventListener('change', function() {
                canvas.style.cursor = "default";
                if (tools[this.value])
                    tool = new tools[this.value]();
                tool.started = false;
            }, false);

            var imageLoader = document.getElementById('openButton');
            imageLoader.addEventListener('change', imageLoad, false);

            cancelButton.addEventListener('click', cancelAction, false);

            var saveButton = document.getElementById("saveButton");
            saveButton.addEventListener('click', saveAction, false);

            canvas.addEventListener('mousedown', evCanvas, false);
            canvas.addEventListener('mousemove', evCanvas, false);
            canvas.addEventListener('mouseup', evCanvas, false);
        }

        function imageLoad(e) {
            backCanvas = document.getElementById('imageViewBackground');
            backContext = backCanvas.getContext('2d');
            var reader = new FileReader();
            reader.onload = function(event) {
                var img = new Image();
                img.onload = function() {
                    backCanvas.width = img.width;
                    backCanvas.height = img.height;
                    mainCanvas.width = img.width;
                    mainCanvas.height = img.height;
                    document.getElementById('imageTemp').width = img.width;
                    document.getElementById('imageTemp').height = img.height;
                    backContext.drawImage(img, 0, 0);
                }
                img.src = event.target.result;
            }
            reader.readAsDataURL(e.target.files[0]);
        }

        function cancelAction() {
            context.clearRect(0, 0, canvas.width, canvas.height);
            cancelButton.disabled = true;
        }

        function saveAction() {
            contextCommit();
            var canvas = document.getElementById('imageViewBackground');
            var image = canvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
            window.location.href = image;
        }

        function evCanvas(ev) {
            if (ev.layerX || ev.layerX == 0) { // Firefox
                ev._x = ev.layerX;
                ev._y = ev.layerY;
            } else if (ev.offsetX || ev.offsetX == 0) { // Opera
                ev._x = ev.offsetX;
                ev._y = ev.offsetY;
            }

            var func = tool[ev.type];
            if (func)
                func(ev);
        }

        function contextCommit() {
            mainContext.drawImage(canvas, 0, 0);
            context.clearRect(0, 0, canvas.width, canvas.height);
        }

        function propertiesInit() {
            context.strokeStyle = colorPickerButton.style.borderColor;
            context.fillStyle = fillColorPickerButton.style.borderColor;
            context.font = textSize + "px" + " sans-serif";
            context.lineWidth = lineWidth;
        }

        tools.pencil = function() {
            var tool = this;
            this.started = false;
            this.mousedown = function(ev) {
                contextCommit();
                propertiesInit();
                context.beginPath();
                context.moveTo(ev._x, ev._y);
                tool.started = true;
            };
            this.mousemove = function(ev) {
                if (tool.started) {
                    context.lineTo(ev._x, ev._y);
                    context.stroke();
                }
            };
            this.mouseup = function(ev) {
                if (tool.started) {
                    tool.mousemove(ev);
                    tool.started = false;
                    cancelButton.disabled = false;
                }
            };
        };

        tools.eraser = function() {
            var tool = this;
            this.started = false;
            contextCommit();
            cancelButton.disabled = true;
            this.mousedown = function(ev) {
            	mainContext.globalCompositeOperation = 'destination-out';
                mainContext.beginPath();
                mainContext.moveTo(ev._x, ev._y);
                tool.started = true;
            };
            this.mousemove = function(ev) {
                if (tool.started) {
                    mainContext.lineTo(ev._x, ev._y);
                    mainContext.stroke();
                }
            };
            this.mouseup = function(ev) {
                if (tool.started) {
                    tool.mousemove(ev);
                    tool.started = false;
                    mainContext.closePath();
                    mainContext.globalCompositeOperation = 'source-over';
                }
            };
        };

        tools.rect = function() {
            var tool = this;
            this.started = false;
            this.mousedown = function(ev) {
                contextCommit();
                propertiesInit();
                tool.started = true;
                tool.x0 = ev._x;
                tool.y0 = ev._y;
            };
            this.mousemove = function(ev) {
                if (!tool.started)
                    return;
                var x = Math.min(ev._x, tool.x0),
                    y = Math.min(ev._y, tool.y0),
                    w = Math.abs(ev._x - tool.x0),
                    h = Math.abs(ev._y - tool.y0);
                context.clearRect(0, 0, canvas.width, canvas.height);
                if (!w || !h)
                    return;
                context.strokeRect(x, y, w, h);
            };
            this.mouseup = function(ev) {
                if (tool.started) {
                    tool.mousemove(ev);
                    tool.started = false;
                    cancelButton.disabled = false;
                }
            };
        };

        tools.fillrect = function() {
            var tool = this;
            this.started = false;
            this.mousedown = function(ev) {
                contextCommit();
                propertiesInit();

                tool.started = true;
                tool.x0 = ev._x;
                tool.y0 = ev._y;
            };
            this.mousemove = function(ev) {
                if (!tool.started)
                    return;
                var x = Math.min(ev._x, tool.x0),
                    y = Math.min(ev._y, tool.y0),
                    w = Math.abs(ev._x - tool.x0),
                    h = Math.abs(ev._y - tool.y0);
                context.clearRect(0, 0, canvas.width, canvas.height);
                if (!w || !h)
                    return;
                context.fillRect(x, y, w, h);
            };
            this.mouseup = function(ev) {
                if (tool.started) {
                    tool.mousemove(ev);
                    tool.started = false;
                    cancelButton.disabled = false;
                }
            };
        };

        tools.line = function() {
            var tool = this;
            this.started = false;
            this.mousedown = function(ev) {
                contextCommit();
                propertiesInit();
                tool.started = true;
                tool.x0 = ev._x;
                tool.y0 = ev._y;
            };
            this.mousemove = function(ev) {
                if (!tool.started)
                    return;
                context.clearRect(0, 0, canvas.width, canvas.height);
                context.beginPath();
                context.moveTo(tool.x0, tool.y0);
                context.lineTo(ev._x, ev._y);
                context.stroke();
                context.closePath();
            };
            this.mouseup = function(ev) {
                if (tool.started) {
                    tool.mousemove(ev);
                    tool.started = false;
                    cancelButton.disabled = false;
                }
            };
        };

        tools.text = function() {
            var tool = this;
            canvas.style.cursor = "text";
            this.mousedown = function(ev) {
                contextCommit();
                propertiesInit();
                var text = prompt("Введите текст:", "");
                if (text) {
                    context.fillStyle = colorPickerButton.style.borderColor;
                    context.fillText(text, ev._x, ev._y + textSize / 4);
                    context.fillStyle = fillColorPickerButton.style.borderColor;
                    cancelButton.disabled = false;
                }
            };
        };

        init();

    }, false);
}