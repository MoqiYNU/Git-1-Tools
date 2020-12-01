package toolkits.def.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toolkits.utils.block.InnerNet;

/**
 * @author Moqi
 * ���������,������ģҵ����̼������
 */
public class ProNet {
	
	private Marking source;//һ����ʼ��ʶ�������ֹ��ʶ
	private List<Marking> sinks;
	private List<String> places;//������(������Ϣ����)
	private List<String> msgPlaces;
	private List<String> resPlaces;
	//��Դ,����Դ�����༯��ʾ
	private List<String> resources;
	private List<String> inputMsgs;
	private List<String> outputMsgs;
	private List<String> trans;//��Ǩ��
	private List<Flow> flows;//����ϵ
	private Map<String, String> tranLabelMap;//��ź���
	//��Դ����ӳ��:0Ϊ���ظ�ʹ����Դ,1Ϊ������Դ
	private Map<String, Integer> resProperMap;
	//ӳ��:<��Դ,�����Ǩ>
	private Map<String, List<String>> reqResMap;
	
	public ProNet() {
		sinks = new ArrayList<Marking>();
		places = new ArrayList<String>();
		msgPlaces = new ArrayList<String>();
		resPlaces = new ArrayList<String>();
		inputMsgs = new ArrayList<String>();
		outputMsgs = new ArrayList<String>();
		trans = new ArrayList<String>();
		flows = new ArrayList<Flow>();
		tranLabelMap = new HashMap<String, String>();
		resProperMap = new HashMap<String, Integer>();
		reqResMap = new HashMap<String, List<String>>();
	}
	
	
	/****************************Add����******************************/
	
	//���һ��sink����
	public void addSink(Marking sink) {
		sinks.add(sink);
	}
	//���һ������(�����ظ����)
	public void addPlace(String place) {
		if (!places.contains(place)) {
			places.add(place);
		}
	}
	//��Ӷ������(�����ظ����)
	public void addPlaces(List<String> tempPlaces) {
		for (String tempPlace : tempPlaces) {
			if (!places.contains(tempPlace)) {
				places.add(tempPlace);
			}
		}
	}
	//���tran(�����ظ����)
	public void addTran(String tran) {
		if (!trans.contains(tran)) {
			trans.add(tran);
		}
	}
	//���һ��flow
	public void addFlow(Flow flow) {
		flows.add(flow);
	}
	//��Ӷ���flow
	public void addFlows(List<Flow> tempFlows) {
		flows.addAll(tempFlows);
	}
	//�����Ϣ����(�����ظ����)
	public void addMsgPlace(String msgPlace) {
		if (!msgPlaces.contains(msgPlace)) {
			msgPlaces.add(msgPlace);
		}
	}
	//���������Ϣ����(�����ظ����)
	public void addInputMsgPlace(String msgPlace) {
		if (!inputMsgs.contains(msgPlace)) {
			inputMsgs.add(msgPlace);
		}
	}
	//��������Ϣ����(�����ظ����)
	public void addOutputMsgPlace(String msgPlace) {
		if (!outputMsgs.contains(msgPlace)) {
			outputMsgs.add(msgPlace);
		}
	}
	//�����Դ����(�����ظ����)
	public void addResPlace(String resPlace) {
		if (!resPlaces.contains(resPlace)) {
			resPlaces.add(resPlace);
		}
	}
	
	/*************************Get��Set����****************************/
	
	public Marking getSource() {
		return source;
	}
	public void setSource(Marking source) {
		this.source = source;
	}	
	public List<Marking> getSinks() {
		return sinks;
	}
	public void setSinks(List<Marking> sinks) {
		this.sinks = sinks;
	}
	public List<String> getPlaces() {
		return places;
	}
	public void setPlaces(List<String> places) {
		this.places = places;
	}
	public List<String> getTrans() {
		return trans;
	}
	public void setTrans(List<String> trans) {
		this.trans = trans;
	}
	public List<Flow> getFlows() {
		return flows;
	}
	public void setFlows(List<Flow> flows) {
		this.flows = flows;
	}
	public List<String> getMsgPlaces() {
		return msgPlaces;
	}
	public void setMsgPlaces(List<String> msgPlaces) {
		this.msgPlaces = msgPlaces;
	}
	public List<String> getInputMsgs() {
		return inputMsgs;
	}
	public void setInputMsgs(List<String> inputMsgs) {
		this.inputMsgs = inputMsgs;
	}
	public List<String> getOutputMsgs() {
		return outputMsgs;
	}
	public void setOutputMsgs(List<String> outputMsgs) {
		this.outputMsgs = outputMsgs;
	}
	public List<String> getResPlaces() {
		return resPlaces;
	}
	public void setResPlaces(List<String> resPlaces) {
		this.resPlaces = resPlaces;
	}
	public List<String> getResources() {
		return resources;
	}
	public void setResources(List<String> resources) {
		this.resources = resources;
	}
	public Map<String, String> getTranLabelMap() {
		return tranLabelMap;
	}
	public void setTranLabelMap(Map<String, String> tranLabelMap) {
		this.tranLabelMap = tranLabelMap;
	}
	public Map<String, Integer> getResProperMap() {
		return resProperMap;
	}
	public void setResProperMap(Map<String, Integer> resProperMap) {
		this.resProperMap = resProperMap;
	}
	public Map<String, List<String>> getReqResMap() {
		return reqResMap;
	}
	public void setReqResMap(Map<String, List<String>> reqResMap) {
		this.reqResMap = reqResMap;
	}


	/***********************���ع�����������(������������)**************************/
	
	public InnerNet getInnerNet() {
		
		InnerNet innerNet = new InnerNet();
		//Դ����ֻ��1��
        innerNet.setSource(getSource().getPlaces().get(0));
        //��ֹ����1��
        innerNet.setSink(getSinks().get(0).getPlaces().get(0));
        innerNet.setTrans(getTrans());
        List<Flow> interFlows = new ArrayList<Flow>();
        for (Flow flow : getFlows()) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			//�Ƴ���Ϣ������Դ��
			if (getMsgPlaces().contains(from) || getMsgPlaces().contains(to)
					|| getResPlaces().contains(from) || getResPlaces().contains(to)) {
				continue;
			}
			interFlows.add(flow);
        }
        innerNet.setFlows(interFlows);
        //��ȡ������,�ų���Ϣ/��Դ����
        for (Flow flow : interFlows) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (getPlaces().contains(from)) {
				innerNet.addPlace(from);
			}
			if (getPlaces().contains(to)) {
				innerNet.addPlace(to);
			}
		}
        innerNet.setTranLabelMap(getTranLabelMap());
        return innerNet;
        
	}
	
	//��ȡProNet�н������
	public List<String> getInterActs() {
		
		List<String> interActs = new ArrayList<String>();
		for (Flow flow : getFlows()) {
        	String from = flow.getFlowFrom();
			String to = flow.getFlowTo();
			if (getMsgPlaces().contains(from)) {
				interActs.add(to);
			}else if (getMsgPlaces().contains(to)) {
				interActs.add(from);
			}
        }
		return interActs;
		
	}

}
