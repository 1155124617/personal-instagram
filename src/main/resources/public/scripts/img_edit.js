console.log('img_edit.js is running :)');

///// GLOBAL VARIABLES /////
// ? I tried to make the canvas bigger, but it seemed to be too much for my PC, I guess
// const htmlLeftContainer = document.querySelector("#left-container").getBoundingClientRect();
// const canvasRatio = 4/3;

// const width = htmlLeftContainer.width*0.8;
// const height = width/canvasRatio;
const width = 320;
const height = 240;

let uploadedImage = null;
let preset = null; // grayscale, black & white, etc.
let isPickingColor = false;
let sc_r = 255, sc_g = 255, sc_b = 255;

let canvas;
let redSlider, greenSlider, blueSlider;
let brightSlider;

const htmlColorBox = $('#current-color');
const htmlToleranceSlider = $('#tolerance');

///// SETUP /////
function setup() {
    // Create canvas
    canvas = createCanvas(width, height);
    canvas.parent('canvas-container');
    // noLoop();

    // Standard pixel density
    pixelDensity(1);

    // Image dropzone
    let dropzone = select('#dropzone');
    dropzone.dragOver(() => {
        dropzone.addClass('dragover');
    });
    dropzone.dragLeave(() => {
        dropzone.removeClass('dragover');
    });
    dropzone.drop((file) => {
        console.log(file);
        // uploadedImage = createImg(file.data, ''); // as shown in https://p5js.org/reference/#/p5/createFileInput
        // uploadedImage.hide(); // don't show in DOM
        uploadedImage = loadImage(file.data); // this way, uploadedImage is a p5 image and we can use the .loadPixels method on it
        console.log(uploadedImage);
    }, () => dropzone.removeClass('dragover'));
    uploadedImage = loadImage($('#img_url').text());
    console.log(uploadedImage);

    // Sliders
    redSlider = createSlider(-255, 255, 0);
    greenSlider = createSlider(-255, 255, 0);
    blueSlider = createSlider(-255, 255, 0);
    brightSlider = createSlider(-255, 255, 0);
    redSlider.parent('#red-slider-container');
    greenSlider.parent('#green-slider-container');
    blueSlider.parent('#blue-slider-container');
    brightSlider.parent('#bright-slider-container');
    // redSlider.input(redraw);
    // greenSlider.input(redraw);
    // blueSlider.input(redraw);
}

///// DRAW /////
function draw() {
    background(255, 0); // clean canvas each draw
    if(uploadedImage === null) return;

    // Resize, center and draw image
    let originalW = uploadedImage.width;
    let originalH = uploadedImage.height;
    let originalRatio = originalW/originalH;
    
    let targetRatio = width/height;
    let x = 0, y = 0, w, h;
    if(targetRatio > originalRatio) { // if the (proportional) width of the canvas is bigger than the one of the image
        h = height; // make image as high as possible
        w = originalRatio * height;
        x = (width - w)/2; // free space / 2
    }
    else {
        w = width;
        h = width/originalRatio;
        y = (height - h)/2;
    }

    image(uploadedImage, x, y, w, h);

    // Apply filters
    loadPixels();

    if(isPickingColor && mouseInCanvas()) {
        x = Math.round(mouseX);
        y = Math.round(mouseY);
        let index = (y*width + x) * 4;
        sc_r = pixels[index];
        sc_g = pixels[index+1];
        sc_b = pixels[index+2];
        htmlColorBox.css('background-color', 'rgb('+sc_r+','+sc_g+','+sc_b+')'); // rgb(r,g,b)
    }

    if(preset === null) sliderFilter(pixels, width, height);
    else if (preset === 'grey') grayscaleFilter(pixels, width, height);
    else if (preset === 'bw') blackAndWhiteFilter(pixels, width, height);
    else if (preset === 'sc') singleColorFilter(pixels, width, height);

    updatePixels();
}

///// HELPER FUNCTIONS /////
function mouseInCanvas() {
    return 0 <= mouseX && mouseX < width && 0 <= mouseY && mouseY < height;
}

///// EVENT LISTENERS /////
function mouseClicked() {
    if (mouseInCanvas()) {
        isPickingColor = false;
        $('body').removeClass('picking-color');
    }
}

