import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GUI implements ActionListener {
    JFrame frame;
    JButton capture;
    JTextField left;
    JTextField top;
    JTextField width;
    JTextField height;
    JButton start;
    JButton stop;
    JButton findCircles;
    Robot robo;
    boolean starting;
    boolean readyToCapture;
    boolean readyToFind;
    Graphics g;
    JPanel control;
    JLabel imagePlace;
    int x;
    int y;
    int nheight;
    int nwidth;
    BufferedImage image;
    BufferedImage circle;
    start ehhh;
    static ArrayList <champion> champs;

    public GUI() {
        frame = new JFrame("SCIENCE FAIR");
        capture = new JButton("capture");
        left = new JTextField("left");
        top = new JTextField("top");
        width = new JTextField("width");
        height = new JTextField("height");
        start = new JButton("start");
        stop = new JButton("stop");
        control = new JPanel();
        imagePlace = new JLabel();
        findCircles = new JButton("find Circles");

        frame.setSize(575, 450);
        frame.setLayout(new BorderLayout());
        frame.add(control, BorderLayout.WEST);
        frame.add(imagePlace, BorderLayout.CENTER);

        capture.addActionListener(this);

        
        control.add(findCircles);
        findCircles.addActionListener(this);
        start.addActionListener(this);
        stop.addActionListener(this);
        control.setLayout(new GridLayout(8, 1));
        control.setSize(150, 400);
        imagePlace.setSize(400, 400);

        final java.net.URL imageURL =

        this.getClass().getClassLoader().getResource("minimap2.bmp");

        final java.net.URL imageURL2 = this.getClass().getClassLoader()
                .getResource("circle-22.PNG");
        try {
            image = ImageIO.read(imageURL);
            circle = ImageIO.read(imageURL2);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        imagePlace.setIcon(new ImageIcon(image));
        try {
            robo = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ehhh = new start();
    }

    public static void main(String[] args) {
        new GUI();
    }

    public BufferedImage createBufferedImage(Image imageIn, Component comp) {
        return createBufferedImage(imageIn, BufferedImage.TYPE_INT_ARGB, comp);
    }

    public BufferedImage createBufferedImage(Image imageIn, int imageType,
            Component comp) {
        MediaTracker mt = new MediaTracker(comp);
        mt.addImage(imageIn, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ie) {
        }
        BufferedImage bufferedImageOut = new BufferedImage(
                imageIn.getWidth(null), imageIn.getHeight(null), imageType);
        g = bufferedImageOut.getGraphics();
        g.drawImage(imageIn, 0, 0, null);
        return bufferedImageOut;
    }

    public int getScore(int i, int j, BufferedImage tempImage) {
        int score = 0;
        int originx = i;
        int originy = j - 11;

        for (int a = originx; a < originx + 23; a++) {
            for (int b = originy; b < originy + 23; b++) {
                try {
                    int pix1 = tempImage.getRGB(a, b);
                    int pix2 = circle.getRGB(a - originx, b - originy);

                    if (pix1 == 0xFF000000 && pix2 == 0xFF000000) {
                        score++;
                    }
                } catch (Exception c) {

                }
            }
        }
        return score;
    }

    void capture() {
        readyToCapture = false;
        Dimension blerhg = Toolkit.getDefaultToolkit().getScreenSize();
        y = blerhg.height - blerhg.height / 4 - 30;
        x = blerhg.width - blerhg.width / 6 - 30;
        nheight = blerhg.height / 4 + 30;
        nwidth = blerhg.width / 6 + 30;

        System.out.println(blerhg.width + "  --  " + blerhg.height);
        try {
            x = Integer.parseInt(left.getText());
            y = Integer.parseInt(top.getText());
            nheight = Integer.parseInt(height.getText());
            nwidth = Integer.parseInt(width.getText());
        } catch (Exception b) {

        }
        BufferedImage bloopbloop = robo.createScreenCapture(new Rectangle(x, y, nwidth, nheight));
        image = bloopbloop;
        imagePlace.setIcon(new ImageIcon(image));
        readyToFind = true;
    }

    public void findCamera()
    {
        
    }
    public void findCircles() {
        readyToFind = false;
        System.out.println("Phase1");
        BufferedImage tempImage = image;
        boolean[][] dots = new boolean[tempImage.getWidth()][tempImage
                .getHeight()];
        boolean[][] deadZones = new boolean[tempImage.getWidth()][tempImage
                .getHeight()];
        for (int i = 0; i < tempImage.getWidth(); i++) {
            for (int j = 0; j < tempImage.getHeight(); j++) {
                int rgb = tempImage.getRGB(i, j);
                int red = (rgb >> 16) & 0x000000FF;
                int green = (rgb >> 8) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;
                boolean notMinion = false;

                if (green > 100 && red < 50 && blue < 50) {
                    tempImage.setRGB(i, j, 0xFF000000);
                    dots[i][j] = true;
                } else if (red > 100 && green < 50 && blue < 50) {
                    tempImage.setRGB(i, j, 0xFF000000);
                    dots[i][j] = true;
                } else {
                    tempImage.setRGB(i, j, 0xFFFFFFFF);
                    dots[i][j] = false;
                }

                // for(int x = j; x > j-3; x--)
                // {
                // for(int y = i; y > i-3; y--)
                // {
                // try{
                // if(tempImage.getRGB(x,y) == 0xFFFFFFFF)
                // {
                // notMinion = true;
                // }
                // }
                // catch(Exception alpha){}
                // }
                // }
                // if(!notMinion)
                // {
                // for(int x = j; x > j-5; x--)
                // {
                // for(int y = i; y > i-5; y--)
                // {
                // try{
                // tempImage.setRGB(x, y, 0xFFFFFFFF);
                // }
                // catch(Exception beta){System.out.println("beta");}
                // }
                // }
                // }
            }
        }
        imagePlace.setIcon(new ImageIcon(tempImage));
        imagePlace.repaint();

        System.out.println("Phase 2");
        for (int i = 0; i < tempImage.getWidth(); i++) {
            for (int j = 0; j < tempImage.getHeight(); j++) {
                int score = 0;
                int score2 = 0;
                try {
                    if (dots[i][j] && !deadZones[i + 11][j]) {
                        score = getScore(i, j, tempImage);
                    }
                    if (dots[i - 23][j] && !deadZones[i - 11][j]) {
                        score2 = getScore(i, j, tempImage);
                    }
                } catch (Exception blergh) {
                }
                if (score >= 30) {
                    System.out.println("Phase 3 -- " + score);
                    // tempImage.setRGB(j, i, 0xFF00FF00);
                    for (int x = 0; x < 5; x++) {
                        try {
                            tempImage.setRGB(i + 13 - x, j, 0xFF00FF00);
                            tempImage.setRGB(i + 11, j + 2 - x, 0xFF00FF00);
                            deadZones[i + 9][j - 2 + x] = true;
                            deadZones[i + 10][j - 2 + x] = true;
                            deadZones[i + 11][j - 2 + x] = true;
                            deadZones[i + 12][j - 2 + x] = true;
                            deadZones[i + 13][j - 2 + x] = true;
                        } catch (Exception d) {

                        }

                    }
                }
                if (score2 >= 30) {
                    System.out.println("Phase 3 -- " + score);
                    // tempImage.setRGB(j, i, 0xFF00FF00);
                    for (int x = 0; x < 5; x++) {
                        try {
                            tempImage.setRGB(i - 13 - x, j, 0xFF00FF00);
                            tempImage.setRGB(i - 11, j + 2 - x, 0xFF00FF00);
                            deadZones[i - 9][j - 2 + x] = true;
                            deadZones[i - 10][j - 2 + x] = true;
                            deadZones[i - 11][j - 2 + x] = true;
                            deadZones[i - 12][j - 2 + x] = true;
                            deadZones[i - 13][j - 2 + x] = true;
                        } catch (Exception d) {

                        }

                    }
                }
            }
        }
        imagePlace.setIcon(new ImageIcon(tempImage));
        imagePlace.repaint();
        System.out.println("operation complete");
        readyToCapture = true;
    }
    class start extends Thread
    {
        public start()
        {
            while(true)
            {
            while (starting) {
                try
                {
                capture();
                findCircles();
                champs.clear();
                }
                catch (Exception e){}
//              try {
//                  wait(500);
//              } catch (Exception e) {
//                  e.printStackTrace();
//              }
            }
        }
        }
        
    }

    static class champion
    {
        int x;
        int y;
        
        public champion(int x,int y)
        {
            this.x = x;
            this.y = y;
            champs.add(this);
        }
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(capture)) {
            capture();
        }
        if (e.getSource().equals(findCircles)) {
            findCircles();
        }
        if (e.getSource().equals(start)) {
            starting = true;
            }

        
        if (e.getSource().equals(stop))
        {
            starting = false;
            
        }
    }
}
