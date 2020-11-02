package cn.com.maxpanda.library.processor;

import cn.com.maxpanda.library.annotation.Getter;
import cn.com.maxpanda.library.annotation.Setter;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/**
 * @author MaxPanda
 * @version 1.0.0
 * date 2020年11月2日 15:56
 * description TODO
 */
public class LombokTreeTranslator extends TreeTranslator {

	private final TreeMaker treeMaker;
	private final Names names;
	JavacElements javacElements;

	/**
	 * 需要插入的Getter和Setter方法
	 */
	private List<JCTree> getters = List.nil();
	private List<JCTree> setters = List.nil();

	public LombokTreeTranslator(TreeMaker treeMaker, Names names, JavacElements javacElements) {
		this.treeMaker = treeMaker;
		this.names = names;
		this.javacElements = javacElements;
	}

	/**
	 * 遍历到类的时候执行
	 */
	@Override
	public void visitClassDef(JCClassDecl jcClassDecl) {
		super.visitClassDef(jcClassDecl);
		// 插入getter方法
		if (!getters.isEmpty()) {
			jcClassDecl.defs = jcClassDecl.defs.appendList(this.getters);
		}
		// 插入setter方法
		if (!setters.isEmpty()) {
			jcClassDecl.defs = jcClassDecl.defs.appendList(this.setters);
		}
		this.result = jcClassDecl;
	}

	/**
	 * 遍历成员遍历，参数等等
	 */
	@Override
	public void visitVarDef(JCVariableDecl jcVariableDecl) {
		super.visitVarDef(jcVariableDecl);
		JCModifiers modifiers = jcVariableDecl.getModifiers();
		List<JCAnnotation> annotations = modifiers.getAnnotations();
		if (annotations == null || annotations.size() <= 0) {
			return;
		}
		for (JCAnnotation annotation : annotations) {
			if (Getter.class.getName().equals(annotation.type.toString())) {
				// 生成getter方法
				JCMethodDecl getterMethod = createGetterMethod(jcVariableDecl);
				this.getters = this.getters.append(getterMethod);
			}
			if (Setter.class.getName().equals(annotation.type.toString())) {
				// 生成setter方法
				JCMethodDecl setterMethod = createSetterMethod(jcVariableDecl);
				this.setters = this.setters.append(setterMethod);
			}
		}
	}

	/**
	 * 创建Getter方法
	 */
	private JCTree.JCMethodDecl createGetterMethod(JCTree.JCVariableDecl jcVariableDecl) {
		JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);//public
		JCTree.JCExpression retrunType = jcVariableDecl.vartype;//xxx
		Name name = getterMethodName(jcVariableDecl);// getXxx
		JCTree.JCStatement jcStatement = // retrun this.xxx
				treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.name));

		List<JCTree.JCStatement> jcStatementList = List.nil();
		jcStatementList = jcStatementList.append(createPrintStatement());
		jcStatementList = jcStatementList.append(jcStatement);
		JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatementList);//构建代码块
		List<JCTree.JCTypeParameter> methodGenericParams = List.nil();//泛型参数列表
		List<JCTree.JCVariableDecl> parameters = List.nil();//参数列表
		List<JCTree.JCExpression> throwsClauses = List.nil();//异常抛出列表
		return treeMaker.MethodDef(jcModifiers, name, retrunType, methodGenericParams, parameters, throwsClauses, jcBlock, null);
	}

	/**
	 * 创建Setter方法
	 */
	private JCTree.JCMethodDecl createSetterMethod(JCTree.JCVariableDecl jcVariableDecl) {
		JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);//public
		JCTree.JCExpression returnType = treeMaker.TypeIdent(TypeTag.VOID);//或 treeMaker.Type(new Type.JCVoidType())
		Name name = setterMethodName(jcVariableDecl);// setXxx()
		List<JCTree.JCVariableDecl> parameters = List.nil();//参数列表
		JCTree.JCVariableDecl param = treeMaker.VarDef(
				treeMaker.Modifiers(Flags.PARAMETER), jcVariableDecl.name, jcVariableDecl.vartype, null);
		param.pos = jcVariableDecl.pos;//设置形参这一句不能少，不然会编译报错(java.lang.AssertionError: Value of x -1)
		parameters = parameters.append(param);
		//this.xxx = xxx;  setter方法中的赋值语句
		JCTree.JCStatement jcStatement = treeMaker.Exec(treeMaker.Assign(
				treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.name),
				treeMaker.Ident(jcVariableDecl.name)));
		List<JCTree.JCStatement> jcStatementList = List.nil();
        jcStatementList = jcStatementList.append(createPrintStatement());
		jcStatementList = jcStatementList.append(jcStatement);
		JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatementList);//代码块
		List<JCTree.JCTypeParameter> methodGenericParams = List.nil();//泛型参数列表
		List<JCTree.JCExpression> throwsClauses = List.nil();//异常抛出列表
		//最后构建setter方法
		return treeMaker.MethodDef(jcModifiers, name, returnType, methodGenericParams, parameters, throwsClauses, jcBlock, null);
	}

	private JCTree.JCStatement createPrintStatement() {
		return treeMaker.Exec(
				treeMaker.Apply(
						List.<JCExpression>nil(),
						treeMaker.Select(
								treeMaker.Select(
										treeMaker.Ident(
												javacElements.getName("System")
										),
										javacElements.getName("out")
								),
								javacElements.getName("println")
						),
						List.<JCExpression>of(
								treeMaker.Literal("Hello, world!!!")
						)
				)
		);
	}

	private Name getterMethodName(JCTree.JCVariableDecl jcVariableDecl) {
		String varName = jcVariableDecl.name.toString();
		Name name = names.fromString("get" + varName.substring(0, 1).toUpperCase() + varName.substring(1, varName.length()));
		return name;
	}

	private Name setterMethodName(JCTree.JCVariableDecl jcVariableDecl) {
		String varName = jcVariableDecl.name.toString();
		Name name = names.fromString("set" + varName.substring(0, 1).toUpperCase() + varName.substring(1, varName.length()));
		return name;
	}
}
