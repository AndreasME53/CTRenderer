

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class CTRenderer extends Application implements Initializable {
    @FXML
    private Button sliec76button;
    @FXML
    private Button volrendButton;
    @FXML
    private Button depthButton;
    @FXML
    private Slider TopSlider;
    @FXML
    private Slider FrontSlider;
    @FXML
    private Slider SideSlider;
    @FXML
    private Slider OpacitySlider;
    @FXML
    private Slider LightSlider;
    @FXML
    private ImageView TopView;
    @FXML
    private ImageView FrontView;
    @FXML
    private ImageView SideView;


    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    int CT_x_axis = 256;
    int CT_y_axis = 256;
    int CT_z_axis = 113;
    //Here's the top view - looking down on the top of the head (each slice we are looking at is CT_x_axis x CT_y_axis)
    int Top_width = CT_x_axis;
    int Top_height = CT_y_axis;

    //Here's the front view - looking at the front (nose) of the head (each slice we are looking at is CT_x_axis x CT_z_axis)
    int Front_width = CT_x_axis;
    int Front_height = CT_z_axis;

    // here is the side of the head from left to right
    int Slide_width = CT_z_axis;
    int Slide_height = CT_y_axis;

    //and you do the other (side view) - looking at the ear of the head

    //We need 3 things to see an image
    //1. We create an image we can write to
    WritableImage top_image = new WritableImage(Top_width, Top_height);
    WritableImage front_image = new WritableImage(Front_width, Front_height);// added * ***********
    WritableImage side_image = new WritableImage(Slide_height, Slide_width);// added * ***********

    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();

        //Good practice: Define your top view, front view and side view images (get the height and width correct)

        //2. We create a view of that image
        ImageView TopView = new ImageView(top_image);
        ImageView FrontView = new ImageView(front_image); // added * ***********
        ImageView SideView = new ImageView(side_image); // added * ***********

        Button slice76_button = new Button("slice"); //an example button to get the slice 76
        Button volrend_button = new Button("volrend"); //an example button to get the volume render
        Button depth_button = new Button("depth");
        //sliders to step through the slices (top and front directions) (remember 113 slices in top direction 0-112)
        Slider Top_slider = new Slider(0, CT_z_axis - 1, 0);
        Slider Front_slider = new Slider(0, CT_y_axis - 1, 0);
        Slider Side_slider = new Slider(0, CT_x_axis - 1, 0);
        Slider Opacity_slider = new Slider(0, 500, 100); // last


        Slider Light_slider = new Slider(0, 300, 0);





        Parent root = FXMLLoader.load(getClass().getResource("Example.fxml"));

//https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/

        //3. (referring to the 3 things we need to display an image)
        //we need to add it to the flow pane
       // root.getChildren().addAll(TopView, FrontView, SideView, slice76_button, volrend_button, depth_button,
         //       Top_slider, Front_slider,Side_slider, Opacity_slider, Light_slider);

        Scene scene = new Scene(root, 900, 680);
        stage.setScene(scene);
        stage.show();
    }

    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
        File file = new File("CThead");
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min = Short.MAX_VALUE;
        max = Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[CT_z_axis][CT_y_axis][CT_x_axis]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k = 0; k < CT_z_axis; k++) {
            for (j = 0; j < CT_y_axis; j++) {
                for (i = 0; i < CT_x_axis; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read = (short) ((b2 << 8) | b1); //and swizzle the bytes around
                    if (read < min) min = read; //update the minimum
                    if (read > max) max = read; //update the maximum
                    cthead[k][j][i] = read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        System.out.println(min + " " + max); //diagnostic - for CThead this should be -1117, 2248
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
        //maybe put your histogram equalization code here to set up the mapping array
    }

    /*
       This function shows how to carry out an operation on an image.
       It obtains the dimensions of the image, and then loops through
       the image carrying out the copying of a slice of data into the
       image.
   */
    public void TopDownSlice76(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                // for (int k = 0; k < 112; k++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                datum = cthead[76][j][i]; //get values from slice 76 (change this in your assignment) **********
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col = (((float) datum - (float) min) / ((float) (max - min)));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
            } // column loop
        } // row loop
        //  }


    }

    public static double[] interpolate(double start, double end, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("interpolate: illegal count!");
        }
        double[] array = new double[count + 1];
        for (int i = 0; i <= count; ++ i) {
            array[i] = start + i * (end - start) / count;
        }
        return array;
    }

    public double SMR( int j, int k,int i, int lightPos){
        // point light source {0,0,57} => (x,y,z)
        // direction of the light source
        float dx = lightPos-i;
        float dy = 0-j;
        float dz = -157-k;
        float length = (float) Math.sqrt((dx*dx) + (dy*dy) + (dz*dz));
        dx = dx/length;
        dy = dy/length;
        dz = dz/length;

        j = (j-1==-1) ? 1 : j;
        j = (j+1==113) ? 110 : j;

        float gradientX = cthead[j][k][i-1] - cthead[j][k][i+1];
        float gradientZ =  cthead[j][k-1][i] - cthead[j][k+1][i];
        float gradientY =  cthead[j-1][k][i] - cthead[j+1][k][i];

        float gradientL = (float) Math.sqrt(gradientX*gradientX + gradientY*gradientY + gradientZ*gradientZ);
        gradientX = gradientX/gradientL;
        gradientY = gradientY/gradientL;
        gradientZ = gradientZ/gradientL;

        double nl = (gradientX*dx) + (gradientY*dy) + (gradientZ*dz); // gets the dot product

        return Math.max(nl,0.0);
    }

    public void shadingFront(WritableImage image, int lightPos) {
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double col=0.0;
                for (int k = 0; k < CT_y_axis; k++) {
                    short datum = cthead[j][k][i];

                   if(datum > 400){

                       col = SMR(j,k,i,lightPos);
                      //col = (float) 1.0-k/255.0;
                       k=CT_y_axis;
                   }
                }
                image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
            }
        }
    }

    public Color transferFunction(short hu, int opacity) {
        if (hu < -300) {
            return (new Color(0.0, 0.0, 0.0, 0.0));
        } else if (hu >= -300 && hu <= 49) {
            return (new Color(1.0, 0.79, 0.6, opacity*0.005));
        } else if (hu >= 50 && hu <= 299) {
            return (new Color(0.0, 0.0, 0.0, 0.0));
        } else {
            return (new Color(1.0, 1.0, 1.0, 0.8));
        }
    }

    public void volumeRenderFront(WritableImage image, int opacity) {
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int j = 0; j < CT_z_axis; j++) {
            for (int i = 0; i < CT_x_axis; i++) {
                double r = 0.0;
                double g = 0.0;
                double b = 0.0;
                double t = 1.0;
                for (int k = 0; k < CT_y_axis; k++) {
                    short datum = cthead[j][k][i];

                    Color pixel = transferFunction(datum, opacity);

                    r = r + (t * pixel.getOpacity() * pixel.getRed());
                    g = g + (t * pixel.getOpacity() * pixel.getGreen());
                    b = b + (t * pixel.getOpacity() * pixel.getBlue());
                    t = t * (1.0 - pixel.getOpacity());
                    if(t<0.1){
                        break;
                    }
                }
                image_writer.setColor(i, j, Color.color(Math.min(r,1.0), Math.min(g,1.0),
                        Math.min(b,1.0), 1.0));
            }
        }
    }

    public void volumeRenderTop(WritableImage image, int opacity) {
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double r = 0.0;
                double g = 0.0;
                double b = 0.0;
                double t = 1.0;
                for (int k = 0; k < CT_z_axis; k++) {
                    short datum = cthead[k][j][i];
                    Color pixel = transferFunction(datum, opacity);
                    r = r + (t * pixel.getOpacity() * pixel.getRed());
                    g = g + (t * pixel.getOpacity() * pixel.getGreen());
                    b = b + (t * pixel.getOpacity() * pixel.getBlue());
                    t = t * (1.0 - pixel.getOpacity());
                    if(t<0.1){
                        break;
                    }
                }
                image_writer.setColor(i, j, Color.color(Math.min(r,1.0), Math.min(g,1.0),
                        Math.min(b,1.0), 1.0));
            }
        }
    }

    public void volumeRenderSide(WritableImage image, int opacity) {

        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double r = 0.0;
                double g = 0.0;
                double b = 0.0;
                double t = 1.0;
                for (int k = 0; k < CT_x_axis; k++) {
                    short datum = cthead[j][i][k];
                    Color pixel = transferFunction(datum, opacity);

                    r = r + (t * pixel.getOpacity() * pixel.getRed());
                    g = g + (t * pixel.getOpacity() * pixel.getGreen());
                    b = b + (t * pixel.getOpacity() * pixel.getBlue());
                    t = t * (1.0 - pixel.getOpacity());
                    if(t<0.1){
                        break;
                    }
                }
                image_writer.setColor(i, j, Color.color(Math.min(r,1.0), Math.min(g,1.0),
                        Math.min(b,1.0), 1.0));
            }
        }
    }

    public void TopSliderPrinter(WritableImage image, int value) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double col;
        short datum;
        //Shows how to loop through each pixel and colour
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                datum = cthead[value][j][i]; //get values from top_slider
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col = (((float) datum - (float) min) / ((float) (max - min)));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
            } // column loop
        } // row loop
    }

    public void FrontSliderPrinter(WritableImage image, int value) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        //Shows how to loop through each pixel and colour
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {

                datum = cthead[j][value][i]; //get values from top_slider
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col = (((float) datum - (float) min) / ((float) (max - min)));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
            } // column loop
        } // row loop
    }

    public void SideSliderPrinter(WritableImage image, int value) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        //Shows how to loop through each pixel and colour
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {

                datum = cthead[j][i][value]; //get values from top_slider
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col = (((float) datum - (float) min) / ((float) (max - min)));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
            } // column loop
        } // row loop
    }

    public static void main(String[] args) {
        launch();
    }