$('#preset-button-row .square-button').click(function() {
    let selected = this.dataset.preset;
    if(selected === 'none') preset = null;
    else preset = selected;

    $('#preset-button-row .square-button').removeClass('selected');
    $(`[data-preset=${selected}]`).addClass('selected');
});

htmlColorBox.click(function() {
    isPickingColor = !isPickingColor;
    $('body').addClass('picking-color');
});

$('#download-button').click(() => {
    // Don't try to download if there's no image yet
    if(uploadedImage === null) {
        alert('No image has been uploaded!');
        return;
    }

    uploadedImage.loadPixels();
    
    // Backup pixels
    let originalValues = []; 
    uploadedImage.pixels.forEach(value => originalValues.push(value));

    // Apply filters
    if(preset === null) sliderFilter(uploadedImage.pixels, uploadedImage.width, uploadedImage.height);
    else if (preset === 'grey') grayscaleFilter(uploadedImage.pixels, uploadedImage.width, uploadedImage.height);
    else if (preset === 'bw') blackAndWhiteFilter(uploadedImage.pixels, uploadedImage.width, uploadedImage.height);
    else if (preset === 'sc') singleColorFilter(uploadedImage.pixels, uploadedImage.width, uploadedImage.height);

    uploadedImage.updatePixels();
    save(uploadedImage, 'edit.png');

    // Restore original pixels
    uploadedImage.loadPixels();
    originalValues.forEach((value, index) => uploadedImage.pixels[index] = value);
    uploadedImage.updatePixels();
});

///// FILTERS /////
function sliderFilter(pixels, width, height) {
    let r = redSlider.value(); // do NOT put this inside loops; it reduces performance heavily
    let g = greenSlider.value();
    let b = blueSlider.value();
    let bright = brightSlider.value();
    for(let x = 0; x < width; x++) {
        for(let y = 0; y < height; y++) {
            let index = (y*width + x) * 4; // pixel index * 4, since each pixel has 4 elements in the pixels array: r, g, b and a
            pixels[index+0] += r + bright;
            pixels[index+1] += g + bright;
            pixels[index+2] += b + bright;
        }
    }
}

function grayscaleFilter(pixels, width, height) {
    for(let x = 0; x < width; x++) {
        for(let y = 0; y < height; y++) {
            let index = (y*width + x) * 4;

            let average = (pixels[index] + pixels[index+1] + pixels[index+2]) / 3;
            pixels[index+0] = average;
            pixels[index+1] = average;
            pixels[index+2] = average;
        }
    }
}

function blackAndWhiteFilter(pixels, width, height) {
    for(let x = 0; x < width; x++) {
        for(let y = 0; y < height; y++) {
            let index = (y*width + x) * 4;

            let average = (pixels[index] + pixels[index+1] + pixels[index+2]) / 3;
            let color;
            if(average >= 128) color = 255;
            else color = 0;

            pixels[index+0] = pixels[index+1] = pixels[index+2] = color;
            // if(average >= 128) pixels[index+3] = 0; // blackAndTransparent :O
        }
    }
}

function singleColorFilter(pixels, width, height) {
    for(let x = 0; x < width; x++) {
        for(let y = 0; y < height; y++) {
            let index = (y*width + x) * 4;

            // See if pixel color is close enough to the color they chose
            // let difference = Math.abs(pixels[index] - sc_r) + Math.abs(pixels[index+1] - sc_g) + Math.abs(pixels[index+2] - sc_b);
            // if(difference <= htmlToleranceSlider.val()) continue;

            let tolerance = htmlToleranceSlider.val();
            if(Math.abs(pixels[index+0] - sc_r) <= tolerance)
                if(Math.abs(pixels[index+1] - sc_g) <= tolerance)
                    if(Math.abs(pixels[index+2] - sc_b) <= tolerance) {
                        // pixels[index+3] = 0; // make color transparent instead of keep
                        continue;
                    }

            let average = (pixels[index] + pixels[index+1] + pixels[index+2]) / 3;
            pixels[index+0] = average;
            pixels[index+1] = average;
            pixels[index+2] = average;
        }
    }
}