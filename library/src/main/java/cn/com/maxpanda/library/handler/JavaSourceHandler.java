package cn.com.maxpanda.library.handler;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author MaxPanda
 * @version 1.0.0
 * date 2020年11月02日 16:24
 * description 用于测试 java 源码生成
 */
public class JavaSourceHandler {
	/**
	 * 生成java文件
	 */
	public static void generateFile(Filer filer, String path) {
		BufferedWriter writer = null;
		try {
			// 1. 创建java源文件
			JavaFileObject sourceFile = filer.createSourceFile(path);
			int period = path.lastIndexOf('.');
			String myPackage = period > 0 ? path.substring(0, period) : null;
			String clazz = path.substring(period + 1);
			// 2. 写入代码
			writer = new BufferedWriter(sourceFile.openWriter());
			if (myPackage != null) {
				writer.write("package " + myPackage + ";\n\n");
			}
			writer.write("public class " + clazz + " {\n");
			writer.write("    private static final String TEST;\n\n");
			writer.write("    static {\n");
			writer.write("        TEST = \"hello world\";\n\n");
			writer.write("    }\n\n");
			writer.write("    public static String getTest() {\n");
			writer.write("        return TEST;\n");
			writer.write("    }\n\n");
			writer.write("}\n");
		} catch (IOException e) {
			throw new RuntimeException("Could not write source for " + path, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

