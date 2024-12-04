package org.learning.sikuli;

import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.learning.sikuli.util.ResourceUtil.path;

public class SikuliDemo {

    public static void main(String[] args) {
        Screen screen = new Screen();
        try {
            // 打开计算器应用程序
            Runtime.getRuntime().exec("calc");

            // 等待计算器窗口出现
            screen.wait(path("calculator.png"), 15);

            // 点击数字 1
            screen.click(new Pattern(path("1.png")));

            // 点击加号
            screen.click(new Pattern(path("plus.png")));

            // 点击数字 2
            screen.click(new Pattern(path("2.png")));

            // 点击等于号
            screen.click(new Pattern(path("equals.png")));

            // 等待结果出现
            screen.wait(path("result_3.png"), 5);

            // 识别出特定文本
          /*  screen.findAll(path("close.png")).forEachRemaining(match -> {
                Image matchImage = match.getImage();
                BufferedImage bufferedImage = matchImage.get();
                TesseractOcr.recognizeText(bufferedImage);
            });*/

            BufferedImage image = screen.capture().getImage();
            ImageIO.write(image, "png", new File("D:/test.png"));
            // 创建 ColorConvertOp 对象，指定目标颜色空间为灰度空间

            TesseractOcr.recognizeText(image);
//            screen.type("c", KeyModifier.CTRL);
            // 关闭计算器应用程序
            screen.click(new Pattern(path("close.png")));
        } catch (FindFailed e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
