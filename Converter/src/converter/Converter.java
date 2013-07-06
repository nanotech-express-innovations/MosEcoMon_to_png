/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;
import com.thoughtworks.xstream.XStream;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;





/**
 *
 * @author Artem
 */

class Station {
   String Name;
   int x,y;
   List<String> Date = new ArrayList<String>();
   List<String> Types = new ArrayList<String>();
   ArrayList<ArrayList<Double>> Data = new ArrayList<ArrayList<Double>>();
   Station(String Name)
   {
       this.Name = Name;      
   }
   
}

class WeightedPoint {

    int x;
    int y;
    double value;

    WeightedPoint(int x, int y, double value) {

        this.x = x;
        this.y = y;
        this.value = value;
    }
}

class ColorRange {

    double min;
    double max;
    Color color;

    ColorRange(double min, double max, Color color) {

        this.min = min;
        this.max = max;
        this.color = color;
    }
}



public class Converter {

    /**
     * @param args the command line arguments
     */
    
    static List<WeightedPoint> weightedPoints = new ArrayList<WeightedPoint>();
    static List<ColorRange> colorRanges = new ArrayList<ColorRange>();
    static int power = 4;
    
    static List<String> Folders = new ArrayList<String>();
    static List<Station> Stations = new ArrayList<Station>();
    static List<String> UniqTypes = new ArrayList<String>();
    
