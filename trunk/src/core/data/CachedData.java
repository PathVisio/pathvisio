package data;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data.GmmlGdb.IdCodePair;
import data.GmmlGex.Sample;

public class CachedData {
	
	HashMap<IdCodePair, List<Data>> data;
		
	public CachedData() {
		data = new HashMap<IdCodePair, List<Data>>();
	}
	
	public boolean hasData(IdCodePair pwId) {
		return data.containsKey(pwId);
	}
	
	public boolean hasMultipleData(IdCodePair pwId) {
		List<Data> d = data.get(pwId);
		if(d != null) {
			return d.size() > 1;
		} else {
			return false;
		}
	}
	
	public List<Data> getData(IdCodePair idc) {
		return data.get(idc);
	}
	
	public Data getSingleData(IdCodePair idc) {
		List<Data> dlist = data.get(idc);
		if(dlist != null && dlist.size() > 0) return dlist.get(0);
		return null;
	}
	
	public void addData(IdCodePair idc, Data d) {
		List<Data> dlist = data.get(idc);
		if(dlist == null) 
			data.put(idc, dlist = new ArrayList<Data>());
		dlist.add(d);
	}
	
	public HashMap<Integer, Object> getAverageSampleData(IdCodePair idc)
	{
		HashMap<Integer, Object> averageData = new HashMap<Integer, Object>();
		List<Data> dlist = data.get(idc);
		if(dlist != null) {
			HashMap<Integer, Sample> samples = GmmlGex.getSamples();
			for(int idSample : samples.keySet())
			{
				int dataType = samples.get(idSample).getDataType();
				if(dataType == Types.REAL) {
					averageData.put(idSample, averageDouble(dlist, idSample));
				} else {
					averageData.put(idSample, averageString(dlist, idSample));
				}
			}
		}
		return averageData;
	}
	
	private Object averageDouble(List<Data> dlist, int idSample)
	{
		double avg = 0;
		int n = 0;
		for(Data d : dlist) {
			try { 
				Double value = (Double)d.getSampleData(idSample);
				if( !value.isNaN() ) {
					avg += value;
					n++;
				}
			} catch(Exception e) { e.printStackTrace(); }
		}
		if(n > 0) {
			return avg / n;
		} else {
			return averageString(dlist, idSample);
		}
	}
	
	private Object averageString(List<Data> dlist, int idSample)
	{
		StringBuilder sb = new StringBuilder();
		for(Data d : dlist) {
			sb.append(d.getSampleData(idSample) + ", ");
		}
		int end = sb.lastIndexOf(", ");
		return end < 0 ? "" : sb.substring(0, end).toString();
	}
	
	public static class Data {
		IdCodePair idc;
		int group;
		HashMap<Integer, Object> sampleData;
		
		public Data(IdCodePair ref, int groupId) {
			idc = ref;
			group = groupId;
			sampleData = new HashMap<Integer, Object>();
		}
		
		public IdCodePair getIdCodePair() { return idc; }
		public int getGroup() { return group; }
		
		public HashMap<Integer, Object> getSampleData() {
			return sampleData;
		}
		
		public Object getSampleData(int sampleId) {
			return sampleData.get(sampleId);
		}
		
		public void setSampleData(int sampleId, String data) {
			Object parsedData = null;
			try { parsedData = Double.parseDouble(data); }
			catch(Exception e) { parsedData = data; }
			sampleData.put(sampleId, parsedData);
		}
		
	}
	
	
//	public static class CachedData
//	{
//		private HashMap<IdCodePair, Data> data;		
//		
//		public boolean hasData(IdCodePair idc)
//		{
//			if(data.containsKey(idc)) return data.get(idc).idcode.equals(idc);
//			else return false;
//		}
//		
//		public Data getData(IdCodePair idc)
//		{
//			Data d = null;
//			if(data.containsKey(idc)) { 
//				d = data.get(idc);
//				if(!d.idcode.equals(idc)) d = null;//why??
//			}
//			return d;
//		}
//		
//		public void addData(String id, String code, Data mappIdData)
//		{
//			data.put(new IdCodePair(id, code), mappIdData);
//		}
//		
//		public class Data implements Comparable
//		{
//			private IdCodePair idcode;
//			private HashMap<Integer, Object> sampleData;
//			private HashMap<IdCodePair, Data> refData;
//			
//			public Data(String id, String code) {
//				idcode = new IdCodePair(id, code);
//				refData = new HashMap<IdCodePair, Data>();
//				sampleData = new HashMap<Integer, Object>();
//			}
//			
//			public Data(IdCodePair idcode) {
//				this.idcode = idcode;
//				sampleData = new HashMap<Integer, Object>();
//			}
//			
//			public boolean hasData() { return refData.size() > 0; }
//			
//			public void addRefData(String id, String code, int sampleId, String data) 
//			{ 
//				Data ref = null;
//				IdCodePair idcode = new IdCodePair(id, code);
//				if(refData.containsKey(idcode)) {
//					ref = refData.get(idcode);
//				} else ref = new Data(idcode);
//				
//				Object parsedData = null;
//				try { parsedData = Double.parseDouble(data); }
//				catch(Exception e) { parsedData = data; }
//				ref.addSampleData(sampleId, parsedData);
//				refData.put(idcode, ref);
//			}
//			
//			public void addSampleData(int sampleId, Object data)
//			{
//				if(data != null) sampleData.put(sampleId, data);
//			}
//			
//			public List<Data> getRefData()
//			{
//				List<Data> rd = new ArrayList<Data>(refData.values());
//				Collections.sort(rd);
//				return rd;
//			}
//			
//			public Object getData(int idSample)
//			{
//				if(sampleData.containsKey(idSample)) return sampleData.get(idSample);
//				return null;
//			}
//			
//			public boolean hasMultipleData()
//			{
//				return refData.keySet().size() > 1;
//			}
//			
//			public HashMap<Integer, Object> getSampleData() {
//				if(sampleData.size() == 0) {
//					if(refData.size() > 0) return getAverageSampleData();
//				}
//				return sampleData;
//			}
//			
//
//			
//
//
//			public int compareTo(Object o) {
//				Data d = (Data)o;
//				return idcode.compareTo(d.idcode);
//			}
//		}
//	}
	
}
