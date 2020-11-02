package cn.com.maxpanda.library.processor;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
	/**
	 * 语法树
	 */
	private Trees trees;

	/**
	 * 树节点创建工具类
	 */
	private TreeMaker treeMaker;

	/**
	 * 命名工具类
	 */
	private Names names;

	JavacElements javacElements;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.trees = Trees.instance(processingEnv);
		Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
		this.treeMaker = TreeMaker.instance(context);
		this.names = Names.instance(context);
		this.javacElements = (JavacElements) processingEnv.getElementUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!roundEnv.processingOver()) {
			for (Element element : roundEnv.getRootElements()) {
				if (element.getKind().isClass()) {
					// 获取语法树
					JCTree tree = (JCTree) trees.getTree(element);
					// 使用TreeTranslator遍历
					tree.accept(new LombokTreeTranslator(treeMaker, names, javacElements));
				}
			}
		}
		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
