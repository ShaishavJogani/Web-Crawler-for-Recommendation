Crawler:(Crawler folder)
1> To crawl Java Wikibooks Pages data, run: 
	python crwal.py
2> To crawl Oracle Java Tutorial data, run: 
	python crwalOracle.py
3> For stemming and stop words removal on Wikibooks crawl data
	-> Change the raw_file_path in CleanData.py to 'Documents\\Raw\\'
	-> run: python CleanData.py
4> For stemming and stop words removal on Oracle Java Tutorial data
	-> Change the raw_file_path in CleanData.py to 'Documents\\Oracle\\'
	-> run: python CleanData.py


Indexing with Lucene: (server folder)
1> Open the 'Server' project in eclipse.
2> Import all the necessary jar files from jars folder.
3> Change the path of IndexDataPath, CodeDatePath, RawDataPath and OracleDataPath in SimpleLuceneIndexing.java, which links to the appropriate Documents folder files created in crawler.


Web Application: (WebUI folder)
1> Open the index.html page.
	
