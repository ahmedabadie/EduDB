/*
EduDB is made available under the OSI-approved MIT license.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package net.edudb.db_operator;

import net.edudb.db_operator.FilterOperator;
import net.edudb.db_operator.DBOperator;
import net.edudb.index.BPlusTree.DBBTreeIterator;
import net.edudb.page.DBPage;
import net.edudb.page.DBPageRead;
import net.edudb.page.DBPageWrite;
import net.edudb.server.ServerWriter;
import net.edudb.structure.DBRecord;
import net.edudb.transcation.Step;

import java.util.ArrayList;

public class UpdateOperator implements DBOperator {

	private String tableName;
	private ArrayList<DBAssignment> assignments;
	private DBCond condition;
	private DBPage page;
//	private int state;

	public UpdateOperator(String tableName, ArrayList<DBAssignment> assignments, DBCondition condition) {
		this.tableName = tableName;
		this.assignments = assignments;
		this.condition = condition;
	}

	@Override
	public DBResult execute() {
		FilterOperator filterOperator = new FilterOperator();
		RelationOperator relationOperator = new RelationOperator();
		relationOperator.setTableName(tableName);
		filterOperator.giveParameter(relationOperator);
		filterOperator.giveParameter(condition);
		DBBTreeIterator resultIterator = (DBBTreeIterator) filterOperator.execute();
		DBRecord record = (DBRecord) resultIterator.first();
		do {
			record.update(assignments);
			record = (DBRecord) resultIterator.next();
		} while (record != null);
		resultIterator.write();
		return resultIterator;
	}

	@Override
	public DBParameter[] getChildren() {
		return new DBParameter[0];
	}

	@Override
	public void giveParameter(DBParameter par) {

	}

	@Override
	public void print() {

	}

	@Override
	public int numOfParameters() {
		return 0;
	}

	public ArrayList<Step> getSteps() {
		ArrayList<Step> out = new ArrayList<>();
		DBPageRead read = new DBPageRead(this, tableName, true);
		out.add(read);

		DBPageWrite write = new DBPageWrite(this);

		out.add(write);
		return out;
	}

	public void runStep(DBPage page) {
		this.page = page;
		ServerWriter.getInstance().writeln("run");
		ServerWriter.getInstance().writeln(page);
		FilterOperator filterOperator = new FilterOperator();
		filterOperator.giveParameter(page.getData());
		filterOperator.giveParameter(condition);
		DBIterator iterator = (DBIterator) filterOperator.execute();
		DBRecord record = (DBRecord) iterator.first();
		do {
			record.update(assignments);
			record = (DBRecord) iterator.next();
		} while (record != null);
	}

	public DBPage getPage() {
		return page;
	}
}