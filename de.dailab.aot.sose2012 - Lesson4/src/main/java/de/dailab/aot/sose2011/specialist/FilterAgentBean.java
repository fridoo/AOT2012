package de.dailab.aot.sose2011.specialist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;
import de.dailab.aot.sose2012.ontology.IFeedItem;

public class FilterAgentBean extends BlackboardAgentBean {

	StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
	Directory index = new RAMDirectory();
	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
			analyzer);
	IndexWriter w;
	IFeedItem iFeedItemTpl = new IFeedItem();

	@Override
	public void doInit() throws Exception {
		super.doInit();
		w = new IndexWriter(index, config);
	}

	@Override
	public void execute() {
		int hitsize = 200;
		HashSet<IFeedItem> hItems = (HashSet<IFeedItem>) blackboard
				.removeAll(iFeedItemTpl);
		ArrayList<String> queries = new ArrayList<String>();
		ArrayList<IFeedItem> items = new ArrayList<IFeedItem>();
		ArrayList<text_score> text_scores = new ArrayList<text_score>();
		try {
			for (IFeedItem i : hItems) {
				addDoc(w, i.getFeedItem().getDescriptionAsText());
				queries.add(i.getFeedItem().getDescriptionAsText());
				items.add(i);
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < queries.size(); ++i) {
			String querystr = queries.get(i);
			Query q = null;
			try {
				q = new QueryParser(Version.LUCENE_36, "title", analyzer).parse(querystr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			int hitsPerPage = hitsize;
			IndexReader reader = null;
			try {
				reader = IndexReader.open(index);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			try {
				searcher.search(q, collector);
			} catch (IOException e) {
				e.printStackTrace();
			}
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			text_score temp = new text_score(hitsize);
            temp.query =  queries.get(i);
            temp.query_id = i;
            int j = 0;
            for (ScoreDoc item : hits) {
                int docId = item.doc;
                Document d = null;
				try {
					d = searcher.doc(docId);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
                temp.text[j] = d.get("title");
                temp.score[j] = item.score;
                temp.doc[j] = docId;
                j++;
            }
            temp.computeTotal();
            text_scores.add(temp);
		}
	}

	private static void addDoc(IndexWriter w, String value) throws IOException {
		Document doc = new Document();
		doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
		w.addDocument(doc);
	}

	public class text_score {
        public String[] text;
        public float[] score;
        public int[] doc;
        public float totalScore = 0;
        public Integer query_id;
        public String query;
 
        public text_score(int size) {
            this.text = new String[size];
            this.score = new float[size];
            this.doc = new int[size];
            this.query_id = null;
        }
        
        public text_score() {
        	this.text = null;
            this.score = null;
            this.doc = null;
            this.query_id = null;
        }
        
        public Integer getText_id() {
			return query_id;
		}

		public void setText_id(Integer query_id) {
			this.query_id = query_id;
		}

		public void computeTotal() {
            totalScore = 0;
            float baseScore = score[0];
            // need to normalize
            for (int i = 0; i < score.length; i++)
            {
                score[i] = score[i] / baseScore;
            }
 
            for (int i = 0; i < score.length; i++)
            {
                totalScore += score[i];
 
            }
 
        }
    }
	
	public class array_text_score {
        public text_score[] array_text_scores = new text_score[5];
    }
	
    public class text_finalScore {
        public int docid;
        public String text;
        public float total;
        public String relateddocs;
        public String relatedscores;
    }
    
    private static void  removeSimilar(ArrayList<text_score> items)
    {
        for (int i = 0; i < items.size(); i++)
        {
            float basescore = items.get(i).score[0];
            for (int j = 1; j < items.get(i).doc.length; j++)
            {
                if (Math.abs(items.get(i).score[j] / basescore - 1.0) < .40)
                {
                    text_score temp = null;
                    for (int o = 0; o < items.size(); ++o) {
                    	if(items.get(o).query_id == items.get(i).doc[j])
                    	temp = items.get(o);
                    	break;
                    }

                    if(temp!=null){
                        items.remove(temp);
                    }
                }
            }
        }
    }
}
