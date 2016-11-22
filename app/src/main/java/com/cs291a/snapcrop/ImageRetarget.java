package com.cs291a.snapcrop;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class ImageRetarget {

    public static Bitmap globalNewImg;

    private static String TAG = "ImageRetarget";
    private static Mat newImg=null;
    private static int width,height;
    private static Mat img = null;
    private static int newW;
    private static int newH;

    int[][] Vbuffer = null;
    int[][] Hbuffer = null;

    Map<Integer, Map<Integer, Integer>> pathMapVertical = new HashMap<Integer, Map<Integer, Integer>>();
    Map<Integer, Map<Integer, Integer>> pathMapHorizontal = new HashMap<Integer, Map<Integer, Integer>>();


    public static void retargetImage(int nW, int nH) {
        Bitmap bitmap = PictureSelectFragment.globalBitmap;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        newW = nW;
        newH = nH;

        newImg = new Mat(height, width, CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, newImg);

        img = new Mat();

        Log.e(TAG, "Calling run with h, w " + height + " " + width);
        run();
    }

    private Mat Sobel(Mat originalMat)
    {
        Mat grayMat = new Mat();
        //Mat sobel = new Mat(); //Mat to store the result
        //Mat to store gradient and absolute gradient respectively
        Mat grad_x = new Mat();
        Mat abs_grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_y = new Mat();
        Mat originalGMat= new Mat();
        Imgproc.GaussianBlur(originalMat, originalGMat,new Size(3,3), 0,0, 0);
        //Converting the image to grayscale
        Imgproc.cvtColor(originalGMat
                ,grayMat,Imgproc.COLOR_BGR2GRAY);
        //Calculating gradient in horizontal direction
        Imgproc.Scharr(grayMat, grad_x, CvType.CV_16S, 1, 0, 1,0,0);
        // Imgproc.Sobel(grayMat, grad_x,CvType.CV_16S, 1, 0,3, 1,0,0);
        //Calculating gradient in vertical direction
        Imgproc.Scharr(grayMat, grad_y, CvType.CV_16S, 0, 1, 1,0,0);
        // Imgproc.Sobel(grayMat, grad_y,CvType.CV_16S, 0, 1,3, 1,0,0);
        //Calculating absolute value of gradients in both the direction
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        //Calculating the resultant gradient
        Core.addWeighted(abs_grad_x, 0.5,
                abs_grad_y, 0.5, 0, img);
        //System.out.println(img.dump());
        return img;
    }

    public Mat setMat(){
        int row = 0, col = 0;
        //byte data[] = { 1,2,3,1,2,1,2,3,1,4,8,4};
        //byte data[]={6,5,4,3,2,1,6,5,4,3,2,1,6,5,4,3,2,1,6,5,4,3,2,1,6,5,4,3,2,1,6,5,4,3,2,1,6,5,4,3,2,1};
        byte data[]={5,5,5,5,5,5,6,6,6,6,6,6,7,7,7,7,7,7,1,1,1,1,1,1,2,2,2,2,2,2,3,3,3,3,3,3,4,4,4,4,4,4};
        //allocate Mat before calling put
        newImg = new Mat( 7, 6, CvType.CV_8UC1 );
        newImg.put( row, col, data );
        img=newImg;
        height=getHeight();
        width=getWidth();
        return newImg;
    }

    public static int getHeight(){
        return newImg.height();
    }

    public static int getWidth(){
        return newImg.width();
    }

    public void setImageMat(Mat originalMat){
        newImg=originalMat;
        img = new Mat();
        height=getHeight();
        width=getWidth();
    }

    public void setVBuffer(){
        Vbuffer= new int[getHeight()][getWidth()];
    }

    public void setHBuffer(){
        Hbuffer= new int[getHeight()][getWidth()];
    }


    private   Map<Integer, Integer> findVerticalSeam(){
        int min_val;
        int min_ptr = 0;
        int x,y,val;
        double[] post,cur= new double[1];
        double[] data=new double[1];
        //Mat img = getMat();

        img.convertTo(img, CvType.CV_16S);

        int height=img.height();
        int width=img.width();
        //System.out.print(img.dump());
        Mat cost=Mat.zeros(height, width, img.type());
        short[] temp = new short[width];
        img.get(0, 0,temp);
        cost.put(0,0,temp);
        for (int i=1;i<height;i++)
        {
            for (int j=0;j<width;j++){
                data=img.get(i, j);
                if(j==0)
                    min_val= (int) Math.min(cost.get(i-1, j)[0],cost.get(i-1, j+1)[0]);
                else if(j<=width-2){
                    min_val=(int) Math.min(cost.get(i-1, j)[0],cost.get(i-1, j+1)[0]);
                    min_val=(int) Math.min(min_val,cost.get(i-1, j-1)[0]);
                }
                else
                    min_val=(int) Math.min(cost.get(i-1, j)[0],cost.get(i-1, j-1)[0]);
                data[0] = data[0]+min_val;
                cost.put(i, j, data);
            }
        }

        min_val=Integer.MAX_VALUE;
        Map<Integer, Integer> path = new HashMap<Integer, Integer>();
        cost.get(height-1, 0,temp);
        for(int j=0;j<width;j++){
            if(temp[j]<min_val){
                min_val=(int) temp[j];
                min_ptr=j;
            }
        }
        x=height-1; y=  min_ptr;
        path.put(x, y);

        while(x>0)
        {
            cur=cost.get(x-1, y);
            val=(int) (cost.get(x,y)[0] - img.get(x, y)[0]);
            if(y==0){
                if(!(val==cur[0]))
                    y=y+1;
            }
            else if (y<=width-2){
                post=cost.get(x-1, y+1);
                if(val==post[0])
                    y=y+1;
                else if(!(val==cur[0]))
                    y=y-1;
            }
            else{
                if(!(val==cur[0]))
                    y=y-1;
            }
            x=x-1;
            path.put(x, y);
        }
        return path;
    }


    private Mat deleteVerticalSeam(Map<Integer, Integer> path, Mat image){
        //Mat img = getMat();
        //img.convertTo(img, CvType.CV_64FC1);

        //int height=img.height();
        //int width=img.width();
        //newImg=Mat.zeros(height, width-1, newImg.type());
        Mat output=new Mat(height, width-1, image.type());
        for (int i=0;i<height;i++){
            int y=path.get(i);
            for(int j=0;j<y;j++)
                output.put(i, j, image.get(i,j));
            for(int j=y;j<width-1;j++)
                output.put(i, j, image.get(i, j+1));
        }
        //--width;
        //newImg=output;
        return output;

    }


    private void getVerticalBuffer(Map<Integer, Integer> path){
        int height=img.height();
        int width=img.width();

        Mat output=new Mat(height, width-1, img.type());

        for (int i=0;i<height;i++){
            int y=path.get(i);
            for(int j=0;j<y;j++){
                //buffer[i][j]=prebuffer[i][j];
                output.put(i, j, img.get(i,j));}
            for(int j=y;j<width-1;j++){
                Vbuffer[i][j]=Vbuffer[i][j+1]+2;
                output.put(i, j, img.get(i, j+1));
            }
        }
        img=output;
        //return img;
    }


    private void createVerticalMap(Map<Integer, Integer> path,int index){
        int height=img.height();
        //int width=img.width();
        Map<Integer, Integer> innerpath = new HashMap<Integer, Integer>();
        for(int i=0;i<height;i++){
            int y=path.get(i);
            innerpath.put(i, y+Vbuffer[i][y]);}
        pathMapVertical.put(index, innerpath);
        getVerticalBuffer(path);
    }

    private Mat addVerticalSeam(Map<Integer, Integer> path){
        int j;
        Mat output=new Mat(height, width+1, newImg.type());
        for (int i=0;i<height;i++){
            int y=path.get(i);
            for(j=0;j<y;j++)
                output.put(i, j, newImg.get(i,j));

            if(j==0){output.put(i, j, newImg.get(i,j));
                output.put(i, j+1, vector_avg(newImg.get(i,j),newImg.get(i, j+1),newImg.channels()));}
            else if(j==width-1){output.put(i, j, newImg.get(i,j));
                output.put(i, j+1, vector_avg(newImg.get(i,j),newImg.get(i, j-1),newImg.channels()));}
            else if(j<=width-2){output.put(i, j, newImg.get(i,j));
                output.put(i, j+1, vector_avg(newImg.get(i,j+1),newImg.get(i, j-1),newImg.channels()));}
            for(j=y+1;j<width;j++)
                output.put(i, j+1, newImg.get(i, j));
        }
        ++width;
        newImg=output;
        return newImg;
    }

    private double[] vector_avg(double[]a , double[]b, int length)
    {   double[] sum = new double[length];
        for (int i=0;i<length;i++)
        {
            sum[i]=(a[i]+b[i])/2.0;
        }
        return sum;
    }

    private   Map<Integer, Integer> findHorizontalSeam(){
        int min_val;
        int min_ptr = 0;
        int x,y,val;
        double[] post,cur= new double[1];
        double[] data=new double[1];
        //    Mat img = getMat();
        img.convertTo(img, CvType.CV_16S);
        int height=img.height();
        int width=img.width();
        Mat cost=Mat.zeros(height, width, img.type());

        img.col(0).copyTo(cost.col(0));

        for (int i=1;i<width;i++)
        {
            for (int j=0;j<height;j++){
                data=img.get(j, i);
                if(j==0)
                    min_val= (int) Math.min(cost.get(j, i-1)[0],cost.get(j+1, i-1)[0]);
                else if(j<=height-2){
                    min_val=(int) Math.min(cost.get(j, i-1)[0],cost.get(j+1, i-1)[0]);
                    min_val=(int) Math.min(min_val,cost.get(j-1, i-1)[0]);
                }
                else
                    min_val=(int) Math.min(cost.get(j, i-1)[0],cost.get(j-1, i-1)[0]);
                data[0] = data[0]+min_val;
                cost.put(j, i, data);
            }
        }

        min_val=Integer.MAX_VALUE;
        Map<Integer, Integer> path = new HashMap<Integer, Integer>();

        for(int j=0;j<height;j++){
            if(cost.get(j, width-1)[0]<min_val){
                min_val=(int) cost.get(j, width-1)[0];
                min_ptr=j;
            }
        }
        x=width-1; y=  min_ptr;
        path.put(x,y);

        while(x>0)
        {
            cur=cost.get(y, x-1);
            val=(int) (cost.get(y,x)[0] - img.get(y, x)[0]);
            if(y==0){
                if(!(val==cur[0]))
                    y=y+1;
            }
            else if (y<=height-2){
                post=cost.get(y+1, x-1);
                if(val==post[0])
                    y=y+1;
                else if(!(val==cur[0]))
                    y=y-1;
            }
            else{
                if(!(val==cur[0]))
                    y=y-1;
            }
            x=x-1;
            path.put(x, y);
        }
        return path;
    }

    private Mat deleteHorizontalSeam(Map<Integer, Integer> path , Mat image){
        //    Mat img = getMat();
        //img.convertTo(img, CvType.CV_64FC1);
        //int height=img.height();
        //int width=img.width();
        Mat output=new Mat(height-1, width, image.type());
        for (int i=0;i<width;i++){
            int y=path.get(i);
            for(int j=0;j<y;j++)
                output.put(j, i, image.get(j,i));
            for(int j=y;j<height-1;j++)
                output.put(j, i, image.get(j+1, i));
        }
        //img=output;
        //--height;
        //newImg=output;
        return output;
    }


    private void getHorizontalBuffer(Map<Integer, Integer> path){
        int height=img.height();
        int width=img.width();

        Mat output=new Mat(height-1, width, img.type());

        for (int i=0;i<width;i++){
            int y=path.get(i);
            for(int j=0;j<y;j++){
                //buffer[i][j]=prebuffer[i][j];
                output.put(j, i, img.get(j,i));}
            for(int j=y;j<height-1;j++){
                Hbuffer[j][i]=Hbuffer[j+1][i]+2;
                output.put(j, i, img.get(j+1, i));
            }
        }
        img=output;
        //return img;
    }


    private void createHorizontalMap(Map<Integer, Integer> path,int index){
        int width=img.width();
        //int width=img.width();
        Map<Integer, Integer> innerpath = new HashMap<Integer, Integer>();
        for(int i=0;i<width;i++){
            int y=path.get(i);
            innerpath.put(i, y+Hbuffer[y][i]);}
        pathMapHorizontal.put(index, innerpath);
        getHorizontalBuffer(path);
    }

    private Mat addHorizontalSeam(Map<Integer, Integer> path){
        //int height=img.height();
        //int width=img.width();
        int j;
        Mat output=new Mat(height+1, width, newImg.type());
        for (int i=0;i<width;i++){
            int y=path.get(i);
            for(j=0;j<y;j++)
                output.put(j, i, newImg.get(j,i));

            if(j==0){output.put(j, i, newImg.get(j,i));
                output.put(j+1, i, vector_avg(newImg.get(j,i),newImg.get(j+1, i),newImg.channels()));}
            else if(j==height-1){output.put(j, i, newImg.get(j,i));
                output.put(j+1, i, vector_avg(newImg.get(j,i),newImg.get(j-1, i),newImg.channels()));}
            else if(j<=height-2){output.put(j, i, newImg.get(j,i));
                output.put(j+1, i, vector_avg(newImg.get(j+1,i),newImg.get(j-1, i),newImg.channels()));}
            for(j=y+1;j<height;j++)
                output.put(j+1, i, newImg.get(j, i));
        }
        ++height;
        newImg=output;
        return newImg;
    }

    public void reduceWidth(int diff ){
        Sobel(newImg);
        for(int i=0;i<diff;i++){
            Map<Integer, Integer> path= findVerticalSeam();
            newImg=deleteVerticalSeam(path,newImg);--width;
            img=deleteVerticalSeam(path,img);
        }
    }

    public void increaseWidth(int diff){
        Sobel(newImg);
        setVBuffer();
        for(int i=0;i<diff;i++){
            Map<Integer, Integer> path= findVerticalSeam();
            createVerticalMap(path, i);
        }
        for(int i=0;i<diff;i++)
            addVerticalSeam(pathMapVertical.get(i));
    }

    public void reduceHeight(int diff){
        Sobel(newImg);
        for(int i=0;i<diff;i++){
            Map<Integer, Integer> path= findHorizontalSeam();
            newImg=deleteHorizontalSeam(path,newImg); --height;
            img=deleteHorizontalSeam(path,img);
        }
    }

    public void increaseHeight(int diff){
        Sobel(newImg);
        setHBuffer();
        for(int i=0;i<diff;i++){
            Map<Integer, Integer> path= findHorizontalSeam();
            createHorizontalMap(path, i);
        }
        for(int i=0;i<diff;i++)
            addHorizontalSeam(pathMapHorizontal.get(i));
    }





    public static void run() {
        long startTime = System.currentTimeMillis();

        // TODO: Image source
        Mat inputimg = null; //Highgui.imread("C:/Users/meera/wave.png");

        ImageRetarget hCV = new ImageRetarget();

        int h= newH; //380;
        int w= newW; //400;

        if(w<width){
            hCV.reduceWidth(width-w);
            // System.out.println("verical removal");
            // System.out.println(hCV.newImg.dump());
        }

        else
        { hCV.increaseWidth(w-width);
            //System.out.println("verical addition");
            //System.out.println(hCV.newImg.dump());
        }

        if(h<height)
        { hCV.reduceHeight(height-h);
            //System.out.println("Horizontal removal");
            //System.out.println(hCV.newImg.dump());
        }

        else
        { hCV.increaseHeight(h-height);
            // System.out.println("Horizontal addition");
            // System.out.println(hCV.newImg.dump());
        }

        //new LoadImage("C:/Users/meera/test.png",hCV.newImg);
        Log.d(TAG, getHeight()+" "+ getWidth()+ " ");
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "Total retarget time: " + totalTime);

        globalNewImg = Bitmap.createBitmap(newImg.cols(), newImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(newImg, globalNewImg);
    }


}


