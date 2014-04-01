package main;

/*
 * Copyright 2008 Novamente LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import relex.ParseStats;
import relex.RelationExtractor;
import relex.Sentence;
import relex.WebFormat;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.output.CompactView;

public class RelExHttp extends RelationExtractor
{
	
	public static void main(String[] args)  {
        System.out.println("Initializing...");
        HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(9002), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HttpContext context = null;
		try {
			context = server.createContext("/", new MyHandler());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        context.getFilters().add(new ParameterFilter());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Running");
    }

    static class MyHandler implements HttpHandler {
    	
    	public MyHandler() throws IOException
    	{
    		super();
    	}
    	
        public void handle(HttpExchange t) throws IOException {
        	@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>)t.getAttribute("parameters");
            String response = relex(params.get("text").toString());
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        private String relex(String sentencep) throws IOException
        {
        		String url = null;
        		String sentence = null;
        		int maxParses = 1;
        		int maxParseSeconds = 60;

        		CompactView cv = new CompactView();


        		cv.setMaxParses(maxParses);
        		cv.setSourceURL(url);

        		WebFormat re = new WebFormat();
        		re.setAllowSkippedWords(true);
        		re.setMaxParses(maxParses);
        		re.setMaxParseSeconds(maxParseSeconds);

        		// Pass along the version string.
        		cv.setVersion(re.getVersion());

        		// If sentence is not passed at command line, read from standard input:
        		InputStream is = new ByteArrayInputStream(sentencep.getBytes() );
        		BufferedReader stdin = new BufferedReader(new InputStreamReader(is));
        		DocSplitter ds = DocSplitterFactory.create();

        		// Collect statistics
        		int sentence_count = 0;
        		ParseStats stats = new ParseStats();

        		String content="";
        		content+=cv.header();

        		while(true)
        		{
        			// Read text from stdin.
        			while (sentence == null)
        			{
        				try {
        					sentence = stdin.readLine();
        					if ((sentence == null) || "END.".equals(sentence))
        					{
        						content+=cv.footer();
        						return content;
        					}
        				} catch (IOException e) {
        					System.err.println("Error reading sentence from the standard input!");
        				}

        				// Buffer up input text, and wait for a whole,
        				// complete sentence before continuing.
        				ds.addText(sentence + " ");
        				sentence = ds.getNextSentence();
        			}

        			while (sentence != null)
        			{
        				Sentence sntc = re.processSentence(sentence);

        				// Print output
        				content+=cv.toString(sntc);

        				// Collect statistics
        				sentence_count ++;
        				stats.bin(sntc);

        				if (sentence_count%20 == 0)
        				{
        					content+="\n" + stats.toString();
        				}

        				sentence = ds.getNextSentence();
        			}
        		}
        }
    }
}

/* ============================ END OF FILE ====================== */
