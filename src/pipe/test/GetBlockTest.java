package pipe.test;

import java.util.List;

import toolkits.utils.block.Block;
import toolkits.utils.block.GetAndBlock;
import toolkits.utils.block.GetLoopBlock;
import toolkits.utils.block.GetSeqBlock;
import toolkits.utils.block.GetXorBlock;
import toolkits.utils.block.InnerNet;
import toolkits.utils.petri.ParsePetri;

public class GetBlockTest {

	public static void main(String[] args) throws Exception {
		
		String path = "C:\\Users\\Moqi\\Desktop\\B4.xml";
		ParsePetri parsePetri = new ParsePetri();
		InnerNet innerNet = parsePetri.parse(path);
		innerNet.setSource("P14");
		innerNet.setSink("P53");
		
		//测试Get循环部件
		GetLoopBlock getLoopBlock = new GetLoopBlock();
		
		List<Block> metaLoopBlocks = getLoopBlock.getMetaLoopBlocks(innerNet);
		int loopIndex = 0;
		for (Block block : metaLoopBlocks) {
			System.out.println("LoopBlock_" + loopIndex + "*****************" + "\n");
			System.out.println("Entry: " + block.getEntry() + "\n");
			System.out.println("Exit: " + block.getExit() + "\n");
			System.out.println("EntryActs: " + block.getEntryPost() + "\n");
			System.out.println("exitActs: " + block.getExitPre() + "\n");
			System.out.println("Type: " + block.getType() + "\n");
			loopIndex ++;
			
		}
		
		//测试Get选择部件
		List<Block> loopBlocks = getLoopBlock.getLoopBlocks();
		GetXorBlock getXorBlock = new GetXorBlock();
		getXorBlock.compute(innerNet, loopBlocks);
		List<Block> xorBlocks = getXorBlock.getMetaXORBlocks();
		int xorIndex = 0;
		for (Block part : xorBlocks) {
			System.out.println("XorBlock_" + xorIndex + "*****************" + "\n");
			System.out.println("Entry: " + part.getEntry() + "\n");
			System.out.println("Exit: " + part.getExit() + "\n");
			System.out.println("EntryActs: " + part.getEntryPost() + "\n");
			System.out.println("exitActs: " + part.getExitPre() + "\n");
			System.out.println("Type: " + part.getType() + "\n");
			xorIndex ++;
			
		}
		
		//测试Get并发部件
		GetAndBlock getAndBlock = new GetAndBlock();
		getAndBlock.compute(innerNet);
		List<Block> andBlocks = getAndBlock.getMetaANDBlocks();
		int andIndex = 0;
		for (Block block : andBlocks) {
			System.out.println("AndBlock_" + andIndex + "*****************" + "\n");
			System.out.println("Entry: " + block.getEntry() + "\n");
			System.out.println("Exit: " + block.getExit() + "\n");
			System.out.println("EntryActs: " + block.getEntryPost() + "\n");
			System.out.println("exitActs: " + block.getExitPre() + "\n");
			System.out.println("Type: " + block.getType() + "\n");
			andIndex ++;
			
		}
		
		//测试Get顺序部件
		GetSeqBlock getSeqBlock = new GetSeqBlock();
		getSeqBlock.compute(innerNet);
		List<Block> seqBlocks = getSeqBlock.getMaxSEQBlocks();
		int seqIndex = 0;
		for (Block block : seqBlocks) {
			System.out.println("SeqBlock_" + seqIndex + "*****************" + "\n");
			System.out.println("Entry: " + block.getEntry() + "\n");
			System.out.println("Exit: " + block.getExit() + "\n");
			System.out.println("seqActs: " + block.getSeqActs() + "\n");
			System.out.println("Type: " + block.getType() + "\n");
			seqIndex ++;
			
		}
		
		
		
		
		
		

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