    static DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
    static DateFormat dff = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    static Calendar c = Calendar.getInstance();
    static Calendar cc = Calendar.getInstance();
    

    
    public static void main(String[] args) {

        
        String start = "01.01.2099 00:00";
        String finish = "01.01.2000 00:00";
        String line = "";
        String[] ss;
        
        File configFile = new File("converter.cfg");
        Properties config = new Properties();
        try {
            FileInputStream is= new FileInputStream(configFile);
            config.loadFromXML(is);
        } catch (IOException ex) {
        }
        /*
        try {
            FileOutputStream os= new FileOutputStream(configFile);
            config.setProperty("data-file", "C:\\ecology\\data.xml");
            config.setProperty("map-file", "C:\\ecology\\map.png");
            config.setProperty("output-folder", "C:\\ecology\\output");
            config.storeToXML(os, "Converter Settings");
            System.exit(0);
        } catch (IOException ex) {
        } */
        

        
        
        if(args.length == 0)
            System.exit(0);
        
        if(args[0].equals("setProperty"))
        {
            
            try {
                FileOutputStream os = new FileOutputStream(configFile);
                config.setProperty(args[1], args[2]);
                config.storeToXML(os, "Converter Settings");
                System.exit(0);
            } catch (IOException ex) {
            }
            System.exit(0);
        }
        
        if(args[0].equals("convertData"))
        {
            convertToXml(args[1]);
            XStream xstream = new XStream();
            String xml = xstream.toXML(Stations);
            try {
                PrintWriter writer = new PrintWriter(args[2], "UTF-8");
                writer.println(xml);
                writer.close();
            } catch (FileNotFoundException ex) {
            } catch (UnsupportedEncodingException ex) {
            }
            System.exit(0);
        }
        
        if(args[0].equals("dataInfo"))
        {
            String data = "";
            try {
                data = FileUtils.readFileToString(new File(config.getProperty("data-file")), "UTF-8");
            } catch (IOException ex) {
            }
            
            XStream xstream = new XStream();
            Stations = (List<Station>) xstream.fromXML(data);
          
            for (int i = 0; i < Stations.size(); i++) {
                for (int j = 0; j < Stations.get(i).Types.size(); j++) {
                    if (!UniqTypes.contains(Stations.get(i).Types.get(j))) {
                        UniqTypes.add(Stations.get(i).Types.get(j));
                    }
                }
            }


            System.out.println("Data file: " + config.getProperty("data-file"));
            System.out.println("Chemicals: " + UniqTypes);
            System.out.println();
            
            for(int k = 0; k < UniqTypes.size(); k++)
            {
                boolean exists = false;
                
                start = "01.01.2099 00:00";
                finish = "01.01.2000 00:00";

                for (int i = 0; i < Stations.size(); i++) {

                    for (int j = 0; j < Stations.get(i).Types.size(); j++) {
                        if (Stations.get(i).Types.get(j).equals(UniqTypes.get(k))) {
                            exists = true;
                        }
                    }

                    if (exists) {
                        for (int j = 0; j < Stations.get(i).Date.size(); j++) {
                            try {
                                c.setTime(df.parse(Stations.get(i).Date.get(j)));
                                cc.setTime(dff.parse(start));
                                if (c.before(cc)) {
                                    start = Stations.get(i).Date.get(j);
                                }
                                cc.setTime(dff.parse(finish));
                                if (c.after(cc)) {
                                    finish = Stations.get(i).Date.get(j);
                                }
                            } catch (ParseException ex) {
                            }
                        }
                    }

                    exists = false;
                }
                System.out.println(UniqTypes.get(k));
                System.out.println("Earliest records: " + start);
                System.out.println("Most recent records: " + finish);
                System.out.println();

            }
                
            System.out.println("Earliest records: " + start);
            System.out.println("Most recent records: " + finish);

        }

        
        if(args[0].equals("generatePng"))
        {
            String data = "";
            try {
                data = FileUtils.readFileToString(new File(config.getProperty("data-file")), "UTF-8");
            } catch (IOException ex) {
            }
            
            XStream xstream = new XStream();
            Stations = (List<Station>) xstream.fromXML(data);
            
            String date = "";
            String type = "";
            int typeIndex = 0;
            int dateIndex = 0;
            double max = 0.0;
            
            date = args[2];
            type = args[3];
            
            boolean b = true;
            
            for (int i = 0; i < Stations.size(); i++) 
                if (Stations.get(i).Types.contains(type)) 
                    if (Stations.get(i).Date.contains(date)) 
                    {
                        typeIndex = Stations.get(i).Types.indexOf(type);
                        dateIndex = Stations.get(i).Date.indexOf(date);
                        if (max < Stations.get(i).Data.get(typeIndex).get(dateIndex)) {
                            max = Stations.get(i).Data.get(typeIndex).get(dateIndex);
                        }
                        b = false;
                    }

            if (b) {
                System.out.println("No data about this chemical on this date");
                System.exit(0);
            }
            
            initializeColors(max);
            
            if(args[1].equals("layer"))
            {
                BufferedImage image = generateLayer(type, date);
                String path = "\\layer " + type + " " + date + ".png";
                path = path.replaceAll(":", ".");
                path = config.getProperty("output-folder") + path;
                File imageFile = new File(path);
                try {
                    ImageIO.write(image, "png", imageFile);
                } catch (IOException ex) {
                }
                System.exit(0);
            }
            
            if(args[1].equals("image"))
            {
                try {
                    BufferedImage image = generateLayer(type, date);
                    BufferedImage map = ImageIO.read(new File(config.getProperty("map-file"))); 
                    BufferedImage combination = combineLayers(map, image, type, date);
                    
                    String path = "\\image " + type + " " + date + ".png";
                    path = path.replaceAll(":", ".");
                    path = config.getProperty("output-folder") + path;
                    File imageFile = new File(path);
                    ImageIO.write(combination, "png", imageFile);
                    System.exit(0);
                } catch (IOException ex) {
                }
            }   
        }
        
        if(args[0].equals("generateGif"))
        {
            String data = "";
            try {
                data = FileUtils.readFileToString(new File(config.getProperty("data-file")), "UTF-8");
            } catch (IOException ex) {
            }
            
            XStream xstream = new XStream();
            Stations = (List<Station>) xstream.fromXML(data);
            
            String dateStart = "";
            String dateFinish = "";
            int intervalTime = 1;
            int intervalSlides = 1000;
            String type = "";
            int typeIndex = 0;
            int dateIndex = 0;
            double max = 0.0;
            
            dateStart = args[2];
            dateFinish = args[3];
            intervalTime = Integer.parseInt(args[4]);
            type = args[5];
            intervalSlides = Integer.parseInt(args[6]);
            
            boolean b = true;
            
            List<BufferedImage> layers = new ArrayList<BufferedImage>();
            List<String> dates = new ArrayList<String>();
            
            
            SimpleDateFormat parser=new SimpleDateFormat("dd.MM.yyyy HH:mm");
            try {
                Calendar c = Calendar.getInstance();
                Calendar ce = Calendar.getInstance();
                c.setTime(parser.parse(dateStart));
                ce.setTime(parser.parse(dateFinish)); 
                while(c.before(ce))
                {
                    String tmp = parser.format(c.getTime());
                    b = true;
                    for (int i = 0; i < Stations.size(); i++) {
                        if (Stations.get(i).Types.contains(type)) {
                            if (Stations.get(i).Date.contains(tmp)) {
                                typeIndex = Stations.get(i).Types.indexOf(type);
                                dateIndex = Stations.get(i).Date.indexOf(tmp);
                                if(max < Stations.get(i).Data.get(typeIndex).get(dateIndex))
                                    max = Stations.get(i).Data.get(typeIndex).get(dateIndex);
                                b = false;
                            }
                        }
                    }

                    if (!b) {
                        dates.add(tmp);
                    }   
                    c.add(Calendar.HOUR, intervalTime);
                }
                
            } catch (ParseException ex) {
            }
            
            initializeColors(max);
            
            for (int i = 0; i < dates.size(); i++) {
                System.out.println("Generating layer " + i);
                layers.add(generateLayer(type, dates.get(i)));
                String path = "\\layer " + type + " " + dates.get(i) + ".png";
                path = path.replaceAll(":", ".");
                path = config.getProperty("output-folder") + path;
                File imageFile = new File(path);
                try {
                    ImageIO.write(layers.get(i), "png", imageFile);
                } catch (IOException ex) {
                }
            }

            
            
            if(args[1].equals("layer"))
            {
                
                
                String path = "\\layer " + type + " " + dateStart + "-" + dateFinish + ".gif";
                path = path.replaceAll(":", ".");
                path = config.getProperty("output-folder") + path;
                
                AnimatedGifEncoder gif = new AnimatedGifEncoder();
                gif.start(path);
                gif.setDelay(intervalSlides);
                gif.setRepeat(0);
                for( int i = 0; i < layers.size(); i++){
                    System.out.println("Adding layer " + i);
                    gif.addFrame(layers.get(i));
                }
                    
                gif.finish();
 
                System.exit(0);
            }
            
            if(args[1].equals("image"))
            {
                try {
                    String path = "\\layer " + type + " " + dateStart + "-" + dateFinish + ".gif";
                    path = path.replaceAll(":", ".");
                    path = config.getProperty("output-folder") + path;
                    
                    BufferedImage map = ImageIO.read(new File(config.getProperty("map-file")));
                    AnimatedGifEncoder gif = new AnimatedGifEncoder();
                    gif.start(path);
                    gif.setDelay(intervalSlides);
                    gif.setRepeat(0);
                    for( int i = 0; i < layers.size(); i++){
                        System.out.println("Adding layer " + i);
                        gif.addFrame(combineLayers(map, layers.get(i), type, dates.get(i)));
                    }
                        
                    gif.finish();
     
                    System.exit(0);
                } catch (IOException ex) {
                }
            }
             
             
        }
        
    }
    
