package info.mabin.wce.supportlibrary;
import info.mabin.wce.manager.Logger;
import info.mabin.wce.manager.ComponentAbstract.ComponentContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Dispacher
 */
public class ServletDispatcher extends HttpServlet{
	private static final long serialVersionUID = Constant.VERSION_COMPONENTSUPPORT_VERSIONCODE;

	private static ComponentContext context;
	private static MappingNode mapMappingTree = new MappingNode();
	private static Logger logger;




	/**
	 * GET Request
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException ,IOException {
		response.setCharacterEncoding("UTF-8");

		String reqUrl = request.getRequestURI();

		MappingNode targetNode = searchMappingNode(mapMappingTree, reqUrl);

		try {
			targetNode.targetMethodGet.invoke(targetNode.targetModule, request, response);
		} catch (IllegalAccessException e) {
			logger.e(e);
			throw new ServletException(e);
		} catch (IllegalArgumentException e) {
			logger.e(e);
			throw new ServletException(e);
		} catch (InvocationTargetException e) {
			logger.e(e);
			throw new ServletException(e);
		} catch (NullPointerException e){
			logger.e("Not Mapped URL: " + reqUrl);
			logger.d(mapMappingTree.toString());
		}
	}



	/**
	 * POST Request
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException ,IOException {
		response.setCharacterEncoding("UTF-8");

		String reqUrl = request.getRequestURI();

		logger.d("RequestURI: " + reqUrl);

		MappingNode targetNode = searchMappingNode(mapMappingTree, reqUrl);
		try {
			targetNode.targetMethodPost.invoke(targetNode.targetModule, request, response);
		} catch (IllegalAccessException e) {
			logger.e(e);
			throw new ServletException(e);
		} catch (IllegalArgumentException e) {
			logger.e(e);
			throw new ServletException(e);
		} catch (InvocationTargetException e) {
			logger.e(e);
			throw new ServletException(e);
		} catch (NullPointerException e){
			logger.e("Not Mapped URL: " + reqUrl);
			logger.d(mapMappingTree.toString());
		}
	};



	private MappingNode searchMappingNode(MappingNode mappingTree, String requestUri){
		logger.d("DefaultUri: " + context.getDefaultUri());
		logger.d("OriginalUrl: " + requestUri + ", ReplacedUrl: " + requestUri.replace(context.getDefaultUri(), "/"));
		
		requestUri = requestUri.replace(context.getDefaultUri(), "/");

		MappingNode handler = mappingTree;
		MappingNode lastFallback = mappingTree.fallBackNode;

		String[] parsedPattern = requestUri.split("/");

		int i = 0;
		for(String fragment: parsedPattern){
			MappingNode tmpNode = null;

			tmpNode = handler.childNodeFixed.get(fragment);

			if(tmpNode == null){
				if(i <= parsedPattern.length){
					if(handler.variableNode == null){
						return lastFallback;
					} else {
						handler = handler.variableNode;
					}
				} else {
					return lastFallback;
				}
			} else {
				handler = tmpNode;
				if(handler.fallBackNode != null){
					lastFallback = handler.fallBackNode;
				}
			}

			i++;
		}

		
		return handler;
	}

	/**
	 * Component Class로부터 필요한 변수를 전달받기위한 메소드
	 * @param context ComponentContext
	 * @param tree URI 매핑 정보
	 * @param logger 로깅을 위한 로거
	 */
	static void setVariables(ComponentContext context, MappingNode tree, Logger logger){
		ServletDispatcher.context = context;
		ServletDispatcher.mapMappingTree = tree;
		ServletDispatcher.logger = logger;
	}
	
	static class MappingNode {
		Map<String, MappingNode> childNodeFixed = new HashMap<String, MappingNode>();
		MappingNode variableNode;
		MappingNode fallBackNode;

		ComponentModule targetModule;
		Method targetMethodGet;
		Method targetMethodPost;
		//TODO: Other HTTP Methods
		
		@Override
		public String toString() {
			String string = super.toString() + "\n";
			string += "Node: Parent\n";
			
			string += toString(0);
			
			return string;
		}
		
		public String toString(int depth){
			String string = "";
			String tab = "";
			for(int i = 0; i < depth; i++){
				tab += "\t";
			}
			
			for(String child: childNodeFixed.keySet()){
				string += tab + "Child: " + child + "\n";
				string += tab + childNodeFixed.get(child).toString(depth + 1);
				string += tab + "\n";
			}
			
			string += tab + "variableNode: \n";
			if(variableNode == null){
				string += tab + "None\n";
			} else {
				string += tab + variableNode.toString(depth + 1);
			}
			string += tab + "\n";
			
			string += tab + "fallBackNode: \n";
			if(fallBackNode == null){
				string += tab + "None\n";
			} else {
				string += tab + fallBackNode.toString(depth + 1);
			}
			string += tab + "\n";
			
			string += tab + "TargetModule: \n";
			if(targetModule == null){
				string += tab + "None\n";
			} else {
				string += tab + targetModule.getClass().getName() + "\n";
			}
			string += tab + "\n";

			string += tab + "TargetMethodGet: \n";
			if(targetMethodGet == null){
				string += tab + "None\n";
			} else {
				string += tab + targetMethodGet.getName() + "\n";
			}
			string += tab + "\n";

			string += tab + "TargetMethodPost: \n";
			if(targetMethodGet == null){
				string += tab + "None\n";
			} else {
				string += tab + targetMethodGet.getName() + "\n";
			}
			string += tab + "\n";
			
			return string;
		}
	}
}