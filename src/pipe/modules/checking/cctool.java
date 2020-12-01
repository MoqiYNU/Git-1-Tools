package pipe.modules.checking;

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
import toolkits.utils.file.DotUtils;
import toolkits.utils.file.PrintUtils;
import toolkits.utils.petri.PetriUtils;
import toolkits.utils.petri.RGUtils;

/**
 * @author Moqi
 * 定义正确性迫使模块cetool
 */
public class cctool implements Module{

	private static final String MODULE_NAME = "cctool";
	@SuppressWarnings("unused")
	private PetriNetChooserPanel sourceFilePanel;
	private JDialog guiDialog;
	private DataLayerUtils dataLayerUtils;
	private Composition composition;
	private CheckUtils checkUtils;
	private DataLayer tempPnmldata;
	private PetriUtils petriUtils;
	private DotUtils dotUtils;
	private ResultsHTMLPane results;
	private RGUtils rgUtils;
	
	public cctool() {
		dataLayerUtils = new DataLayerUtils();
		composition = new Composition();
		checkUtils = new CheckUtils();
		petriUtils = new PetriUtils();
		dotUtils = new DotUtils();
		rgUtils = new RGUtils();
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
	    @SuppressWarnings("static-access")
		public void actionPerformed(ActionEvent arg0) {
	    	
	    	// 1.组合过程网
			List<ProNet> proNets = dataLayerUtils.genProNetsFromDL(tempPnmldata);
			
			// 2.组合过程网
			composition.setProNets(proNets);
			ProNet colPro = composition.compose();
			
			System.out.println("print comp net...");
			PrintUtils.printNet(colPro);
			
			
			/******************************基于稳固集正确性检测*************************/
			
			String outputStr = "<h2>Collaborative Business Process infromation</h2>";
			outputStr += ResultsHTMLPane.makeTable(new String[]{
					     "partis: ", "" + proNets.size(),
					     "places: ", "" + colPro.getPlaces().size(),
					     "trans: ", "" + colPro.getTrans().size(),
			              }, 2, false, true, false, true);
			
			RG rg = petriUtils.genRGWithStubSet(colPro);
			LTS colProLTS = rgUtils.rg2lts(rg);
			
			
            outputStr += "<h2>The correctness of collaborative business process</h2>";
            long startTime = System.currentTimeMillis();
            String corrResult = checkUtils.checkCorrect(colProLTS);
            long endTime = System.currentTimeMillis();
			outputStr += ResultsHTMLPane.makeTable(new String[]{
					     "correctness: ", "" + corrResult,
			              }, 2, false, true, false, true);
			long checkTime = endTime - startTime;
			outputStr += ResultsHTMLPane.makeTable(new String[]{
				     "checking time: ", "" + checkTime + "ms",
		              }, 2, false, true, false, true);
			
		    
		    results.setText(outputStr);
		    
		    
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
		    
			
			/*****************************重复5次取平均时间作为正确性迫使耗费时间************************//*
		    
			long totalTime = 0;
			for (int i = 0; i < 5; i++) {
				long startTime = System.currentTimeMillis(); 
				GetController getController = new GetController();
				getController.generate(proNets);
				long endTime = System.currentTimeMillis();
				long time = endTime - startTime;
				totalTime = totalTime + time;
			}
			long enfTime = totalTime / 5;
			
			String outputStr = "<h2>Average time for enforcement</h2>";
			outputStr += ResultsHTMLPane.makeTable(new String[]{
				     "avg time: ", "" + enfTime + " ms",
		              }, 2, false, true, false, true);
			results.setText(outputStr);*/
			
			
	    }
	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