    static BufferedImage generateLayer(String type, String date)
    {
        int typeIndex;
        int dateIndex;
        
        for (int i = 0; i < Stations.size(); i++) {
            if (Stations.get(i).Types.contains(type)) {
                if (Stations.get(i).Date.contains(date)) {
                    typeIndex = Stations.get(i).Types.indexOf(type);
                    dateIndex = Stations.get(i).Date.indexOf(date);
                    weightedPoints.add(new WeightedPoint(Stations.get(i).x, Stations.get(i).y, Stations.get(i).Data.get(typeIndex).get(dateIndex)));
                }
            }
        }


        BufferedImage image = getImage(600, 600);

        Graphics2D g2d = image.createGraphics();
        g2d.setFont(new Font("Calibri", Font.PLAIN, 20));
        g2d.setColor(Color.black);
        g2d.drawString(date + " " + type, 10, 30);
        g2d.drawString(colorRanges.get(0).min + "", 35, 375);
        g2d.drawString(colorRanges.get(colorRanges.size() - 1).max + "", 35, 560);
        g2d.drawRect(19, 359, 11, 201);

        for (int i = 0; i < Stations.size(); i++) {
            if (Stations.get(i).Types.contains(type)) {
                if (Stations.get(i).Date.contains(date)) {
                    g2d.drawOval(Stations.get(i).x, Stations.get(i).y, 2, 2);
                }
            }
        }

        int counter = 0;
        for (ColorRange r : colorRanges) {
            g2d.setColor(r.color);
            g2d.fillRect(20, 360 + counter * 2, 10, 2);
            counter++;
        }

        return image;
    }
    
