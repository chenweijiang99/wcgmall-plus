package com.river.service.impl;

import com.river.common.RedisConstants;
import com.river.config.properties.CaptchaConfigProperties;
import com.river.dto.Captcha;
import com.river.exception.ServiceException;
import com.river.service.CaptchaService;
import com.river.utils.CaptchaUtil;
import com.river.utils.RedisUtil;
import com.river.utils.SpringUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final CaptchaConfigProperties captchaConfigProperties;

    @Override
    public Captcha getCaptcha() {
        //获取画布的宽高
        int canvasWidth = captchaConfigProperties.getCanvasWidth();
        int canvasHeight = captchaConfigProperties.getCanvasHeight();
        //获取阻塞块的宽高/半径
        int blockWidth = captchaConfigProperties.getBlockWidth();
        int blockHeight = captchaConfigProperties.getBlockHeight();
        int blockRadius = captchaConfigProperties.getBlockRadius();
        //获取资源图
        BufferedImage canvasImage = getBufferedImage();
        //调整原图到指定大小
        canvasImage = CaptchaUtil.imageResize(canvasImage, canvasWidth, canvasHeight);
        //随机生成阻塞块坐标
        int blockX = CaptchaUtil.getNonceByRange(blockWidth, canvasWidth - blockWidth - 10);
        int blockY = CaptchaUtil.getNonceByRange(10, canvasHeight - blockHeight + 1);
        //阻塞块
        BufferedImage blockImage = new BufferedImage(blockWidth, blockHeight, BufferedImage.TYPE_4BYTE_ABGR);
        //新建的图像根据轮廓图颜色赋值，源图生成遮罩
        CaptchaUtil.cutByTemplate(canvasImage, blockImage, blockWidth, blockHeight, blockRadius, blockX, blockY);
        // 移动横坐标
        String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");
        // 缓存
        saveImageCode(nonceStr,String.valueOf(blockX));
        //设置返回参数
        Captcha captcha =new Captcha();
        captcha.setNonceStr(nonceStr);
        captcha.setBlockY(blockY);
        captcha.setBlockSrc(CaptchaUtil.toBase64(blockImage, "png"));
        captcha.setCanvasSrc(CaptchaUtil.toBase64(canvasImage, "png"));
        return captcha;
    }

    /**
     * 获取验证码资源图
     **/
    public BufferedImage getBufferedImage() {
        try {
            //随机图片
            int nonce = CaptchaUtil.getNonceByRange(0, 1000);
            //获取网络资源图片
            if ("remote".equals(captchaConfigProperties.getPlace())) {
//                String imgUrl = String.format(IMG_URL, nonce);
                URL url = new URL(captchaConfigProperties.getRemotePath());
                return ImageIO.read(url.openStream());
            }
            //获取本地图片
            else{
                // 随机获取本地文件夹下的图片
                File folder = new File(captchaConfigProperties.getLocalPath());
                File[] files = folder.listFiles();
                if (Objects.isNull(files) || files.length == 0) {
                    throw new ServiceException("本地验证码文件夹下没有图片");
                }
                File randomFile = files[new Random().nextInt(files.length)];
                if (!randomFile.isFile()) {
                    throw new ServiceException("本地验证码文件夹下不是文件");
                }
                return ImageIO.read(randomFile);
            }
        } catch (Exception e) {
            System.out.println("获取拼图资源失败");
            //异常处理
            return null;
        }
    }

    /**
     * 缓存验证码，有效期1分钟
     * @param key
     * @param code
     **/
    public void saveImageCode(String key, String code) {
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        redisUtil.set(RedisConstants.SLIDER_CAPTCHA_CODE_KEY + key, code, captchaConfigProperties.getExpire(), TimeUnit.SECONDS);
    }
}
