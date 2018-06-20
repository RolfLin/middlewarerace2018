package me.bai.aliware.mesh.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimeRecorder {

	private static final Logger LOGGER = LoggerFactory.getLogger("TIME_RECORDER");

	private static Map<Integer, List<Cell>> listHashMap = new HashMap<>();
	private static Map<Integer, Long> startMarkTimeMap = new HashMap<>();
	private static Map<Integer, Long> lastMarkTimeMap = new HashMap<>();

	public static void mark(int id, String msg) {
		if (log(id)) {
			List<Cell> list = listHashMap.computeIfAbsent(id, k -> new LinkedList<>());
			long now = System.currentTimeMillis();
			long lastMarkTime = lastMarkTimeMap.computeIfAbsent(id, k -> {
				startMarkTimeMap.put(id, now);
				return now;
			});
			short dif = (short) (now - lastMarkTime);
			list.add(new Cell(dif, msg));
			lastMarkTimeMap.put(id, now);
		}
	}

	public static void end(int id) {
		if (log(id)) {
			long thisStart = System.currentTimeMillis();
			mark(id, "end");
			List<Cell> list = listHashMap.remove(id);
			long start = startMarkTimeMap.remove(id);
			long end = lastMarkTimeMap.remove(id);
			String listString = list.toString();
			long thisCost = System.currentTimeMillis() - thisStart;
			LOGGER.debug("id:{}	cost:{}	start:{}	t:{}	end:{}	thisCost:{}", id, end - start, start, listString, end, thisCost);
		}
	}

	public static boolean log(int id) {
//		return false;
//		return true;
		return (id & 0x3f) == 0;
	}

	private static class Cell {
		private short dif;
		private String msg;

		public Cell(short dif, String msg) {
			this.dif = dif;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return dif + "ms后" + msg;
		}
	}

}