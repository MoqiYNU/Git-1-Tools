package toolkits.utils.block;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import toolkits.def.petri.Flow;
import toolkits.def.petri.ProNet;
import toolkits.utils.file.DotUtils;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * 定义与过程树相关的Utils
 */
public class ProTreeUtils {
	
	private GenProTree genProTree;
	private DotUtils dotUtils;
	
	public ProTreeUtils() {
		genProTree = new GenProTree();
		dotUtils = new DotUtils();
	}
	
	/****************************从过程网中生成过程树 ******************************/
	
	public ProTree genProTreeFromProNet(ProNet proNet, int index) throws Exception {
		
		//1.获取proNet内网及移除并发块中冗余库所
		InnerNet interNet = proNet.getInnerNet();
		InnerNet petri = rovReduPlaces(interNet);
		//2.获得初始过程树
		ProTree initProTree = genProTree.compute(petri);
		//dotUtils.pt2Dot(initProTree, proNet.getTranLabelMap(), "pt" + index);
		//3.压缩初始过程树
		ProTree proTree = compress(initProTree);
		dotUtils.pt2Dot(proTree, proNet.getTranLabelMap(), "cpt" + index);
		System.out.println("\n");
		return proTree;
		
	}
	
	/************************将过程树分解为片段 (自顶向下,避免生成重复片段)**************************/
	
	public List<ProTree> genFragments(ProTree proTree/*, InnerNet innerNet*/) throws Exception {
		
		//根据XOR节点分解后得到片段
		List<ProTree> fragments = new ArrayList<>();
		
		//即将访问的队列visitingQueue并将proTree入队
		Queue<ProTree> visitingQueue = new LinkedList<>(); 
		visitingQueue.offer(proTree);
		//迭代计算
	    while(visitingQueue.size() > 0){
	    	
	    	ProTree proTreeFrom = visitingQueue.poll();
	    	
	    	Node xorNode = isDecompose(proTreeFrom);
	    	
	    	if (xorNode == null) {// 1.proTreeFrom不可分解,则直接添加到片段中并跳出本次循环
	    		//Note:最终片段需压缩处理
				fragments.add(compress(proTreeFrom));
				continue;
			}
	    	
	    	// 2.proTreeFrom可分解,则首先获得其父亲节点
			Node xorNodeFather = getFatherNode(proTreeFrom, xorNode);
			
			if (xorNodeFather == null) {// 2.1若父亲节点为空,则以每个孩子节点为根生成一颗过程树
				
				List<String> chaNodes = xorNode.getChaNodes();
				for (String chaNode : chaNodes) {
					List<Node> desc = getDesc(proTree, chaNode);
					ProTree depProTree = new ProTree();//分解后过程树
					depProTree.setNodes(desc);
					visitingQueue.offer(depProTree);		
				}
				
			}else {// 2.2若父亲节点非空,则以xorNode节点的每个孩子节点生成一颗对应片段
				
				//System.out.println("test............................");
				
				List<String> chaNodes = xorNode.getChaNodes();
				for (String chaNode : chaNodes) {
					
					/*if (chaNode.getType().equals("leaf")) {
                		System.out.println("chaNode: " + innerNet.getTranLabelMap().get(chaNode.getIdf()));
					}else {
						System.out.println("chaNode: " + chaNode.getIdf());
					}*/
					
					//2.2.1 构建片段中节点及其关联孩子节点集
					List<Node> depNodes = new ArrayList<Node>();
					for (Node node : proTreeFrom.getNodes()) {
						//1.是父亲节点,则将其孩子节点中xorNode替换为chaNode
						if (node.getIdf().equals(xorNodeFather.getIdf())) {
							List<String> updaChaNodesFather = new ArrayList<String>();
							List<String> chaNodesFather = xorNodeFather.getChaNodes();
							for (String chaNodeFather : chaNodesFather) {
								if (chaNodeFather.equals(xorNode.getIdf())) {
									//System.out.println("add node: " + chaNode.getIdf());
									updaChaNodesFather.add(chaNode);
								}else {
									updaChaNodesFather.add(chaNodeFather);
								}
							}
							//Note:必须新建,否则出错
							Node newXorNodeFather = new Node();
							newXorNodeFather.setIdf(xorNodeFather.getIdf());
							newXorNodeFather.setType(xorNodeFather.getType());
							newXorNodeFather.setChaNodes(updaChaNodesFather);
							depNodes.add(newXorNodeFather);
						}else {//2.否则直接添加
							depNodes.add(node);
						}
					}
					
					//2.2.2 构建分解过程树
					ProTree depProTree = new ProTree();
					depProTree.setNodes(depNodes);
					
					//2.2.3 移除xorNode节点
                    depProTree.removeNode(xorNode);
                    
                    //2.2.4 移除非chaNode节点及其子孙节点
                    List<String> restNodes = getRestNodes(chaNodes, chaNode);
                    for (String restNode : restNodes) {
                    	List<Node> desc = getDesc(proTree, restNode);
						for (Node descNode : desc) {
							//System.out.println("remove node:" + descNode.getIdf());
							depProTree.removeNode(descNode);
						}
					}
                    
                   /* List<Node> nodes = depProTree.getNodes();
                    for (Node node : nodes) {
                    	if (node.getType().equals("leaf")) {
                    		System.out.println("updated nodes: " + innerNet.getTranLabelMap().get(node.getIdf()));
						}else {
							System.out.println("updated nodes: " + node.getIdf());
						}
					}*/
          
					//2.2.4添加第j个分解树到访问队列
                    visitingQueue.offer(depProTree);
					
				}
			}
		}
	    return fragments;
	    
	}
	
