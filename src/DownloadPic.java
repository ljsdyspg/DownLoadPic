import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DownloadPic {
    //获取全景图的URL
    private String urlFormat = "http://pcsv0.map.bdimg.com/?qt=pdata&sid=%s&pos=%s&z=5";
    private String posFormat = "%d_%d";
    private String picFolder = "OUTPUT/";
    private String folder = "TEMP/";

    private URL url;
    private String sid = null;
    private int piece_num = 0;
    private int num = 0;

    private String location = null;

    private String[] horizontal_files = new String[16];
    private String[] vertical_files = new String[8];

    private Map<String,String> map = new HashMap<>();
    //private Map<String,String> urlSet = new HashMap<>();
    private ArrayList<String> urlList = new ArrayList<>();

    //读取的地址及PID文件
    private String input_file = "test.txt";

    //读取坐标和PID存起来
    private Map<String,String> getMap(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        while (sc.hasNext()){
            map.put(sc.nextLine(),sc.nextLine());
        }
        sc.close();
        //return map = filter(map);
        return map;
    }

    //过滤掉PID值相同的(坐标，PID)
    private Map<String,String> filter(Map<String,String> map){
        Map<String,String> temp = new HashMap<String,String>();
        for (String key : map.keySet()) {
            if (!temp.containsValue(map.get(key))){
                temp.put(key,map.get(key));
            }
        }return temp;
    }

    //格式化批量生成地址
    private void generateURL(String key) {
        //sid = "01002900001406071027428365L";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                String pos = String.format(posFormat, i, j);
                //System.out.println(pos);
                String url_string = String.format(urlFormat, map.get(key), pos);
                urlList.add(url_string);
                //System.out.println(url_string);
            }
        }
    }

    private void run(Map<String,String> map) throws MalformedURLException {
        for (String key : map.keySet()) {
            generateURL(key);
            location = key;
            download();
        }
    }


    //下载全景图片
    private void download() throws MalformedURLException {
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;
        for (String url_string : urlList) {
            url = new URL(url_string);
            try {
                URLConnection conn = url.openConnection();
                InputStream inStream = conn.getInputStream();
                FileOutputStream fs = new FileOutputStream(folder+(piece_num++)+".jpg");

                byte[] buffer = new byte[1204];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                //System.out.println(piece_num+" done!");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        generatePic();
        urlList.clear();
    }

    //合成两张图片
    public void mergeImage(String[] files, int type, String targetFile) {
        int len = files.length;
        if (len < 1) {
            throw new RuntimeException("图片数量小于1");
        }
        File[] src = new File[len];
        BufferedImage[] images = new BufferedImage[len];
        int[][] ImageArrays = new int[len][];
        for (int i = 0; i < len; i++) {
            try {
                src[i] = new File(files[i]);
                images[i] = ImageIO.read(src[i]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int width = images[i].getWidth();
            int height = images[i].getHeight();
            ImageArrays[i] = new int[width * height];
            ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
        }
        int newHeight = 0;
        int newWidth = 0;
        for (int i = 0; i < images.length; i++) {
            // 横向
            if (type == 1) {
                newHeight = newHeight > images[i].getHeight() ? newHeight : images[i].getHeight();
                newWidth += images[i].getWidth();
            } else if (type == 2) {// 纵向
                newWidth = newWidth > images[i].getWidth() ? newWidth : images[i].getWidth();
                newHeight += images[i].getHeight();
            }
        }
        if (type == 1 && newWidth < 1) {
            return;
        }
        if (type == 2 && newHeight < 1) {
            return;
        }
        // 生成新图片
        try {
            BufferedImage ImageNew = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            int height_i = 0;
            int width_i = 0;
            for (int i = 0; i < images.length; i++) {
                if (type == 1) {
                    ImageNew.setRGB(width_i, 0, images[i].getWidth(), newHeight, ImageArrays[i], 0,
                            images[i].getWidth());
                    width_i += images[i].getWidth();
                } else if (type == 2) {
                    ImageNew.setRGB(0, height_i, newWidth, images[i].getHeight(), ImageArrays[i], 0, newWidth);
                    height_i += images[i].getHeight();
                }
            }
            //输出想要的图片
            ImageIO.write(ImageNew, "jpg", new File(targetFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //将小图拼成大图
    public void generatePic(){
        num = 0;
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++;}mergeImage(horizontal_files,1,folder+"X0.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X1.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X2.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X3.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X4.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X5.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X6.jpg");
        for (int i = 0; i < 16; i++) { horizontal_files[i] =folder+num+".jpg"; num++; }mergeImage(horizontal_files,1,folder+"X7.jpg");
        for (int i = 0; i < 8; i++) { vertical_files[i] =folder+"X"+i+".jpg"; }mergeImage(vertical_files,2,picFolder+location+".jpg");
        System.out.println(location+"     done!");
    }

    private void checkFolder(){
        File temp_folder = new File(folder);
        File pic_folder = new File(picFolder);
        if (!temp_folder.exists()) temp_folder.mkdirs();
        if (!pic_folder.exists()) pic_folder.mkdirs();

    }

    //先把那10个地址过滤好，拿到10个PID数据集，这个最重要了
    //依次导入运行，我就试一个就行,感觉还要打包成jar文件
    public static void main(String[] args) throws MalformedURLException, FileNotFoundException {

        DownloadPic dc = new DownloadPic();
        dc.checkFolder();
        dc.run(dc.getMap(dc.input_file));
    }
}