// Slider Top_slider = new Slider(0, CT_z_axis - 1, 0);
//        Slider Front_slider = new Slider(0, CT_y_axis - 1, 0);
//        Slider Side_slider = new Slider(0, CT_x_axis - 1, 0);
//        Slider Opacity_slider = new Slider(0, 100, 100); // last
    public void setSliderValues(){
        TopSlider.setMin(0);
        TopSlider.setMax(CT_z_axis - 1);
        TopSlider.setValue(0);
        SideSlider.setMin(0);
        SideSlider.setMax(CT_x_axis - 1);
        SideSlider.setValue(0);
        FrontSlider.setMin(0);
        FrontSlider.setMax(CT_y_axis - 1);
        FrontSlider.setValue(0);
        OpacitySlider.setMin(0);
        OpacitySlider.setMax(100);
        OpacitySlider.setValue(0);
        LightSlider.setMin(1);
        LightSlider.setMax(300);
        LightSlider.setValue(0);


    }
    public void setImagers(){
        TopView.setImage(top_image);
        FrontView.setImage(front_image);
        SideView.setImage(side_image);
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {



        try {
            ReadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSliderValues();
        depthButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                shadingFront(front_image, 0);
                setImagers();
            }
        });


        sliec76button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TopSlider.adjustValue(76);
                FrontSlider.adjustValue(76);
                SideSlider.adjustValue(76);
                setImagers();
            }
        });

        volrendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                OpacitySlider.adjustValue(12);
                setImagers();
            }
        });

// dealing with the top slider
        TopSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        //System.out.println(newValue.intValue());
                        TopSliderPrinter(top_image, newValue.intValue());
                        setImagers();
                    }
                });


// dealing with the front slider
        FrontSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        //System.out.println(newValue.intValue());
                        FrontSliderPrinter(front_image, newValue.intValue());
                        setImagers();
                    }
                });

// dealing with the side slider
        SideSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        //System.out.println(newValue.intValue());
                        SideSliderPrinter(side_image, newValue.intValue());
                        setImagers();
                    }
                });

// dealing with the Opacity slider
        OpacitySlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        volumeRenderTop(top_image,newValue.intValue());
                        volumeRenderFront(front_image, newValue.intValue());
                        volumeRenderSide(side_image,newValue.intValue());
                        setImagers();
                    }
                });
        LightSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        shadingFront(front_image, newValue.intValue());
                        setImagers();
                    }
                });
    }
}