    static BufferedImage combineLayers(BufferedImage back, BufferedImage image, String type, String date)
    {
        BufferedImage combination = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combination.createGraphics();

        g2d.drawImage(back, 0, 0, null);
        
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
        g2d.setComposite(ac);
        g2d.drawImage(image, 0, 0, null);

        g2d.setFont(new Font("Calibri", Font.PLAIN, 20));
        g2d.setColor(Color.black);
        ac = AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f);
        g2d.setComposite(ac);
        g2d.drawString(date + " " + type, 10, 30);
        g2d.drawString(colorRanges.get(0).min + "", 35, 375);
        g2d.drawString(colorRanges.get(colorRanges.size() - 1).max + "", 35, 560);
        g2d.drawRect(19, 359, 11, 201);
        for (int i = 0; i < Stations.size(); i++) {
            if (Stations.get(i).Types.contains(type)) {
                if (Stations.get(i).Date.contains(date)) {
                    g2d.drawOval(Stations.get(i).x, Stations.get(i).y, 2, 2);
                }
            }
        }
        
        return combination;
    }
    
    static void convertToXml(String path)
    {
        
        File fname = new File(path);
        File[] fileNames;
        fileNames = fname.listFiles();
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].isDirectory()) {
                Folders.add(fileNames[i].getPath());
                Stations.add( new Station(fileNames[i].getName()));
            }
        }
       
       
        
        for(int i = 0; i < Folders.size(); i++)
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader(Folders.get(i)+"\\coord.txt"));
                String line = br.readLine();
                Stations.get(i).x = Integer.parseInt(line.split(" ")[0]);
                Stations.get(i).y = Integer.parseInt(line.split(" ")[1]);
                br.close();
            } catch (Exception e) {
            }
          
        }
        
 
        String d, M, y, h, m, line, curDate;
        String[] ss;
        BufferedReader br;

        for(int i = 0; i < Folders.size(); i++)
        {
            fname = new File(Folders.get(i));
            fileNames = fname.listFiles();
            for (int j = 0; j < fileNames.length; j++) {
                if(fileNames[j].getName().equals("coord.txt"))
                    continue;

                System.out.println("Filename: " + fileNames[j].getName());
                try {
                    br = new BufferedReader(new FileReader(fileNames[j]));
                    line = br.readLine();
                    line = line.trim();
                    ss = line.split(" ");
                    if (Stations.get(i).Types.isEmpty()) {
                        for (int ii = 2; ii < ss.length; ii++) {
                            Stations.get(i).Types.add(ss[ii]);
                            Stations.get(i).Data.add(new ArrayList<Double>());
                        }
                    }

                    line = br.readLine();
                    line = line.trim();
                    ss = line.split(" ");
                    
                    c.setTime(df.parse(ss[0] + " " + ss[1]));
                    d = (c.get(c.DATE)<10)?("0" + String.valueOf(c.get(c.DATE))):(String.valueOf(c.get(c.DATE)));
                    M = (c.get(c.MONTH)+1<10)?("0" + String.valueOf(c.get(c.MONTH)+1)):(String.valueOf(c.get(c.MONTH)+1));
                    y = String.valueOf(c.get(c.YEAR));
                    h = (c.get(c.HOUR_OF_DAY)<10)?("0" + String.valueOf(c.get(c.HOUR_OF_DAY))):(String.valueOf(c.get(c.HOUR_OF_DAY)));
                    m = (c.get(c.MINUTE)<10)?("0" + String.valueOf(c.get(c.MINUTE))):(String.valueOf(c.get(c.MINUTE)));
                    curDate = d + "." + M + "." +  y + " " + h + ":" + m;
                    
                    
                    if(!Stations.get(i).Date.contains(curDate))
                    {
                        Stations.get(i).Date.add(curDate);
                        for(int ii = 2; ii < ss.length; ii ++)
                        {
                            Stations.get(i).Data.get(ii-2).add((ss[ii].equals("----"))?(0.0):(Double.parseDouble(ss[ii])));
                        }
                        
                    }

                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        ss = line.split(" ");
                        c.add(c.HOUR_OF_DAY, 1);
                        d = (c.get(c.DATE) < 10) ? ("0" + String.valueOf(c.get(c.DATE))) : (String.valueOf(c.get(c.DATE)));
                        M = (c.get(c.MONTH) + 1 < 10) ? ("0" + String.valueOf(c.get(c.MONTH) + 1)) : (String.valueOf(c.get(c.MONTH) + 1));
                        y = String.valueOf(c.get(c.YEAR));
                        h = (c.get(c.HOUR_OF_DAY) < 10) ? ("0" + String.valueOf(c.get(c.HOUR_OF_DAY))) : (String.valueOf(c.get(c.HOUR_OF_DAY)));
                        m = (c.get(c.MINUTE) < 10) ? ("0" + String.valueOf(c.get(c.MINUTE))) : (String.valueOf(c.get(c.MINUTE)));
                        curDate = d + "." + M + "." + y + " " + h + ":" + m;

                        if (!Stations.get(i).Date.contains(curDate)) {
                            Stations.get(i).Date.add(curDate);
                            for (int ii = 2; ii < ss.length; ii++) {
                                Stations.get(i).Data.get(ii-2).add((ss[ii].equals("----"))?(0.0):(Double.parseDouble(ss[ii])));
                            }

                        }
                    }
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        
    }
    
    void addWeightedPoint(WeightedPoint p) {
        weightedPoints.add(p);
    }
    
    
    static void initializeColors(double max) {
 
        float r = 0.33f;
        int n = 100;
 
        for (int i = 0; i < n; i++) {
            r += 0.7222f / n;
 
            colorRanges.add(new ColorRange(i * (max / n), i * (max / n)
                    + (max / n), Color.getHSBColor(r, 1f, 1f)));
        }
 
    }
    
    static BufferedImage getImage(int width, int height) {
 
        BufferedImage bufferedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        double[][] imgArray = new double[width][height];
        imgArray = interpolateImage(width, height);
        
        double max = 0.0;
         for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(imgArray[i][j]>max)
                    max = imgArray[i][j];
            }
         }

        
        drawImage(bufferedImage, imgArray);
 
        return bufferedImage;
    }
    
     static double[][] interpolateImage(int width, int height) {

        double[][] imgArray = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imgArray[i][j] =  getValueShepard(i, j);
            }
        }
        return imgArray;
    }
    
     static void drawImage(BufferedImage bufferedImage, double[][] imgArray) {
 
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                bufferedImage.setRGB(i, j, getColor(getValueShepard(i, j)));
            }
        }
    }
 
    static double getValueShepard(int i, int j) {
 
        double dTotal = 0.0;
        double result = 0.0;
 
        for (WeightedPoint p : weightedPoints) {
 
            double d = distance(p.x,p.y, i, j);
            if (power != 1) {
                d = Math.pow(d, power);
            }
            if (d > 0.0) {
                d = 1.0 / d;
            } else { // if d is real small set the inverse to a large number
                     // to avoid INF
                d = 1.e20;
            }
            result += p.value * d;
            dTotal += d;
        }
 
        if (dTotal > 0) {
            return result / dTotal;
        } else {
            return 0;
        }
 
    }
 
    static int getColor(double val) {
        for (ColorRange r : colorRanges) {
            if (val >= r.min && val < r.max) {
                return r.color.getRGB();
            }
        }
        return 0;
    }
    
    static double distance(double xDataPt, double yDataPt, double xGrdPt,
            double yGrdPt) {
        double dx = xDataPt - xGrdPt;
        double dy = yDataPt - yGrdPt;
        return Math.sqrt(dx * dx + dy * dy);
    }
 
    // bufferedImage.setRGB(i, j, new Random(100).nextInt());
 
    
    
    
 
}