	//获取chaNodes中除node外节点集
	public List<String> getRestNodes(List<String> chaNodes, String node) {
		List<String> restNodes = new ArrayList<String>();
		for (String chaNode : chaNodes) {
			if (chaNode.equals(node)) {
				continue;
			}
			restNodes.add(chaNode);
		}
		return restNodes;
		
	}
	
	//获取以node为根的所有叶子子孙节点的Idf
	public List<String> getLeafDesc(ProTree proTree, Node node) {
		
		List<String> desc = new ArrayList<>();
		Queue<Node> queue = new LinkedList<>();
		queue.add(node);
		while(queue.size() > 0){
			Node tempNode = queue.poll();
			if (tempNode.getType().equals("leaf")) {
				desc.add(tempNode.getIdf());
			}else {
				List<String> chaNodes = tempNode.getChaNodes();
				for (String chaNode : chaNodes) {
					queue.add(getNodeByIdf(proTree, chaNode));
				}
			}
		}
		return desc;
		
	}
	
	//获取idf对应过程树中的结点
	public Node getNodeByIdf(ProTree proTree, String idf) {
		List<Node> nodes = proTree.getNodes();
		for (Node node : nodes) {
			String tempIdf = node.getIdf();
			if (tempIdf.equals(idf)) {
				return node;
			}
		}
		return null;
		
	}
	
	//获取以node为根的所有子孙节点
	public List<Node> getDesc(ProTree proTree, String nodeIdf) {
		List<Node> desc = new ArrayList<Node>();
		Node node = getNodeByIdf(proTree, nodeIdf);
		desc.add(node);//Note:包含自己
		Queue<Node> queue = new LinkedList<>();
		List<String> chaNodes = node.getChaNodes();
		for (String chaNodeIdf : chaNodes) {
			Node chaNode = getNodeByIdf(proTree, chaNodeIdf);
			desc.add(chaNode);
			queue.add(chaNode);
		}
		while(queue.size() > 0){
			Node tempNode = queue.poll();
			if (tempNode.getType().equals("leaf")) {
				continue;
			}
			List<String> descNodes = tempNode.getChaNodes();
			for (String descIdf : descNodes) {
				Node desdNode = getNodeByIdf(proTree, descIdf);
				desc.add(desdNode);
				queue.add(desdNode);
			}
		}
		return desc;
	}
	
	//获取xorNode的父亲节点
	public Node getFatherNode(ProTree proTree, Node xorNode) {
		List<Node> nodes = proTree.getNodes();
		for (Node node : nodes) {
			List<String> chaNodes = node.getChaNodes();
			if (chaNodes.contains(xorNode.getIdf())) {
				return node;
			}
		}
		return null;
	}	
		
	//判断node是否为孩子节点
	public boolean isChaNode(List<Node> chaNodes, Node node) {
		for (Node chaNode : chaNodes) {
			if (chaNode.getIdf().equals(node.getIdf())) {
				return true;
			}
		}
		return false;
	}
	
	//判断proTree是否可以分解,即存在XOR节点
	public Node isDecompose(ProTree proTree) {
		List<Node> nodes = proTree.getNodes();
		int size = nodes.size();
		//Note:从根开始访问,避免生成重复的片段(测试B31)
		for (int i = size-1; i >= 0; i--) {
			Node node = nodes.get(i);
			String type = node.getType();
			if (type.equals("XOR")) {
				return node;
			}
		}
		return null;
	}
	
	//获取proTree中SEQ节点集
	public List<Node> getSEQNode(ProTree proTree) {
		
		List<Node> SEQNodes = new ArrayList<Node>();
		List<Node> nodes = proTree.getNodes();
		for (Node node : nodes) {
			String type = node.getType();
			if (type.equals("SEQ")) {
				SEQNodes.add(node);
			}
		}
		return SEQNodes;
		
	}
	

	/********************************预处理内网**********************************/
	
