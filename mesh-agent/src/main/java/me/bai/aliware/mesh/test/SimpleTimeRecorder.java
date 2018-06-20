//package me.bai.aliware.mesh.test;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.LinkedList;
//import java.util.List;
//
//public class SimpleTimeRecorder {
//	private static final Logger LOGGER = LoggerFactory.getLogger("TIME_RECORDER");
//
//	private int id = -1;
//	private long start;
//	private long last;
//	private List<Cell> ts = new LinkedList<>();
//
//	public void init(int id) {
//		this.id = id;
//		if (TimeRecorder.log(id)) {
//			last = start = System.currentTimeMillis();
//		}
//	}
//
//	public void mark(String msg) {
//		if (TimeRecorder.log(id)) {
//			long now = System.currentTimeMillis();
//			short dif = (short) (now - last);
//			ts.add(new Cell(dif, msg));
//			last = now;
//		}
//	}
//
//	public void end() {
//		if (TimeRecorder.log(id)) {
//			mark("end");
//			String listString = ts.toString();
//			LOGGER.debug("id:{}	cost:{}	start:{}	t:{}	end:{}", id, last - start, start, listString, last);
//			id = -1;
//			ts.clear();
//		}
//	}
//
//	private class Cell {
//		short dif;
//		String msg;
//
//		public Cell(short dif, String msg) {
//			this.dif = dif;
//			this.msg = msg;
//		}
//
//		@Override
//		public String toString() {
//			return dif + "ms后" + msg;
//		}
//	}
//
//}
