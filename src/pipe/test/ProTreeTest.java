package pipe.test;

import java.util.List;

import toolkits.utils.block.GenProTree;
import toolkits.utils.block.InnerNet;
import toolkits.utils.block.Node;
import toolkits.utils.block.ProTree;
import toolkits.utils.block.ProTreeUtils;
import toolkits.utils.file.DotUtils;
import toolkits.utils.petri.ParsePetri;

public class ProTreeTest {

	public static void main(String[] args) throws Exception {
		
		String path = "C:\\Users\\Moqi\\Desktop\\AND1.xml";
		ParsePetri parsePetri = new ParsePetri();
		InnerNet innerNet = parsePetri.parse(path);
		innerNet.setSource("P0");
		innerNet.setSink("P7");
		
		DotUtils dotUtils = new DotUtils();
		dotUtils.ipn2Dot(innerNet, "innerNet");
		
		ProTreeUtils ptUtils = new ProTreeUtils();
		InnerNet petri = ptUtils.rovReduPlaces(innerNet);
		
		GenProTree genProTree = new GenProTree();
		ProTree tree = genProTree.compute(petri);
		dotUtils.pt2Dot(tree, innerNet.getTranLabelMap(), "proTree");
		
		/*ProTreeUtils proTreeUtils = new ProTreeUtils();
		List<ProTree> fragments = proTreeUtils.genFragments(tree, innerNet);
		for (int j = 0; j < fragments.size(); j++) {
			String name = "upfg" + j;
			dotUtils.pt2Dot(fragments.get(j), innerNet.getTranLabelMap(), name);
			
		}*/
		
		
		//dotUtils.pt2Dot(tree, innerNet.getTranLabelMap(), "pt");
		
		/*List<ProTree> fragments = ptUtils.genFragments(tree);
		int index = 0;
		for (ProTree fragment : fragments) {
            for (Node node : fragment.getNodes()) {
            	System.out.println("test......................");
            	if (node.getType().equals("leaf")) {
            		System.out.println("node: " + innerNet.getTranLabelMap().get(node.getIdf()));
				}else {
					System.out.println("node: " + node.getIdf());
				}
            	for (Node chaNode : node.getChaNodes()) {
            		if (chaNode.getType().equals("leaf")) {
                		System.out.println("chaNode: " + innerNet.getTranLabelMap().get(chaNode.getIdf()));
    				}else {
    					System.out.println("chaNode: " + chaNode.getIdf());
    				}
				}
			}
            //ProTree compFrag = ptUtils.compress(fragment);
            dotUtils.pt2Dot(fragment, innerNet.getTranLabelMap(), "cfg" + index);
			index ++;
		}*/
		
		/*List<Node> nodes = tree.getNodes();
		for (Node node : nodes) {
			System.out.println("idf: " + node.getIdf());
		}*/
		
		/*
		
		ProTree compTree = ptUtils.compress(tree);
		dotUtils.pt2Dot(compTree, "cpt");*/
		
		
		

	}

}
