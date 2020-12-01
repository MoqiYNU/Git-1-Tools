package pipe.modules.plan;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.Module;
import pipe.utils.DataLayerUtils;
import toolkits.def.petri.Composition;
import toolkits.def.petri.ProNet;
import toolkits.utils.block.GenProTree;
import toolkits.utils.block.ProTree;
import toolkits.utils.block.ProTreeUtils;
import toolkits.utils.file.DotUtils;
import toolkits.utils.file.PrintUtils;
import toolkits.utils.plan.CompFrag;
import toolkits.utils.plan.PlanUtils;

/**
 * @author Moqi
 * 定义可靠执行路径推荐(Du方法)模块
 */
public class TestModule implements Module{

	private static final String MODULE_NAME = "TestModule";
	@SuppressWarnings("unused")
	private PetriNetChooserPanel sourceFilePanel;
	private JDialog guiDialog;
	private DataLayerUtils dataLayerUtils;
	private DataLayer tempPnmldata;
	private ResultsHTMLPane results;
	
	public TestModule() {
		dataLayerUtils = new DataLayerUtils();
		
	}
	
	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public void run(DataLayer pnmldata) {
		
        if((pnmldata == null) || !pnmldata.getPetriNetObjects().hasNext()){
			
			String legend = "No Petri net objects defined!";
			CreateGui.appGui.getStatusBar().changeText(legend);
			  
		 }else {
			  
			// Build interface
		    guiDialog = new JDialog(CreateGui.getApp(),"Correctness Enforcement",true);
		    
		    // 1 Set layout
		    Container contentPane=guiDialog.getContentPane();
		    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
		    
		    // 2 Add file browser
		    contentPane.add(sourceFilePanel = new PetriNetChooserPanel("Source net",pnmldata));
		    
		    // 3 Add results pane
		    results = new ResultsHTMLPane(pnmldata.getURI());
		    contentPane.add(results);
			  
		    // 4 Add button
		    tempPnmldata = pnmldata;
		    contentPane.add(new ButtonBar("Check", enforceButtonClick));
		
		    // 5 Make window fit contents' preferred size
		    guiDialog.pack();
		    
		    // 6 Move window to the middle of the screen
		    guiDialog.setLocationRelativeTo(null);
		    
		    guiDialog.setVisible(true);
				  
		  }
		  
	}
	
	ActionListener enforceButtonClick = new ActionListener() {
	    public void actionPerformed(ActionEvent arg0) {
	    	
	    	List<ProNet> proNets = dataLayerUtils.genProNetsFromDL(tempPnmldata);
	    	
	    	//组合过程网
	    	Composition composition = new Composition();
			composition.setProNets(proNets);
			ProNet compNet = composition.compose();
			
			PrintUtils.printNet(compNet);
			
			System.out.println("Net Infor: " + "orgNum: " + proNets.size() + 
			           "; placeNum: " + compNet.getPlaces() + 
			           "; tranNum: " + compNet.getTrans()
			            );
	    	
			DotUtils dotUtils = new DotUtils();
			
			/*ProTreeUtils proTreeUtils = new ProTreeUtils();
			int size = proNets.size();
			for (int i = 0; i < size; i++) {
				ProNet proNet = proNets.get(i);
				try {
					proTreeUtils.genProTreeFromProNet(proNet, i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			
			PlanUtils planUtils = new PlanUtils();
	    	
			List<CompFrag> compFrags;
			try {
				compFrags = planUtils.getSemtCompaCompFrags(proNets);
				for (int i = 0; i < compFrags.size(); i++) {
					List<ProTree> frags = compFrags.get(i).getFrags();
					for (int j = 0; j < frags.size(); j++) {
						ProTree proTree = frags.get(j);
						String name = "fg" + i + j;
						dotUtils.pt2Dot(proTree, proNets.get(j).getTranLabelMap(), name);
					}
					System.out.println("semt comp frag: " + i + " end............................");
					System.out.println("\n");
					
				/*	int index = 0;
					int[][] graph = planUtils.creatInterGraph(compFrags.get(i), proNets);
					//打印创建的交互图graph
					System.out.println("inter graph: " + index + "..............................\n");
					index ++;
					for(int k = 0; k < graph.length; k++){  
						for(int l = 0; l < graph[k].length; l++){
							System.out.print(graph[k][l] + ", ");
						}
						System.out.println("\n");
					}*/
				}
					
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	    }
	};

	
	
}
