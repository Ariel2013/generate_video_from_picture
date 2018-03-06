package com.patientx;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FfmpegUtil
 * com.vlife.timerbox.ffmpeg.FfmpegUtil
 * @author XuShenglai  <br/>
 * create at 2018年2月28日 下午2:37:44
 */
public class FfmpegUtil {

	private static Logger log = LoggerFactory.getLogger("FfmpegUtil");
	
	/**
	 * 线程数
	 */
	private static final int THREAD_COUNT = 8;
//	/**
//	 * 单个子视频时间，单位秒
//	 */
//	private static final int SINGLE_TIME = 3;
	/**
	 * 视频宽高
	 */
	private static final int WIDTH = 720;
	private static final int HEIGHT = 538;
	
	/**
	 * 平移方向
	 */
	private static final int DIRECTION_LEFT = 1;
	
	private static final int DIRECTION_RIGHT = 2;
	
	private static final int DIRECTION_UP = 3;
	
	private static final int DIRECTION_DOWN = 4;
	
	private static final String FONT_FILE_HEITI = "/Users/xushenglai/Downloads/yueheiti/heiti.otf";
	
	private static final String OUTPUT_PATH = "output/";
	
	private static final String FFMPEG_EXEC_PATH = "/usr/local/bin/ffmpeg";
	
	private static final String FILE_NAME_LIST = "names.txt";
	
	private static final String RESULT_NAME = "result.mp4";
	
