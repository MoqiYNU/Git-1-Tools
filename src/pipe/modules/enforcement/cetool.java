package pipe.modules.enforcement;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import toolkits.def.petri.Marking;
import toolkits.def.petri.ProNet;
import toolkits.def.petri.RG;
import toolkits.utils.enfro.CheckUtils;
import toolkits.utils.enfro.GetController;
import toolkits.utils.enfro.GetEnforProLTS;
import toolkits.utils.enfro.PruneUtils;
import toolkits.utils.file.DotUtils;
import toolkits.utils.file.PrintUtils;
import toolkits.utils.petri.PetriUtils;
import toolkits.utils.petri.RGUtils;

/**
 * @author Moqi
 * 定义正确性迫使模块cetool
 */
public class cetool implements Module{

	private static final String MODULE_NAME = "cetool";
	@SuppressWarnings("unused")
	private PetriNetChooserPanel sourceFilePanel;
	private JDialog guiDialog;
	private DataLayerUtils dataLayerUtils;
	private Composition composition;
	private CheckUtils checkUtils;
	private DataLayer tempPnmldata;
	private PetriUtils petriUtils;
	private DotUtils dotUtils;
	private PruneUtils pruneUtils;
	private ResultsHTMLPane results;
	
	public cetool() {
		dataLayerUtils = new DataLayerUtils();
		composition = new Composition();
		checkUtils = new CheckUtils();
		petriUtils = new PetriUtils();
		dotUtils = new DotUtils();
		pruneUtils = new PruneUtils();
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
		    contentPane.add(new ButtonBar("Enforce", enforceButtonClick));
		
		    // 5 Make window fit contents' preferred size
		    guiDialog.pack();
		    
		    // 6 Move window to the middle of the screen
		    guiDialog.setLocationRelativeTo(null);
		    
		    guiDialog.setVisible(true);
				  
		  }
		  
	}
	
	ActionListener enforceButtonClick = new ActionListener() {
	    @SuppressWarnings("static-access")
		public void actionPerformed(ActionEvent arg0) {
	    	
	    	// 1.组合过程网
			List<ProNet> proNets = dataLayerUtils.genProNetsFromDL(tempPnmldata);
			
			// 2.组合过程网
			composition.setProNets(proNets);
			ProNet orgPro = composition.compose();
			
			/*****************************测试Bi方法是否有效************************/
			
			/* //测试Bi方法是否有效
			 RG rg = petriUtils.genRG(orgPro);
			 LTS orgProLTS = RGUtils.rg2lts(rg);
			 RG rrg = petriUtils.genRGWithStubSet(orgPro);
			 LTS rOrgProLTS = RGUtils.rg2lts(rrg);
			 boolean isValid = checkUtils.isValid_BiApproach(orgPro, orgProLTS, rOrgProLTS);
			 System.out.println("Bi_Approach is valid: " + isValid);*/
			
			
			/********************************正确性迫使***************************/
			
			String outputStr = "<h2>Orginal Process infromation</h2>";
			outputStr += ResultsHTMLPane.makeTable(new String[]{
					     "participants: ", "" + proNets.size(),
					     "places: ", "" + orgPro.getPlaces().size(),
					     "trans: ", "" + orgPro.getTrans().size(),
			              }, 2, false, true, false, true);
			
			//RG rg = petriUtils.genRG(orgPro);
			//System.out.println("RG: nodes: " + rg.getVertexs().size() + ", trans: " + rg.getEdges().size());
			//LTS orgProLTS = RGUtils.rg2lts(rg);
			
           /* outputStr += "<h2>The correctness of orginal process</h2>";
            String corrResult = checkUtils.checkCorrect(orgProLTS);
			outputStr += ResultsHTMLPane.makeTable(new String[]{
					     "correctness: ", "" + corrResult,
			              }, 2, false, true, false, true);*/
			
           /* LTS core = pruneUtils.prune(orgProLTS);
            * outputStr += "<h2>Orginal process enforcement</h2>";
			long startTime1 = System.currentTimeMillis();
			GetController getController = new GetController();
			List<LTS> controllers = getController.generate(proNets);
			long endTime1 = System.currentTimeMillis();
		    long elapsedTime1 = endTime1 - startTime1;//正确性迫使耗费时间
		    outputStr += ResultsHTMLPane.makeTable(new String[]{
				     "enfor. time: ", "" + elapsedTime1 + "ms",
		              }, 2, false, true, false, true);
			
			//一致性检测
			outputStr += "<h2>Consistency checking between core and orginal process</h2>";
		    long startTime2 = System.currentTimeMillis();
		    GetEnforProLTS getEnforProLTS = new GetEnforProLTS();
			LTS enforProLTS = getEnforProLTS.generate(orgPro, controllers);
			
		    boolean consResult = checkUtils.checkConsist(core, enforProLTS);
		    long endTime2 = System.currentTimeMillis();
		    long elapsedTime2 = endTime2 - startTime2; //一致性检测耗费时间
		    if (consResult) {
		    	outputStr += ResultsHTMLPane.makeTable(new String[]{
		    			 "consistency: ", "" + "TRUE",
					     "enfor. time: ", "" + elapsedTime2 + " ms",
			              }, 2, false, true, false, true);
		    }else {
		    	outputStr += ResultsHTMLPane.makeTable(new String[]{
		    			 "consistency: ", "" + "FALSE",
					     "enfor. time: ", "" + elapsedTime2 + " ms",
			              }, 2, false, true, false, true);
		    }
		    
		    results.setText(outputStr);*/
		    
		    
		    /*****************************将核和控制器及迫使过程LTS写入硬盘************************/
		    
		   /* try {
			dotUtils.lts2Dot(core, "core");
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    
		    //将控制器写入硬盘
			int index = 0;
			for (LTS controller : controllers) {
				try {
					dotUtils.lts2Dot(controller, "Ct" + index);
				} catch (Exception e) {
					e.printStackTrace();
				}
				index ++;
			}
		    
		    try {
			dotUtils.lts2Dot(enforProLTS, "enforProLTS");
		    } catch (Exception e) {
			e.printStackTrace();
		    }*/
		    
			
			/*****************************重复5次取平均时间作为正确性迫使耗费时间************************/
		    
			long startTime = System.currentTimeMillis(); 
			GetController getController = new GetController();
			getController.generate(proNets);
			long endTime = System.currentTimeMillis();
			long time = endTime - startTime;
			
			outputStr += "<h2>Time for enforcement</h2>";
			outputStr += ResultsHTMLPane.makeTable(new String[]{
				     "enfor time: ", "" + time + " ms",
		              }, 2, false, true, false, true);
			results.setText(outputStr);
			
			/*long totalTime = 0;
			for (int i = 0; i < 5; i++) {
				long startTime = System.currentTimeMillis(); 
				GetController getController = new GetController();
				getController.generate(proNets);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
