package com.josoft.collections;

import java.util.Iterator;

import com.josoft.collections.CircBuffer;

public final class CircBufferTests {

	public static void main(String[] args) {
		CircBuffer<Integer> cBuff1 = new CircBuffer<Integer>(10);
		CircBuffer<Integer> cBuff2 = new CircBuffer<Integer>(15);

		for (int i = 0; i < 17; i++) {
			cBuff1.add(i);
		}
		for (int i = 0; i < 7; i++) {
			cBuff2.add(i);
		}

		Object[] array1 = cBuff1.toArray();
		System.out.print("Elements: ");
		for (int i = 0; i < array1.length; i++) {
			System.out.print(array1[i].toString() + ", ");
		}
		System.out.print("\r\n");
		
		System.out.print("Oldest element buffer 2: ");
		System.out.print(cBuff2.popOldest().toString());
		System.out.print("\r\n");
		
		System.out.print("Newest element buffer 2: ");
		System.out.print(cBuff2.popNewest().toString());
		System.out.print("\r\n");

		Object[] array2 = cBuff2.toArray();
		System.out.print("Elements: ");
		for (int i = 0; i < array2.length; i++) {
			System.out.print(array2[i].toString() + ", ");
		}
		System.out.print("\r\n");

		if (cBuff1.size() == 10 && cBuff2.size() == 5) {
			System.out.print("Size determination OK");
		} else {
			System.out.print("Size determination FAILED");
		}
		System.out.print("\r\n");

		boolean contentOK = true;
		if (cBuff1.getFromOldest(0) != 16) {
			contentOK = false;
		}
		for (int i = 7; i < 16; i++) {
			if (cBuff1.getFromOldest(i - 6) != i) {
				contentOK = false;
			}
			if (i < 12 && cBuff2.getFromNewest(i - 7) != 12 - i) {
				contentOK = false;
			}
		}

		if (contentOK) {
			System.out.print("Content OK");
		} else {
			System.out.print("Content NOT OK");
		}
		System.out.print("\r\n");

		boolean OOBExOK = false;
		try {
			cBuff1.getFromOldest(10);
		} catch (ArrayIndexOutOfBoundsException e) {
			OOBExOK = true;
		}

		if (OOBExOK) {
			System.out.print("Out of bounds exception OK");
		} else {
			System.out.print("Out of bounds exception NOT OK");
		}
		System.out.print("\r\n");

		for (int i = 0; i < 10; i++) {
			cBuff1.popNewest();
		}
		for (int i = 0; i < 5; i++) {
			cBuff2.popOldest();
		}

		if (cBuff1.isEmpty() && cBuff2.isEmpty()) {
			System.out.print("Removal OK");
		} else {
			System.out.print("Removal NOT OK");
		}
		System.out.print("\r\n");

		for (int i = 0; i < 17; i++) {
			cBuff1.add(i);
		}
		for (int i = 0; i < 7; i++) {
			cBuff2.add(i);
		}

		cBuff1.removeFromNewest(5);
		cBuff2.removeFromOldest(3);

		System.out.print(cBuff1.toString() + "\r\n");
		System.out.print(cBuff2.toString() + "\r\n");

		if (cBuff1.getFromNewest(4) == 12 && cBuff2.getFromOldest(3) == 4) {
			System.out.print("Mid removal OK");
		} else {
			System.out.print("Mid removal NOT OK");
		}
		System.out.print("\r\n");

		cBuff1.clear();

		if (cBuff1.isEmpty()) {
			System.out.print("Clear OK");
		} else {
			System.out.print("Clear OK");
		}
		System.out.print("\r\n");
		
		Iterator<Integer> iter = cBuff2.iterator();
		System.out.print("Multiple element iterator: [");
		if (iter.hasNext())
		{
			System.out.print(iter.next().toString());
		}
		while (iter.hasNext())
		{
			System.out.print(", " + iter.next().toString());
		}
		System.out.print("]\r\n");
		
		cBuff2 = new CircBuffer<Integer>(5, true);
		cBuff2.add(3);
		
		iter = cBuff2.iterator();
		System.out.print("Single element iterator: [");
		if (iter.hasNext())
		{
			System.out.print(iter.next().toString());
		}
		while (iter.hasNext())
		{
			System.out.print(", " + iter.next().toString());
		}
		System.out.print("]\r\n");
		
	}
}