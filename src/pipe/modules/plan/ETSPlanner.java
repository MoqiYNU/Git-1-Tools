package pipe.modules.plan;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.Module;
import pipe.utils.DataLayerUtils;
import pipe.gui.widgets.ButtonBar;
import toolkits.def.lts.LTS;
import toolkits.def.petri.Composition;
import toolkits.def.petri.ProNet;
import toolkits.def.petri.RG;
import toolkits.utils.enfro.CheckUtils;
import toolkits.utils.petri.PetriUtils;
import toolkits.utils.petri.RGUtils;
import toolkits.utils.plan.Bag;
import toolkits.utils.plan.PlanUtils;

/**
 * @author Moqi
 * 定义面向执行计划的正确性迫使模块-ETS
 */
public class ETSPlanner implements Module{

	private static final String MODULE_NAME = "ETSPlanner";
	@SuppressWarnings("unused")
	private PetriNetChooserPanel sourceFilePanel;
	private JDialog guiDialog;
	private DataLayerUtils dataLayerUtils;
	private DataLayer tempPnmldata;
	private ResultsHTMLPane results;
    private Composition composition;
	
	public ETSPlanner() {
		dataLayerUtils = new DataLayerUtils();
		composition = new Composition();
		
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
		    guiDialog = new JDialog(CreateGui.getApp(),"Correctness By Construction",true);
		    
		    guiDialog.setModal(false);
		    
		    // 1 Set layout
		    Container contentPane = guiDialog.getContentPane();
		    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
		    
		    // 2 Add file browser
		    contentPane.add(sourceFilePanel = new PetriNetChooserPanel("Source net",pnmldata));
		    
		    // 3 Add results pane
		    results = new ResultsHTMLPane(pnmldata.getURI());
		    contentPane.add(results);
			  
		    // 4 Add button
		    tempPnmldata = pnmldata;
		    
		    Box horBox = Box.createHorizontalBox();
		    horBox.add(new ButtonBar("Construct", enforceButtonClick));
		    horBox.add(new ButtonBar("Execute", executeButtonClick));
		    contentPane.add(horBox);
		
		    // 5 Make window fit contents' preferred size
		    guiDialog.pack();
		    
		    // 6 Move window to the middle of the screen
		    guiDialog.setLocationRelativeTo(null);
		    
		    guiDialog.setVisible(true);
				  
		  }
		  
	}
	
	ActionListener enforceButtonClick = new ActionListener() {
	
		public void actionPerformed(ActionEvent arg0) {
	    	
	    	// 1.过程网集
			List<ProNet> proNets = dataLayerUtils.genProNetsFromDL(tempPnmldata);
			// 2.组合过程网
			composition.setProNets(proNets);
			ProNet orgPro = composition.compose();
			
			String outputStr = "<h2>ETS infromation</h2>";
			outputStr += ResultsHTMLPane.makeTable(new String[]{
					     "participants: ", "" + proNets.size(),
					     "places: ", "" + orgPro.getPlaces().size(),
					     "trans: ", "" + orgPro.getTrans().size(),
			              }, 2, false, true, false, true);
			
			PetriUtils petriUtils = new PetriUtils();
			RG rg = petriUtils.genRGWithStubSet(orgPro);
			LTS orgProLTS = RGUtils.rg2lts(rg);
			CheckUtils checkUtils = new CheckUtils();
			outputStr += "<h2>Correctness of ETS</h2>";
            String corrResult = checkUtils.checkCorrect(orgProLTS);
			outputStr += ResultsHTMLPane.makeTable(new String[]{
					     "correctness: ", "" + corrResult,
			              }, 2, false, true, false, true);
			
			outputStr += "<h2>ETS enforcement</h2>";
			long startTime = System.currentTimeMillis();
			
			List<Bag> initBags = null;
			PlanUtils planUtils = new PlanUtils();
			try {
				initBags = planUtils.enforce(proNets);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			long endTime = System.currentTimeMillis();
			long time = endTime - startTime;
			
		    outputStr += ResultsHTMLPane.makeTable(new String[]{
		    		 "plans Num: ", "" + initBags.get(0).getAvailPlans().size(),
				     "enfor. time: ", "" + time + "ms",
		              }, 2, false, true, false, true);
			
		    results.setText(outputStr);
		    
			
			/*****************************重复5次取平均时间作为正确性迫使耗费时间************************/
		    
			/*long totalTime = 0;
			PlanUtils planUtils = new PlanUtils();
			for (int i = 0; i < 5; i++) {
			    long startTime = System.currentTimeMillis(); 
			    try {
					planUtils.enforce(proNets);
				} catch (Exception e) {
					e.printStackTrace();
				}
				long endTime = System.currentTimeMillis();
				long time = endTime - startTime;
				totalTime = totalTime + time;
			}
			long enfTime = totalTime / 5;
			
			outputStr += "<h2>Average time for enforcement</h2>";
			outputStr += ResultsHTMLPane.makeTable(new String[]{
				     "avg time: ", "" + enfTime + " ms",
		              }, 2, false, true, false, true);
			
			results.setText(outputStr);*/
			
			
	    }
	};
	
	ActionListener executeButtonClick = new ActionListener() {
	    public void actionPerformed(ActionEvent arg0) {
	    	
			List<ProNet> proNets = dataLayerUtils.genProNetsFromDL(tempPnmldata);
			
			//组合过程网
			composition.setProNets(proNets);
			ProNet compNet = composition.compose();
			//组合网中交互数
			int inters = compNet.getMsgPlaces().size();
			
			PlanUtils planUtils = new PlanUtils();
			try {
				List<Bag> initBags = planUtils.enforce(proNets);
				LTS lts = planUtils.executeETS(proNets, initBags);
				RGUtils.genGraphFromLTS(lts, proNets.size(), tempPnmldata.getPlacesCount(), tempPnmldata.getTransitionsCount(), inters);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	};
	
	
}
