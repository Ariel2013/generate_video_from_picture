package com.patientx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author jianghui
 * 集合工具类
 *
 */
public class SetUtils {

	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}
	
	public static <K, V> boolean isEmpty(Map<K, V> map) {
		return map == null || map.isEmpty();
	}
	
	public static boolean isNotEmpty(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}
	
	public static <K, V> boolean isNotEmpty(Map<K, V> map) {
		return map != null && !map.isEmpty();
	}
	
	/**
	 * compare two list, result[0] is add list, result[1] is same list, result[2] is delete list
 	 * @param past
	 * @param future
	 * @return List<List<Long>>
	 */
	public static List<List<Long>> compareList(List<Long> past, List<Long> future) {
		List<Long> addList = new ArrayList<Long>();
		List<Long> holdList = new ArrayList<Long>();
		List<Long> deleteList = new ArrayList<Long>();
		List<List<Long>> result = new LinkedList<List<Long>>();
		result.add(addList);
		result.add(holdList);
		result.add(deleteList);
		
		if (SetUtils.isEmpty(past)) {
			if (SetUtils.isEmpty(future)) {
				return result;
			} else {
				addList.addAll(future);
				return result;
			}
		} else {
			if (SetUtils.isEmpty(future)) {
				deleteList.addAll(past);
				return result;
			}
		}
		
		Collections.sort(past);
		Collections.sort(future);
		int pastPoint = 0, futurePoint = 0, pastLength = past.size(), futureLength = future.size();
		long pastValue = 0, futureValue = 0;
		while (true) {
			if (pastPoint < pastLength && futurePoint < futureLength) {
				pastValue = past.get(pastPoint);
				futureValue = future.get(futurePoint);
				if (pastValue < futureValue) {
					pastPoint++;
					deleteList.add(pastValue);
					continue;
				} else if (pastValue == futureValue) {
					pastPoint++;
					futurePoint++;
					holdList.add(pastValue);
					continue;
				} else {
					futurePoint++;
					addList.add(futureValue);
					continue;
				}
			} else {
				break;
			}
		}
		if (pastPoint < pastLength) {
			deleteList.addAll(past.subList(pastPoint, pastLength));
		}
		if (futurePoint < futureLength) {
			addList.addAll(future.subList(futurePoint, futureLength));
		}
		return result;
	}
	
	
	public static <E> List<E> subList(List<E> list,int offset,int rows){
		if(list==null){
			return null;
		}
		int fromIndex = offset;
		int toIndex = offset + rows;
		if(toIndex>list.size()){
			toIndex = list.size();
		}
		if(fromIndex<list.size()){
			return list.subList(fromIndex, toIndex);
		}
		return null;
	}

}
