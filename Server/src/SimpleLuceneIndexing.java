import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import config.ObjectKeys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimpleLuceneIndexing {

	public final String IndexDataPath = "D:\\shaishav\\GoogleDrive\\CSE591 AW\\Assignment2\\Crawler\\Documents\\IndexData";
	public final String CodeDataPath = "D:\\shaishav\\GoogleDrive\\CSE591 AW\\Assignment2\\Crawler\\Documents\\IndexData\\Code\\";
	public final String RawDataPath = "D:\\shaishav\\GoogleDrive\\CSE591 AW\\Assignment2\\Crawler\\Documents\\Raw\\";
	public final String OracleDataPath = "D:\\shaishav\\GoogleDrive\\CSE591 AW\\Assignment2\\Crawler\\Documents\\Oracle\\";

	private StandardAnalyzer analyzer;
	private Directory indexDir;
	private File dataDir = new File(IndexDataPath);

	public SimpleLuceneIndexing() throws IOException {
		startIndexing();
	}

	private void indexDirectory(IndexWriter writer, File dir) throws IOException {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				indexDirectory(writer, f); // recurse
			} else if (f.getName().endsWith(".txt")) {
				// call indexFile to add the title of the txt file to your index (you can also
				// index html)
				indexFile(writer, f);
			}
		}
	}

	private void indexFile(IndexWriter writer, File f) throws IOException {
		System.out.println("Indexing " + f.getName());
		Document doc = new Document();
		doc.add(new TextField("filename", f.getName(), TextField.Store.YES));

		// open each file to index the content
		try {

			FileInputStream is = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuffer stringBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line).append("\n");
			}
			reader.close();
			doc.add(new TextField("contents", stringBuffer.toString(), TextField.Store.YES));

		} catch (Exception e) {

			System.out.println("something wrong with indexing content of the files");
		}

		writer.addDocument(doc);

	}

	private void startIndexing() throws IOException {

		if (!dataDir.exists() || !dataDir.isDirectory()) {
			throw new IOException(dataDir + " does not exist or is not a directory");
		}
		indexDir = new RAMDirectory();

		// Specify the analyzer for tokenizing text.
		analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(indexDir, config);

		// call indexDirectory to add to your index
		// the names of the txt files in dataDir
		indexDirectory(writer, dataDir);
		writer.close();
	}

	public JSONObject getIndexData(String query) throws IOException, ParseException {
		JSONObject response = new JSONObject();
		JSONArray resultset = new JSONArray();
		List<String> hitFiles = getIndexFiles(query);
		int relavance = 1;
		for (String hitfile : hitFiles) {
			String type = getFileType(hitfile);
			String title = hitfile;
			int rank = relavance++;
			String text = getFileContent(hitfile, type);
			JSONObject answer = new JSONObject();
			answer.put(ObjectKeys.TITLE, title);
			answer.put(ObjectKeys.RANK, rank);
			answer.put(ObjectKeys.TYPE, type);
			answer.put(ObjectKeys.TEXT, text);
			resultset.put(answer);
		}

		response.put(ObjectKeys.response, resultset);
		return response;
	}

	private String getFileContent(String filename, String type) throws IOException {
		String path = "";
		if (type.equals(ObjectKeys.CODETYPE))
			path = CodeDataPath + filename;
		else if (type.equals(ObjectKeys.OracleTYPE))
			path = OracleDataPath + filename;
		else
			path = RawDataPath + filename;

		BufferedReader br = new BufferedReader(new FileReader(path));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	private String getFileType(String filename) {
		// System.out.println(filename.substring(0, 7));
		if (filename.substring(0, 7).equals(ObjectKeys.OracleTYPE))
			return ObjectKeys.OracleTYPE;
		filename = filename.substring(0, filename.length() - 4);
		String type = filename.substring(filename.length() - 4);
		if (type.equals(ObjectKeys.CODETYPE))
			return ObjectKeys.CODETYPE;
		else
			return ObjectKeys.TEXTTYPE;
	}

	private List<String> getIndexFiles(String query) throws IOException, ParseException {
		List<String> files = new ArrayList<>();

		Query q = new QueryParser("contents", analyzer).parse(QueryParser.escape(query));
		int hitsPerPage = 10;
		IndexReader reader = null;

		TopScoreDocCollector collector = null;
		IndexSearcher searcher = null;
		reader = DirectoryReader.open(indexDir);
		searcher = new IndexSearcher(reader);
		collector = TopScoreDocCollector.create(hitsPerPage);
		searcher.search(q, collector);

		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		System.out.println("Found " + hits.length + " hits.");
		System.out.println();

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d;
			d = searcher.doc(docId);

			System.out.println((i + 1) + ". " + d.get("filename"));
			files.add(d.get("filename"));
		}
		reader.close();

		return files;
	}

	public static void main(String[] args) throws IOException, ParseException {
		String query = "One way to implement deep copy is to add copy constructors to each associated class. A copy constructor takes an instance of 'this' as its single argument and copies all the values from it. Quite some work, but pretty straightforward and safe. EDIT: note that you don't need to use accessor methods to read fields. You can access all fields directly because the source instance is always of the same type as the instance with the copy constructor. Obvious but might be overlooked. Example: Edit: Note that when using copy constructors you need to know the runtime type of the object you are copying. With the above approach you cannot easily copy a mixed list (you might be able to do it with some reflection code). ";
		query += "public class Order {private long number;public Order() {}/** * Copy constructor */public Order(Order source) {number = source.number;}}public class Customer {private String name;private List<Order> orders = new ArrayList<Order>();public Customer() {}/** * Copy constructor */public Customer(Customer source) {name = source.name;for (Order sourceOrder : source.orders) {orders.add(new Order(sourceOrder));}}public String getName() {return name;}public void setName(String name) {this.name = name;}}";

		SimpleLuceneIndexing sli = new SimpleLuceneIndexing();
		JSONObject jobj = sli.getIndexData(query);
		System.out.println(jobj.toString());
	}

}