	public static void exeCmd(String commandStr) {
		BufferedReader br = null;
		try {
			log.info(commandStr);
			Process p = Runtime.getRuntime().exec(commandStr);
			InputStreamReader ir = new InputStreamReader(p.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line;
			while ((line = input.readLine()) != null) {
				log.info(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void build(String folderPath, String title, String time, boolean autoOrient) throws IOException {
		File folder = new File(folderPath);
		List<File> files = new ArrayList<File>();
		handleFiles(folder, files, autoOrient);
		String outputPath = folderPath + OUTPUT_PATH;
		handleBaseFile(outputPath);
		StringBuilder nameStr = new StringBuilder();
		float totalTime = 0.0f;
		for (int i = 0; i < files.size(); i++) {
			int type = new Random().nextInt(5) + 1;
			String commandStr = null;
			String fileFullPath = folderPath + files.get(i).getName();
			String outFullPath = outputPath + i + ".mp4";
			
			int singleTimeType = new Random().nextInt(2);
			float singleTime = 0.0f;
			if (singleTimeType == 1) {
				singleTime = 1.5f;
			} else {
				singleTime = 2.5f;
			}
			totalTime += singleTime;
			boolean vertical = getImgWidth(files.get(i)) == 3016;
			if (type == 5) {
				commandStr = buildZoomOutStr(fileFullPath, outFullPath, singleTime, vertical);
//				commandStr = buildFadeInStr(fileFullPath, outFullPath, singleTime, vertical);
//			} else if (type == 6) {
			} else {
				commandStr = buildTranslationStr(fileFullPath, outFullPath, type, singleTime, vertical);
			}
			exeCmd(commandStr);
			nameStr.append("file '" + i + ".mp4'\n");
		}
		try {
			addTitle(outputPath + "0.mp4", title, FONT_FILE_HEITI);
			addTime(outputPath + "0.mp4", time, FONT_FILE_HEITI, outputPath);
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}
		try {
			FileUtil.writeFileContent(outputPath + FILE_NAME_LIST, nameStr.toString());
		} catch (IOException e) {
			log.error("", e);
		}
		mergeVideos(outputPath);
		mergeMusic(outputPath, folderPath + "music.mp3", files.size(), totalTime);
//		deleteTempFiles(outputPath);
		log.info("finish.");
	}

	private static void handleFiles(File folder, List<File> files, boolean autoOrient) {
		if (folder.exists() && folder.isDirectory()) {
			File[] listFiles = folder.listFiles();
			if (listFiles != null && listFiles.length > 0) {
				for (int i = 0; i < listFiles.length; i++) {
					files.add(listFiles[i]);
				}
			}
		} else {
			log.error("data error.");
		}
		if (SetUtils.isEmpty(files)) {
			log.error("data error.");
		}
		Iterator<File> iterator = files.iterator();
		while (iterator.hasNext()) {
			File file = (File) iterator.next();
			if (file.isDirectory() || (!file.getName().endsWith(".jpg") && !file.getName().endsWith(".png"))) {
				iterator.remove();
			} else if (autoOrient) {
				exeCmd("magick " + file.getAbsolutePath() + " -auto-orient " + file.getAbsolutePath());
			}
		}
		if (SetUtils.isEmpty(files)) {
			log.error("data error.");
		}
	}

	private static void handleBaseFile(String outputPath) throws IOException {
		File outputDir = new File(outputPath);
		if (outputDir.exists()) {
			outputDir.delete();
		}
		exeCmd("mkdir " + outputPath);
		File outputFileName = new File(outputPath + FILE_NAME_LIST);
		if (outputFileName.exists()) {
			outputFileName.delete();
		}
		outputFileName.createNewFile();
		File resultFile = new File(outputPath + RESULT_NAME);
		if (resultFile.exists()) {
			resultFile.delete();
		}
	}
	
	/**
	 * 转场动画，渐现
	 * @param imagePath
	 * @param outputPath
	 * @param singleTime 
	 * @param vertical 
	 * @return
	 * Create by XuShenglai at 2018年2月28日 下午3:44:16
	 */
	private static String buildFadeInStr(String imagePath, String outputPath, float singleTime, boolean vertical) {
		String width = String.valueOf(WIDTH);
		String height = String.valueOf(HEIGHT);
		if (vertical) {
			int realWidth = (int) (HEIGHT * HEIGHT / WIDTH);
			width = String.valueOf(realWidth);
			height += ",pad=" + WIDTH + ":" + HEIGHT + ":" + (WIDTH-realWidth) / 2 + ":0:black";
		}
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -loop 1 -y -r 25 -i " + imagePath + " -vf scale='" + width + "x" + height + "' -vf fade=in:0:25 -t " + singleTime + " " + outputPath;
		return commandStr;
	}
	
	/**
	 * 转场动画，放大
	 * @param imagePath
	 * @param outputPath
	 * @param singleTime 
	 * @param vertical 
	 * @return
	 * Create by XuShenglai at 2018年2月28日 下午3:44:04
	 */
	private static String buildZoomOutStr(String imagePath, String outputPath, float singleTime, boolean vertical) {
		String width = String.valueOf(WIDTH);
		String height = String.valueOf(HEIGHT) + "'";
		if (vertical) {
			int realWidth = (int) (HEIGHT * HEIGHT / WIDTH);
			width = String.valueOf(realWidth);
			height += ",pad='" + WIDTH + ":" + HEIGHT + ":" + (WIDTH-realWidth) / 2 + ":0:black'";
		}
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -y -r 25 -i " + imagePath + " -vf zoompan=z='zoom+0.0003':s='" + width + "*" + height + " -t "
				+ singleTime + " " + outputPath;
		return commandStr;
	}
	
	/**
	 * 转场动画，平移
	 * @param imagePath
	 * @param outputPath
	 * @param direction
	 * @param singleTime 
	 * @param vertical 
	 * @return
	 * Create by XuShenglai at 2018年2月28日 下午3:43:28
	 */
	private static String buildTranslationStr(String imagePath, String outputPath, int direction, float singleTime, boolean vertical) {
		String directionStr = null;
		switch (direction) {
		case DIRECTION_LEFT:
			directionStr = "x='if(eq(x,0),100,x-1)'";
			break;
		case DIRECTION_RIGHT:
			directionStr = "x='x+1'";
			break;
		case DIRECTION_UP:
			directionStr = "y='if(eq(y,0),200,y-1)'";
			break;
		case DIRECTION_DOWN:
			directionStr = "y='y+1'";
			break;
		default:
			break;
		}
		String width = String.valueOf(WIDTH);
		String height = String.valueOf(HEIGHT) + "'";
		if (vertical) {
			int realWidth = (int) (HEIGHT * HEIGHT / WIDTH);
			width = String.valueOf(realWidth);
			height += ",pad='" + WIDTH + ":" + HEIGHT + ":" + (WIDTH-realWidth) / 2 + ":0:black'";
		}
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -y -r 25 -i " + imagePath
				+ " -vf zoompan=z=1.1:" + directionStr + ":s='" + width + "*" + height + " -t " + singleTime + " " + outputPath;
		return commandStr;
	}
	
	/**
	 * 增加过渡文字
	 * @param inputPath
	 * @param textContent
	 * @param fontFilePath
	 * @throws UnsupportedEncodingException
	 * Create by XuShenglai at 2018年2月28日 下午3:43:02
	 */
	private static void addTitle(String inputPath, String textContent, String fontFilePath) throws UnsupportedEncodingException {
		textContent = new String(textContent.getBytes(), "utf-8");
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -y -i " + inputPath + " -vf drawtext=" + fontFilePath + ":text='"
				+ textContent + "':x=(w-text_w)/2:y=(h-text_h)/2:fontsize='max(40\\,110-500*t)':alpha='150*t':fontcolor=white:shadowy=2 temp.mp4";
		exeCmd(commandStr);
		String rmString = "rm " + inputPath;
		String renameStr = "mv temp.mp4 " + inputPath;
		exeCmd(rmString);
		exeCmd(renameStr);
	}
	
	/**
	 * 增加时间文字
	 * @param inputPath
	 * @param textContent
	 * @param fontFilePath
	 * @param outputPath
	 * @throws UnsupportedEncodingException
	 * Create by XuShenglai at 2018年3月1日 下午2:35:20
	 */
	private static void addTime(String inputPath, String textContent, String fontFilePath, String outputPath)
			throws UnsupportedEncodingException {
		textContent = new String(textContent.getBytes(), "utf-8");
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -y -i " + inputPath + " -vf drawtext=" + fontFilePath + ":text='"
				+ textContent + "':x=(w-text_w)/2:y='(h-text_h)/2+50':fontsize=28:alpha='if(lt(t,0.25),0,t-0.25)':fontcolor=white:shadowy=2 "
				+ outputPath + "temp.mp4";
		exeCmd(commandStr);
		String rmString = "rm " + inputPath;
		String renameStr = "mv " + outputPath + "temp.mp4 " + inputPath;
		exeCmd(rmString);
		exeCmd(renameStr);
	}
	
	/**
	 * 合并所有子视频
	 * @param outputPath
	 * Create by XuShenglai at 2018年3月1日 下午2:35:33
	 */
	private static void mergeVideos(String outputPath) {
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -y -f concat -i " + outputPath + FILE_NAME_LIST + " -c copy "
				+ outputPath + "nomusic.mp4";
		exeCmd(commandStr);
	}
	
	/**
	 * 添加音乐
	 * @param outputPath
	 * @param musicFilePath
	 * @param fileCount
	 * Create by XuShenglai at 2018年3月1日 下午2:35:50
	 * @param totalTime 
	 */
	private static void mergeMusic(String outputPath, String musicFilePath, int fileCount, float totalTime) {
		String commandStr = FFMPEG_EXEC_PATH + " -threads " + THREAD_COUNT + " -y -i " + outputPath + "nomusic.mp4 -i " + musicFilePath
				+ " -t " + totalTime + " " + outputPath + RESULT_NAME;
		exeCmd(commandStr);
	}
	
	/**
	 * 删除所有临时文件
	 * @param outputPath
	 */
	private static void deleteTempFiles(String outputPath) {
		File folder = new File(outputPath);
		if (folder.exists() && folder.isDirectory()) {
			File[] listFiles = folder.listFiles();
			if (listFiles != null && listFiles.length > 0) {
				for (int i = 0; i < listFiles.length; i++) {
					if (listFiles[i].exists() && !listFiles[i].getName().equals(RESULT_NAME)) {
						listFiles[i].delete();
					}
				}
			}
		}
	}
	
	 /**
     * 获取图片宽度
     * @param file  图片文件
     * @return 宽度
     */
    public static int getImgWidth(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = javax.imageio.ImageIO.read(is);
            ret = src.getWidth(null); // 得到源图宽
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
	
}