	//在生成过程树之前,先移除内网并发块中冗余库所
	public InnerNet rovReduPlaces(InnerNet net) {
		
		InnerNet interNet = new InnerNet();//待生成内网
		
		List<String> reduPlaces = new ArrayList<String>();
		List<String> places = net.getPlaces();
		for (String place : places) {
			List<String> fromTrans = PetriUtils.getPreSet(place, net.getFlows());
			List<String> toTrans = PetriUtils.getPostSet(place, net.getFlows());
			if (fromTrans.size() == 1 && toTrans.size() == 1) {
				String fromTran = fromTrans.get(0);
				String toTran = toTrans.get(0);
				//place在并发块中
				if (PetriUtils.getPostSet(fromTran, net.getFlows()).size() > 1
						&& PetriUtils.getPreSet(toTran, net.getFlows()).size() > 1) {
					reduPlaces.add(place);
				}
			}
		}
		
		//1.生成interNet中的流集
		List<Flow> noReduFlows = new ArrayList<Flow>();
		List<Flow> flows = net.getFlows();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (reduPlaces.contains(from) || reduPlaces.contains(to)) {
				continue;
			}else {
				noReduFlows.add(flow);
			}
		}
		
		//2.生成interNet中的库所
        for (Flow flow : noReduFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (places.contains(from)) {
				interNet.addPlace(from);
			}
			if (places.contains(to)) {
				interNet.addPlace(to);
			}
		}
        
        //3.生成interNet中的变迁
        List<String> trans = net.getTrans();
        for (Flow flow : noReduFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (trans.contains(from)) {
				interNet.addTran(from);
			}
			if (trans.contains(to)) {
				interNet.addTran(to);
			}
		}
        
        //返回interNet
        interNet.setSource(net.getSource());
        interNet.setSink(net.getSink());
        interNet.setFlows(noReduFlows);
        interNet.setTranLabelMap(net.getTranLabelMap());
		return interNet;
		
	}
	
	/********************************压缩过程树**********************************/
	
	//将过程树进行压缩,以保证其中每个孩子节点与父亲节点的类型不同
	public ProTree compress(ProTree proTree) {
		
		//即将访问的队列visitingQueue并将proTree入队
		Queue<ProTree> visitingQueue = new LinkedList<>(); 
		visitingQueue.offer(proTree);
		
		//迭代计算
	    while(visitingQueue.size() > 0){
	    	
	    	ProTree proTreeFrom = visitingQueue.poll();
	    	
	    	// 1.若proTreeFrom不可压缩,则直接返回
		    Node compNode = isCompress(proTreeFrom);
		    if (compNode == null) {
				return proTreeFrom;
			}
		    
		    // 2.可以压缩,则新建过程树(Note:必须新建避免出错,因为更新)
		    Node compNodeFather = getFatherNode(proTreeFrom, compNode);
		    //存储压缩过程树中的节点
			List<Node> compNodes = new ArrayList<Node>();
			for (Node node : proTreeFrom.getNodes()) {
				if (node.getIdf().equals(compNode.getIdf())) {//跳过待压缩节点
					continue;
				}
				if (node.getIdf().equals(compNodeFather.getIdf())) {//重新排列父亲节点下面的孩子节点
					//存储压缩后父节点的孩子节点
					List<String> updaChaNodesFather = new ArrayList<String>();
					for (String chaNode : compNodeFather.getChaNodes()) {
						if (chaNode.equals(compNode.getIdf())) {
							updaChaNodesFather.addAll(compNode.getChaNodes());
						}else {
							updaChaNodesFather.add(chaNode);
						}
					}
					//Note:必须新建(因为更新),否则出错
					Node newCompNodeFather = new Node();
					newCompNodeFather.setIdf(compNodeFather.getIdf());
					newCompNodeFather.setType(compNodeFather.getType());
					newCompNodeFather.setChaNodes(updaChaNodesFather);
					compNodes.add(newCompNodeFather);
				}else {
					compNodes.add(node);
				}
			}
			
			//创建新压缩过程树
			ProTree compProTree = new ProTree();
			compProTree.setNodes(compNodes);
			visitingQueue.offer(compProTree);
			
	    }
		return null;
		
	}
	
	//判断pt能否压缩,即存在一个节点且其类型与其父亲节点一致
	public Node isCompress(ProTree pt) {
		List<Node> nodes = pt.getNodes();
		for (Node node : nodes) {//叶子节点无须压缩
			if (node.getType().equals("leaf")) {
				continue;
			}
			Node nodeFather = getFatherNode(pt, node);
			if (nodeFather == null) {//父节点为空无须压缩
				continue;
			}
			if (nodeFather.getType().equals(node.getType())) {
				return node;
			}
		}
		return null;
		
	}
	
}
