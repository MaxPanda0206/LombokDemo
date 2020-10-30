package cn.com.maxpanda.library.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

/**
 * @author MaxPanda
 * @version 1.0.0
 * date 2020年10月30日 15:56
 * description TODO
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"cn.com.maxpanda.library.annotation.*"})
public class LombokProcessor extends AbstractProcessor {
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("LombokProcessor process");
		for (TypeElement element : annotations) {
			roundEnv.getElementsAnnotatedWith(element).forEach(it -> {
				generateFile("cn.com.maxpanda.TestJavaSourceGenerate");
			});
		}
		return false;
	}

	/**
	 * 生成java文件
	 */
	private void generateFile(String path) {
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
