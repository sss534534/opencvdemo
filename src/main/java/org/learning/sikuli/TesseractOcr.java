package org.learning.sikuli;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.IOUtils;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TesseractOcr {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String recognizeText(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        return recognizeText(image);
    }

    public static String recognizeText(BufferedImage bufferedImage) {
        ITesseract instance = new Tesseract();
        instance.setDatapath("data"); // 设置 tessdata 路径
        instance.setLanguage("chi_sim"); // 设置语言为简体中文
        try {
            Mat src = bufferedImageToMatV2(bufferedImage);
            Mat dst = new Mat();
            // 使用中值滤波
            Imgproc.medianBlur(src, dst, 3);
            bufferedImage = matToBufferedImage(dst);
//            instance.setPageSegMode(3);
//            instance.setPageSegMode(PSM_AUTO_OSD); // 自动选择最佳页面分段模式
//            instance.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_LSTM_COMBINED);
            String result = instance.doOCR(bufferedImage);
            System.out.println("识别结果: " + result);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Mat bufferedImageToMatV2(BufferedImage bufferedImage) {
        // 创建一个与BufferedImage相同尺寸的Mat
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType() == BufferedImage.TYPE_3BYTE_BGR ? CvType.CV_8UC3 : CvType.CV_8UC1);

        // 将BufferedImage的像素数据复制到Mat
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int rgb = bufferedImage.getRGB(x, y);
                // 将RGB值转换为OpenCV的BGR格式
                mat.put(y, x, new byte[]{(byte) ((rgb >> 16) & 0xFF), // B
                        (byte) ((rgb >> 8) & 0xFF),  // G
                        (byte) (rgb & 0xFF)          // R
                });
            }
        }
        return mat;
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
            return mat;
        } else if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
            return mat;
        } else {
            int width = bi.getWidth();
            int height = bi.getHeight();
            Mat mat = new Mat(height, width, CvType.CV_8UC3);
            int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
            byte[] bytes = new byte[data.length * 3];

            for (int i = 0; i < data.length; i++) {
                int pixel = data[i];
                bytes[i * 3] = (byte) ((pixel >> 16) & 0xFF); // Red
                bytes[i * 3 + 1] = (byte) ((pixel >> 8) & 0xFF); // Green
                bytes[i * 3 + 2] = (byte) (pixel & 0xFF); // Blue
            }

            mat.put(0, 0, bytes);
            return mat;
        }
    }

    public static String recognizeText(Mat image) {
        if (Objects.isNull(image)) {
            return null;
        }
        // 读取图像
        if (image.empty()) {
            System.out.println("无法读取图像");
            return null;
        }

        // 显示图像
        HighGui.imshow("Display window", image);

        // 转换为灰度图像
        Mat gray = new Mat(image.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // 显示灰度图像
        HighGui.imshow("灰度图像", gray);

        // 保存灰度图像
        Imgcodecs.imwrite("output_image.jpg", gray);

        // 使用高斯模糊减少噪声
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(3, 3), 0);

        // 使用二值化处理
        Mat binary = new Mat();
        Imgproc.threshold(blurred, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // 显示处理后的图像
        HighGui.imshow("处理后的图像", binary);

        // 将 Mat 转换为 BufferedImage
        BufferedImage bufferedImage = matToBufferedImage(binary);

        // 配置 Tesseract
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("data/"); // 设置 tessdata 路径
        tesseract.setLanguage("chi_sim"); // 设置语言

        try {
            // 进行 OCR 识别
            String result = tesseract.doOCR(bufferedImage);
            System.out.println("识别结果: " + result);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    public static String recognizeCN(Mat image) {
        // 预处理图像
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
//        Mat blurred = new Mat();
//        Imgproc.GaussianBlur(gray, blurred, new Size(3, 3), 0);
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 128, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // 绘制轮廓
        Mat contourOutput = image.clone();
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(0, 255, 0); // 绿色
            Imgproc.drawContours(contourOutput, contours, i, color, 2);
        }

        // 将 Mat 转换为字节数组
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", binary, matOfByte);
        byte[] byteArray = matOfByte.toArray();

        // 使用 Tesseract 进行 OCR
        ITesseract instance = new Tesseract();
        instance.setDatapath("data"); // 设置 tessdata 路径
        instance.setLanguage("chi_sim"); // 设置语言为简体中文

        try {
            String outputImageFile = "local_temp.png";
            IOUtils.write(byteArray, new FileOutputStream(outputImageFile));
            // 从字节数组创建输入流
            String result = instance.doOCR(new File(outputImageFile));
            System.out.println("识别结果: " + result);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Mat enhanceContrast(Mat image) {
        Mat labImage = new Mat();
        Imgproc.cvtColor(image, labImage, Imgproc.COLOR_BGR2Lab);

        Mat lChannel = new Mat();
        Mat aChannel = new Mat();
        Mat bChannel = new Mat();
        List<Mat> mats = Arrays.asList(lChannel, aChannel, bChannel);
        Core.split(labImage, mats);

        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        Mat cl = new Mat();
        clahe.apply(lChannel, cl);

        Mat labeImage = new Mat();
        List<Mat> outMats = Arrays.asList(cl, aChannel, bChannel);
        Core.merge(outMats, labeImage);

        Mat finalImage = new Mat();
        Imgproc.cvtColor(labeImage, finalImage, Imgproc.COLOR_Lab2BGR);

        return finalImage;
    }

    // 将 Mat 转换为 BufferedImage
    private static BufferedImage matToBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;

        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

}
