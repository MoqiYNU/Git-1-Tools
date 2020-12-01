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
 * �������������ص�Utils
 */
public class ProTreeUtils {
	
	private GenProTree genProTree;
	private DotUtils dotUtils;
	
	public ProTreeUtils() {
		genProTree = new GenProTree();
		dotUtils = new DotUtils();
	}
	
	/****************************�ӹ����������ɹ����� ******************************/
	
	public ProTree genProTreeFromProNet(ProNet proNet, int index) throws Exception {
		
		//1.��ȡproNet�������Ƴ����������������
		InnerNet interNet = proNet.getInnerNet();
		InnerNet petri = rovReduPlaces(interNet);
		//2.��ó�ʼ������
		ProTree initProTree = genProTree.compute(petri);
		//dotUtils.pt2Dot(initProTree, proNet.getTranLabelMap(), "pt" + index);
		//3.ѹ����ʼ������
		ProTree proTree = compress(initProTree);
		dotUtils.pt2Dot(proTree, proNet.getTranLabelMap(), "cpt" + index);
		System.out.println("\n");
		return proTree;
		
	}
	
	/************************���������ֽ�ΪƬ�� (�Զ�����,���������ظ�Ƭ��)**************************/
	
	public List<ProTree> genFragments(ProTree proTree/*, InnerNet innerNet*/) throws Exception {
		
		//����XOR�ڵ�ֽ��õ�Ƭ��
		List<ProTree> fragments = new ArrayList<>();
		
		//�������ʵĶ���visitingQueue����proTree���
		Queue<ProTree> visitingQueue = new LinkedList<>(); 
		visitingQueue.offer(proTree);
		//��������
	    while(visitingQueue.size() > 0){
	    	
	    	ProTree proTreeFrom = visitingQueue.poll();
	    	
	    	Node xorNode = isDecompose(proTreeFrom);
	    	
	    	if (xorNode == null) {// 1.proTreeFrom���ɷֽ�,��ֱ����ӵ�Ƭ���в���������ѭ��
	    		//Note:����Ƭ����ѹ������
				fragments.add(compress(proTreeFrom));
				continue;
			}
	    	
	    	// 2.proTreeFrom�ɷֽ�,�����Ȼ���丸�׽ڵ�
			Node xorNodeFather = getFatherNode(proTreeFrom, xorNode);
			
			if (xorNodeFather == null) {// 2.1�����׽ڵ�Ϊ��,����ÿ�����ӽڵ�Ϊ������һ�Ź�����
				
				List<String> chaNodes = xorNode.getChaNodes();
				for (String chaNode : chaNodes) {
					List<Node> desc = getDesc(proTree, chaNode);
					ProTree depProTree = new ProTree();//�ֽ�������
					depProTree.setNodes(desc);
					visitingQueue.offer(depProTree);		
				}
				
			}else {// 2.2�����׽ڵ�ǿ�,����xorNode�ڵ��ÿ�����ӽڵ�����һ�Ŷ�ӦƬ��
				
				//System.out.println("test............................");
				
				List<String> chaNodes = xorNode.getChaNodes();
				for (String chaNode : chaNodes) {
					
					/*if (chaNode.getType().equals("leaf")) {
                		System.out.println("chaNode: " + innerNet.getTranLabelMap().get(chaNode.getIdf()));
					}else {
						System.out.println("chaNode: " + chaNode.getIdf());
					}*/
					
					//2.2.1 ����Ƭ���нڵ㼰��������ӽڵ㼯
					List<Node> depNodes = new ArrayList<Node>();
					for (Node node : proTreeFrom.getNodes()) {
						//1.�Ǹ��׽ڵ�,���亢�ӽڵ���xorNode�滻ΪchaNode
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
							//Note:�����½�,�������
							Node newXorNodeFather = new Node();
							newXorNodeFather.setIdf(xorNodeFather.getIdf());
							newXorNodeFather.setType(xorNodeFather.getType());
							newXorNodeFather.setChaNodes(updaChaNodesFather);
							depNodes.add(newXorNodeFather);
						}else {//2.����ֱ�����
							depNodes.add(node);
						}
					}
					
					//2.2.2 �����ֽ������
					ProTree depProTree = new ProTree();
					depProTree.setNodes(depNodes);
					
					//2.2.3 �Ƴ�xorNode�ڵ�
                    depProTree.removeNode(xorNode);
                    
                    //2.2.4 �Ƴ���chaNode�ڵ㼰������ڵ�
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
          
					//2.2.4��ӵ�j���ֽ��������ʶ���
                    visitingQueue.offer(depProTree);
					
				}
			}
		}
	    return fragments;
	    
	}
	
	//��ȡchaNodes�г�node��ڵ㼯
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
	
	//��ȡ��nodeΪ��������Ҷ������ڵ��Idf
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
	
	//��ȡidf��Ӧ�������еĽ��
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
	
	//��ȡ��nodeΪ������������ڵ�
	public List<Node> getDesc(ProTree proTree, String nodeIdf) {
		List<Node> desc = new ArrayList<Node>();
		Node node = getNodeByIdf(proTree, nodeIdf);
		desc.add(node);//Note:�����Լ�
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
	
	//��ȡxorNode�ĸ��׽ڵ�
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
		
	//�ж�node�Ƿ�Ϊ���ӽڵ�
	public boolean isChaNode(List<Node> chaNodes, Node node) {
		for (Node chaNode : chaNodes) {
			if (chaNode.getIdf().equals(node.getIdf())) {
				return true;
			}
		}
		return false;
	}
	
	//�ж�proTree�Ƿ���Էֽ�,������XOR�ڵ�
	public Node isDecompose(ProTree proTree) {
		List<Node> nodes = proTree.getNodes();
		int size = nodes.size();
		//Note:�Ӹ���ʼ����,���������ظ���Ƭ��(����B31)
		for (int i = size-1; i >= 0; i--) {
			Node node = nodes.get(i);
			String type = node.getType();
			if (type.equals("XOR")) {
				return node;
			}
		}
		return null;
	}
	
	//��ȡproTree��SEQ�ڵ㼯
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
	

	/********************************Ԥ��������**********************************/
	
	//�����ɹ�����֮ǰ,���Ƴ��������������������
	public InnerNet rovReduPlaces(InnerNet net) {
		
		InnerNet interNet = new InnerNet();//����������
		
		List<String> reduPlaces = new ArrayList<String>();
		List<String> places = net.getPlaces();
		for (String place : places) {
			List<String> fromTrans = PetriUtils.getPreSet(place, net.getFlows());
			List<String> toTrans = PetriUtils.getPostSet(place, net.getFlows());
			if (fromTrans.size() == 1 && toTrans.size() == 1) {
				String fromTran = fromTrans.get(0);
				String toTran = toTrans.get(0);
				//place�ڲ�������
				if (PetriUtils.getPostSet(fromTran, net.getFlows()).size() > 1
						&& PetriUtils.getPreSet(toTran, net.getFlows()).size() > 1) {
					reduPlaces.add(place);
				}
			}
		}
		
		//1.����interNet�е�����
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
		
		//2.����interNet�еĿ���
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
        
        //3.����interNet�еı�Ǩ
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
        
        //����interNet
        interNet.setSource(net.getSource());
        interNet.setSink(net.getSink());
        interNet.setFlows(noReduFlows);
        interNet.setTranLabelMap(net.getTranLabelMap());
		return interNet;
		
	}
	
	/********************************ѹ��������**********************************/
	
	//������������ѹ��,�Ա�֤����ÿ�����ӽڵ��븸�׽ڵ�����Ͳ�ͬ
	public ProTree compress(ProTree proTree) {
		
		//�������ʵĶ���visitingQueue����proTree���
		Queue<ProTree> visitingQueue = new LinkedList<>(); 
		visitingQueue.offer(proTree);
		
		//��������
	    while(visitingQueue.size() > 0){
	    	
	    	ProTree proTreeFrom = visitingQueue.poll();
	    	
	    	// 1.��proTreeFrom����ѹ��,��ֱ�ӷ���
		    Node compNode = isCompress(proTreeFrom);
		    if (compNode == null) {
				return proTreeFrom;
			}
		    
		    // 2.����ѹ��,���½�������(Note:�����½��������,��Ϊ����)
		    Node compNodeFather = getFatherNode(proTreeFrom, compNode);
		    //�洢ѹ���������еĽڵ�
			List<Node> compNodes = new ArrayList<Node>();
			for (Node node : proTreeFrom.getNodes()) {
				if (node.getIdf().equals(compNode.getIdf())) {//������ѹ���ڵ�
					continue;
				}
				if (node.getIdf().equals(compNodeFather.getIdf())) {//�������и��׽ڵ�����ĺ��ӽڵ�
					//�洢ѹ���󸸽ڵ�ĺ��ӽڵ�
					List<String> updaChaNodesFather = new ArrayList<String>();
					for (String chaNode : compNodeFather.getChaNodes()) {
						if (chaNode.equals(compNode.getIdf())) {
							updaChaNodesFather.addAll(compNode.getChaNodes());
						}else {
							updaChaNodesFather.add(chaNode);
						}
					}
					//Note:�����½�(��Ϊ����),�������
					Node newCompNodeFather = new Node();
					newCompNodeFather.setIdf(compNodeFather.getIdf());
					newCompNodeFather.setType(compNodeFather.getType());
					newCompNodeFather.setChaNodes(updaChaNodesFather);
					compNodes.add(newCompNodeFather);
				}else {
					compNodes.add(node);
				}
			}
			
			//������ѹ��������
			ProTree compProTree = new ProTree();
			compProTree.setNodes(compNodes);
			visitingQueue.offer(compProTree);
			
	    }
		return null;
		
	}
	
	//�ж�pt�ܷ�ѹ��,������һ���ڵ������������丸�׽ڵ�һ��
	public Node isCompress(ProTree pt) {
		List<Node> nodes = pt.getNodes();
		for (Node node : nodes) {//Ҷ�ӽڵ�����ѹ��
			if (node.getType().equals("leaf")) {
				continue;
			}
			Node nodeFather = getFatherNode(pt, node);
			if (nodeFather == null) {//���ڵ�Ϊ������ѹ��
				continue;
			}
			if (nodeFather.getType().equals(node.getType())) {
				return node;
			}
		}
		return null;
		
	}
	
}
