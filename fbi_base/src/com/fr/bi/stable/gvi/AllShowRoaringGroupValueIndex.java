package com.fr.bi.stable.gvi;import com.fr.bi.stable.gvi.roaringbitmap.RoaringBitmap;import com.fr.bi.stable.gvi.traversal.BrokenTraversalAction;import com.fr.bi.stable.gvi.traversal.SingleRowTraversalAction;import com.fr.bi.stable.gvi.traversal.TraversalAction;import java.io.DataInput;import java.io.DataOutput;import java.io.IOException;/** * Created by Hiram on 2015/7/22. */public class AllShowRoaringGroupValueIndex extends AbstractGroupValueIndex {	/**	 * 	 */	private static final long serialVersionUID = -1712666277155873986L;	private int rowCount;	public AllShowRoaringGroupValueIndex(int rowCount) {		this.rowCount = rowCount;	}	public AllShowRoaringGroupValueIndex() {	}	@Override	public GroupValueIndex AND(GroupValueIndex valueIndex) {		return valueIndex;	}		@Override	public GroupValueIndex ANDNOT(GroupValueIndex valueIndex) {		return valueIndex.NOT(rowCount);	}		@Override	public GroupValueIndex andnot(GroupValueIndex valueIndex) {		return ANDNOT(valueIndex);	}	@Override	public GroupValueIndex OR(GroupValueIndex valueIndex) {		return this;	}	@Override	public AllShowRoaringGroupValueIndex clone() {		return this;	}	@Override	protected RoaringBitmap getBitMap() {		RoaringBitmap bitmap = new RoaringBitmap();		bitmap.flip(0, rowCount);		return bitmap;	}	//改变自身and	@Override	public GroupValueIndex and(GroupValueIndex valueIndex) {		return valueIndex.clone();	}	//改变自身的or	@Override	public GroupValueIndex or(GroupValueIndex valueIndex) {		return this;	}	@Override	public GroupValueIndex NOT(int rowCount) {		return new RoaringGroupValueIndex();	}	@Override	public void addValueByIndex(int index) {	}	@Override	public boolean isAllEmpty() {		return false;	}	private static final int POS = 10;		private static final int LEN = 1 << POS;	@Override	public void Traversal(TraversalAction action) {		int size = ((rowCount - 1) / LEN) + 1;		for (int i = 0; i < size; i++) {			int len = (i == size - 1) ? (rowCount % LEN) : LEN;			int[] array = new int[len];            int start = i << POS;			for(int j = 0; j < len; j ++){				array[j] = start  + j;			}			action.actionPerformed(array);		}	}	@Override	public void Traversal(SingleRowTraversalAction action) {		for (int i = 0; i < rowCount; i++) {			action.actionPerformed(i);		}	}	@Override	public boolean BrokenableTraversal(BrokenTraversalAction action) {//		unsupported();		for (int i = 0; i < rowCount; i++) {			if (action.actionPerformed(i)) {				return true;			}		}		return false;	}		@Override	public boolean isOneAt(int rowIndex) {		return true;	}	@Override	public int getRowsCountWithData() {		return rowCount;	}	@Override	public boolean hasSameValue(GroupValueIndex parentIndex) {		return parentIndex.getRowsCountWithData() > 0;	}	@Override	public void write(DataOutput out) throws IOException {		out.writeLong(rowCount);	}	@Override	public void readFields(DataInput in) throws IOException {		this.rowCount = in.readInt();	}	@Override	protected byte getType() {		return GroupValueIndexCreator.ROARING_INDEX_All_SHOW.getType();	}	@Override	public boolean equals(Object o) {		if (this == o) {			return true;		}		if (o == null || getClass() != o.getClass()) {			return false;		}		AllShowRoaringGroupValueIndex that = (AllShowRoaringGroupValueIndex) o;		return rowCount == that.rowCount;	}	@Override	public int hashCode() {		return (int)rowCount;	}}