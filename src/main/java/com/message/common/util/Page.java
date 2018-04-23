package com.message.common.util;
import java.io.Serializable;


public class Page implements Serializable {

	private static final long serialVersionUID = 1L;

	private int size = 50;// 每页显示多少条
	private int count;// 总条数
	private int current;// 当前页
	private int totalPage;// 总页数

	public Page() {
		size = 50;
		current = 1;
		count = -1;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getTotalPage() {
		int i= (int) Math.ceil((double) this.count / (double) this.size);
		return i = i == 0 ? 1 : i;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public void adjustStartPage() {
		if (this.current > this.totalPage || getCurrent() < 0)
			this.current = this.totalPage;
		if (this.current == 0)
			this.current = 1;
		if (this.totalPage == 0)
			this.totalPage = 1;
	}

	public int getFirstResultNumber() {
		return (this.current - 1) * this.size;
	}
}
