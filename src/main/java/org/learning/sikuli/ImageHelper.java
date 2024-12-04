package org.learning.sikuli;

import java.awt.*;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

public class ImageHelper {

    public static void main(String[] args) throws IOException {
        process(ImageIO.read(new File("D:/test.png")));
    }

    public static void process(BufferedImage rgbImage) throws IOException {
        rgbImage = sharpenImage(rgbImage);
        rgbImage = rgb2grayImage(rgbImage);
        System.out.println("图像已成功转换为灰度图像并保存。");
        rgbImage = increaseContrast(rgbImage, 1.5, 0);

        rgbImage = scaleImage(rgbImage, 3);

        ImageIO.write(rgbImage, "png", new File("D:/test-contrast.png"));
        TesseractOcr.recognizeText(rgbImage);
    }

    public static BufferedImage scaleImage(BufferedImage image, int multiplier) {

        // 定义放大后的尺寸
        int scaledWidth = image.getWidth() * multiplier; // 放大到原来的两倍宽度
        int scaledHeight = image.getHeight() * multiplier; // 放大到原来的两倍高度

        // 创建一个新的 BufferedImage 来存储放大后的图像
        BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());

        // 获取 Graphics2D 对象
        Graphics2D g2d = resizedImage.createGraphics();

        // 设置抗锯齿以获得更好的质量
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 绘制放大后的图像
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return resizedImage;
    }

    private static BufferedImage rgb2grayImage(BufferedImage rgbImage) {
        // 创建 ColorConvertOp 对象，指定目标颜色空间为灰度空间
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        // 将 RGB 图像转换为灰度图像
        rgbImage = op.filter(rgbImage, null);
        return rgbImage;
    }

    public static BufferedImage sharpenImage(BufferedImage rgbImage) {
        // 创建一个卷积核
        float[] matrix = { -1, -1, -1, -1, 9, -1, -1, -1, -1 };
        Kernel kernel = new Kernel(3, 3, matrix);

        // 创建ConvolveOp对象并应用到图像
        ConvolveOp convolveOp = new ConvolveOp(kernel);
        return convolveOp.filter(rgbImage, null);
    }

    public static BufferedImage increaseContrast(BufferedImage image, double contrast, int brightness) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);

                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                // 增加对比度和亮度
                r = (int) (contrast * (r - 128) + 128 + brightness);
                g = (int) (contrast * (g - 128) + 128 + brightness);
                b = (int) (contrast * (b - 128) + 128 + brightness);

                // 确保像素值在 0 到 255 之间
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                // 设置新的像素值
                p = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, p);
            }
        }

        return result;
    }
}
