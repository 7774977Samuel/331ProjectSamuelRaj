package projectq2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class imageReadWrite {
    public static void readJpgImage(String fileName, colourImage imgStruct) {
        try {
            File file = new File(fileName);
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();

            imgStruct.width = width;
            imgStruct.height = height;
            imgStruct.pixels = new short[height][width][3];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);
                    imgStruct.pixels[y][x][0] = (short) color.getRed();
                    imgStruct.pixels[y][x][1] = (short) color.getGreen();
                    imgStruct.pixels[y][x][2] = (short) color.getBlue();
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading image: " + e.getMessage());
        }
    }

    public static void writeJpgImage(colourImage imgStruct, String fileName) {
        try {
            int width = imgStruct.width;
            int height = imgStruct.height;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int r = imgStruct.pixels[y][x][0];
                    int g = imgStruct.pixels[y][x][1];
                    int b = imgStruct.pixels[y][x][2];
                    Color color = new Color(r, g, b);
                    image.setRGB(x, y, color.getRGB());
                }
            }

            File file = new File(fileName);
            ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            System.out.println("Error writing image: " + e.getMessage());
        }
    }
}
