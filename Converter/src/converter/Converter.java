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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
        String line;
        String[] ss;
        System.out.println("Enter command: ");
        System.out.println("1 - convert data to xml");
        System.out.println("2 - load data from xml");
        
        try{
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    line = bufferRead.readLine(); 
            //line = "2";
            if("1".equals(line))
            {
                System.out.println("Enter path to data folder: ");
                line = bufferRead.readLine();
                convertToXml(line);
                System.out.println("Enter path to save xml: ");
                line = bufferRead.readLine();
                
                XStream xstream = new XStream();
                String xml = xstream.toXML(Stations);
                try {
                    PrintWriter writer = new PrintWriter(line, "UTF-8");
                    writer.println(xml);
                    writer.close();
                } catch (FileNotFoundException ex) {
                } catch (UnsupportedEncodingException ex) {
                }
                System.exit(0);
            } else
                if("2".equals(line))
                {
                    System.out.println("Enter path to xml: ");
                    line = bufferRead.readLine();
                    // = "C:/ecology/data.xml";
                    line = FileUtils.readFileToString(new File(line), "UTF-8");
                    XStream xstream = new XStream();
                    Stations = (List<Station>)xstream.fromXML(line);
                    line = "";
                    
                   for(int i = 0; i<Stations.size();i++)
                   {
                       for(int j = 0; j<Stations.get(i).Types.size();j++)
                           if(!UniqTypes.contains(Stations.get(i).Types.get(j)))
                               UniqTypes.add(Stations.get(i).Types.get(j));
                       
                       for(int j = 0; j<Stations.get(i).Date.size();j++)
                       {
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
                   
                   
                    System.out.println("Chemicals: " + UniqTypes);
                    System.out.println("Earliest records: " + start);
                    System.out.println("Most recent records: " + finish);
                    System.out.println("Date format: dd.MM.yy HH:mm");
                    
                    int typeIndex;
                    int dateIndex;
                    String date = "";
                    String type = "2";
                    
                    boolean b = true;
                    while (b) {
                        System.out.println("Enter  date and chemical separated by space:");
                        line = bufferRead.readLine();
                        //line = "20.11.2012 12:00 CO";

                        ss = line.split(" ");
                        date = ss[0] + " " + ss[1];
                        type = ss[2];

                        for (int i = 0; i < Stations.size(); i++) {
                            if (Stations.get(i).Types.contains(type)) {
                                if (Stations.get(i).Date.contains(date)) {
                                    b = false;
                                }
                            }
                        }
                        if (b) {
                            System.out.println("No data about this chemical on this date");
                        }                          
                    }

                  
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
                    g2d.drawString(line, 10, 30);
                    g2d.drawString(colorRanges.get(0).min+"", 35, 375);
                    g2d.drawString(colorRanges.get(colorRanges.size()-1).max+"", 35, 560);
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
                        g2d.fillRect(20, 360+counter*2, 10, 2);
                        counter++;
                    }
                    
                    System.out.println("Enter path for generated image: ");
                    String path = bufferRead.readLine();
                    //path = "C:/ecology/Image.png";
                    File imageFile = new File(path);
                    ImageIO.write(image, "png", imageFile);
                    
                    System.out.println("Enter path with map file: ");
                    path = bufferRead.readLine();
                    //path = "C:/ecology/map.png";
                    
                    BufferedImage back = ImageIO.read(new File(path));
                    BufferedImage alpha = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
                    AlphaComposite ac =  AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.3f);
                    g2d = alpha.createGraphics();
                    g2d.setComposite(ac);                
                                        

                    g2d.drawImage(back, 0, 0, null);
                  
                    g2d.drawImage(image, 0, 0, null);
                    
                    g2d.setFont(new Font("Calibri", Font.PLAIN, 20));
                    g2d.setColor(Color.black);  
                    ac =  AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f);
                    g2d.setComposite(ac);
                    g2d.drawString(line, 10, 30);
                    g2d.drawString(colorRanges.get(0).min+"", 35, 375);
                    g2d.drawString(colorRanges.get(colorRanges.size()-1).max+"", 35, 560);
                    g2d.drawRect(19, 359, 11, 201);
                    for (int i = 0; i < Stations.size(); i++) {
                        if (Stations.get(i).Types.contains(type)) {
                            if (Stations.get(i).Date.contains(date)) {
                                g2d.drawOval(Stations.get(i).x, Stations.get(i).y, 2, 2);
                            }
                        }
                    }
                    
                    System.out.println("Enter path for map+image file: ");
                    path = bufferRead.readLine();
                    //path = "C:/ecology/ImageAlpha.png";
                   imageFile = new File(path);
                   ImageIO.write(alpha, "png", imageFile);
 

                    
                    System.exit(0);        
                }
            else
                {
                    System.exit(0);
                }
        } catch(IOException e)
	{
	}
        

        
        
    

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
 
        float r = 8.0f;
        int n = 100;
 
        for (int i = 0; i < n; i++) {
            r -= 0.7999f / n;
 
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
         max = max;
        initializeColors(max);
        
        
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
