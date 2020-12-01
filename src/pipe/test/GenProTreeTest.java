package pipe.test;

import toolkits.utils.block.GenProTree;
import toolkits.utils.block.InnerNet;
import toolkits.utils.block.ProTree;
import toolkits.utils.block.ProTreeUtils;
import toolkits.utils.file.DotUtils;
import toolkits.utils.petri.ParsePetri;

public class GenProTreeTest {

	public static void main(String[] args) throws Exception {
		
		String path = "C:\\Users\\Moqi\\Desktop\\���������Լ�\\B3.xml";
		ParsePetri parsePetri = new ParsePetri();
		InnerNet innerNet = parsePetri.parse(path);
		innerNet.setSource("P5");
		innerNet.setSink("P12");
		
		DotUtils dotUtils = new DotUtils();
		ProTreeUtils ptUtils = new ProTreeUtils();
		
		InnerNet petri = ptUtils.rovReduPlaces(innerNet);
		dotUtils.ipn2Dot(petri, "innerNet");
		
		GenProTree genProTree = new GenProTree();
		ProTree initProTree = genProTree.compute(petri);
		dotUtils.pt2Dot(initProTree, innerNet.getTranLabelMap(), "pt" + 1);
		//3.ѹ����ʼ������
		ProTree proTree = ptUtils.compress(initProTree);
		dotUtils.pt2Dot(proTree, innerNet.getTranLabelMap(), "cpt" + 1);
		System.out.println("\n");

	}

}
