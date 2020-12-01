package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import toolkits.def.petri.Flow;
import toolkits.utils.petri.PetriUtils;

/**
 * @author Moqi
 * ͨ��DFS����������ѡ���
 */
public class GetXorBlock {
	
	//����Ԫѡ�񲿼�
	private List<Block> metaXORBlocks;
	
	public GetXorBlock() {
		metaXORBlocks = new ArrayList<Block>();
	}
	
	public List<Block> getMetaXORBlocks() {
		return metaXORBlocks;
	}
	
	public void compute(InnerNet net, List<Block> loopBlocks) {
		
		//����ǰ����ջ���metaXORBlocks
		metaXORBlocks.clear();
		
		// 1.����ѭ�����д���ڳ����Ļ����������ڵĻ��
		List<String> entryActsInLoop = new ArrayList<String>();
		List<String> exitActsInLoop = new ArrayList<String>();
		for (Block block : loopBlocks) {
			entryActsInLoop.addAll(block.getEntryPost());
			exitActsInLoop.addAll(block.getExitPre());
		}
		
		// 2.���ÿ����֧�������м���Ԫѡ���
		List<String> places = net.getPlaces();
		for (String place : places) {
			//��place����������(�ų���ѭ������)
			List<Flow> splitFlows = computeSplitFlows(place, net.getFlows(), entryActsInLoop);
			if (splitFlows.size() > 1) {//Ϊ��֧����
				Block block = creatMetaXORBlock(place, splitFlows, net.getFlows());
				if (block != null) {
					metaXORBlocks.add(block);
				}
			}
		}
		
	}
	
	//�����place����������(Note:�ų�ѭ������)
	private List<Flow> computeSplitFlows(String place, List<Flow> flows, List<String> entryActsInLoop) {
		
		List<Flow> succFlows = new ArrayList<Flow>();
		for (Flow flow : flows) {
			String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (place.equals(from) && !entryActsInLoop.contains(to)) {
				succFlows.add(flow);
			}
		}
		return succFlows;
	}
	
	//����Ԫѡ���
	public Block creatMetaXORBlock(String place, List<Flow> splitFlows, List<Flow> flows) {
		
		List<String> exits = new ArrayList<String>();
		List<String> exitsGen = new ArrayList<String>();
		for (Flow flow : splitFlows) {//���ÿ����֧�����м���
			String act = flow.getFlowTo();
			List<String> preSet = PetriUtils.getPreSet(act, flows);
			List<String> postSet = PetriUtils.getPostSet(act, flows);
			exitsGen.addAll(postSet);
			if (preSet.size() == 1 && postSet.size() == 1) {
				exits.add(postSet.get(0));
			}
		}
		
		/**C1:���һ��Ԫѡ���XOR2(Ԫѡ����),������Bagȷ�ϲ��ظ�exitsGen��..............................*/
		Bag<String> bagGen = new HashBag<String>(exitsGen);
		Set<String> uniqueSetGen = bagGen.uniqueSet();
		if (uniqueSetGen.size() == 1) {//��Ӧ����������XOR2
			Block block = new Block();
			block.setEntry(place);
			List<String> actsInMetaXor = PetriUtils.getPostSet(place, flows);
			for (String actInMetaXor : actsInMetaXor) {
				block.addEntryPost(actInMetaXor);
				block.addExitPre(actInMetaXor);
			}
			block.setType("XOR");
			block.setExit(exitsGen.get(0));
			//System.out.println("actInMetaXor: " + actsInMetaXor + ", uniqueSetGen: " + uniqueSetGen);
			return block;
		}
		
		/**C2:���XOR1(Ԫѡ��ǰ��),������Bagȷ�ϲ��ظ�exits��...................................*/
		Bag<String> bag = new HashBag<String>(exits);
		Set<String> uniqueSet = bag.uniqueSet();
		if (uniqueSet.size() == 0) {
			return null;
		}else {
			for (String exit : uniqueSet) {
				if (bag.getCount(exit) > 1) {
					if (!isValidExit(exit, flows)) {
						continue;
					}
					Block block = new Block();
					block.setEntry(place);
					List<String> actsInMetaXor = PetriUtils.getPreSet(exit, flows);
					for (String actInMetaXor : actsInMetaXor) {
						block.addEntryPost(actInMetaXor);
						block.addExitPre(actInMetaXor);
					}
					block.setType("XOR");
					block.setExit(exit);
					//System.out.println("actInMetaXor: " + actsInMetaXor + ", uniqueSet: " + uniqueSet);
					return block;
				}else {
					continue;
				}
			}
		}
	    return null;
		
	}
	
	//�ж�exit����Ƿ���Ч,�μ�����������XOR1
	public boolean isValidExit(String exit, List<Flow> flows) {
		
		List<String> entries = new ArrayList<String>();
		List<String> preSet = PetriUtils.getPreSet(exit, flows);
		for (String tran : preSet) {
			List<String> places = PetriUtils.getPreSet(tran, flows);
			entries.addAll(places);
		}
		//����Bagȥ��entries
		Bag<String> bag = new HashBag<String>(entries);
		Set<String> uniqueSet = bag.uniqueSet();
		if (uniqueSet.size() == 1) {
			return true;
		}
		return false;
		
	}
	
	//�����place�����ı�Ǩ��(Note:�ų���ѭ������)
	public List<String> getToTransNotInLoop(String place, InnerNet net, List<Block> loopBlocks) {
		
		List<String> trans = new ArrayList<String>();
		//����ѭ�������д���ڳ����Ļ����������ڵĻ��
		List<String> entryActsInLoop = new ArrayList<String>();
		List<String> exitActsInLoop = new ArrayList<String>();
		for (Block block : loopBlocks) {
			entryActsInLoop.addAll(block.getEntryPost());
			exitActsInLoop.addAll(block.getExitPre());
		}
		//��place����������(�ų���ѭ������)
		List<Flow> splitFlows = computeSplitFlows(place, net.getFlows(), entryActsInLoop);
		for (Flow flow : splitFlows) {
			String tran = flow.getFlowTo();
			trans.add(tran);
		}
		return trans;
	}
	
}
