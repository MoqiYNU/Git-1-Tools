package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moqi
 * 定义过程树中节点,包括idf,节点类型及孩子节点
 */
public class Node {
	
	private String idf;
	private String type;
	//Note:用idf标识
	private List<String> chaNodes;
	
	public Node() {
		chaNodes = new ArrayList<>();
	}
	
	//添加孩子节点
	public void addChaNode(String node) {
		chaNodes.add(node);
	}
	public void addChaNodes(List<String> nodes) {
		chaNodes.addAll(nodes);
	}
	
	public String getIdf() {
		return idf;
	}
	public void setIdf(String idf) {
		this.idf = idf;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getChaNodes() {
		return chaNodes;
	}
	public void setChaNodes(List<String> chaNodes) {
		this.chaNodes = chaNodes;
	}

}
